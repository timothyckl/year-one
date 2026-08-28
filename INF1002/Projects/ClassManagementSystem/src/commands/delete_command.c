#include "commands/command.h"
#include "commands/command_utils.h"
#include "constants.h"
#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief executes DELETE operation to remove record
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_delete(StudentDatabase *db) {
  // validate database pointer
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }

  // validate database is loaded
  if (!db->is_loaded || db->table_count == 0) {
    return cmd_report_error("Database not loaded.", OP_ERROR_DB_NOT_LOADED);
  }

  // access the StudentRecords table
  StudentTable *table = db->tables[STUDENT_RECORDS_TABLE_INDEX];
  if (!table) {
    return cmd_report_error("Table error.", OP_ERROR_GENERAL);
  }

  // check if table has records before prompting
  if (table->record_count == 0) {
    return cmd_report_error("No records available to delete.",
                            OP_ERROR_GENERAL);
  }

  // prompt for student id
  char id_buf[INPUT_BUFFER_SIZE];
  printf("Enter student ID: ");
  fflush(stdout);

  if (!fgets(id_buf, sizeof id_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  // strip trailing newline/carriage return
  size_t id_len = strcspn(id_buf, "\r\n");
  id_buf[id_len] = '\0';

  // validate ID is not empty
  if (id_len == 0) {
    return cmd_report_error("Student ID cannot be empty.", OP_ERROR_VALIDATION);
  }

  // parse ID using strtol for safe conversion with overflow detection
  char *endptr;
  errno = 0;
  long id_long = strtol(id_buf, &endptr, 10);

  // check for conversion errors and overflow
  if (errno == ERANGE || *endptr != '\0' || endptr == id_buf) {
    return cmd_report_error("Invalid student ID format. Please enter a number.",
                            OP_ERROR_VALIDATION);
  }

  // check ID range (fits in int)
  if (id_long < MIN_STUDENT_ID || id_long > MAX_STUDENT_ID) {
    return cmd_report_error("Student ID must be a 7-digit number between 2500000 and 2600000.",
                            OP_ERROR_VALIDATION);
  }

  int student_id = (int)id_long;

  // ask user to confirm deletion
  char confirm[SMALL_INPUT_SIZE];
  printf("CMS: Are you sure you want to delete record with ID=%d? Type \"Y\" "
         "to Confirm or type \"N\" to cancel.\n",
         student_id);
  fflush(stdout);

  if (!fgets(confirm, sizeof confirm, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  // strip trailing newline/carriage return
  size_t confirm_len = strcspn(confirm, "\r\n");
  confirm[confirm_len] = '\0';

  // validate confirmation input
  if (confirm_len == 0 ||
      (toupper(confirm[0]) != 'Y' && toupper(confirm[0]) != 'N')) {
    return cmd_report_error("Invalid input. Operation cancelled.",
                            OP_ERROR_VALIDATION);
  }

  // handle cancellation
  if (toupper(confirm[0]) == 'N') {
    printf("CMS: The deletion is cancelled.\n");
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // user confirmed - attempt to remove the record
  DBStatus db_status = table_remove_record(table, student_id);

  // handle record not found
  if (db_status == DB_ERROR_NOT_FOUND) {
    printf("CMS: The record with ID=%d does not exist.\n", student_id);
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // handle other database errors
  if (db_status != DB_SUCCESS) {
    char err_msg[ERROR_MESSAGE_SIZE];
    snprintf(err_msg, sizeof err_msg, "Failed to delete record: %s",
             db_status_string(db_status));
    return cmd_report_error(err_msg, OP_ERROR_GENERAL);
  }

  printf("CMS: The record with ID=%d is successfully deleted.\n", student_id);

  cmd_wait_for_user();

  return OP_SUCCESS;
}
