#ifndef EVENT_LOG_H
#define EVENT_LOG_H

#include "commands/command.h"
#include <stddef.h>
#include <time.h>

/**
 * @file event_log.h
 * @brief event log module for tracking user operations within a session
 *
 * provides functionality to:
 * - log operations with timestamps and status
 * - display operation history
 * - manage log lifecycle (init, free)
 *
 * capacity: starts at 50 entries, grows to 1000 maximum
 * overflow: implements circular buffer (overwrites oldest entries)
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

// maximum number of events to store
#define EVENT_LOG_MAX_CAPACITY 1000
#define EVENT_LOG_INITIAL_CAPACITY 50

/*
 * represents a single logged operation event
 *
 * captures operation type, execution status, and timestamp
 * details field reserved for future enhancements (unused initially)
 */
typedef struct {
  time_t timestamp;    // when operation occurred (unix time)
  Operation operation; // operation type from Operation enum
  OpStatus status;     // operation result status
  char details[128];   // optional context (unused initially, buffer reduced for
                       // safety)
} EventEntry;

/*
 * event log container managing dynamic array of events
 *
 * implements growth strategy:
 * - starts with 50 entries
 * - doubles when full
 * - caps at 1000 entries
 * - uses circular buffer when max reached
 */
struct EventLog {
  EventEntry *entries; // dynamic array of event entries
  size_t count;        // number of logged events (may exceed capacity for
                       // circular buffer)
  size_t capacity;     // current allocated capacity
};

/**
 * @brief initialises a new event log
 * @return pointer to new log on success, NULL on allocation failure
 * @note caller responsible for freeing with event_log_free()
 * @note allocates initial capacity of 50 entries
 */
EventLog *event_log_init(void);

/**
 * @brief frees event log and all associated memory
 * @param[in] log pointer to the event log to free (can be NULL)
 * @note safely handles NULL pointer (no-op)
 */
void event_log_free(EventLog *log);

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
void log_event(EventLog *log, Operation op, OpStatus status);

/**
 * @brief converts operation enum to display string
 * @param[in] op operation enum value to convert
 * @return pointer to static string with operation name (e.g., "OPEN", "INSERT")
 * @note returns "UNKNOWN" for invalid operation values
 */
const char *event_operation_to_string(Operation op);

/**
 * @brief converts operation status to display string
 * @param[in] status operation status value to convert
 * @return pointer to static string with status description (e.g., "SUCCESS", "ERROR")
 * @note returns "UNKNOWN" for invalid status values
 */
const char *event_status_to_string(OpStatus status);

/**
 * @brief formats timestamp for display
 * @param[in] timestamp unix timestamp to format
 * @param[out] buffer output buffer for formatted string
 * @param[in] buffer_size size of output buffer (recommend 32 bytes minimum)
 * @return pointer to buffer for convenience
 * @note uses ISO 8601 format: YYYY-MM-DD HH:MM:SS
 */
const char *format_timestamp(time_t timestamp, char *buffer,
                             size_t buffer_size);

#endif // EVENT_LOG_H
