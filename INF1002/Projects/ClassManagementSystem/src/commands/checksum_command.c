#include "commands/command.h"
#include "commands/command_utils.h"
#include "checksum.h"
#include <stdio.h>
#include <sys/stat.h>
#include <time.h>

/**
 * @brief executes CHECKSUM operation to verify data integrity
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_checksum(StudentDatabase *db) {
  // validate database pointer
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }

  // check if database is loaded
  if (!db->is_loaded || db->table_count == 0) {
    return cmd_report_error("Database not loaded. Use OPEN to load a database "
                            "first.",
                            OP_ERROR_DB_NOT_LOADED);
  }

  // check if file path is available
  if (db->filepath[0] == '\0') {
    return cmd_report_error("No file path available.", OP_ERROR_GENERAL);
  }

  printf("\n");
  printf("========================================\n");
  printf("  Database Integrity Check\n");
  printf("========================================\n\n");

  // compute current database checksum
  unsigned long db_checksum = compute_database_checksum(db);

  // compute file checksum
  unsigned long file_checksum = compute_file_checksum(db->filepath);

  // display checksums
  printf("In-memory database checksum: 0x%08lX\n", db_checksum);
  printf("File checksum (%s): 0x%08lX\n", db->filepath, file_checksum);
  printf("Last saved checksum:         0x%08lX\n\n", db->last_saved_checksum);

  // determine status
  bool match = (db_checksum == db->last_saved_checksum);

  if (match) {
    printf("Status: MATCH - Database is consistent with last save\n");
  } else {
    printf("Status: MISMATCH - Database has unsaved changes\n");
  }

  // get record count
  if (db->tables && db->tables[STUDENT_RECORDS_TABLE_INDEX]) {
    StudentTable *table = db->tables[STUDENT_RECORDS_TABLE_INDEX];
    printf("\nRecord count: %zu\n", table->record_count);
  }

  // get file info
  struct stat file_stat;
  if (stat(db->filepath, &file_stat) == 0) {
    printf("File size: %lld bytes\n", (long long)file_stat.st_size);

    // format last modified time
    char time_buf[80];
    struct tm *time_info = localtime(&file_stat.st_mtime);
    if (time_info) {
      strftime(time_buf, sizeof time_buf, "%Y-%m-%d %H:%M:%S", time_info);
      printf("Last modified: %s\n", time_buf);
    }
  }

  printf("\n========================================\n");

  cmd_wait_for_user();

  return OP_SUCCESS;
}
