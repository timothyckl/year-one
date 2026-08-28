#include "database.h"
#include "checksum.h"
#include "event_log.h"
#include "parser.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief creates a new empty table with the given name
 * @param[in] table_name name for the new table
 * @return pointer to newly created StudentTable on success, NULL on failure
 */
StudentTable *table_init(const char *table_name) {
  StudentTable *table = malloc(sizeof(StudentTable));
  if (!table) {
    return NULL;
  }

  strncpy(table->table_name, table_name, sizeof(table->table_name) - 1);
  table->table_name[sizeof(table->table_name) - 1] = '\0';

  table->column_headers = NULL;
  table->column_count = 0;
  table->records = malloc(INITIAL_RECORD_CAPACITY * sizeof(StudentRecord));
  if (!table->records) {
    free(table);
    return NULL;
  }

  table->record_count = 0;
  table->record_capacity = INITIAL_RECORD_CAPACITY;

  return table;
}

/**
 * @brief frees all memory associated with a table
 * @param[in] table pointer to the table to free (can be NULL)
 */
void table_free(StudentTable *table) {
  if (!table) {
    return;
  }

  for (size_t i = 0; i < table->column_count; i++) {
    free(table->column_headers[i]);
  }
  free(table->column_headers);

  free(table->records);
  free(table);
}

/**
 * @brief sets column headers for a table (takes ownership of headers array)
 * @param[in,out] table pointer to the table to modify
 * @param[in] headers array of header strings
 * @param[in] count number of headers in the array
 * @return DB_SUCCESS on success, appropriate error code on failure
 */
DBStatus table_set_column_headers(StudentTable *table, char **headers,
                                  size_t count) {
  if (!table || !headers) {
    return DB_ERROR_NULL_POINTER;
  }

  for (size_t i = 0; i < table->column_count; i++) {
    free(table->column_headers[i]);
  }
  free(table->column_headers);

  table->column_headers = headers;
  table->column_count = count;

  return DB_SUCCESS;
}

/**
 * @brief adds a record to the table (grows capacity if needed)
 * @param[in,out] table pointer to the table to add the record to
 * @param[in] record pointer to the student record to add
 * @return DB_SUCCESS on success, DB_ERROR_MEMORY if reallocation fails
 */
DBStatus table_add_record(StudentTable *table, StudentRecord *record) {
  if (!table || !record) {
    return DB_ERROR_NULL_POINTER;
  }

  if (table->record_count >= table->record_capacity) {
    size_t new_capacity = table->record_capacity * 2;
    StudentRecord *temp =
        realloc(table->records, new_capacity * sizeof(StudentRecord));

    if (!temp) {
      return DB_ERROR_MEMORY;
    }

    table->records = temp;
    table->record_capacity = new_capacity;
  }

  table->records[table->record_count] = *record;
  table->record_count++;

  return DB_SUCCESS;
}

/**
 * @brief removes a record from the table by student id
 * @param[in,out] table pointer to the table to remove the record from
 * @param[in] student_id id of the student record to remove
 * @return DB_SUCCESS on success, DB_ERROR_NOT_FOUND if record not found
 */
DBStatus table_remove_record(StudentTable *table, int student_id) {
  if (!table) {
    return DB_ERROR_NULL_POINTER;
  }

  // search for record with matching id using sentinel value pattern
  size_t deleted_index = (size_t)-1;
  for (size_t i = 0; i < table->record_count; i++) {
    if (table->records[i].id == student_id) {
      deleted_index = i;
      break;
    }
  }

  if (deleted_index == (size_t)-1) {
    return DB_ERROR_NOT_FOUND;
  }

  // delete record using safe array shifting
  // only shift if deleted record is not the last element
  if (deleted_index < table->record_count - 1) {
    memmove(&table->records[deleted_index], &table->records[deleted_index + 1],
            (table->record_count - deleted_index - 1) * sizeof(StudentRecord));
  }

  table->record_count--;
  memset(&table->records[table->record_count], 0, sizeof(StudentRecord));

  return DB_SUCCESS;
}

/**
 * @brief creates a new empty database
 * @return pointer to newly created StudentDatabase on success, NULL on failure
 */
StudentDatabase *db_init(void) {
  StudentDatabase *db = malloc(sizeof(StudentDatabase));
  if (!db) {
    return NULL;
  }

  memset(db->db_name, 0, sizeof(db->db_name));
  memset(db->authors, 0, sizeof(db->authors));

  db->tables = malloc(INITIAL_TABLE_CAPACITY * sizeof(StudentTable *));
  if (!db->tables) {
    free(db);
    return NULL;
  }

  db->table_count = 0;
  db->table_capacity = INITIAL_TABLE_CAPACITY;
  db->is_loaded = false;
  db->filepath[0] = '\0';
  db->last_saved_checksum = 0;
  db->file_loaded_checksum = 0;
  db->event_log = NULL;

  return db;
}

/**
 * @brief frees all memory associated with a database
 * @param[in] db pointer to the database to free (can be NULL)
 */
void db_free(StudentDatabase *db) {
  if (!db) {
    return;
  }

  for (size_t i = 0; i < db->table_count; i++) {
    table_free(db->tables[i]);
  }
  free(db->tables);

  event_log_free(db->event_log);

  free(db);
}

/**
 * @brief adds a table to the database (grows capacity if needed)
 * @param[in,out] db pointer to the database to add the table to
 * @param[in] table pointer to the table to add
 * @return DB_SUCCESS on success, DB_ERROR_MEMORY if reallocation fails
 */
