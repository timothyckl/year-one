#include "commands/command.h"
#include "commands/command_utils.h"
#include <stdio.h>
#include <string.h>

/**
 * @brief executes SHOW_ALL operation to display all records
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_show_all(StudentDatabase *db) {
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }

  if (!db->is_loaded || db->table_count == 0) {
    return cmd_report_error("Database not loaded.", OP_ERROR_DB_NOT_LOADED);
  }

  // note: assumes tables[0] is always StudentRecords per database schema
  StudentTable *table = db->tables[STUDENT_RECORDS_TABLE_INDEX];
  if (!table) {
    return cmd_report_error("Table error.", OP_ERROR_GENERAL);
  }

  if (table->record_count == 0) {
    printf("CMS: No records found in table \"%s\".\n", table->table_name);

    cmd_wait_for_user();

    return OP_SUCCESS;
  }

  // print header message
  printf("Table Name: %s\n\n", table->table_name);

  // calculate dynamic column widths
  size_t max_id_width = 2;   // "ID" header minimum
  size_t max_name_width = 4; // "Name" header minimum
  size_t max_prog_width = 9; // "Programme" header minimum
  size_t max_mark_width = 4; // "Mark" header minimum

  for (size_t i = 0; i < table->record_count; i++) {
    StudentRecord *rec = &table->records[i];

    char format_buf[32];
    int len;

    // calculate id width
    len = snprintf(format_buf, sizeof format_buf, "%d", rec->id);
    if (len > 0 && (size_t)len > max_id_width) {
      max_id_width = (size_t)len;
    }

    // calculate name width
    size_t name_len = strlen(rec->name);
    if (name_len > max_name_width) {
      max_name_width = name_len;
    }

    // calculate programme width
    size_t prog_len = strlen(rec->prog);
    if (prog_len > max_prog_width) {
      max_prog_width = prog_len;
    }

    // calculate mark width
    len = snprintf(format_buf, sizeof format_buf, "%.2f", rec->mark);
    if (len > 0 && (size_t)len > max_mark_width) {
      max_mark_width = (size_t)len;
    }
  }

  // print column headers with calculated widths
  printf("%-*s  %-*s  %-*s  %*s\n", (int)max_id_width, "ID",
         (int)max_name_width, "Name", (int)max_prog_width, "Programme",
         (int)max_mark_width, "Mark");

  // print all records
  for (size_t i = 0; i < table->record_count; i++) {
    StudentRecord *rec = &table->records[i];
    printf("%-*d  %-*s  %-*s  %*.2f\n", (int)max_id_width, rec->id,
           (int)max_name_width, rec->name, (int)max_prog_width, rec->prog,
           (int)max_mark_width, rec->mark);
  }

  // add trailing newline
  printf("\n");

  cmd_wait_for_user();

  return OP_SUCCESS;
}
