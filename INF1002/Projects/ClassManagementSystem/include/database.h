#ifndef DATABASE_H
#define DATABASE_H

/**
 * @file database.h
 * @brief database module for managing student records in tables
 *
 * manages student records organised in tables within a database structure.
 * uses dynamic arrays that grow automatically as needed (doubling capacity).
 * supports loading from and saving to text files, along with basic
 * operations like adding and removing records.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "constants.h"
#include <stdbool.h>
#include <stddef.h>

// forward declarations to avoid circular dependency
typedef struct EventLog EventLog;

// capacity constants
#define INITIAL_TABLE_CAPACITY 2
#define INITIAL_RECORD_CAPACITY 10
#define MAX_FILE_PATH 260

// operation status codes
typedef enum {
  DB_SUCCESS = 0,          // operation succeeded
  DB_ERROR_NULL_POINTER,   // null pointer passed to function
  DB_ERROR_MEMORY,         // malloc/realloc failed
  DB_ERROR_FILE_NOT_FOUND, // cannot open file
  DB_ERROR_FILE_READ,      // error reading from file
  DB_ERROR_DUPLICATE_ID,   // duplicate student ID (for future insert)
  DB_ERROR_NOT_FOUND,      // record not found (for future query/update/delete)
  DB_ERROR_INVALID_DATA    // invalid data format or values
} DBStatus;

// our struct is defined here because out parser functions will be making use of
// it
typedef struct {
  int id;
  char name[MAX_NAME_LENGTH];
  char prog[MAX_PROGRAMME_LENGTH];
  float mark;
} StudentRecord;

// table container for column headers and records
typedef struct {
  char table_name[MAX_TABLE_NAME_LENGTH]; // name of this table

  // column headers
  char **column_headers; // array of header strings
  size_t column_count;   // number of columns

  // record storage (dynamic array)
  StudentRecord *records; // heap-allocated array
  size_t record_count;    // current number of records
  size_t record_capacity; // allocated capacity for records
} StudentTable;

// database container for tables and metadata
typedef struct {
  // database-level metadata
  char db_name[MAX_DB_NAME_LENGTH]; // from "Database Name:" line
  char authors[MAX_AUTHORS_LENGTH]; // from "Authors:" line

  // table storage (dynamic array)
  StudentTable **tables; // array of table pointers
  size_t table_count;    // current number of tables
  size_t table_capacity; // allocated capacity for tables

  // state tracking
  // tracks if database has been loaded from file
  bool is_loaded;

  // where the database was loaded from (for saving)
  char filepath[MAX_FILE_PATH];

  // checksum tracking for automatic change detection
  unsigned long last_saved_checksum;  // checksum when last saved or loaded
  unsigned long file_loaded_checksum; // checksum of file when loaded

  // session event log for tracking operations
  // pointer to event log (NULL until first event or until initialised)
  EventLog *event_log;
} StudentDatabase;

// table lifecycle
/**
 * @brief creates a new empty table with the given name
 * @param[in] table_name name for the new table
 * @return pointer to newly created StudentTable on success, NULL on failure
 */
StudentTable *table_init(const char *table_name);

/**
 * @brief frees all memory associated with a table
 * @param[in] table pointer to the table to free (can be NULL)
 */
void table_free(StudentTable *table);

/**
 * @brief sets column headers for a table (takes ownership of headers array)
 * @param[in,out] table pointer to the table to modify
 * @param[in] headers array of header strings
 * @param[in] count number of headers in the array
 * @return DB_SUCCESS on success, appropriate error code on failure
 */
DBStatus table_set_column_headers(StudentTable *table, char **headers,
                                  size_t count);

/**
 * @brief adds a record to the table (grows capacity if needed)
 * @param[in,out] table pointer to the table to add the record to
 * @param[in] record pointer to the student record to add
 * @return DB_SUCCESS on success, DB_ERROR_MEMORY if reallocation fails
 */
DBStatus table_add_record(StudentTable *table, StudentRecord *record);

/**
 * @brief removes a record from the table by student id
 * @param[in,out] table pointer to the table to remove the record from
 * @param[in] student_id id of the student record to remove
 * @return DB_SUCCESS on success, DB_ERROR_NOT_FOUND if record not found
 */
DBStatus table_remove_record(StudentTable *table, int student_id);

// database lifecycle
/**
 * @brief creates a new empty database
 * @return pointer to newly created StudentDatabase on success, NULL on failure
 */
StudentDatabase *db_init(void);

/**
 * @brief frees all memory associated with a database
 * @param[in] db pointer to the database to free (can be NULL)
 */
void db_free(StudentDatabase *db);

/**
 * @brief adds a table to the database (grows capacity if needed)
 * @param[in,out] db pointer to the database to add the table to
 * @param[in] table pointer to the table to add
 * @return DB_SUCCESS on success, DB_ERROR_MEMORY if reallocation fails
 */
DBStatus db_add_table(StudentDatabase *db, StudentTable *table);

// file operations
/**
 * @brief loads database from a text file
 * @param[in,out] db pointer to the database to load data into
 * @param[in] filename path to the file to load
 * @param[out] stats optional pointer to ParsingStats structure (can be NULL)
 * @return DB_SUCCESS on success, appropriate error code on failure
 * @note if stats is provided, it will be populated with parsing statistics
 */
DBStatus db_load(StudentDatabase *db, const char *filename, void *stats);

/**
 * @brief saves database to a text file
 * @param[in] db pointer to the database to save
 * @param[in] filename path to the file to save to
 * @return DB_SUCCESS on success, appropriate error code on failure
 */
DBStatus db_save(StudentDatabase *db, const char *filename);

/**
 * @brief converts database status code to human-readable string
 * @param[in] status the database status code to convert
 * @return pointer to static string describing the status
 */
const char *db_status_string(DBStatus status);

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
                          const char *new_prog, const float *new_mark);

#endif // DATABASE_H
