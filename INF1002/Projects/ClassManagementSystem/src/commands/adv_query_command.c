#include "adv_query.h"
#include "commands/command.h"
#include "commands/command_utils.h"
#include <stdio.h>

/**
 * @brief executes ADV_QUERY operation for advanced filtering
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_adv_query(StudentDatabase *db) {
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }
  // guided prompt that builds a GREP/MARK pipeline then runs adv query
  AdvQueryStatus adv_status = adv_query_run_prompt(db);
  if (adv_status == ADV_QUERY_ERROR_EMPTY_DATABASE) {
    cmd_wait_for_user();
    return OP_SUCCESS;
  }
  if (adv_status != ADV_QUERY_SUCCESS) {
    printf("CMS: Advanced query failed: %s\n",
           adv_query_status_string(adv_status));
    cmd_wait_for_user();
    return OP_ERROR_GENERAL;
  }

  cmd_wait_for_user();
  return OP_SUCCESS;
}
