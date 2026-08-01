#include "commands/command.h"
#include "commands/command_utils.h"
#include "checksum.h"
#include "parser.h"
#include <ctype.h>
#include <stdio.h>
#include <string.h>

/**
 * @brief executes OPEN operation to load database from file
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_open(StudentDatabase *db) {
  if (db->is_loaded) {
    // warn about unsaved changes before reload using checksums
    unsigned long current_checksum = compute_database_checksum(db);
    if (current_checksum != db->last_saved_checksum) {
      printf("\nWarning: You have unsaved changes that will be lost if you "
             "reload!\n");
    }

    char confirm[10];
    printf("A database is already opened. Do you want to reload? (Y/N): ");
    fflush(stdout);

    if (!fgets(confirm, sizeof confirm, stdin)) {
      return OP_ERROR_INPUT;
    }

    size_t len = strcspn(confirm, "\r\n");
    confirm[len] = '\0';

    if (len == 0 ||
        (toupper(confirm[0]) != 'Y' && toupper(confirm[0]) != 'N')) {
      printf("CMS: Invalid input. Operation cancelled.\n");
      return OP_ERROR_VALIDATION;
    }

    if (toupper(confirm[0]) == 'N') {
      cmd_wait_for_user();
      return OP_SUCCESS;
    }

    // user confirmed reload - clear existing data to prevent memory leak
    for (size_t i = 0; i < db->table_count; i++) {
      table_free(db->tables[i]);
    }
    db->table_count = 0;
  }

  char path_buf[256];
  const char *path = NULL;

  printf("Enter a file path (press ENTER for default data file): ");
  fflush(stdout);

  if (!fgets(path_buf, sizeof path_buf, stdin)) {
    // eof or error - use default
    printf(DEFAULT_FILE_MSG, DEFAULT_DATA_FILE);
    path = DEFAULT_DATA_FILE;
  } else {
    size_t len = strcspn(path_buf, "\r\n");
    path_buf[len] = '\0';

    if (len == 0) {
      printf(DEFAULT_FILE_MSG, DEFAULT_DATA_FILE);
      path = DEFAULT_DATA_FILE;
    } else {
      path = path_buf;
    }
  }

  ParseStatistics stats;
  DBStatus status = db_load(db, path, &stats);
  if (status != DB_SUCCESS) {
    printf("CMS: Failed to load database: %s\n", db_status_string(status));

    // if this was a reload, mark database as not loaded
    db->is_loaded = false;

    cmd_wait_for_user();

    return OP_ERROR_OPEN;
  }

  strncpy(db->filepath, path, sizeof db->filepath);
  db->filepath[sizeof db->filepath - 1] = '\0';

  db->is_loaded = true;

  // compute and store checksums after successful load
  db->file_loaded_checksum = compute_file_checksum(path);
  db->last_saved_checksum = compute_database_checksum(db);

  // display summary of loaded records
  printf("\n");
  printf("CMS: The database file \"%s\" is successfully opened.\n", path);

  if (stats.total_records_attempted > 0) {
    printf("CMS: Summary - %d record(s) loaded, %d record(s) skipped.\n",
           stats.records_loaded, stats.records_skipped);

    if (stats.records_skipped > 0) {
      printf("CMS: Details - %d validation error(s), %d parse error(s).\n",
             stats.validation_errors, stats.parse_errors);
      printf(
          "CMS: Note - Invalid records were skipped and not added to the "
          "database.\n");
    }
  }

  cmd_wait_for_user();

  return OP_SUCCESS;
}
