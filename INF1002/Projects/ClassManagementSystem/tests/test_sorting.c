#include <math.h>
#include "../include/database.h"
#include "../include/sorting.h"
#include "test_utils.h"

// =============================================================================
// sort_records() tests
// =============================================================================

void test_sort_records_empty_array(void) {
  sort_records(NULL, 0, SORT_FIELD_ID, SORT_ORDER_ASC);
  ASSERT_TRUE(true, "Sorting empty array should not crash");
}

void test_sort_records_null_array(void) {
  sort_records(NULL, 5, SORT_FIELD_ID, SORT_ORDER_ASC);
  ASSERT_TRUE(true, "Sorting NULL array should not crash");
}

void test_sort_records_single_record(void) {
  StudentTable *table = create_test_table_with_records("Test", 1);
  StudentRecord original = table->records[0];

  sort_records(table->records, table->record_count, SORT_FIELD_ID,
               SORT_ORDER_ASC);

  ASSERT_EQUAL_INT(original.id, table->records[0].id,
                   "Single record should remain unchanged");

  table_free(table);
}

void test_sort_records_by_id_ascending(void) {
  StudentTable *table = table_init("Test");

  // add records in random order
  int ids[] = {1005, 1001, 1003, 1002, 1004};
  for (int i = 0; i < 5; i++) {
    StudentRecord record =
        create_test_record(ids[i], "Test", "Programme", 75.0f);
    table_add_record(table, &record);
  }

  sort_records(table->records, table->record_count, SORT_FIELD_ID,
               SORT_ORDER_ASC);

  // verify ascending order
  for (size_t i = 1; i < table->record_count; i++) {
    ASSERT_TRUE(table->records[i - 1].id < table->records[i].id,
                "Records should be in ascending ID order");
  }

  table_free(table);
}

void test_sort_records_by_id_descending(void) {
  StudentTable *table = table_init("Test");

  // add records in random order
  int ids[] = {1005, 1001, 1003, 1002, 1004};
  for (int i = 0; i < 5; i++) {
    StudentRecord record =
        create_test_record(ids[i], "Test", "Programme", 75.0f);
    table_add_record(table, &record);
  }

  sort_records(table->records, table->record_count, SORT_FIELD_ID,
               SORT_ORDER_DESC);

  // verify descending order
  for (size_t i = 1; i < table->record_count; i++) {
    ASSERT_TRUE(table->records[i - 1].id > table->records[i].id,
                "Records should be in descending ID order");
  }

  table_free(table);
}

void test_sort_records_by_mark_ascending(void) {
  StudentTable *table = table_init("Test");

  // add records with different marks
  float marks[] = {85.5f, 60.0f, 92.3f, 70.5f, 88.0f};
  for (int i = 0; i < 5; i++) {
    StudentRecord record =
        create_test_record(1000 + i, "Test", "Programme", marks[i]);
    table_add_record(table, &record);
  }

  sort_records(table->records, table->record_count, SORT_FIELD_MARK,
               SORT_ORDER_ASC);

  // verify ascending order by mark
  for (size_t i = 1; i < table->record_count; i++) {
    ASSERT_TRUE(table->records[i - 1].mark <= table->records[i].mark,
                "Records should be in ascending mark order");
  }

  table_free(table);
}

void test_sort_records_by_mark_descending(void) {
  StudentTable *table = table_init("Test");

  // add records with different marks
  float marks[] = {85.5f, 60.0f, 92.3f, 70.5f, 88.0f};
  for (int i = 0; i < 5; i++) {
    StudentRecord record =
        create_test_record(1000 + i, "Test", "Programme", marks[i]);
    table_add_record(table, &record);
  }

  sort_records(table->records, table->record_count, SORT_FIELD_MARK,
               SORT_ORDER_DESC);

  // verify descending order by mark
  for (size_t i = 1; i < table->record_count; i++) {
    ASSERT_TRUE(table->records[i - 1].mark >= table->records[i].mark,
                "Records should be in descending mark order");
  }

  table_free(table);
}

void test_sort_records_already_sorted(void) {
  StudentTable *table = table_init("Test");

  // add records already in ascending order
  for (int i = 0; i < 5; i++) {
    StudentRecord record =
        create_test_record(1000 + i, "Test", "Programme", 50.0f + i);
    table_add_record(table, &record);
  }

  sort_records(table->records, table->record_count, SORT_FIELD_ID,
               SORT_ORDER_ASC);

  // verify still in ascending order
  for (size_t i = 1; i < table->record_count; i++) {
    ASSERT_TRUE(table->records[i - 1].id < table->records[i].id,
                "Already sorted records should remain sorted");
  }

  table_free(table);
}

void test_sort_records_reverse_sorted(void) {
  StudentTable *table = table_init("Test");

  // add records in descending order
  for (int i = 0; i < 5; i++) {
    StudentRecord record =
        create_test_record(1004 - i, "Test", "Programme", 75.0f);
    table_add_record(table, &record);
  }

  sort_records(table->records, table->record_count, SORT_FIELD_ID,
               SORT_ORDER_ASC);

  // verify now in ascending order
  for (size_t i = 1; i < table->record_count; i++) {
    ASSERT_TRUE(table->records[i - 1].id < table->records[i].id,
                "Reverse sorted records should be sorted");
  }

  table_free(table);
}

