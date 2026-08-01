#include "../include/commands/command.h"
#include "../include/event_log.h"
#include "test_utils.h"

// =============================================================================
// event_log_init() tests
// =============================================================================

void test_event_log_init_valid(void) {
  EventLog *log = event_log_init();
  ASSERT_NOT_NULL(log, "event_log_init should return non-NULL pointer");
  if (log) {
    ASSERT_EQUAL_INT(50, log->capacity, "Initial capacity should be 50");
    ASSERT_EQUAL_INT(0, log->count, "Initial count should be 0");
    ASSERT_NOT_NULL(log->entries, "Entries array should be allocated");
    event_log_free(log);
  }
}

// =============================================================================
// event_log_free() tests
// =============================================================================

void test_event_log_free_empty(void) {
  EventLog *log = event_log_init();
  event_log_free(log);
  ASSERT_TRUE(true, "event_log_free on empty log should not crash");
}

void test_event_log_free_with_events(void) {
  EventLog *log = event_log_init();
  if (log) {
    log_event(log, OPEN, OP_SUCCESS);
    log_event(log, INSERT, OP_SUCCESS);
    event_log_free(log);
    ASSERT_TRUE(true, "event_log_free with events should not crash");
  }
}

void test_event_log_free_null(void) {
  event_log_free(NULL);
  ASSERT_TRUE(true, "event_log_free with NULL should not crash");
}

// =============================================================================
// log_event() tests
// =============================================================================

void test_log_event_first_event(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  log_event(log, OPEN, OP_SUCCESS);

  ASSERT_EQUAL_INT(1, log->count, "Count should be 1 after first event");
  if (log->count > 0) {
    ASSERT_EQUAL_INT(OPEN, log->entries[0].operation,
                     "Operation should be OPEN");
    ASSERT_EQUAL_INT(OP_SUCCESS, log->entries[0].status,
                     "Status should be OP_SUCCESS");
  }

  event_log_free(log);
}

void test_log_event_multiple_events(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  log_event(log, OPEN, OP_SUCCESS);
  log_event(log, INSERT, OP_SUCCESS);
  log_event(log, QUERY, OP_SUCCESS);
  log_event(log, UPDATE, OP_SUCCESS);
  log_event(log, DELETE, OP_SUCCESS);

  ASSERT_EQUAL_INT(5, log->count, "Count should be 5 after 5 events");

  event_log_free(log);
}

void test_log_event_null_log(void) {
  log_event(NULL, OPEN, OP_SUCCESS);
  ASSERT_TRUE(true, "log_event with NULL log should not crash");
}

void test_log_event_fill_to_initial_capacity(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  // fill to initial capacity (50)
  for (int i = 0; i < 50; i++) {
    log_event(log, INSERT, OP_SUCCESS);
  }

  ASSERT_EQUAL_INT(50, log->count, "Count should be 50");
  ASSERT_EQUAL_INT(50, log->capacity, "Capacity should still be 50");

  event_log_free(log);
}

void test_log_event_trigger_capacity_growth(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  // add 51 events to trigger capacity growth
  for (int i = 0; i < 51; i++) {
    log_event(log, INSERT, OP_SUCCESS);
  }

  ASSERT_EQUAL_INT(51, log->count, "Count should be 51");
  ASSERT_TRUE(log->capacity > 50, "Capacity should have grown beyond 50");
  ASSERT_TRUE(log->capacity >= 100, "Capacity should be at least 100");

  event_log_free(log);
}

void test_log_event_multiple_capacity_doublings(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  // add 500 events to trigger multiple capacity doublings
  for (int i = 0; i < 500; i++) {
    log_event(log, INSERT, OP_SUCCESS);
  }

  ASSERT_EQUAL_INT(500, log->count, "Count should be 500");
  ASSERT_TRUE(log->capacity >= 500, "Capacity should accommodate 500 events");

  event_log_free(log);
}

