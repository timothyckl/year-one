#include "commands/command.h"
#include "commands/command_utils.h"
#include "constants.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief executes UPDATE operation to modify existing record
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_update(StudentDatabase *db) {
  // validate database pointer
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }

  // ensure database is loaded
  if (!db->is_loaded || db->table_count == 0) {
    return cmd_report_error("Database not loaded.", OP_ERROR_DB_NOT_LOADED);
  }

  // retrieve student table reference
  StudentTable *table = db->tables[STUDENT_RECORDS_TABLE_INDEX];
  if (!table) {
    return cmd_report_error("Table error.", OP_ERROR_GENERAL);
  }

  // check for empty table
  if (table->record_count == 0) {
    printf("CMS: No records available to update.\n");
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // prompt user for student ID
  char input_buf[64];
  printf("Enter student ID to update: ");
  fflush(stdout);

  if (!fgets(input_buf, sizeof input_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  size_t len = strcspn(input_buf, "\r\n");
  input_buf[len] = '\0';

  if (len == 0) {
    return cmd_report_error("Student ID cannot be empty.", OP_ERROR_VALIDATION);
  }

  char *endptr = NULL;
  long parsed_id = strtol(input_buf, &endptr, 10);
  if (endptr == input_buf || *endptr != '\0') {
    return cmd_report_error("Please enter a numeric student ID.",
                            OP_ERROR_VALIDATION);
  }

  if (parsed_id < MIN_STUDENT_ID || parsed_id > MAX_STUDENT_ID) {
    return cmd_report_error("Student ID must be a 7-digit number between 2500000 and 2600000.",
                            OP_ERROR_VALIDATION);
  }

  // search for record (same logic as QUERY)
  StudentRecord *record = NULL;

  for (size_t r = 0; r < table->record_count; r++) {
    if (table->records[r].id == (int)parsed_id) {
      record = &table->records[r];
      break;
    }
  }

  if (!record) {
    printf("CMS: The record with ID=%ld does not exist.\n", parsed_id);
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // field selection
  printf("Select field to update:\n");
  printf("  [1] Name\n");
  printf("  [2] Programme\n");
  printf("  [3] Mark\n");
  printf("Enter your choice: ");
  fflush(stdout);

  char choice_buf[16];
  if (!fgets(choice_buf, sizeof choice_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  size_t choice_len = strcspn(choice_buf, "\r\n");
  choice_buf[choice_len] = '\0';

  if (choice_len != 1 || (choice_buf[0] < '1' || choice_buf[0] > '3')) {
    return cmd_report_error("Invalid choice.", OP_ERROR_VALIDATION);
  }

  int choice = choice_buf[0] - '0';

  const char *new_name = NULL;
  const char *new_prog = NULL;
  float new_mark = 0.0f;
  float *new_mark_ptr = NULL;

  char buffer[256];

  if (choice == 1) {
    printf("Enter new Name: ");
    fflush(stdout);
    if (!fgets(buffer, sizeof buffer, stdin)) {
      return cmd_report_error("Failed to read name.", OP_ERROR_INPUT);
    }
    size_t nlen = strcspn(buffer, "\r\n");
    buffer[nlen] = '\0';
    if (nlen == 0) {
      return cmd_report_error("Name cannot be empty.", OP_ERROR_VALIDATION);
    }
    if (!cmd_is_alphabetic(buffer)) {
      return cmd_report_error(
          "Name must contain only alphabetic characters and spaces.",
          OP_ERROR_VALIDATION);
    }
    new_name = buffer;

  } else if (choice == 2) {
    printf("Enter new Programme: ");
    fflush(stdout);
    if (!fgets(buffer, sizeof buffer, stdin)) {
      return cmd_report_error("Failed to read programme.", OP_ERROR_INPUT);
    }
    size_t plen = strcspn(buffer, "\r\n");
    buffer[plen] = '\0';
    if (plen == 0) {
      return cmd_report_error("Programme cannot be empty.",
                              OP_ERROR_VALIDATION);
    }
    if (!cmd_is_alphabetic(buffer)) {
      return cmd_report_error(
          "Programme must contain only alphabetic characters and spaces.",
          OP_ERROR_VALIDATION);
    }
    new_prog = buffer;

  } else if (choice == 3) {
    printf("Enter new Mark: ");
    fflush(stdout);

    char mark_buf[64];
    if (!fgets(mark_buf, sizeof mark_buf, stdin)) {
      return cmd_report_error("Failed to read mark.", OP_ERROR_INPUT);
    }

    // strip trailing newline/carriage return
    size_t mark_len = strcspn(mark_buf, "\r\n");
    mark_buf[mark_len] = '\0';

    char *m_endptr;
    new_mark = strtof(mark_buf, &m_endptr);
    if (m_endptr == mark_buf || *m_endptr != '\0') {
      return cmd_report_error("Invalid mark entered.", OP_ERROR_VALIDATION);
    }
    new_mark_ptr = &new_mark;
  }

  // call database-layer update
  DBStatus db_status =
      db_update_record(db, (int)parsed_id, new_name, new_prog, new_mark_ptr);

  if (db_status != DB_SUCCESS) {
    char err_msg[256];
    snprintf(err_msg, sizeof err_msg, "Failed to update record (error: %s).",
             db_status_string(db_status));
    return cmd_report_error(err_msg, OP_ERROR_GENERAL);
  }

  printf("CMS: The record with ID=%d is successfully updated.\n",
         (int)parsed_id);

  cmd_wait_for_user();
  return OP_SUCCESS;
}