void test_sort_records_duplicate_marks_tiebreaker(void) {
  StudentTable *table = table_init("Test");

  // add records with duplicate marks but different ids
  table_add_record(table, &(StudentRecord){1005, "E", "P", 75.0f});
  table_add_record(table, &(StudentRecord){1002, "B", "P", 75.0f});
  table_add_record(table, &(StudentRecord){1004, "D", "P", 75.0f});
  table_add_record(table, &(StudentRecord){1001, "A", "P", 75.0f});
  table_add_record(table, &(StudentRecord){1003, "C", "P", 75.0f});

  sort_records(table->records, table->record_count, SORT_FIELD_MARK,
               SORT_ORDER_ASC);

  // verify tie-breaking by id (ascending)
  for (size_t i = 1; i < table->record_count; i++) {
    if (table->records[i - 1].mark == table->records[i].mark) {
      ASSERT_TRUE(table->records[i - 1].id < table->records[i].id,
                  "Duplicate marks should be tie-broken by ID");
    }
  }

  table_free(table);
}

void test_sort_records_all_same_values(void) {
  StudentTable *table = table_init("Test");

  // add records with all same marks and sequential ids
  for (int i = 0; i < 5; i++) {
    StudentRecord record =
        create_test_record(1000 + i, "Test", "Programme", 75.0f);
    table_add_record(table, &record);
  }

  sort_records(table->records, table->record_count, SORT_FIELD_MARK,
               SORT_ORDER_ASC);

  // verify order is maintained or sorted by id
  for (size_t i = 1; i < table->record_count; i++) {
    ASSERT_TRUE(table->records[i - 1].id <= table->records[i].id,
                "Same marks should maintain or sort by ID");
  }

  table_free(table);
}

void test_sort_records_boundary_ids(void) {
  StudentTable *table = table_init("Test");

  // add records with boundary ids
  table_add_record(table, &(StudentRecord){2600000, "Max", "P", 75.0f});
  table_add_record(table, &(StudentRecord){2500000, "Min", "P", 80.0f});
  table_add_record(table, &(StudentRecord){2550000, "Mid", "P", 70.0f});

  sort_records(table->records, table->record_count, SORT_FIELD_ID,
               SORT_ORDER_ASC);

  ASSERT_EQUAL_INT(2500000, table->records[0].id, "Minimum ID should be first");
  ASSERT_EQUAL_INT(2600000, table->records[2].id, "Maximum ID should be last");

  table_free(table);
}

void test_sort_records_boundary_marks(void) {
  StudentTable *table = table_init("Test");

  // add records with boundary marks
  table_add_record(table, &(StudentRecord){1001, "A", "P", 100.0f});
  table_add_record(table, &(StudentRecord){1002, "B", "P", 0.0f});
  table_add_record(table, &(StudentRecord){1003, "C", "P", 50.0f});

  sort_records(table->records, table->record_count, SORT_FIELD_MARK,
               SORT_ORDER_ASC);

  ASSERT_EQUAL_FLOAT(0.0f, table->records[0].mark, 0.01f,
                     "Minimum mark should be first");
  ASSERT_EQUAL_FLOAT(100.0f, table->records[2].mark, 0.01f,
                     "Maximum mark should be last");

  table_free(table);
}

void test_sort_records_large_dataset(void) {
  StudentTable *table = create_test_table_with_records("Test", 100);

  sort_records(table->records, table->record_count, SORT_FIELD_ID,
               SORT_ORDER_ASC);

  // verify sorted
  for (size_t i = 1; i < table->record_count; i++) {
    ASSERT_TRUE(table->records[i - 1].id <= table->records[i].id,
                "Large dataset should be sorted correctly");
  }

  table_free(table);
}

// =============================================================================
// test suite runner
// =============================================================================

int main(void) {
  TEST_SUITE_START("Sorting Module Tests");

  RUN_TEST(test_sort_records_empty_array);
  RUN_TEST(test_sort_records_null_array);
  RUN_TEST(test_sort_records_single_record);
  RUN_TEST(test_sort_records_by_id_ascending);
  RUN_TEST(test_sort_records_by_id_descending);
  RUN_TEST(test_sort_records_by_mark_ascending);
  RUN_TEST(test_sort_records_by_mark_descending);
  RUN_TEST(test_sort_records_already_sorted);
  RUN_TEST(test_sort_records_reverse_sorted);
  RUN_TEST(test_sort_records_duplicate_marks_tiebreaker);
  RUN_TEST(test_sort_records_all_same_values);
  RUN_TEST(test_sort_records_boundary_ids);
  RUN_TEST(test_sort_records_boundary_marks);
  RUN_TEST(test_sort_records_large_dataset);

  TEST_SUITE_END();
}
