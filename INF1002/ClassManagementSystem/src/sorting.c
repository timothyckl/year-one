#include "sorting.h"
#include "database.h"
#include <stddef.h>

/*
 * sort student records using bubble sort algorithm
 * sorts in-place by modifying the records array
 * stable sort - maintains relative order of equal elements
 */
static void bubble_sort_records(StudentRecord *records, size_t count,
                                int (*compare)(const StudentRecord *,
                                               const StudentRecord *)) {
  // defensive check
  if (!records || count == 0 || !compare) {
    return;
  }

  // bubble sort with early termination optimisation
  for (size_t i = 0; i < count - 1; i++) {
    int swapped = 0;

    for (size_t j = 0; j < count - i - 1; j++) {
      // compare adjacent elements
      if (compare(&records[j], &records[j + 1]) > 0) {
        // swap records
        StudentRecord temp = records[j];
        records[j] = records[j + 1];
        records[j + 1] = temp;
        swapped = 1;
      }
    }

    // if no swaps occurred, array is sorted
    if (!swapped) {
      break;
    }
  }
}

/*
 * compare student records by id in ascending order
 * returns: negative if a < b, zero if a == b, positive if a > b
 */
static int compare_id_asc(const StudentRecord *a, const StudentRecord *b) {
  // defensive null checks
  if (!a || !b)
    return 0;

  // safe comparison - avoid integer overflow from subtraction
  if (a->id < b->id)
    return -1;
  if (a->id > b->id)
    return 1;
  return 0;
}

/*
 * compare student records by id in descending order
 * returns: negative if a > b, zero if a == b, positive if a < b
 */
static int compare_id_desc(const StudentRecord *a, const StudentRecord *b) {
  // defensive null checks
  if (!a || !b)
    return 0;

  // independent implementation - not just reversed parameter order
  if (a->id > b->id)
    return -1;
  if (a->id < b->id)
    return 1;
  return 0;
}

/*
 * compare student records by mark in ascending order
 * includes tie-breaker: sorts by id when marks are equal
 * returns: negative if a < b, zero if a == b, positive if a > b
 */
static int compare_mark_asc(const StudentRecord *a, const StudentRecord *b) {
  // defensive null checks
  if (!a || !b)
    return 0;

  // safe float comparison - validation ensures marks in [0.0, 100.0]
  if (a->mark < b->mark)
    return -1;
  if (a->mark > b->mark)
    return 1;

  // tie-breaker: sort by id when marks are equal (ensures stable sort)
  if (a->id < b->id)
    return -1;
  if (a->id > b->id)
    return 1;
  return 0;
}

/*
 * compare student records by mark in descending order
 * includes tie-breaker: sorts by id when marks are equal
 * returns: negative if a > b, zero if a == b, positive if a < b
 */
static int compare_mark_desc(const StudentRecord *a, const StudentRecord *b) {
  // defensive null checks
  if (!a || !b)
    return 0;

  // independent implementation with tie-breaker
  if (a->mark > b->mark)
    return -1;
  if (a->mark < b->mark)
    return 1;

  // tie-breaker: sort by id when marks are equal
  if (a->id < b->id)
    return -1;
  if (a->id > b->id)
    return 1;
  return 0;
}

/**
 * @brief sorts student records by specified field and order
 * @param[in,out] records array of student records to sort (modified in-place)
 * @param[in] count number of records in the array
 * @param[in] field field to sort by (ID or mark)
 * @param[in] order sort order (ascending or descending)
 * @note uses stable bubble sort algorithm
 */
void sort_records(StudentRecord *records, size_t count, SortField field,
                  SortOrder order) {
  // defensive checks
  if (!records || count == 0) {
    return;
  }

  // select appropriate comparator based on field and order
  int (*comparator)(const StudentRecord *, const StudentRecord *) = NULL;

  if (field == SORT_FIELD_ID && order == SORT_ORDER_ASC) {
    comparator = compare_id_asc;
  } else if (field == SORT_FIELD_ID && order == SORT_ORDER_DESC) {
    comparator = compare_id_desc;
  } else if (field == SORT_FIELD_MARK && order == SORT_ORDER_ASC) {
    comparator = compare_mark_asc;
  } else if (field == SORT_FIELD_MARK && order == SORT_ORDER_DESC) {
    comparator = compare_mark_desc;
  }

  // perform bubble sort using selected comparator
  if (comparator) {
    bubble_sort_records(records, count, comparator);
  }
}