DBStatus db_add_table(StudentDatabase *db, StudentTable *table) {
  if (!db || !table) {
    return DB_ERROR_NULL_POINTER;
  }

  if (db->table_count >= db->table_capacity) {
    size_t new_capacity = db->table_capacity * 2;
    StudentTable **temp =
        realloc(db->tables, new_capacity * sizeof(StudentTable *));

    if (!temp) {
      return DB_ERROR_MEMORY;
    }

    db->tables = temp;
    db->table_capacity = new_capacity;
  }

  db->tables[db->table_count] = table;
  db->table_count++;

  return DB_SUCCESS;
}

/**
 * @brief loads database from a text file
 * @param[in,out] db pointer to the database to load data into
 * @param[in] filename path to the file to load
 * @param[out] stats optional pointer to ParsingStats structure (can be NULL)
 * @return DB_SUCCESS on success, appropriate error code on failure
 * @note if stats is provided, it will be populated with parsing statistics
 */
DBStatus db_load(StudentDatabase *db, const char *filename, void *stats) {
  if (!db || !filename) {
    return DB_ERROR_NULL_POINTER;
  }
  return parse_file(filename, db, (ParseStatistics *)stats);
}

/**
 * @brief saves database to a text file
 * @param[in] db pointer to the database to save
 * @param[in] filename path to the file to save to
 * @return DB_SUCCESS on success, appropriate error code on failure
 * @note writes database metadata, table structure, and all records to file
 */
DBStatus db_save(StudentDatabase *db, const char *filename) {
  if (!db || !filename) {
    return DB_ERROR_NULL_POINTER;
  }

  if (db->table_count == 0) {
    return DB_ERROR_INVALID_DATA;
  }

  StudentTable *table = db->tables[0];
  if (!table) {
    return DB_ERROR_NULL_POINTER;
  }

  if (!table->column_headers || table->column_count == 0) {
    return DB_ERROR_INVALID_DATA;
  }

  FILE *fp = fopen(filename, "w");
  if (!fp) {
    return DB_ERROR_FILE_NOT_FOUND;
  }

  fprintf(fp, "Database Name: %s\n", db->db_name);
  fprintf(fp, "Authors: %s\n", db->authors);
  fprintf(fp, "\n");
  fprintf(fp, "Table Name: %s\n", table->table_name);

  for (size_t i = 0; i < table->column_count; i++) {
    fprintf(fp, "%s", table->column_headers[i]);
    if (i + 1 < table->column_count) {
      fputc('\t', fp);
    }
  }
  fputc('\n', fp);

  for (size_t i = 0; i < table->record_count; i++) {
    const StudentRecord *r = &table->records[i];
    fprintf(fp, "%d\t%s\t%s\t%.2f\n", r->id, r->name, r->prog, r->mark);
  }

  if (fclose(fp) != 0) {
    return DB_ERROR_FILE_READ;
  }

  // update checksums after successful save
  db->last_saved_checksum = compute_database_checksum(db);
  db->file_loaded_checksum = compute_file_checksum(filename);

  return DB_SUCCESS;
}

/**
 * @brief updates a student record by id
 * @param[in,out] db pointer to the database containing the record
 * @param[in] id student id of the record to update
 * @param[in] new_name new name (NULL to leave unchanged)
 * @param[in] new_prog new programme (NULL to leave unchanged)
 * @param[in] new_mark pointer to new mark (NULL to leave unchanged)
 * @return DB_SUCCESS on success, appropriate error code on failure
 * @note only non-null parameters will update the corresponding field
 */
DBStatus db_update_record(StudentDatabase *db, int id, const char *new_name,
                          const char *new_prog, const float *new_mark) {
  if (!db) {
    return DB_ERROR_NULL_POINTER;
  }

  // All records are stored in the first table (StudentRecords)
  if (db->table_count == 0) {
    return DB_ERROR_INVALID_DATA;
  }

  StudentTable *table = db->tables[0];
  if (!table) {
    return DB_ERROR_NULL_POINTER;
  }

  StudentRecord *rec = NULL;
  for (size_t i = 0; i < table->record_count; i++) {
    if (table->records[i].id == id) {
      rec = &table->records[i];
      break;
    }
  }

  if (!rec) {
    return DB_ERROR_NOT_FOUND;
  }

  // Prepare copy for validation
  StudentRecord updated = *rec;

  if (new_name) {
    strncpy(updated.name, new_name, sizeof(updated.name) - 1);
    updated.name[sizeof(updated.name) - 1] = '\0';
  }

  if (new_prog) {
    strncpy(updated.prog, new_prog, sizeof(updated.prog) - 1);
    updated.prog[sizeof(updated.prog) - 1] = '\0';
  }

  if (new_mark) {
    updated.mark = *new_mark;
  }

  // validate the updated record before applying changes
  ValidationStatus val_status = validate_record(&updated);
  if (val_status != VALID_RECORD) {
    return DB_ERROR_INVALID_DATA;
  }

  *rec = updated;

  return DB_SUCCESS;
}

/**
 * @brief converts database status code to human-readable string
 * @param[in] status the database status code to convert
 * @return pointer to static string describing the status
 */
const char *db_status_string(DBStatus status) {
  switch (status) {
  case DB_SUCCESS:
    return "success";
  case DB_ERROR_NULL_POINTER:
    return "null pointer error";
  case DB_ERROR_MEMORY:
    return "memory allocation failed";
  case DB_ERROR_FILE_NOT_FOUND:
    return "file not found";
  case DB_ERROR_FILE_READ:
    return "file read error";
  case DB_ERROR_DUPLICATE_ID:
    return "duplicate ID";
  case DB_ERROR_NOT_FOUND:
    return "record not found";
  case DB_ERROR_INVALID_DATA:
    return "invalid data";
  default:
    return "unknown error";
  }
}
