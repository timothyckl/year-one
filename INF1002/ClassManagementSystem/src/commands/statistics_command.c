#include "commands/command.h"
#include "commands/command_utils.h"
#include "statistics.h"
#include <stdio.h>

/**
 * @brief executes STATISTICS operation to compute summary stats
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_statistics(StudentDatabase *db) {
  // validate database pointer
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }

  // validate database is loaded
  if (!db->is_loaded || db->table_count == 0) {
    return cmd_report_error("Database not loaded.", OP_ERROR_DB_NOT_LOADED);
  }

  // access the StudentRecords table
  // note: assumes tables[0] is always StudentRecords per database schema
  StudentTable *table = db->tables[STUDENT_RECORDS_TABLE_INDEX];
  if (!table) {
    return cmd_report_error("Table error.", OP_ERROR_GENERAL);
  }

  // validate table records array
  if (!table->records) {
    return cmd_report_error("Table records array is NULL.", OP_ERROR_GENERAL);
  }

  // handle empty table
  if (table->record_count == 0) {
    printf("CMS: No records found in table \"%s\".\n", table->table_name);
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // calculate statistics
  StudentStatistics stats;
  DBStatus db_status = calculate_statistics(table, &stats);

  // convert DBStatus to OpStatus
  if (db_status != DB_SUCCESS) {
    char err_msg[256];
    snprintf(err_msg, sizeof err_msg, "Failed to calculate statistics: %s",
             db_status_string(db_status));
    return cmd_report_error(err_msg, OP_ERROR_GENERAL);
  }

  // display statistics
  printf("Summary Statistics for Table: %s\n\n", table->table_name);
  printf("Total Students:    %zu\n", stats.total_count);
  printf("Average Mark:      %.2f\n", stats.average_mark);
  printf("Highest Mark:      %.2f (ID=%d, Name=%s)\n", stats.highest_mark,
         stats.highest_student_id, stats.highest_student_name);
  printf("Lowest Mark:       %.2f (ID=%d, Name=%s)\n", stats.lowest_mark,
         stats.lowest_student_id, stats.lowest_student_name);
  printf("\n");

  cmd_wait_for_user();

  return OP_SUCCESS;
}
