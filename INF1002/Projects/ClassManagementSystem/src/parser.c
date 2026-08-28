#include "parser.h"
#include "constants.h"
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief validates student record fields
 * @param[in] record pointer to the record to validate
 * @return VALID_RECORD if valid, appropriate error code otherwise
 */
ValidationStatus validate_record(const StudentRecord *record) {
  if (!record) {
    return INVALID_FIELD_COUNT;
  }

  // validate ID range
  if (record->id < MIN_STUDENT_ID || record->id > MAX_STUDENT_ID) {
    return INVALID_ID_RANGE;
  }

  // validate mark range
  if (record->mark < 0.0f || record->mark > 100.0f) {
    return INVALID_MARK_RANGE;
  }

  // validate non-empty name
  if (strlen(record->name) == 0) {
    return INVALID_EMPTY_NAME;
  }

  // validate non-empty programme
  if (strlen(record->prog) == 0) {
    return INVALID_EMPTY_PROGRAMME;
  }

  return VALID_RECORD;
}

/**
 * @brief converts validation error code to human-readable string
 * @param[in] error the validation error code to convert
 * @return pointer to static string describing the error
 */
const char *validation_error_string(ValidationStatus error) {
  switch (error) {
  case VALID_RECORD:
    return "valid record";
  case INVALID_ID_RANGE:
    return "ID out of range";
  case INVALID_MARK_RANGE:
    return "mark out of range";
  case INVALID_EMPTY_NAME:
    return "empty name field";
  case INVALID_EMPTY_PROGRAMME:
    return "empty programme field";
  case INVALID_FIELD_COUNT:
    return "invalid field count";
  default:
    return "unknown validation error";
  }
}

/**
 * @brief converts parse status code to human-readable string
 * @param[in] status the parse status code to convert
 * @return pointer to static string describing the status
 */
const char *parse_status_string(ParseStatus status) {
  switch (status) {
  case PARSE_SUCCESS:
    return "parse success";
  case PARSE_ERROR_FORMAT:
    return "invalid format";
  case PARSE_ERROR_EMPTY:
    return "empty line";
  case PARSE_ERROR_INCOMPLETE:
    return "incomplete data";
  default:
    return "unknown parse error";
  }
}

/**
 * @brief parses single metadata line (e.g., "Database Name: value")
 * @param[in] line input line to parse
 * @param[out] key buffer to store the parsed key
 * @param[out] value buffer to store the parsed value
 * @return PARSE_SUCCESS on success, appropriate error code on failure
 */
ParseStatus parse_metadata(const char *line, char *key, char *value) {
  if (!line || !key || !value) {
    return PARSE_ERROR_FORMAT;
  }

  // find colon separator
  char *colon = strchr(line, ':');
  if (!colon)
    return PARSE_ERROR_FORMAT;

  // extract key (before colon)
  size_t key_len = colon - line;
  strncpy(key, line, key_len);
  key[key_len] = '\0';

  // extract value (after colon + space)
  char *value_start = colon + 1;
  while (*value_start == ' ' || *value_start == '\t') {
    value_start++; // skip whitespace
  }

  if (*value_start == '\0' || *value_start == '\n') {
    return PARSE_ERROR_EMPTY;
  }

  // copy value and remove trailing newline
  // maximum metadata value size is MAX_METADATA_VALUE (largest caller buffer)
  strncpy(value, value_start, MAX_METADATA_VALUE - 1);
  value[MAX_METADATA_VALUE - 1] = '\0';
  size_t value_len = strlen(value);
  if (value_len > 0 && value[value_len - 1] == '\n') {
    value[value_len - 1] = '\0';
  }
  // remove trailing carriage return (windows-style line endings)
  value_len = strlen(value);
  if (value_len > 0 && value[value_len - 1] == '\r') {
    value[value_len - 1] = '\0';
  }

  return PARSE_SUCCESS;
}

/**
 * @brief parses single data record line into StudentRecord
 * @param[in] line input line containing student data
 * @param[out] record pointer to record structure to populate
 * @return PARSE_SUCCESS on success, appropriate error code on failure
 */
