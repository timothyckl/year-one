#include "../include/commands/command.h"
#include "../include/commands/command_utils.h"
#include "../include/database.h"
#include "../include/event_log.h"
#include "../include/parser.h"
#include "test_utils.h"

/*
 * these tests focus on testable aspects (database preconditions, validation
 * logic)
 *
 * current tests cover:
 * - null pointer handling
 * - database state validation
 * - basic error conditions
 */

// helper functions
StudentDatabase *load_test_database(void) {
  StudentDatabase *db = db_init();
  if (!db)
    return NULL;

  ParseStatistics stats = {0};
  DBStatus status = db_load(db, get_test_file_path("test_valid.txt"), &stats);

  if (status != DB_SUCCESS) {
    db_free(db);
    return NULL;
  }

  return db;
}

// execute_open() tests
void test_execute_open_null_database(void) {
  OpStatus status = execute_open(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_open with NULL database should fail");
}

// note: full testing requires stdin mocking for filename input
// execute_query() tests
void test_execute_query_null_database(void) {
  OpStatus status = execute_query(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_query with NULL database should fail");
}

void test_execute_query_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_query(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_query with unloaded database should fail");
  db_free(db);
}

// note: full testing requires stdin mocking for id input
// execute_insert() tests
void test_execute_insert_null_database(void) {
  OpStatus status = execute_insert(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_insert with NULL database should fail");
}

void test_execute_insert_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_insert(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_insert with unloaded database should fail");
  db_free(db);
}

// note: full testing requires stdin mocking for record field inputs
// execute_update() tests
void test_execute_update_null_database(void) {
  OpStatus status = execute_update(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_update with NULL database should fail");
}

void test_execute_update_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_update(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_update with unloaded database should fail");
  db_free(db);
}

// note: full testing requires stdin mocking for id and field inputs
// execute_delete() tests
void test_execute_delete_null_database(void) {
  OpStatus status = execute_delete(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_delete with NULL database should fail");
}

void test_execute_delete_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_delete(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_delete with unloaded database should fail");
  db_free(db);
}

void test_execute_delete_empty_table(void) {
  StudentDatabase *db = db_init();
  StudentTable *table = table_init("StudentRecords");

  // create column headers
  char **headers = malloc(4 * sizeof(char *));
  headers[0] = strdup("ID");
  headers[1] = strdup("Name");
  headers[2] = strdup("Programme");
  headers[3] = strdup("Mark");
  table_set_column_headers(table, headers, 4);

  db_add_table(db, table);
  db->is_loaded = true;

  OpStatus status = execute_delete(db);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_delete with empty table should fail");

  db_free(db);
}

// note: full testing requires stdin mocking for id and confirmation inputs
// execute_save() tests
void test_execute_save_null_database(void) {
  OpStatus status = execute_save(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_save with NULL database should fail");
}

void test_execute_save_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_save(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_save with unloaded database should fail");
  db_free(db);
}

// note: full testing requires filesystem operations and stdin mocking
// execute_sort() tests
void test_execute_sort_null_database(void) {
  OpStatus status = execute_sort(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_sort with NULL database should fail");
}

void test_execute_sort_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_sort(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_sort with unloaded database should fail");
  db_free(db);
}

// note: full testing requires stdin mocking for sort field and order inputs
// execute_show_all() tests
void test_execute_show_all_null_database(void) {
  OpStatus status = execute_show_all(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_show_all with NULL database should fail");
}

void test_execute_show_all_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_show_all(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_show_all with unloaded database should fail");
  db_free(db);
}

void test_execute_show_all_with_records(void) {
  StudentDatabase *db = load_test_database();
  if (!db) {
    ASSERT_TRUE(false, "Failed to load test database");
    return;
  }

  OpStatus status = execute_show_all(db);
  ASSERT_EQUAL_INT(OP_SUCCESS, status,
                   "execute_show_all with records should succeed");

  db_free(db);
}

// execute_adv_query() tests (already tested in test_adv_query.c)
void test_execute_adv_query_null_database(void) {
  OpStatus status = execute_adv_query(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_adv_query with NULL database should fail");
}

void test_execute_adv_query_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_adv_query(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_adv_query with unloaded database should fail");
  db_free(db);
}

// note: comprehensive adv_query tests exist in test_adv_query.c
// execute_statistics() tests
void test_execute_statistics_null_database(void) {
  OpStatus status = execute_statistics(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_statistics with NULL database should fail");
}

void test_execute_statistics_not_loaded(void) {
  StudentDatabase *db = db_init();
  OpStatus status = execute_statistics(db);
  ASSERT_EQUAL_INT(OP_ERROR_DB_NOT_LOADED, status,
                   "execute_statistics with unloaded database should fail");
  db_free(db);
}

void test_execute_statistics_with_records(void) {
  StudentDatabase *db = load_test_database();
  if (!db) {
    ASSERT_TRUE(false, "Failed to load test database");
    return;
  }

  OpStatus status = execute_statistics(db);
  ASSERT_EQUAL_INT(OP_SUCCESS, status,
                   "execute_statistics with records should succeed");

  db_free(db);
}

// execute_show_log() tests
void test_execute_show_log_null_database(void) {
  OpStatus status = execute_show_log(NULL);
  ASSERT_EQUAL_INT(OP_ERROR_GENERAL, status,
                   "execute_show_log with NULL database should fail");
}

void test_execute_show_log_with_log(void) {
  StudentDatabase *db = db_init();
  db->event_log = event_log_init();

  if (db->event_log) {
    log_event(db->event_log, OPEN, OP_SUCCESS);
    log_event(db->event_log, INSERT, OP_SUCCESS);

    OpStatus status = execute_show_log(db);
    ASSERT_EQUAL_INT(OP_SUCCESS, status,
                     "execute_show_log with events should succeed");
  }

  db_free(db);
}

void test_execute_show_log_empty_log(void) {
  StudentDatabase *db = db_init();
  db->event_log = event_log_init();

  OpStatus status = execute_show_log(db);
  ASSERT_EQUAL_INT(OP_SUCCESS, status,
                   "execute_show_log with empty log should succeed");

  db_free(db);
}

// validation helper function tests
void test_cmd_is_alphabetic_valid(void) {
  ASSERT_TRUE(cmd_is_alphabetic("Alice"), "'Alice' should be alphabetic");
  ASSERT_TRUE(cmd_is_alphabetic("John Doe"),
              "'John Doe' should be alphabetic (with space)");
  ASSERT_TRUE(cmd_is_alphabetic("Mary"), "'Mary' should be alphabetic");
}

void test_cmd_is_alphabetic_invalid(void) {
  ASSERT_FALSE(cmd_is_alphabetic("John123"),
               "'John123' should not be alphabetic");
  ASSERT_FALSE(cmd_is_alphabetic("Test@Name"),
               "'Test@Name' should not be alphabetic");
}

void test_cmd_is_alphabetic_empty(void) {
  ASSERT_FALSE(cmd_is_alphabetic(""), "Empty string should not be alphabetic");
}

void test_cmd_is_alphabetic_spaces_only(void) {
  ASSERT_FALSE(cmd_is_alphabetic("   "),
               "Spaces only should not be alphabetic");
}

// test suite runner
int main(void) {
  TEST_SUITE_START("Commands Module Tests");

  printf("\n" COLOR_YELLOW
         "Note: Full command testing requires stdin mocking infrastructure.\n");
  printf("These tests cover database preconditions and error "
         "handling.\n" COLOR_RESET);

  // execute_open tests
  RUN_TEST(test_execute_open_null_database);

  // execute_query tests
  RUN_TEST(test_execute_query_null_database);
  RUN_TEST(test_execute_query_not_loaded);

  // execute_insert tests
  RUN_TEST(test_execute_insert_null_database);
  RUN_TEST(test_execute_insert_not_loaded);

  // execute_update tests
  RUN_TEST(test_execute_update_null_database);
  RUN_TEST(test_execute_update_not_loaded);

  // execute_delete tests
  RUN_TEST(test_execute_delete_null_database);
  RUN_TEST(test_execute_delete_not_loaded);
  RUN_TEST(test_execute_delete_empty_table);

  // execute_save tests
  RUN_TEST(test_execute_save_null_database);
  RUN_TEST(test_execute_save_not_loaded);

  // execute_sort tests
  RUN_TEST(test_execute_sort_null_database);
  RUN_TEST(test_execute_sort_not_loaded);

  // execute_show_all tests
  RUN_TEST(test_execute_show_all_null_database);
  RUN_TEST(test_execute_show_all_not_loaded);
  RUN_TEST(test_execute_show_all_with_records);

  // execute_adv_query tests
  RUN_TEST(test_execute_adv_query_null_database);
  RUN_TEST(test_execute_adv_query_not_loaded);

  // execute_statistics tests
  RUN_TEST(test_execute_statistics_null_database);
  RUN_TEST(test_execute_statistics_not_loaded);
  RUN_TEST(test_execute_statistics_with_records);

  // execute_show_log tests
  RUN_TEST(test_execute_show_log_null_database);
  RUN_TEST(test_execute_show_log_with_log);
  RUN_TEST(test_execute_show_log_empty_log);

  // validation helper tests
  RUN_TEST(test_cmd_is_alphabetic_valid);
  RUN_TEST(test_cmd_is_alphabetic_invalid);
  RUN_TEST(test_cmd_is_alphabetic_empty);
  RUN_TEST(test_cmd_is_alphabetic_spaces_only);

  TEST_SUITE_END();
}
