#include "commands/command.h"
#include "commands/command_utils.h"
#include "constants.h"
#include "parser.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief executes INSERT operation to add new record
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_insert(StudentDatabase *db) {
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }

  if (!db->is_loaded || db->table_count == 0) {
    return cmd_report_error("Database not loaded.", OP_ERROR_DB_NOT_LOADED);
  }

  StudentTable *table = db->tables[STUDENT_RECORDS_TABLE_INDEX];
  if (!table) {
    return cmd_report_error("Table error.", OP_ERROR_GENERAL);
  }

  char id_buf[INPUT_BUFFER_SIZE];
  printf("Enter student ID: ");
  fflush(stdout);

  if (!fgets(id_buf, sizeof id_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  size_t id_len = strcspn(id_buf, "\r\n");
  id_buf[id_len] = '\0';

  if (id_len == 0) {
    return cmd_report_error("Student ID cannot be empty.", OP_ERROR_VALIDATION);
  }

  // parse ID using strtol for safe conversion
  char *endptr;
  long id_long = strtol(id_buf, &endptr, 10);

  if (*endptr != '\0' || endptr == id_buf) {
    return cmd_report_error("Invalid student ID format. Please enter a number.",
                            OP_ERROR_VALIDATION);
  }

  if (id_long < MIN_STUDENT_ID || id_long > MAX_STUDENT_ID) {
    return cmd_report_error("Student ID must be a 7-digit number between 2500000 and 2600000.",
                            OP_ERROR_VALIDATION);
  }

  int student_id = (int)id_long;

  for (size_t i = 0; i < table->record_count; i++) {
    if (table->records[i].id == student_id) {
      char err_msg[ERROR_MESSAGE_SIZE];
      snprintf(err_msg, sizeof err_msg, "The record with ID=%d already exists.",
               student_id);
      return cmd_report_error(err_msg, OP_ERROR_VALIDATION);
    }
  }

  char name_buf[INPUT_BUFFER_SIZE];
  printf("Enter student name: ");
  fflush(stdout);

  if (!fgets(name_buf, sizeof name_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  size_t name_len = strcspn(name_buf, "\r\n");
  name_buf[name_len] = '\0';

  if (name_len == 0) {
    return cmd_report_error("Student name cannot be empty.",
                            OP_ERROR_VALIDATION);
  }

  if (name_len >= 50) {
    return cmd_report_error("Student name is too long (max 49 characters).",
                            OP_ERROR_VALIDATION);
  }

  if (!cmd_is_alphabetic(name_buf)) {
    return cmd_report_error(
        "Student name must contain only alphabetic characters and spaces.",
        OP_ERROR_VALIDATION);
  }

  char prog_buf[INPUT_BUFFER_SIZE];
  printf("Enter programme: ");
  fflush(stdout);

  if (!fgets(prog_buf, sizeof prog_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  size_t prog_len = strcspn(prog_buf, "\r\n");
  prog_buf[prog_len] = '\0';

  if (prog_len == 0) {
    return cmd_report_error("Programme cannot be empty.", OP_ERROR_VALIDATION);
  }

  if (prog_len >= 50) {
    return cmd_report_error("Programme name is too long (max 49 characters).",
                            OP_ERROR_VALIDATION);
  }

  if (!cmd_is_alphabetic(prog_buf)) {
    return cmd_report_error(
        "Programme must contain only alphabetic characters and spaces.",
        OP_ERROR_VALIDATION);
  }

  char mark_buf[INPUT_BUFFER_SIZE];
  printf("Enter mark: ");
  fflush(stdout);

  if (!fgets(mark_buf, sizeof mark_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  size_t mark_len = strcspn(mark_buf, "\r\n");
  mark_buf[mark_len] = '\0';

  if (mark_len == 0) {
    return cmd_report_error("Mark cannot be empty.", OP_ERROR_VALIDATION);
  }

  // parse mark using strtof for safe conversion
  char *mark_endptr;
  float mark = strtof(mark_buf, &mark_endptr);

  if (*mark_endptr != '\0' || mark_endptr == mark_buf) {
    return cmd_report_error("Invalid mark format. Please enter a number.",
                            OP_ERROR_VALIDATION);
  }

  StudentRecord record;
  record.id = student_id;
  strncpy(record.name, name_buf, sizeof record.name - 1);
  record.name[sizeof record.name - 1] = '\0';
  strncpy(record.prog, prog_buf, sizeof record.prog - 1);
  record.prog[sizeof record.prog - 1] = '\0';
  record.mark = mark;

  // validate record using existing validation function
  ValidationStatus val_status = validate_record(&record);
  if (val_status != VALID_RECORD) {
    char err_msg[ERROR_MESSAGE_SIZE];
    snprintf(err_msg, sizeof err_msg, "Invalid record: %s",
             validation_error_string(val_status));
    return cmd_report_error(err_msg, OP_ERROR_VALIDATION);
  }

  DBStatus db_status = table_add_record(table, &record);
  if (db_status != DB_SUCCESS) {
    char err_msg[ERROR_MESSAGE_SIZE];
    snprintf(err_msg, sizeof err_msg, "Failed to insert record: %s",
             db_status_string(db_status));
    return cmd_report_error(err_msg, OP_ERROR_GENERAL);
  }

  printf("CMS: A new record with ID=%d is successfully inserted.\n",
         student_id);

  cmd_wait_for_user();

  return OP_SUCCESS;
}
