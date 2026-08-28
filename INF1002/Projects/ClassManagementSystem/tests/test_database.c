#include <math.h>
#include "../include/database.h"
#include "../include/parser.h"
#include "test_utils.h"

// =============================================================================
// table_init() tests
// =============================================================================

void test_table_init_valid(void) {
  StudentTable *table = table_init("TestTable");
  ASSERT_NOT_NULL(table, "table_init should return non-NULL pointer");
  if (table) {
    ASSERT_EQUAL_INT(10, table->record_capacity,
                     "Initial capacity should be 10");
    ASSERT_EQUAL_INT(0, table->record_count, "Initial count should be 0");
    ASSERT_NOT_NULL(table->records, "Records array should be allocated");
    ASSERT_EQUAL_STRING("TestTable", table->table_name,
                        "Table name should match");
    table_free(table);
  }
}

void test_table_init_empty_name(void) {
  StudentTable *table = table_init("");
  ASSERT_NOT_NULL(table, "table_init with empty name should succeed");
  if (table) {
    ASSERT_EQUAL_STRING("", table->table_name,
                        "Empty name should be preserved");
    table_free(table);
  }
}

void test_table_init_long_name(void) {
  char long_name[60];
  memset(long_name, 'A', 59);
  long_name[59] = '\0';
  StudentTable *table = table_init(long_name);
  ASSERT_NOT_NULL(table, "table_init with long name should succeed");
  if (table) {
    ASSERT_TRUE(strlen(table->table_name) <= 49,
                "Name should be truncated to 49 chars");
    table_free(table);
  }
}

// =============================================================================
// table_free() tests
// =============================================================================

void test_table_free_empty(void) {
  StudentTable *table = table_init("Test");
  table_free(table);
  ASSERT_TRUE(true, "table_free on empty table should not crash");
}

void test_table_free_with_records(void) {
  StudentTable *table = create_test_table_with_records("Test", 5);
  table_free(table);
  ASSERT_TRUE(true, "table_free with records should not crash");
}

void test_table_free_null(void) {
  table_free(NULL);
  ASSERT_TRUE(true, "table_free with NULL should not crash");
}

// =============================================================================
// table_add_record() tests
// =============================================================================

void test_table_add_record_to_empty(void) {
  StudentTable *table = table_init("Test");
  StudentRecord record = create_test_record(1000, "Test", "Programme", 75.0f);

  DBStatus status = table_add_record(table, &record);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Adding to empty table should succeed");
  ASSERT_EQUAL_INT(1, table->record_count, "Record count should be 1");
  if (table->record_count > 0) {
    ASSERT_EQUAL_INT(1000, table->records[0].id, "Record ID should match");
  }

  table_free(table);
}

void test_table_add_record_multiple(void) {
  StudentTable *table = table_init("Test");

  for (int i = 0; i < 5; i++) {
    StudentRecord record =
        create_test_record(1000 + i, "Test", "Programme", 75.0f);
    DBStatus status = table_add_record(table, &record);
    ASSERT_EQUAL_INT(DB_SUCCESS, status, "Adding record should succeed");
  }

  ASSERT_EQUAL_INT(5, table->record_count, "Should have 5 records");

  table_free(table);
}

void test_table_add_record_capacity_growth(void) {
  StudentTable *table = table_init("Test");
  size_t initial_capacity = table->record_capacity;

  // add more than initial capacity
  for (int i = 0; i < 15; i++) {
    StudentRecord record =
        create_test_record(1000 + i, "Test", "Programme", 75.0f);
    DBStatus status = table_add_record(table, &record);
    ASSERT_EQUAL_INT(DB_SUCCESS, status, "Adding record should succeed");
  }

  ASSERT_EQUAL_INT(15, table->record_count, "Should have 15 records");
  ASSERT_TRUE(table->record_capacity > initial_capacity,
              "Capacity should have grown");
  ASSERT_TRUE(table->record_capacity >= 20, "Capacity should be at least 20");

  table_free(table);
}

