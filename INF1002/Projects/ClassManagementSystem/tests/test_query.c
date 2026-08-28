/*
 * test_query.c
 *
 * Test suite for the basic QUERY behaviour: locating records by ID in the
 * primary table (index 0). Mirrors the simple search logic used by the query
 * command without relying on stdin.
 */

#include "../include/database.h"
#include "test_utils.h"

// lightweight lookup that matches the query command behaviour
static StudentRecord *find_record_by_id(StudentDatabase *db, int id) {
  if (!db || db->table_count == 0) {
    return NULL;
  }

  StudentTable *table = db->tables[0];
  if (!table || table->record_count == 0) {
    return NULL;
  }

  for (size_t i = 0; i < table->record_count; i++) {
    if (table->records[i].id == id) {
      return &table->records[i];
    }
  }

  return NULL;
}

// ---------------------------------------------------------------------------
// edge cases and validation
// ---------------------------------------------------------------------------

void test_query_null_database(void) {
  StudentRecord *record = find_record_by_id(NULL, 1234);
  ASSERT_NULL(record, "Query with NULL database should return NULL");
}

void test_query_no_tables(void) {
  StudentDatabase *db = db_init();
  StudentRecord *record = find_record_by_id(db, 1234);
  ASSERT_NULL(record, "Query with no tables should return NULL");
  db_free(db);
}

void test_query_null_table_entry(void) {
  StudentDatabase *db = db_init();
  db->table_count = 1;
  db->tables[0] = NULL; // simulate missing primary table

  StudentRecord *record = find_record_by_id(db, 1234);
  ASSERT_NULL(record, "Query with NULL table slot should return NULL");

  db_free(db);
}

void test_query_empty_table(void) {
  StudentDatabase *db = db_init();
  StudentTable *table = table_init("Empty");
  db->tables[0] = table;
  db->table_count = 1;

  StudentRecord *record = find_record_by_id(db, 1234);
  ASSERT_NULL(record, "Query on empty table should return NULL");

  db_free(db);
}

// ---------------------------------------------------------------------------
// successful lookups and misses
// ---------------------------------------------------------------------------

void test_query_first_record(void) {
  StudentDatabase *db = db_init();
  StudentTable *table = create_test_table_with_records("Records", 3);
  db->tables[0] = table;
  db->table_count = 1;

  int target_id = table->records[0].id;
  StudentRecord *record = find_record_by_id(db, target_id);
  ASSERT_NOT_NULL(record, "Should find first record by ID");
  if (record) {
    ASSERT_EQUAL_INT(target_id, record->id, "Returned record ID should match");
  }

  db_free(db);
}

void test_query_last_record(void) {
  StudentDatabase *db = db_init();
  StudentTable *table = create_test_table_with_records("Records", 5);
  db->tables[0] = table;
  db->table_count = 1;

  int target_id = table->records[table->record_count - 1].id;
  StudentRecord *record = find_record_by_id(db, target_id);
  ASSERT_NOT_NULL(record, "Should find last record by ID");
  if (record) {
    ASSERT_EQUAL_INT(target_id, record->id, "Returned record ID should match");
  }

  db_free(db);
}

void test_query_nonexistent_id(void) {
  StudentDatabase *db = db_init();
  StudentTable *table = create_test_table_with_records("Records", 4);
  db->tables[0] = table;
  db->table_count = 1;

  StudentRecord *record = find_record_by_id(db, 9999999);
  ASSERT_NULL(record, "Query for invalid ID should return NULL");

  db_free(db);
}

void test_query_duplicate_ids_returns_first(void) {
  StudentDatabase *db = db_init();
  StudentTable *table = create_test_table_with_records("Records", 3);
  // introduce a duplicate ID intentionally
  table->records[2].id = table->records[0].id;
  db->tables[0] = table;
  db->table_count = 1;

  StudentRecord *record = find_record_by_id(db, table->records[0].id);
  ASSERT_NOT_NULL(record, "Should return a record when duplicate IDs exist");
  if (record) {
    ASSERT_EQUAL_INT(table->records[0].id, record->id,
                     "Should return the first matching record");
    ASSERT_EQUAL_STRING(table->records[0].name, record->name,
                        "First matching record's data should be returned");
  }

  db_free(db);
}

// ---------------------------------------------------------------------------
// test suite runner
// ---------------------------------------------------------------------------

int main(void) {
  TEST_SUITE_START("Query Tests");

  RUN_TEST(test_query_null_database);
  RUN_TEST(test_query_no_tables);
  RUN_TEST(test_query_null_table_entry);
  RUN_TEST(test_query_empty_table);

  RUN_TEST(test_query_first_record);
  RUN_TEST(test_query_last_record);
  RUN_TEST(test_query_nonexistent_id);
  RUN_TEST(test_query_duplicate_ids_returns_first);

  TEST_SUITE_END();
}
