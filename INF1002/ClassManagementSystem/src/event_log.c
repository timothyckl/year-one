#include "event_log.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief initialises a new event log
 * @return pointer to new log on success, NULL on allocation failure
 * @note caller responsible for freeing with event_log_free()
 * @note allocates initial capacity of 50 entries
 */
EventLog *event_log_init(void) {
  // allocate log structure
  EventLog *log = malloc(sizeof(EventLog));
  if (!log) {
    return NULL; // allocation failed
  }

  // allocate initial entries array
  log->entries = malloc(EVENT_LOG_INITIAL_CAPACITY * sizeof(EventEntry));
  if (!log->entries) {
    free(log);
    return NULL; // allocation failed
  }

  // initialise counters
  log->count = 0;
  log->capacity = EVENT_LOG_INITIAL_CAPACITY;

  return log;
}

/**
 * @brief frees event log and all associated memory
 * @param[in] log pointer to the event log to free (can be NULL)
 * @note safely handles NULL pointer (no-op)
 */
void event_log_free(EventLog *log) {
  if (!log) {
    return; // null-safe: no-op for null pointer
  }

  // free entries array
  if (log->entries) {
    free(log->entries);
  }

  // free log structure
  free(log);
}

/**
 * @brief logs an operation event
 * @param[in,out] log pointer to the event log
 * @param[in] op operation type to log
 * @param[in] status operation result status
 * @note automatically handles capacity growth (doubles until 1000 max)
 * @note uses circular buffer overflow (overwrites oldest entries)
 * @note captures current timestamp automatically
 * @note fails silently on errors (logging is non-critical infrastructure)
 */
void log_event(EventLog *log, Operation op, OpStatus status) {
  // defensive null check
  if (!log) {
    return; // silent fail - logging is non-critical
  }

  // determine if we need to grow capacity
  if (log->count < log->capacity) {
    // space available - add entry normally
    size_t idx = log->count;

    log->entries[idx].timestamp = time(NULL);
    log->entries[idx].operation = op;
    log->entries[idx].status = status;
    log->entries[idx].details[0] = '\0'; // empty details (reserved for future)

    log->count++;
  } else if (log->capacity < EVENT_LOG_MAX_CAPACITY) {
    // at capacity but can grow - attempt to double capacity
    size_t new_capacity = log->capacity * 2;

    // cap at maximum
    if (new_capacity > EVENT_LOG_MAX_CAPACITY) {
      new_capacity = EVENT_LOG_MAX_CAPACITY;
    }

    // attempt reallocation
    EventEntry *temp = realloc(log->entries, new_capacity * sizeof(EventEntry));

    if (!temp) {
      return; // silent fail - allocation failed, don't crash
    }

    // reallocation successful - update capacity
    log->entries = temp;
    log->capacity = new_capacity;

    // add entry
    size_t idx = log->count;
    log->entries[idx].timestamp = time(NULL);
    log->entries[idx].operation = op;
    log->entries[idx].status = status;
    log->entries[idx].details[0] = '\0';

    log->count++;
  } else {
    // at maximum capacity - use circular buffer (overwrite oldest)
    size_t idx = log->count % EVENT_LOG_MAX_CAPACITY;

    log->entries[idx].timestamp = time(NULL);
    log->entries[idx].operation = op;
    log->entries[idx].status = status;
    log->entries[idx].details[0] = '\0';

    log->count++; // keep incrementing for display purposes
  }
}

/**
 * @brief converts operation enum to display string
 * @param[in] op operation enum value to convert
 * @return pointer to static string with operation name (e.g., "OPEN", "INSERT")
 * @note returns "UNKNOWN" for invalid operation values
 */
const char *event_operation_to_string(Operation op) {
  switch (op) {
  case EXIT:
    return "EXIT";
  case OPEN:
    return "OPEN";
  case SHOW_ALL:
    return "SHOW_ALL";
  case INSERT:
    return "INSERT";
  case QUERY:
    return "QUERY";
  case UPDATE:
    return "UPDATE";
  case DELETE:
    return "DELETE";
  case SAVE:
    return "SAVE";
  case SORT:
    return "SORT";
  case ADV_QUERY:
    return "ADV_QUERY";
  case STATISTICS:
    return "STATISTICS";
  default:
    return "UNKNOWN";
  }
}

/**
 * @brief converts operation status to display string
 * @param[in] status operation status value to convert
 * @return pointer to static string with status description (e.g., "SUCCESS", "ERROR")
 * @note returns "UNKNOWN" for invalid status values
 */
const char *event_status_to_string(OpStatus status) {
  switch (status) {
  case OP_SUCCESS:
    return "SUCCESS";
  case OP_ERROR_OPEN:
    return "ERROR_OPEN";
  case OP_ERROR_INPUT:
    return "ERROR_INPUT";
  case OP_ERROR_VALIDATION:
    return "ERROR_VALIDATION";
  case OP_ERROR_DB_NOT_LOADED:
    return "ERROR_DB_NOT_LOADED";
  case OP_ERROR_GENERAL:
    return "ERROR_GENERAL";
  case OP_ERROR_INVALID:
    return "ERROR_INVALID";
  default:
    return "UNKNOWN";
  }
}

/**
 * @brief formats timestamp for display
 * @param[in] timestamp unix timestamp to format
 * @param[out] buffer output buffer for formatted string
 * @param[in] buffer_size size of output buffer (recommend 32 bytes minimum)
 * @return pointer to buffer for convenience
 * @note uses ISO 8601 format: YYYY-MM-DD HH:MM:SS
 */
const char *format_timestamp(time_t timestamp, char *buffer,
                             size_t buffer_size) {
  if (!buffer || buffer_size == 0) {
    return ""; // defensive check
  }

  struct tm *tm_info = localtime(&timestamp);
  if (!tm_info) {
    strncpy(buffer, "INVALID", buffer_size - 1);
    buffer[buffer_size - 1] = '\0';
    return buffer;
  }

  // format as ISO 8601: YYYY-MM-DD HH:MM:SS
  strftime(buffer, buffer_size, "%Y-%m-%d %H:%M:%S", tm_info);

  return buffer;
}
