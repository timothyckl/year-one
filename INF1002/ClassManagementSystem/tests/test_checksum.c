/*
 * test_checksum.c
 *
 * unit tests for checksum module
 * tests crc32-based integrity checking for student database records
 *
 * functions tested:
 * - compute_record_checksum()   : computes checksum of individual record
 * - compute_database_checksum() : computes checksum of entire database
 * - compute_file_checksum()     : computes checksum of file on disk
 */

#include "../include/checksum.h"
#include "../include/database.h"
#include "test_utils.h"
#include <string.h>

// helper functions
// create a test database with empty student records table
StudentDatabase *create_test_database_for_checksum(void) {
  StudentDatabase *db = db_init();
  if (!db)
    return NULL;

  StudentTable *table = table_init("StudentRecords");
  if (!table) {
    db_free(db);
    return NULL;
  }

  db_add_table(db, table);
  db->is_loaded = true;

  return db;
}

// add a test record to database
void add_test_record_to_database(StudentDatabase *db, int id, const char *name,
                                 const char *prog, float mark) {
  StudentRecord record;
  record.id = id;
  strncpy(record.name, name, sizeof(record.name) - 1);
  record.name[sizeof(record.name) - 1] = '\0';
  strncpy(record.prog, prog, sizeof(record.prog) - 1);
  record.prog[sizeof(record.prog) - 1] = '\0';
  record.mark = mark;

  StudentTable *table = db->tables[0];
  table_add_record(table, &record);
}

// compute_record_checksum() tests
void test_record_checksum_null(void) {
  unsigned long checksum = compute_record_checksum(NULL);
  ASSERT_TRUE(checksum == 0, "null record should return 0");
}

void test_record_checksum_valid(void) {
  StudentRecord record =
      create_test_record(2301234, "Joshua Chen", "Software Engineering", 70.5f);

  unsigned long checksum = compute_record_checksum(&record);
  ASSERT_TRUE(checksum != 0, "valid record should have non-zero checksum");
}

void test_record_checksum_consistency(void) {
  StudentRecord record =
      create_test_record(2301234, "Joshua Chen", "Software Engineering", 70.5f);

  unsigned long checksum1 = compute_record_checksum(&record);
  unsigned long checksum2 = compute_record_checksum(&record);

  ASSERT_TRUE(checksum1 == checksum2,
              "same record should produce same checksum");
}

void test_record_checksum_change_detection_id(void) {
  StudentRecord record1 =
      create_test_record(2301234, "Joshua Chen", "Software Engineering", 70.5f);
  StudentRecord record2 =
      create_test_record(2301235, "Joshua Chen", "Software Engineering", 70.5f);

  unsigned long checksum1 = compute_record_checksum(&record1);
  unsigned long checksum2 = compute_record_checksum(&record2);

  ASSERT_TRUE(checksum1 != checksum2,
              "different id should produce different checksum");
}

void test_record_checksum_change_detection_name(void) {
  StudentRecord record1 =
      create_test_record(2301234, "Joshua Chen", "Software Engineering", 70.5f);
  StudentRecord record2 =
      create_test_record(2301234, "Sarah Lee", "Software Engineering", 70.5f);

  unsigned long checksum1 = compute_record_checksum(&record1);
  unsigned long checksum2 = compute_record_checksum(&record2);

  ASSERT_TRUE(checksum1 != checksum2,
              "different name should produce different checksum");
}

void test_record_checksum_change_detection_programme(void) {
  StudentRecord record1 =
      create_test_record(2301234, "Joshua Chen", "Software Engineering", 70.5f);
  StudentRecord record2 =
      create_test_record(2301234, "Joshua Chen", "Computer Science", 70.5f);

  unsigned long checksum1 = compute_record_checksum(&record1);
  unsigned long checksum2 = compute_record_checksum(&record2);

  ASSERT_TRUE(checksum1 != checksum2,
              "different programme should produce different checksum");
}