ParseStatus parse_record_line(const char *line, StudentRecord *record) {
  if (!line || !record) {
    return PARSE_ERROR_FORMAT;
  }

  char line_copy[INPUT_BUFFER_SIZE];
  strncpy(line_copy, line, sizeof(line_copy) - 1);
  line_copy[sizeof(line_copy) - 1] = '\0';

  // remove trailing newline
  size_t len = strlen(line_copy);
  if (len > 0 && line_copy[len - 1] == '\n') {
    line_copy[len - 1] = '\0';
  }
  // remove trailing carriage return (windows-style line endings)
  len = strlen(line_copy);
  if (len > 0 && line_copy[len - 1] == '\r') {
    line_copy[len - 1] = '\0';
  }

  // check for empty line
  if (len == 0 || line_copy[0] == '\0') {
    return PARSE_ERROR_EMPTY;
  }

  // tokenise by tab
  char *token = strtok(line_copy, "\t");
  if (!token) {
    return PARSE_ERROR_INCOMPLETE;
  }

  // parse id with error checking
  char *endptr;
  errno = 0;
  long id_val = strtol(token, &endptr, 10);

  // check for conversion errors
  if (errno == ERANGE || *endptr != '\0' || endptr == token) {
    return PARSE_ERROR_FORMAT;
  }

  // check range (full validation in validate_record, but fail fast on obviously
  // invalid values)
  if (id_val < MIN_STUDENT_ID || id_val > MAX_STUDENT_ID) {
    return PARSE_ERROR_FORMAT;
  }

  record->id = (int)id_val;

  token = strtok(NULL, "\t");
  if (!token) {
    return PARSE_ERROR_INCOMPLETE;
  }
  strncpy(record->name, token, sizeof(record->name) - 1);
  record->name[sizeof(record->name) - 1] = '\0';

  token = strtok(NULL, "\t");
  if (!token) {
    return PARSE_ERROR_INCOMPLETE;
  }
  strncpy(record->prog, token, sizeof(record->prog) - 1);
  record->prog[sizeof(record->prog) - 1] = '\0';

  token = strtok(NULL, "\t");
  if (!token) {
    return PARSE_ERROR_INCOMPLETE;
  }

  // parse mark with error checking
  char *mark_endptr;
  errno = 0;
  float mark_val = strtof(token, &mark_endptr);

  // check for conversion errors
  if (errno == ERANGE || *mark_endptr != '\0' || mark_endptr == token) {
    return PARSE_ERROR_FORMAT;
  }

  // basic range check (full validation in validate_record)
  if (mark_val < 0.0f || mark_val > 100.0f) {
    return PARSE_ERROR_FORMAT;
  }

  record->mark = mark_val;

  return PARSE_SUCCESS;
}

/**
 * @brief parses column header line
 * @param[in] line input line containing column headers
 * @param[out] headers pointer to array of header strings (allocated by function)
 * @param[out] count pointer to store number of headers parsed
 * @return PARSE_SUCCESS on success, appropriate error code on failure
 */
ParseStatus parse_column_headers(const char *line, char ***headers,
                                 size_t *count) {
  if (!line || !headers || !count) {
    return PARSE_ERROR_FORMAT;
  }

  char line_copy[MAX_LINE_LENGTH];
  strncpy(line_copy, line, sizeof(line_copy) - 1);
  line_copy[sizeof(line_copy) - 1] = '\0';

  // remove newline
  size_t len = strlen(line_copy);
  if (len > 0 && line_copy[len - 1] == '\n') {
    line_copy[len - 1] = '\0';
  }
  // remove trailing carriage return (windows-style line endings)
  len = strlen(line_copy);
  if (len > 0 && line_copy[len - 1] == '\r') {
    line_copy[len - 1] = '\0';
  }

  // first pass: count columns
  char *temp_copy = strdup(line_copy);
  if (!temp_copy) {
    return PARSE_ERROR_FORMAT;
  }

  *count = 0;
  char *token = strtok(temp_copy, "\t");
  while (token) {
    (*count)++;
    token = strtok(NULL, "\t");
  }
  free(temp_copy);

  if (*count == 0) {
    return PARSE_ERROR_EMPTY;
  }

  // allocate header array
  *headers = malloc(*count * sizeof(char *));
  if (!*headers)
    return PARSE_ERROR_FORMAT;

  // second pass: copy header strings
  size_t idx = 0;
  token = strtok(line_copy, "\t");
  while (token && idx < *count) {
    (*headers)[idx] = strdup(token);
    if (!(*headers)[idx]) {
      // cleanup on failure
      for (size_t i = 0; i < idx; i++) {
        free((*headers)[i]);
      }
      free(*headers);
      return PARSE_ERROR_FORMAT;
    }
    idx++;
    token = strtok(NULL, "\t");
  }

  return PARSE_SUCCESS;
}

