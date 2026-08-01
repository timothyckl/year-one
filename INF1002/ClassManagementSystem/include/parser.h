#ifndef PARSER_H
#define PARSER_H

/**
 * @file parser.h
 * @brief parser module for reading and validating database files
 *
 * handles reading and parsing student database files.
 * processes metadata lines, table headers, and student records.
 * validates data during parsing to ensure correctness.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "database.h"

typedef enum {
  VALID_RECORD = 0,        // record is valid
  INVALID_ID_RANGE,        // id outside MIN_STUDENT_ID-MAX_STUDENT_ID
  INVALID_MARK_RANGE,      // mark outside 0.0-100.0
  INVALID_EMPTY_NAME,      // name field is empty
  INVALID_EMPTY_PROGRAMME, // programme field is empty
  INVALID_FIELD_COUNT      // incorrect number of fields
} ValidationStatus;

typedef enum {
  PARSE_SUCCESS = 0,     // line parsed successfully
  PARSE_ERROR_FORMAT,    // line format is invalid
  PARSE_ERROR_EMPTY,     // line is empty or whitespace only
  PARSE_ERROR_INCOMPLETE // line missing required fields
} ParseStatus;

// parsing statistics for tracking warnings during file load
typedef struct {
  int total_records_attempted; // total data lines processed
  int records_loaded;          // successfully loaded records
  int records_skipped;         // records skipped due to errors
  int validation_errors;       // count of validation errors
  int parse_errors;            // count of parse format errors
} ParseStatistics;

/**
 * @brief parses entire file into database
 * @param[in] filename path to the file to parse
 * @param[in,out] db pointer to the database to populate
 * @param[out] stats optional pointer to statistics structure (can be NULL)
 * @return DB_SUCCESS on success, appropriate error code on failure
 * @note if stats is provided, it will be populated with parsing statistics
 */
DBStatus parse_file(const char *filename, StudentDatabase *db,
                    ParseStatistics *stats);

/**
 * @brief parses single metadata line (e.g., "Database Name: value")
 * @param[in] line input line to parse
 * @param[out] key buffer to store the parsed key
 * @param[out] value buffer to store the parsed value
 * @return PARSE_SUCCESS on success, appropriate error code on failure
 */
ParseStatus parse_metadata(const char *line, char *key, char *value);

/**
 * @brief parses single data record line into StudentRecord
 * @param[in] line input line containing student data
 * @param[out] record pointer to record structure to populate
 * @return PARSE_SUCCESS on success, appropriate error code on failure
 */
ParseStatus parse_record_line(const char *line, StudentRecord *record);

/**
 * @brief parses column header line
 * @param[in] line input line containing column headers
 * @param[out] headers pointer to array of header strings (allocated by function)
 * @param[out] count pointer to store number of headers parsed
 * @return PARSE_SUCCESS on success, appropriate error code on failure
 */
ParseStatus parse_column_headers(const char *line, char ***headers,
                                 size_t *count);

/**
 * @brief validates student record fields
 * @param[in] record pointer to the record to validate
 * @return VALID_RECORD if valid, appropriate error code otherwise
 */
ValidationStatus validate_record(const StudentRecord *record);

/**
 * @brief converts parse status code to human-readable string
 * @param[in] status the parse status code to convert
 * @return pointer to static string describing the status
 */
const char *parse_status_string(ParseStatus status);

/**
 * @brief converts validation error code to human-readable string
 * @param[in] error the validation error code to convert
 * @return pointer to static string describing the error
 */
const char *validation_error_string(ValidationStatus error);

#endif // PARSER_H