void test_table_add_record_null_table(void) {
  StudentRecord record = create_test_record(1000, "Test", "Programme", 75.0f);
  DBStatus status = table_add_record(NULL, &record);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL table should return error");
}

void test_table_add_record_null_record(void) {
  StudentTable *table = table_init("Test");
  DBStatus status = table_add_record(table, NULL);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL record should return error");
  table_free(table);
}

void test_table_add_record_boundary_id_zero(void) {
  StudentTable *table = table_init("Test");
  StudentRecord record = create_test_record(2500000, "Test", "Programme", 50.0f);
  DBStatus status = table_add_record(table, &record);
  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Adding record with MIN_STUDENT_ID should succeed");
  table_free(table);
}

void test_table_add_record_boundary_id_max(void) {
  StudentTable *table = table_init("Test");
  StudentRecord record =
      create_test_record(2600000, "Test", "Programme", 50.0f);
  DBStatus status = table_add_record(table, &record);
  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Adding record with MAX_STUDENT_ID should succeed");
  table_free(table);
}

void test_table_add_record_boundary_mark_zero(void) {
  StudentTable *table = table_init("Test");
  StudentRecord record = create_test_record(1000, "Test", "Programme", 0.0f);
  DBStatus status = table_add_record(table, &record);
  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Mark=0.0 should be valid");
  table_free(table);
}

void test_table_add_record_boundary_mark_max(void) {
  StudentTable *table = table_init("Test");
  StudentRecord record = create_test_record(1000, "Test", "Programme", 100.0f);
  DBStatus status = table_add_record(table, &record);
  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Mark=100.0 should be valid");
  table_free(table);
}

// =============================================================================
// table_remove_record() tests
// =============================================================================

void test_table_remove_record_existing(void) {
  StudentTable *table = create_test_table_with_records("Test", 5);
  int initial_count = table->record_count;
  int id_to_remove = table->records[2].id;

  DBStatus status = table_remove_record(table, id_to_remove);

  ASSERT_EQUAL_INT(DB_SUCCESS, status,
                   "Removing existing record should succeed");
  ASSERT_EQUAL_INT(initial_count - 1, table->record_count,
                   "Record count should decrease");

  table_free(table);
}

void test_table_remove_record_nonexistent(void) {
  StudentTable *table = create_test_table_with_records("Test", 5);

  DBStatus status = table_remove_record(table, 999999);

  ASSERT_EQUAL_INT(DB_ERROR_NOT_FOUND, status,
                   "Removing nonexistent record should fail");

  table_free(table);
}

void test_table_remove_record_from_empty(void) {
  StudentTable *table = table_init("Test");

  DBStatus status = table_remove_record(table, 1000);

  ASSERT_EQUAL_INT(DB_ERROR_NOT_FOUND, status,
                   "Removing from empty table should fail");

  table_free(table);
}

void test_table_remove_record_first(void) {
  StudentTable *table = create_test_table_with_records("Test", 5);
  int first_id = table->records[0].id;
  int second_id = table->records[1].id;

  DBStatus status = table_remove_record(table, first_id);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Removing first record should succeed");
  if (table->record_count > 0) {
    ASSERT_EQUAL_INT(second_id, table->records[0].id,
                     "Second record should shift to first position");
  }

  table_free(table);
}

void test_table_remove_record_last(void) {
  StudentTable *table = create_test_table_with_records("Test", 5);
  int initial_count = table->record_count;
  int last_id = table->records[initial_count - 1].id;

  DBStatus status = table_remove_record(table, last_id);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Removing last record should succeed");
  ASSERT_EQUAL_INT(initial_count - 1, table->record_count,
                   "Record count should decrease");

  table_free(table);
}

void test_table_remove_record_middle(void) {
  StudentTable *table = create_test_table_with_records("Test", 5);
  int middle_id = table->records[2].id;
  int fourth_id = table->records[3].id;

  DBStatus status = table_remove_record(table, middle_id);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Removing middle record should succeed");
  if (table->record_count > 2) {
    ASSERT_EQUAL_INT(fourth_id, table->records[2].id,
                     "Fourth record should shift to middle position");
  }

  table_free(table);
}