void test_log_event_reach_max_capacity(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  // add exactly 1000 events (max capacity)
  for (int i = 0; i < 1000; i++) {
    log_event(log, INSERT, OP_SUCCESS);
  }

  ASSERT_EQUAL_INT(1000, log->count, "Count should be 1000");
  ASSERT_EQUAL_INT(1000, log->capacity, "Capacity should be capped at 1000");

  event_log_free(log);
}

void test_log_event_circular_buffer_behaviour(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  // add 1500 events (500 more than max capacity)
  for (int i = 0; i < 1500; i++) {
    log_event(log, INSERT, OP_SUCCESS);
  }

  ASSERT_EQUAL_INT(1000, log->count, "Count should be capped at 1000");
  ASSERT_EQUAL_INT(1000, log->capacity, "Capacity should remain at 1000");

  event_log_free(log);
}

void test_log_event_different_operations(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  log_event(log, OPEN, OP_SUCCESS);
  log_event(log, INSERT, OP_ERROR_VALIDATION);
  log_event(log, QUERY, OP_SUCCESS);
  log_event(log, UPDATE, OP_ERROR_DB_NOT_LOADED);
  log_event(log, DELETE, OP_SUCCESS);
  log_event(log, SAVE, OP_ERROR_OPEN);
  log_event(log, SORT, OP_SUCCESS);
  log_event(log, SHOW_ALL, OP_SUCCESS);
  log_event(log, ADV_QUERY, OP_SUCCESS);
  log_event(log, STATISTICS, OP_ERROR_GENERAL);

  ASSERT_EQUAL_INT(10, log->count,
                   "Count should be 10 for different operations");
  ASSERT_EQUAL_INT(OPEN, log->entries[0].operation,
                   "First operation should be OPEN");
  ASSERT_EQUAL_INT(STATISTICS, log->entries[9].operation,
                   "Last operation should be STATISTICS");

  event_log_free(log);
}

void test_log_event_different_statuses(void) {
  EventLog *log = event_log_init();
  if (!log) {
    ASSERT_TRUE(false, "Failed to initialise event log");
    return;
  }

  log_event(log, INSERT, OP_SUCCESS);
  log_event(log, INSERT, OP_ERROR_VALIDATION);
  log_event(log, INSERT, OP_ERROR_DB_NOT_LOADED);
  log_event(log, INSERT, OP_ERROR_OPEN);
  log_event(log, INSERT, OP_ERROR_INPUT);
  log_event(log, INSERT, OP_ERROR_GENERAL);

  ASSERT_EQUAL_INT(6, log->count, "Count should be 6 for different statuses");
  ASSERT_EQUAL_INT(OP_SUCCESS, log->entries[0].status,
                   "First status should be OP_SUCCESS");
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, log->entries[5].status,
                   "Last status should be OP_ERROR_GENERAL");

  event_log_free(log);
}

// =============================================================================
// test suite runner
// =============================================================================

int main(void) {
  TEST_SUITE_START("Event Log Module Tests");

  // event_log_init tests
  RUN_TEST(test_event_log_init_valid);

  // event_log_free tests
  RUN_TEST(test_event_log_free_empty);
  RUN_TEST(test_event_log_free_with_events);
  RUN_TEST(test_event_log_free_null);

  // log_event tests
  RUN_TEST(test_log_event_first_event);
  RUN_TEST(test_log_event_multiple_events);
  RUN_TEST(test_log_event_null_log);
  RUN_TEST(test_log_event_fill_to_initial_capacity);
  RUN_TEST(test_log_event_trigger_capacity_growth);
  RUN_TEST(test_log_event_multiple_capacity_doublings);
  RUN_TEST(test_log_event_reach_max_capacity);
  RUN_TEST(test_log_event_circular_buffer_behaviour);
  RUN_TEST(test_log_event_different_operations);
  RUN_TEST(test_log_event_different_statuses);

  TEST_SUITE_END();
}
