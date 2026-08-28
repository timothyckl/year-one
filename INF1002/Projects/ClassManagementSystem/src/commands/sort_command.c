#include "commands/command.h"
#include "commands/command_utils.h"
#include "sorting.h"
#include <ctype.h>
#include <stdio.h>
#include <string.h>

/**
 * @brief executes SORT operation to order records
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_sort(StudentDatabase *db) {
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

  if (!table->records) {
    return cmd_report_error("Table records array is NULL.", OP_ERROR_GENERAL);
  }

  if (table->record_count > table->record_capacity) {
    return cmd_report_error("Table record count exceeds capacity.",
                            OP_ERROR_VALIDATION);
  }

  if (table->record_count == 0) {
    return cmd_report_error("No records available to sort.", OP_ERROR_GENERAL);
  }

  char field_buf[10];
  printf("Select field to sort by:\n");
  printf("  [1] ID\n");
  printf("  [2] Mark\n");
  printf("Enter your choice (or press ENTER to cancel): ");
  fflush(stdout);

  if (!fgets(field_buf, sizeof field_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  // strip trailing newline/carriage return
  size_t field_len = strcspn(field_buf, "\r\n");
  field_buf[field_len] = '\0';

  // allow empty input to cancel
  if (field_len == 0) {
    printf("CMS: Sort operation cancelled.\n");
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // validate field is exactly "1" or "2"
  char field;
  if (field_len == 1 && (field_buf[0] == '1' || field_buf[0] == '2')) {
    field = field_buf[0];
  } else {
    return cmd_report_error("Invalid field. Enter '1' for ID or '2' for Mark.",
                            OP_ERROR_VALIDATION);
  }

  char order_buf[10];
  printf("Select sort order:\n");
  printf("  [A] Ascending\n");
  printf("  [D] Descending\n");
  printf("Enter your choice (or press ENTER to cancel): ");
  fflush(stdout);

  if (!fgets(order_buf, sizeof order_buf, stdin)) {
    return cmd_report_error("Failed to read input.", OP_ERROR_INPUT);
  }

  // strip trailing newline/carriage return
  size_t order_len = strcspn(order_buf, "\r\n");
  order_buf[order_len] = '\0';

  // allow empty input to cancel
  if (order_len == 0) {
    printf("CMS: Sort operation cancelled.\n");
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // validate order is exactly "A", "a", "D", or "d"
  char order;
  if (order_len == 1 &&
      (toupper(order_buf[0]) == 'A' || toupper(order_buf[0]) == 'D')) {
    order = toupper(order_buf[0]);
  } else {
    return cmd_report_error(
        "Invalid order. Enter 'A' for Ascending or 'D' for Descending.",
        OP_ERROR_VALIDATION);
  }

  // convert user input to sorting module enums
  SortField sort_field = (field == '1') ? SORT_FIELD_ID : SORT_FIELD_MARK;
  SortOrder sort_order = (order == 'A') ? SORT_ORDER_ASC : SORT_ORDER_DESC;

  // perform sort using sorting module
  sort_records(table->records, table->record_count, sort_field, sort_order);

  const char *field_name = (field == '1') ? "ID" : "Mark";
  const char *order_name = (order == 'A') ? "ascending" : "descending";

  printf("CMS: %zu record%s successfully sorted by %s in %s order.\n",
         table->record_count, (table->record_count == 1) ? "" : "s", field_name,
         order_name);

  cmd_wait_for_user();

  return OP_SUCCESS;
}