/**
 * @brief parses entire file into database
 * @param[in] filename path to the file to parse
 * @param[in,out] db pointer to the database to populate
 * @param[out] stats optional pointer to statistics structure (can be NULL)
 * @return DB_SUCCESS on success, appropriate error code on failure
 * @note if stats is provided, it will be populated with parsing statistics
 */
DBStatus parse_file(const char *filename, StudentDatabase *db,
                    ParseStatistics *stats) {
  if (!filename || !db) {
    return DB_ERROR_NULL_POINTER;
  }

  // initialise statistics if provided
  if (stats) {
    stats->total_records_attempted = 0;
    stats->records_loaded = 0;
    stats->records_skipped = 0;
    stats->validation_errors = 0;
    stats->parse_errors = 0;
  }

  FILE *fp = fopen(filename, "r");
  if (!fp) {
    printf("CMS: Error - Cannot open file '%s'\n", filename);
    return DB_ERROR_FILE_NOT_FOUND;
  }

  char line[MAX_LINE_LENGTH];
  int line_num = 0;
  StudentTable *current_table = NULL;
  int awaiting_headers = 0; // flag: next line is column headers

  while (fgets(line, sizeof(line), fp)) {
    line_num++;

    // skip empty lines
    if (line[0] == '\n' || line[0] == '\r' ||
        (line[0] == ' ' && line[1] == '\n')) {
      continue;
    }

    // parse database metadata
    if (strstr(line, "Database Name:")) {
      char key[50], value[100];
      if (parse_metadata(line, key, value) == PARSE_SUCCESS) {
        strncpy(db->db_name, value, sizeof(db->db_name) - 1);
        db->db_name[sizeof(db->db_name) - 1] = '\0';
      }
    } else if (strstr(line, "Authors:")) {
      char key[50], value[200];
      if (parse_metadata(line, key, value) == PARSE_SUCCESS) {
        strncpy(db->authors, value, sizeof(db->authors) - 1);
        db->authors[sizeof(db->authors) - 1] = '\0';
      }
    } else if (strstr(line, "Table Name:")) {
      // create new table
      char key[50], value[50];
      if (parse_metadata(line, key, value) == PARSE_SUCCESS) {
        current_table = table_init(value);
        if (!current_table) {
          fclose(fp);
          return DB_ERROR_MEMORY;
        }

        DBStatus add_status = db_add_table(db, current_table);
        if (add_status != DB_SUCCESS) {
          table_free(current_table);
          fclose(fp);
          return add_status;
        }

        awaiting_headers = 1; // next line should be headers
      }
    } else if (awaiting_headers && current_table) {
      // parse column headers
      char **headers;
      size_t count;
      ParseStatus status = parse_column_headers(line, &headers, &count);

      if (status == PARSE_SUCCESS) {
        table_set_column_headers(current_table, headers, count);
        awaiting_headers = 0; // now we can parse records
      } else {
        printf("CMS: Warning - Failed to parse column headers at line %d\n",
               line_num);
        awaiting_headers = 0; // skip to records anyway
      }
    } else if (current_table && !awaiting_headers) {
      // parse data record
      StudentRecord record;
      ParseStatus parse_status = parse_record_line(line, &record);

      if (parse_status == PARSE_SUCCESS) {
        if (stats) {
          stats->total_records_attempted++;
        }

        ValidationStatus validation = validate_record(&record);

        if (validation == VALID_RECORD) {
          // check for duplicate id
          int duplicate_found = 0;
          for (size_t i = 0; i < current_table->record_count; i++) {
            if (current_table->records[i].id == record.id) {
              duplicate_found = 1;
              break;
            }
          }

          if (duplicate_found) {
            printf("CMS: Warning - duplicate ID %d at line %d (ignored)\n",
                   record.id, line_num);
            if (stats) {
              stats->records_skipped++;
              stats->validation_errors++;
            }
          } else {
            DBStatus add_status = table_add_record(current_table, &record);
            if (add_status != DB_SUCCESS) {
              fclose(fp);
              return add_status;
            }
            if (stats) {
              stats->records_loaded++;
            }
          }
        } else {
          printf("CMS: Warning - %s at line %d\n",
                 validation_error_string(validation), line_num);
          if (stats) {
            stats->records_skipped++;
            stats->validation_errors++;
          }
        }
      } else if (parse_status != PARSE_ERROR_EMPTY) {
        if (stats) {
          stats->total_records_attempted++;
          stats->records_skipped++;
          stats->parse_errors++;
        }
        printf("CMS: Warning - %s at line %d\n",
               parse_status_string(parse_status), line_num);
      }
    }
  }

  fclose(fp);
  return DB_SUCCESS;
}
