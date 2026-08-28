#include "commands/command.h"
#include "commands/command_utils.h"
#include "constants.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief executes QUERY operation to search for records
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_query(StudentDatabase *db) {
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

  if (table->record_count == 0) {
    printf("CMS: No records available to query.\n");
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  char input_buf[64];
  printf("Enter student ID to search: ");
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

  int student_id = (int)parsed_id;

  // search for record with matching ID
  StudentRecord *record = NULL;
  for (size_t r = 0; r < table->record_count; r++) {
    if (table->records[r].id == student_id) {
      record = &table->records[r];
      break;
    }
  }

  if (!record) {
    printf("CMS: The record with ID=%d does not exist.\n", student_id);
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // display found record with dynamic column width formatting
  printf("CMS: The record with ID=%d is found in table \"%s\".\n", record->id,
         table->table_name);
  printf("\n");

  // calculate column widths for single record
  char format_buf[32];
  int id_width = snprintf(format_buf, sizeof format_buf, "%d", record->id);
  int mark_width =
      snprintf(format_buf, sizeof format_buf, "%.2f", record->mark);
  int name_width = (int)strlen(record->name);
  int prog_width = (int)strlen(record->prog);

  // ensure minimum widths for headers
  if (id_width < 2)
    id_width = 2; // "ID"
  if (mark_width < 4)
    mark_width = 4; // "Mark"
  if (name_width < 4)
    name_width = 4; // "Name"
  if (prog_width < 9)
    prog_width = 9; // "Programme"

  // print header with dynamic widths
  printf("%-*s  %-*s  %-*s  %*s\n", id_width, "ID", name_width, "Name",
         prog_width, "Programme", mark_width, "Mark");

  // print record with dynamic widths
  printf("%-*d  %-*s  %-*s  %*.2f\n", id_width, record->id, name_width,
         record->name, prog_width, record->prog, mark_width, record->mark);

  cmd_wait_for_user();

  return OP_SUCCESS;
}
