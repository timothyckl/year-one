/*
 * test_adv_query.c
 *
 * Test suite for adv_query_execute(): pipeline parsing, validation, and happy
 * paths using the shared test utilities and fixtures.
 */

#include "../include/adv_query.h"
#include "test_utils.h"

// helper to load a small database fixture
static StudentDatabase *load_fixture_db(void) {
  StudentDatabase *db = db_init();
  if (!db) {
    return NULL;
  }

  DBStatus status = db_load(db, get_test_file_path("test_valid.txt"), NULL);
  if (status != DB_SUCCESS) {
    db_free(db);
    return NULL;
  }

  return db;
}

// ---------------------------------------------------------------------------
// invalid argument and empty database handling
// ---------------------------------------------------------------------------

void test_adv_query_null_db(void) {
  AdvQueryStatus status = adv_query_execute(NULL, "GREP NAME = A");
  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_INVALID_ARGUMENT, status,
                   "NULL database should error");
}

void test_adv_query_null_pipeline(void) {
  StudentDatabase *db = load_fixture_db();
  ASSERT_NOT_NULL(db, "Fixture DB should load");
  if (!db) {
    return;
  }

  AdvQueryStatus status = adv_query_execute(db, NULL);
  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_INVALID_ARGUMENT, status,
                   "NULL pipeline should error");
  db_free(db);
}

void test_adv_query_empty_db(void) {
  StudentDatabase *db = db_init();
  AdvQueryStatus status = adv_query_execute(db, "GREP NAME = A");
  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_EMPTY_DATABASE, status,
                   "Empty database should error");
  db_free(db);
}

// ---------------------------------------------------------------------------
// parse and validation failures
// ---------------------------------------------------------------------------

void test_adv_query_empty_pipeline(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_PARSE, adv_query_execute(db, ""),
                   "Empty pipeline should parse-fail");
  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_PARSE, adv_query_execute(db, "   "),
                   "Whitespace-only pipeline should parse-fail");

  db_free(db);
}

void test_adv_query_unknown_command(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  AdvQueryStatus status = adv_query_execute(db, "HELLO NAME = X");
  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_PARSE, status,
                   "Unknown command should parse-fail");

  db_free(db);
}

void test_adv_query_disallowed_field(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_PARSE, adv_query_execute(db, "GREP ID = 1"),
                   "GREP ID should parse-fail");

  db_free(db);
}

void test_adv_query_duplicate_filters(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  ASSERT_EQUAL_INT(
      ADV_QUERY_ERROR_PARSE,
      adv_query_execute(db, "GREP NAME = A | GREP NAME = B"),
      "Duplicate GREP on name should parse-fail");
  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_PARSE,
                   adv_query_execute(db, "MARK > 50 | MARK < 60"),
                   "Duplicate MARK filters should parse-fail");

  db_free(db);
}

void test_adv_query_invalid_mark_operator_or_value(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_PARSE, adv_query_execute(db, "MARK != 50"),
                   "Invalid MARK operator should parse-fail");
  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_PARSE,
                   adv_query_execute(db, "MARK > not-a-number"),
                   "Non-numeric MARK value should parse-fail");

  db_free(db);
}

// ---------------------------------------------------------------------------
// successful pipelines
// ---------------------------------------------------------------------------

void test_adv_query_valid_grep_name(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  AdvQueryStatus status = adv_query_execute(db, "GREP NAME = Bo");
  ASSERT_EQUAL_INT(ADV_QUERY_SUCCESS, status,
                   "Valid GREP NAME should succeed");

  db_free(db);
}

void test_adv_query_valid_grep_programme(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  AdvQueryStatus status =
      adv_query_execute(db, "GREP PROGRAMME = Engineering");
  ASSERT_EQUAL_INT(ADV_QUERY_SUCCESS, status,
                   "Valid GREP PROGRAMME should succeed");

  db_free(db);
}

void test_adv_query_valid_mark(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  AdvQueryStatus status = adv_query_execute(db, "MARK > 80");
  ASSERT_EQUAL_INT(ADV_QUERY_SUCCESS, status,
                   "Valid MARK comparison should succeed");

  db_free(db);
}

void test_adv_query_combined_filters(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  AdvQueryStatus status =
      adv_query_execute(db, "GREP PROGRAMME = Science | MARK >= 67");
  // MARK "=" is allowed; using ">=" is not supported, so split into two stages
  ASSERT_EQUAL_INT(ADV_QUERY_ERROR_PARSE, status,
                   "Unsupported operator should parse-fail");

  status = adv_query_execute(db, "GREP PROGRAMME = Science | MARK > 60");
  ASSERT_EQUAL_INT(ADV_QUERY_SUCCESS, status,
                   "Combined GREP + MARK should succeed");

  db_free(db);
}

void test_adv_query_success_zero_matches(void) {
  StudentDatabase *db = load_fixture_db();
  if (!db) {
    ASSERT_TRUE(false, "Fixture DB should load");
    return;
  }

  AdvQueryStatus status =
      adv_query_execute(db, "GREP NAME = Nobody | MARK > 99");
  ASSERT_EQUAL_INT(ADV_QUERY_SUCCESS, status,
                   "Valid pipeline with no matches should succeed");

  db_free(db);
}

// ---------------------------------------------------------------------------
// test suite runner
// ---------------------------------------------------------------------------

int main(void) {
  TEST_SUITE_START("Advanced Query Tests");

  // invalid args / empty db
  RUN_TEST(test_adv_query_null_db);
  RUN_TEST(test_adv_query_null_pipeline);
  RUN_TEST(test_adv_query_empty_db);

  // parse/validation errors
  RUN_TEST(test_adv_query_empty_pipeline);
  RUN_TEST(test_adv_query_unknown_command);
  RUN_TEST(test_adv_query_disallowed_field);
  RUN_TEST(test_adv_query_duplicate_filters);
  RUN_TEST(test_adv_query_invalid_mark_operator_or_value);

  // success paths
  RUN_TEST(test_adv_query_valid_grep_name);
  RUN_TEST(test_adv_query_valid_grep_programme);
  RUN_TEST(test_adv_query_valid_mark);
  RUN_TEST(test_adv_query_combined_filters);
  RUN_TEST(test_adv_query_success_zero_matches);

  TEST_SUITE_END();
}