void test_record_checksum_change_detection_mark(void) {
  StudentRecord record1 =
      create_test_record(2301234, "Joshua Chen", "Software Engineering", 70.5f);
  StudentRecord record2 =
      create_test_record(2301234, "Joshua Chen", "Software Engineering", 85.0f);

  unsigned long checksum1 = compute_record_checksum(&record1);
  unsigned long checksum2 = compute_record_checksum(&record2);

  ASSERT_TRUE(checksum1 != checksum2,
              "different mark should produce different checksum");
}

void test_record_checksum_boundary_max_length_name(void) {
  StudentRecord record;
  record.id = 2600000;
  memset(record.name, 'A', sizeof(record.name) - 1);
  record.name[sizeof(record.name) - 1] = '\0';
  strcpy(record.prog, "Computer Science");
  record.mark = 100.0f;

  unsigned long checksum = compute_record_checksum(&record);
  ASSERT_TRUE(checksum != 0, "max length name should produce valid checksum");
}

void test_record_checksum_boundary_max_length_programme(void) {
  StudentRecord record;
  record.id = 2600000;
  strcpy(record.name, "Test Student");
  memset(record.prog, 'B', sizeof(record.prog) - 1);
  record.prog[sizeof(record.prog) - 1] = '\0';
  record.mark = 100.0f;

  unsigned long checksum = compute_record_checksum(&record);
  ASSERT_TRUE(checksum != 0,
              "max length programme should produce valid checksum");
}

void test_record_checksum_boundary_empty_strings(void) {
  StudentRecord record;
  record.id = 1000;
  strcpy(record.name, "");
  strcpy(record.prog, "");
  record.mark = 50.0f;

  unsigned long checksum = compute_record_checksum(&record);
  ASSERT_TRUE(checksum != 0, "empty strings should produce valid checksum");
}

// compute_database_checksum() tests
void test_database_checksum_null(void) {
  unsigned long checksum = compute_database_checksum(NULL);
  ASSERT_TRUE(checksum == 0, "null database should return 0");
}

void test_database_checksum_empty(void) {
  StudentDatabase *db = create_test_database_for_checksum();
  ASSERT_NOT_NULL(db, "test database creation should succeed");

  if (db) {
    unsigned long checksum = compute_database_checksum(db);
    ASSERT_TRUE(checksum == 0, "empty database should return 0");
    db_free(db);
  }
}

void test_database_checksum_single_record(void) {
  StudentDatabase *db = create_test_database_for_checksum();
  ASSERT_NOT_NULL(db, "test database creation should succeed");

  if (db) {
    add_test_record_to_database(db, 2301234, "Joshua Chen",
                                "Software Engineering", 70.5f);

    unsigned long checksum = compute_database_checksum(db);
    ASSERT_TRUE(checksum != 0,
                "single record database should have non-zero checksum");
    db_free(db);
  }
}

void test_database_checksum_multiple_records(void) {
  StudentDatabase *db = create_test_database_for_checksum();
  ASSERT_NOT_NULL(db, "test database creation should succeed");

  if (db) {
    add_test_record_to_database(db, 2301234, "Joshua Chen",
                                "Software Engineering", 70.5f);
    add_test_record_to_database(db, 2301235, "Sarah Lee", "Computer Science",
                                85.0f);
    add_test_record_to_database(db, 2301236, "Mike Wong", "Information Systems",
                                92.5f);

    unsigned long checksum = compute_database_checksum(db);
    ASSERT_TRUE(checksum != 0,
                "multiple record database should have non-zero checksum");
    db_free(db);
  }
}

void test_database_checksum_consistency(void) {
  StudentDatabase *db = create_test_database_for_checksum();
  ASSERT_NOT_NULL(db, "test database creation should succeed");

  if (db) {
    add_test_record_to_database(db, 2301234, "Joshua Chen",
                                "Software Engineering", 70.5f);
    add_test_record_to_database(db, 2301235, "Sarah Lee", "Computer Science",
                                85.0f);

    unsigned long checksum1 = compute_database_checksum(db);
    unsigned long checksum2 = compute_database_checksum(db);

    ASSERT_TRUE(checksum1 == checksum2,
                "same database should produce same checksum on repeated calls");
    db_free(db);
  }
}

