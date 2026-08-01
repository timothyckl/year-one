#include "commands/command.h"
#include "commands/command_utils.h"
#include "event_log.h"
#include <stdio.h>

/**
 * @brief executes SHOW_LOG operation to display event history
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_show_log(StudentDatabase *db) {
  // validate database pointer
  if (!db) {
    return cmd_report_error("Database error.", OP_ERROR_GENERAL);
  }

  // check if event log exists
  // note: unlike other commands, we don't check is_loaded because
  // event log exists independently of database load state
  if (!db->event_log) {
    printf("CMS: Event log not initialised.\n");
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // handle empty log
  if (db->event_log->count == 0) {
    printf("CMS: No operations have been performed yet.\n");
    cmd_wait_for_user();
    return OP_SUCCESS;
  }

  // display header
  printf("==============================================================\n");
  printf("Operation History for Current Session\n\n");

  // display database info if loaded
  if (db->is_loaded && db->filepath[0] != '\0') {
    printf("Database File: %s\n", db->filepath);
  }

  // calculate display range (handle circular buffer)
  size_t total_ops = db->event_log->count;
  size_t start_idx = (total_ops > EVENT_LOG_MAX_CAPACITY)
                         ? (total_ops - EVENT_LOG_MAX_CAPACITY)
                         : 0;

  printf("Total Operations: %zu", total_ops);
  if (total_ops > EVENT_LOG_MAX_CAPACITY) {
    printf(" (showing most recent %d)\n", EVENT_LOG_MAX_CAPACITY);
  } else {
    printf("\n");
  }
  printf("\n");

  // display table header
  printf("%-20s %-12s %-20s\n", "Timestamp", "Operation", "Status");
  printf("%-20s %-12s %-20s\n", "--------------------", "------------",
         "--------------------");

  // display events
  for (size_t i = start_idx; i < total_ops; i++) {
    // handle circular buffer indexing
    size_t entry_idx = i % EVENT_LOG_MAX_CAPACITY;
    EventEntry *entry = &db->event_log->entries[entry_idx];

    // format timestamp
    char time_buf[32];
    format_timestamp(entry->timestamp, time_buf, sizeof time_buf);

    // get operation and status strings
    const char *op_str = event_operation_to_string(entry->operation);
    const char *status_str = event_status_to_string(entry->status);

    // display row
    printf("%-20s %-12s %-20s\n", time_buf, op_str, status_str);
  }

  printf("==============================================================\n");

  cmd_wait_for_user();

  return OP_SUCCESS;
}
