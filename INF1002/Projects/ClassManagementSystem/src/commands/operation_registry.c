#include "commands/command.h"
#include "checksum.h"
#include "event_log.h"
#include <stdio.h>
#include <string.h>

// operation entry mapping operation type to execution function
typedef struct {
  Operation op;
  CommandFunc func;
  const char *name;
} OperationEntry;

// registry of all available operations
static const OperationEntry operations[] = {
    {OPEN, execute_open, "open"},
    {SHOW_ALL, execute_show_all, "show_all"},
    {INSERT, execute_insert, "insert"},
    {QUERY, execute_query, "query"},
    {UPDATE, execute_update, "update"},
    {DELETE, execute_delete, "delete"},
    {SAVE, execute_save, "save"},
    {SORT, execute_sort, "sort"},
    {ADV_QUERY, execute_adv_query, "adv_query"},
    {STATISTICS, execute_statistics, "statistics"},
    {SHOW_LOG, execute_show_log, "show_log"},
    {CHECKSUM, execute_checksum, "checksum"},
};

static const size_t operation_count =
    sizeof(operations) / sizeof(operations[0]);

/*
 * determines if an operation should be logged
 *
 * excludes display-only operations and special operations
 * view operations (SHOW_ALL, STATISTICS, SHOW_LOG) are not logged
 * EXIT is not logged (session terminator)
 */
static bool should_log_operation(Operation op) {
  return (op != EXIT && op != SHOW_ALL && op != STATISTICS && op != SHOW_LOG &&
          op != CHECKSUM);
}

/**
 * @brief executes the specified operation on the database
 * @param[in] op operation type to execute
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_operation(Operation op, StudentDatabase *db) {
  // handle EXIT operation specially
  if (op == EXIT) {
    // check for unsaved changes before exiting using checksums
    if (db && db->is_loaded) {
      unsigned long current_checksum = compute_database_checksum(db);
      if (current_checksum != db->last_saved_checksum) {
        printf("\nWarning: You have unsaved changes!\n");
        printf("What would you like to do?\n");
        printf("  [1] Save and exit\n");
        printf("  [2] Discard and exit\n");
        printf("  [3] Cancel (return to menu)\n");
        printf("Enter your choice: ");
        fflush(stdout);

        char choice_buf[10];
        if (!fgets(choice_buf, sizeof choice_buf, stdin)) {
          printf("CMS: Failed to read input. Returning to menu.\n");
          return OP_ERROR_INPUT;
        }

        // strip trailing newline/carriage return
        size_t len = strcspn(choice_buf, "\r\n");
        choice_buf[len] = '\0';

        // handle user choice
        if (len == 1 && choice_buf[0] == '1') {
          // save and exit
          if (db->filepath[0] != '\0') {
            DBStatus db_status = db_save(db, db->filepath);
            if (db_status == DB_SUCCESS) {
              printf("CMS: Database saved successfully.\n");
            } else {
              printf("CMS: Failed to save database: %s\n",
                     db_status_string(db_status));
              printf("CMS: Exit cancelled. Returning to menu.\n");
              return OP_ERROR_GENERAL;
            }
          } else {
            printf("CMS: No file path available for saving.\n");
            printf("CMS: Exit cancelled. Returning to menu.\n");
            return OP_ERROR_GENERAL;
          }
        } else if (len == 1 && choice_buf[0] == '2') {
          // discard and exit - just continue to print goodbye
          printf("CMS: Changes discarded.\n");
        } else if (len == 1 && choice_buf[0] == '3') {
          // cancel exit
          printf("CMS: Exit cancelled. Returning to menu.\n");
          return OP_ERROR_GENERAL;
        } else {
          // invalid choice - cancel exit
          printf("CMS: Invalid choice. Exit cancelled. Returning to menu.\n");
          return OP_ERROR_VALIDATION;
        }
      }
    }

    printf("Goodbye!\n");
    return OP_SUCCESS;
  }

  // find and execute the operation
  OpStatus result = OP_ERROR_INVALID;
  for (size_t i = 0; i < operation_count; i++) {
    if (operations[i].op == op) {
      result = operations[i].func(db);
      break;
    }
  }

  // if operation not found, report error
  if (result == OP_ERROR_INVALID) {
    printf("CMS: Invalid operation\n");
    return result;
  }

  // log the operation if database exists and operation should be logged
  if (db && should_log_operation(op)) {
    // initialise event log on first use if needed
    if (!db->event_log) {
      db->event_log = event_log_init();
      // if init fails, continue silently (logging is non-critical)
    }

    // log the event (fails silently if log is NULL)
    if (db->event_log) {
      log_event(db->event_log, op, result);
    }
  }

  return result;
}