void test_table_remove_record_only_record(void) {
  StudentTable *table = create_test_table_with_records("Test", 1);
  int id = table->records[0].id;

  DBStatus status = table_remove_record(table, id);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Removing only record should succeed");
  ASSERT_EQUAL_INT(0, table->record_count, "Table should be empty");

  table_free(table);
}

void test_table_remove_record_null_table(void) {
  DBStatus status = table_remove_record(NULL, 1000);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL table should return error");
}

void test_table_remove_record_negative_id(void) {
  StudentTable *table = create_test_table_with_records("Test", 5);
  DBStatus status = table_remove_record(table, -1);
  ASSERT_EQUAL_INT(DB_ERROR_NOT_FOUND, status,
                   "Negative ID should not be found");
  table_free(table);
}

// =============================================================================
// db_init() tests
// =============================================================================

void test_db_init_valid(void) {
  StudentDatabase *db = db_init();
  ASSERT_NOT_NULL(db, "db_init should return non-NULL pointer");
  if (db) {
    ASSERT_EQUAL_INT(2, db->table_capacity, "Initial capacity should be 2");
    ASSERT_EQUAL_INT(0, db->table_count, "Initial count should be 0");
    ASSERT_NOT_NULL(db->tables, "Tables array should be allocated");
    ASSERT_FALSE(db->is_loaded, "is_loaded should be false");
    db_free(db);
  }
}

// =============================================================================
// db_add_table() tests
// =============================================================================

void test_db_add_table_first(void) {
  StudentDatabase *db = db_init();
  StudentTable *table = table_init("Test");

  DBStatus status = db_add_table(db, table);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Adding first table should succeed");
  ASSERT_EQUAL_INT(1, db->table_count, "Table count should be 1");

  db_free(db);
}

void test_db_add_table_multiple(void) {
  StudentDatabase *db = db_init();

  for (int i = 0; i < 3; i++) {
    StudentTable *table = table_init("Test");
    DBStatus status = db_add_table(db, table);
    ASSERT_EQUAL_INT(DB_SUCCESS, status, "Adding table should succeed");
  }

  ASSERT_EQUAL_INT(3, db->table_count, "Should have 3 tables");

  db_free(db);
}

void test_db_add_table_capacity_growth(void) {
  StudentDatabase *db = db_init();
  size_t initial_capacity = db->table_capacity;

  // add more than initial capacity
  for (int i = 0; i < 5; i++) {
    StudentTable *table = table_init("Test");
    DBStatus status = db_add_table(db, table);
    ASSERT_EQUAL_INT(DB_SUCCESS, status, "Adding table should succeed");
  }

  ASSERT_EQUAL_INT(5, db->table_count, "Should have 5 tables");
  ASSERT_TRUE(db->table_capacity > initial_capacity,
              "Capacity should have grown");

  db_free(db);
}

void test_db_add_table_null_db(void) {
  StudentTable *table = table_init("Test");
  DBStatus status = db_add_table(NULL, table);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL database should return error");
  table_free(table);
}

void test_db_add_table_null_table(void) {
  StudentDatabase *db = db_init();
  DBStatus status = db_add_table(db, NULL);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL table should return error");
  db_free(db);
}

// =============================================================================
// db_load() tests
// =============================================================================

