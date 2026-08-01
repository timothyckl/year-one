#include "commands/command.h"
#include "commands/command_utils.h"
#include <stdio.h>

/**
 * @brief executes SAVE operation to write database to file
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_save(StudentDatabase *db) {
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }

  if (!db->is_loaded || db->table_count == 0) {
    return cmd_report_error("Database not loaded.", OP_ERROR_DB_NOT_LOADED);
  }

  if (db->filepath[0] == '\0') {
    return cmd_report_error("No file path stored for this database.",
                            OP_ERROR_VALIDATION);
  }

  DBStatus db_status = db_save(db, db->filepath);
  if (db_status != DB_SUCCESS) {
    char err_msg[256];
    snprintf(err_msg, sizeof err_msg, "Failed to save database: %s",
             db_status_string(db_status));
    return cmd_report_error(err_msg, OP_ERROR_GENERAL);
  }

  printf("CMS: The database file \"%s\" is successfully saved.\n",
         db->filepath);

  cmd_wait_for_user();

  return OP_SUCCESS;
}