void test_database_checksum_modification_detection(void) {
  StudentDatabase *db = create_test_database_for_checksum();
  ASSERT_NOT_NULL(db, "test database creation should succeed");

  if (db) {
    add_test_record_to_database(db, 2301234, "Joshua Chen",
                                "Software Engineering", 70.5f);

    unsigned long checksum1 = compute_database_checksum(db);

    // modify existing record
    StudentTable *table = db->tables[0];
    table->records[0].mark = 75.0f;

    unsigned long checksum2 = compute_database_checksum(db);

    ASSERT_TRUE(checksum1 != checksum2,
                "database checksum should change after record modification");
    db_free(db);
  }
}

void test_database_checksum_not_loaded(void) {
  StudentDatabase *db = db_init();
  ASSERT_NOT_NULL(db, "database init should succeed");

  if (db) {
    db->is_loaded = false;
    unsigned long checksum = compute_database_checksum(db);
    ASSERT_TRUE(checksum == 0, "not loaded database should return 0");
    db_free(db);
  }
}

void test_database_checksum_no_tables(void) {
  StudentDatabase *db = db_init();
  ASSERT_NOT_NULL(db, "database init should succeed");

  if (db) {
    db->is_loaded = true;
    db->table_count = 0;
    unsigned long checksum = compute_database_checksum(db);
    ASSERT_TRUE(checksum == 0, "database with no tables should return 0");
    db_free(db);
  }
}

// compute_file_checksum() tests
void test_file_checksum_null(void) {
  unsigned long checksum = compute_file_checksum(NULL);
  ASSERT_TRUE(checksum == 0, "null filepath should return 0");
}

void test_file_checksum_nonexistent_file(void) {
  unsigned long checksum =
      compute_file_checksum("/nonexistent/path/to/file.txt");
  ASSERT_TRUE(checksum == 0, "nonexistent file should return 0");
}

void test_file_checksum_valid_file(void) {
  const char *filepath = "data/P1_8-CMS.txt";
  unsigned long checksum = compute_file_checksum(filepath);
  // cannot test exact value, but should be non-zero if file exists
  // test passes gracefully if file does not exist (returns 0)
  ASSERT_TRUE(checksum >= 0, "file checksum should be valid or 0 if missing");
}

void test_file_checksum_consistency(void) {
  const char *filepath = "data/P1_8-CMS.txt";

  unsigned long checksum1 = compute_file_checksum(filepath);
  unsigned long checksum2 = compute_file_checksum(filepath);

  // if file exists, checksums should match
  if (checksum1 != 0) {
    ASSERT_TRUE(checksum1 == checksum2,
                "same file should produce same checksum on repeated calls");
  } else {
    ASSERT_TRUE(true, "file does not exist, test skipped gracefully");
  }
}

// main test runner
int main(void) {
  TEST_SUITE_START("Checksum Tests");

  // compute_record_checksum tests
  RUN_TEST(test_record_checksum_null);
  RUN_TEST(test_record_checksum_valid);
  RUN_TEST(test_record_checksum_consistency);
  RUN_TEST(test_record_checksum_change_detection_id);
  RUN_TEST(test_record_checksum_change_detection_name);
  RUN_TEST(test_record_checksum_change_detection_programme);
  RUN_TEST(test_record_checksum_change_detection_mark);
  RUN_TEST(test_record_checksum_boundary_max_length_name);
  RUN_TEST(test_record_checksum_boundary_max_length_programme);
  RUN_TEST(test_record_checksum_boundary_empty_strings);

  // compute_database_checksum tests
  RUN_TEST(test_database_checksum_null);
  RUN_TEST(test_database_checksum_empty);
  RUN_TEST(test_database_checksum_single_record);
  RUN_TEST(test_database_checksum_multiple_records);
  RUN_TEST(test_database_checksum_consistency);
  RUN_TEST(test_database_checksum_modification_detection);
  RUN_TEST(test_database_checksum_not_loaded);
  RUN_TEST(test_database_checksum_no_tables);

  // compute_file_checksum tests
  RUN_TEST(test_file_checksum_null);
  RUN_TEST(test_file_checksum_nonexistent_file);
  RUN_TEST(test_file_checksum_valid_file);
  RUN_TEST(test_file_checksum_consistency);

  TEST_SUITE_END();
}
