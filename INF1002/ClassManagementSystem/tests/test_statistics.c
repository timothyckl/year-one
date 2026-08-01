#include <math.h>
#include "../include/database.h"
#include "../include/statistics.h"
#include "test_utils.h"

// =============================================================================
// calculate_statistics() tests
// =============================================================================

void test_calculate_statistics_normal(void) {
  StudentTable *table = table_init("Test");

  // add records with varied marks
  table_add_record(table, &(StudentRecord){1001, "Alice", "CS", 95.5f});
  table_add_record(table, &(StudentRecord){1002, "Bob", "SE", 82.0f});
  table_add_record(table, &(StudentRecord){1003, "Charlie", "DS", 67.5f});
  table_add_record(table, &(StudentRecord){1004, "Diana", "CS", 91.0f});
  table_add_record(table, &(StudentRecord){1005, "Eve", "SE", 75.0f});

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Calculation should succeed");
  ASSERT_EQUAL_FLOAT(82.2f, stats.average_mark, 0.1f,
                     "Average should be correct");
  ASSERT_EQUAL_FLOAT(95.5f, stats.highest_mark, 0.01f,
                     "Highest mark should be 95.5");
  ASSERT_EQUAL_FLOAT(67.5f, stats.lowest_mark, 0.01f,
                     "Lowest mark should be 67.5");
  ASSERT_EQUAL_STRING("Alice", stats.highest_student_name,
                      "Highest student should be Alice");
  ASSERT_EQUAL_STRING("Charlie", stats.lowest_student_name,
                      "Lowest student should be Charlie");

  table_free(table);
}

void test_calculate_statistics_null_table(void) {
  StudentStatistics stats;
  DBStatus status = calculate_statistics(NULL, &stats);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL table should return error");
}

void test_calculate_statistics_null_stats(void) {
  StudentTable *table = create_test_table_with_records("Test", 5);
  DBStatus status = calculate_statistics(table, NULL);
  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status,
                   "NULL stats should return error");
  table_free(table);
}

void test_calculate_statistics_empty_table(void) {
  StudentTable *table = table_init("Test");
  StudentStatistics stats;

  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_ERROR_INVALID_DATA, status,
                   "Empty table should return error");

  table_free(table);
}

void test_calculate_statistics_single_record(void) {
  StudentTable *table = table_init("Test");
  table_add_record(table, &(StudentRecord){1001, "Alice", "CS", 75.5f});

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Single record should succeed");
  ASSERT_EQUAL_FLOAT(75.5f, stats.average_mark, 0.01f,
                     "Average should equal the mark");
  ASSERT_EQUAL_FLOAT(75.5f, stats.highest_mark, 0.01f,
                     "Highest should equal the mark");
  ASSERT_EQUAL_FLOAT(75.5f, stats.lowest_mark, 0.01f,
                     "Lowest should equal the mark");
  ASSERT_EQUAL_STRING("Alice", stats.highest_student_name,
                      "Highest student should be Alice");
  ASSERT_EQUAL_STRING("Alice", stats.lowest_student_name,
                      "Lowest student should be Alice");

  table_free(table);
}

void test_calculate_statistics_all_same_marks(void) {
  StudentTable *table = table_init("Test");

  for (int i = 0; i < 5; i++) {
    char name[50];
    snprintf(name, sizeof(name), "Student%d", i + 1);
    table_add_record(table, &(StudentRecord){1000 + i, "", "CS", 75.0f});
    strncpy(table->records[i].name, name, sizeof(table->records[i].name) - 1);
  }

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "All same marks should succeed");
  ASSERT_EQUAL_FLOAT(75.0f, stats.average_mark, 0.01f,
                     "Average should be 75.0");
  ASSERT_EQUAL_FLOAT(75.0f, stats.highest_mark, 0.01f,
                     "Highest should be 75.0");
  ASSERT_EQUAL_FLOAT(75.0f, stats.lowest_mark, 0.01f, "Lowest should be 75.0");

  table_free(table);
}