void test_db_load_valid_file(void) {
  StudentDatabase *db = db_init();
  ParseStatistics stats = {0};

  DBStatus status = db_load(db, get_test_file_path("test_valid.txt"), &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Loading valid file should succeed");
  ASSERT_TRUE(db->table_count > 0, "Should have at least one table");
  ASSERT_TRUE(db->is_loaded, "is_loaded should be true");

  db_free(db);
}

void test_db_load_nonexistent_file(void) {
  StudentDatabase *db = db_init();
  ParseStatistics stats = {0};

  DBStatus status = db_load(db, "nonexistent_file.txt", &stats);

  ASSERT_EQUAL_INT(DB_ERROR_FILE_NOT_FOUND, status,
                   "Loading nonexistent file should fail");

  db_free(db);
}

void test_db_load_null_database(void) {
  ParseStatistics stats = {0};

  DBStatus status = db_load(NULL, get_test_file_path("test_valid.txt"), &stats);

  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL database should return error");
}

void test_db_load_null_filename(void) {
  StudentDatabase *db = db_init();
  ParseStatistics stats = {0};

  DBStatus status = db_load(db, NULL, &stats);

  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL filename should return error");

  db_free(db);
}

void test_db_load_empty_file(void) {
  StudentDatabase *db = db_init();
  ParseStatistics stats = {0};

  DBStatus status = db_load(db, get_test_file_path("test_empty.txt"), &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Loading empty file should succeed");
  if (db->table_count > 0) {
    ASSERT_EQUAL_INT(0, db->tables[0]->record_count,
                     "Table should have 0 records");
  }

  db_free(db);
}

// =============================================================================
// db_save() tests
// =============================================================================

void test_db_save_valid(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  const char *output_file = "tests/fixtures/test_output_temp.txt";

  DBStatus status = db_save(db, output_file);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Saving should succeed");

  // cleanup
  db_free(db);
  remove(output_file);
}

void test_db_save_null_database(void) {
  DBStatus status = db_save(NULL, "test_output.txt");
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL database should return error");
}

void test_db_save_null_filename(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  DBStatus status = db_save(db, NULL);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL filename should return error");
  db_free(db);
}

void test_db_save_empty_database(void) {
  StudentDatabase *db = db_init();
  DBStatus status = db_save(db, "test_output.txt");
  ASSERT_EQUAL_INT(DB_ERROR_INVALID_DATA, status,
                   "Saving empty database should fail");
  db_free(db);
}

// =============================================================================
// db_update_record() tests
// =============================================================================

void test_db_update_record_name(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  int id = db->tables[0]->records[0].id;
  const char *new_name = "Updated Name";

  DBStatus status = db_update_record(db, id, new_name, NULL, NULL);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Updating name should succeed");
  ASSERT_EQUAL_STRING(new_name, db->tables[0]->records[0].name,
                      "Name should be updated");

  db_free(db);
}

void test_db_update_record_programme(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  int id = db->tables[0]->records[0].id;
  const char *new_prog = "New Programme";

  DBStatus status = db_update_record(db, id, NULL, new_prog, NULL);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Updating programme should succeed");
  ASSERT_EQUAL_STRING(new_prog, db->tables[0]->records[0].prog,
                      "Programme should be updated");

  db_free(db);
}

void test_db_update_record_mark(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  int id = db->tables[0]->records[0].id;
  float new_mark = 95.5f;

  DBStatus status = db_update_record(db, id, NULL, NULL, &new_mark);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Updating mark should succeed");
  ASSERT_EQUAL_FLOAT(new_mark, db->tables[0]->records[0].mark, 0.01f,
                     "Mark should be updated");

  db_free(db);
}

void test_db_update_record_all_fields(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  int id = db->tables[0]->records[0].id;
  const char *new_name = "All Updated";
  const char *new_prog = "All Programme";
  float new_mark = 88.0f;

  DBStatus status = db_update_record(db, id, new_name, new_prog, &new_mark);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Updating all fields should succeed");
  ASSERT_EQUAL_STRING(new_name, db->tables[0]->records[0].name,
                      "Name should be updated");
  ASSERT_EQUAL_STRING(new_prog, db->tables[0]->records[0].prog,
                      "Programme should be updated");
  ASSERT_EQUAL_FLOAT(new_mark, db->tables[0]->records[0].mark, 0.01f,
                     "Mark should be updated");

  db_free(db);
}

void test_db_update_record_nonexistent_id(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  const char *new_name = "Test";

  DBStatus status = db_update_record(db, 999999, new_name, NULL, NULL);

  ASSERT_EQUAL_INT(DB_ERROR_NOT_FOUND, status,
                   "Updating nonexistent ID should fail");

  db_free(db);
}

void test_db_update_record_null_database(void) {
  const char *new_name = "Test";
  DBStatus status = db_update_record(NULL, 1000, new_name, NULL, NULL);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL database should return error");
}

void test_db_update_record_invalid_mark(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  int id = db->tables[0]->records[0].id;
  float invalid_mark = 150.0f;

  DBStatus status = db_update_record(db, id, NULL, NULL, &invalid_mark);

  ASSERT_EQUAL_INT(DB_ERROR_INVALID_DATA, status, "Invalid mark should fail");

  db_free(db);
}

void test_db_update_record_empty_name(void) {
  StudentDatabase *db = create_test_database_with_records(5);
  int id = db->tables[0]->records[0].id;

  DBStatus status = db_update_record(db, id, "", NULL, NULL);

  ASSERT_EQUAL_INT(DB_ERROR_INVALID_DATA, status, "Empty name should fail");

  db_free(db);
}

// =============================================================================
// test suite runner
// =============================================================================

int main(void) {
  TEST_SUITE_START("Database Module Tests");

  // table_init tests
  RUN_TEST(test_table_init_valid);
  RUN_TEST(test_table_init_empty_name);
  RUN_TEST(test_table_init_long_name);

  // table_free tests
  RUN_TEST(test_table_free_empty);
  RUN_TEST(test_table_free_with_records);
  RUN_TEST(test_table_free_null);

  // table_add_record tests
  RUN_TEST(test_table_add_record_to_empty);
  RUN_TEST(test_table_add_record_multiple);
  RUN_TEST(test_table_add_record_capacity_growth);
  RUN_TEST(test_table_add_record_null_table);
  RUN_TEST(test_table_add_record_null_record);
  RUN_TEST(test_table_add_record_boundary_id_zero);
  RUN_TEST(test_table_add_record_boundary_id_max);
  RUN_TEST(test_table_add_record_boundary_mark_zero);
  RUN_TEST(test_table_add_record_boundary_mark_max);

  // table_remove_record tests
  RUN_TEST(test_table_remove_record_existing);
  RUN_TEST(test_table_remove_record_nonexistent);
  RUN_TEST(test_table_remove_record_from_empty);
  RUN_TEST(test_table_remove_record_first);
  RUN_TEST(test_table_remove_record_last);
  RUN_TEST(test_table_remove_record_middle);
  RUN_TEST(test_table_remove_record_only_record);
  RUN_TEST(test_table_remove_record_null_table);
  RUN_TEST(test_table_remove_record_negative_id);

  // db_init tests
  RUN_TEST(test_db_init_valid);

  // db_add_table tests
  RUN_TEST(test_db_add_table_first);
  RUN_TEST(test_db_add_table_multiple);
  RUN_TEST(test_db_add_table_capacity_growth);
  RUN_TEST(test_db_add_table_null_db);
  RUN_TEST(test_db_add_table_null_table);

  // db_load tests
  RUN_TEST(test_db_load_valid_file);
  RUN_TEST(test_db_load_nonexistent_file);
  RUN_TEST(test_db_load_null_database);
  RUN_TEST(test_db_load_null_filename);
  RUN_TEST(test_db_load_empty_file);

  // db_save tests
  RUN_TEST(test_db_save_valid);
  RUN_TEST(test_db_save_null_database);
  RUN_TEST(test_db_save_null_filename);
  RUN_TEST(test_db_save_empty_database);

  // db_update_record tests
  RUN_TEST(test_db_update_record_name);
  RUN_TEST(test_db_update_record_programme);
  RUN_TEST(test_db_update_record_mark);
  RUN_TEST(test_db_update_record_all_fields);
  RUN_TEST(test_db_update_record_nonexistent_id);
  RUN_TEST(test_db_update_record_null_database);
  RUN_TEST(test_db_update_record_invalid_mark);
  RUN_TEST(test_db_update_record_empty_name);

  TEST_SUITE_END();
}