void test_calculate_statistics_tie_highest(void) {
  StudentTable *table = table_init("Test");

  table_add_record(table, &(StudentRecord){1001, "Alice", "CS", 100.0f});
  table_add_record(table, &(StudentRecord){1002, "Bob", "SE", 100.0f});
  table_add_record(table, &(StudentRecord){1003, "Charlie", "DS", 90.0f});

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Tie should succeed");
  ASSERT_EQUAL_FLOAT(100.0f, stats.highest_mark, 0.01f,
                     "Highest should be 100.0");
  ASSERT_EQUAL_STRING("Alice", stats.highest_student_name,
                      "First occurrence should be selected");

  table_free(table);
}

void test_calculate_statistics_tie_lowest(void) {
  StudentTable *table = table_init("Test");

  table_add_record(table, &(StudentRecord){1001, "Alice", "CS", 90.0f});
  table_add_record(table, &(StudentRecord){1002, "Bob", "SE", 0.0f});
  table_add_record(table, &(StudentRecord){1003, "Charlie", "DS", 0.0f});

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Tie should succeed");
  ASSERT_EQUAL_FLOAT(0.0f, stats.lowest_mark, 0.01f, "Lowest should be 0.0");
  ASSERT_EQUAL_STRING("Bob", stats.lowest_student_name,
                      "First occurrence should be selected");

  table_free(table);
}

void test_calculate_statistics_boundary_marks(void) {
  StudentTable *table = table_init("Test");

  table_add_record(table, &(StudentRecord){1001, "Min", "CS", 0.0f});
  table_add_record(table, &(StudentRecord){1002, "Max", "SE", 100.0f});
  table_add_record(table, &(StudentRecord){1003, "Mid", "DS", 50.0f});

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Boundary marks should succeed");
  ASSERT_EQUAL_FLOAT(50.0f, stats.average_mark, 0.1f, "Average should be 50.0");
  ASSERT_EQUAL_FLOAT(100.0f, stats.highest_mark, 0.01f,
                     "Highest should be 100.0");
  ASSERT_EQUAL_FLOAT(0.0f, stats.lowest_mark, 0.01f, "Lowest should be 0.0");

  table_free(table);
}

void test_calculate_statistics_large_dataset(void) {
  StudentTable *table = create_test_table_with_records("Test", 100);

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Large dataset should succeed");
  ASSERT_TRUE(stats.average_mark >= 0.0f && stats.average_mark <= 100.0f,
              "Average should be in valid range");
  ASSERT_TRUE(stats.highest_mark >= stats.lowest_mark,
              "Highest should be >= lowest");

  table_free(table);
}

void test_calculate_statistics_floating_point_precision(void) {
  StudentTable *table = table_init("Test");

  table_add_record(table, &(StudentRecord){1001, "A", "CS", 33.333f});
  table_add_record(table, &(StudentRecord){1002, "B", "SE", 66.666f});
  table_add_record(table, &(StudentRecord){1003, "C", "DS", 99.999f});

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Floating point marks should succeed");
  ASSERT_EQUAL_FLOAT(66.666f, stats.average_mark, 0.1f,
                     "Average should be approximately 66.666");

  table_free(table);
}

void test_calculate_statistics_null_records_array(void) {
  StudentTable *table = table_init("Test");
  free(table->records);
  table->records = NULL;

  StudentStatistics stats;
  DBStatus status = calculate_statistics(table, &stats);

  ASSERT_EQUAL_INT(DB_ERROR_INVALID_DATA, status,
                   "NULL records array should return error");

  // manually free without calling table_free to avoid double free
  free(table);
}

// =============================================================================
// test suite runner
// =============================================================================

int main(void) {
  TEST_SUITE_START("Statistics Module Tests");

  RUN_TEST(test_calculate_statistics_normal);
  RUN_TEST(test_calculate_statistics_null_table);
  RUN_TEST(test_calculate_statistics_null_stats);
  RUN_TEST(test_calculate_statistics_empty_table);
  RUN_TEST(test_calculate_statistics_single_record);
  RUN_TEST(test_calculate_statistics_all_same_marks);
  RUN_TEST(test_calculate_statistics_tie_highest);
  RUN_TEST(test_calculate_statistics_tie_lowest);
  RUN_TEST(test_calculate_statistics_boundary_marks);
  RUN_TEST(test_calculate_statistics_large_dataset);
  RUN_TEST(test_calculate_statistics_floating_point_precision);
  RUN_TEST(test_calculate_statistics_null_records_array);

  TEST_SUITE_END();
}
