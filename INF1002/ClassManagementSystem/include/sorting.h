#ifndef SORTING_H
#define SORTING_H

/**
 * @file sorting.h
 * @brief sorting module for student record ordering
 *
 * provides stable sorting of student records by ID or mark in ascending
 * or descending order using bubble sort algorithm.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "database.h"
#include <stddef.h>

// sort field options for student records
typedef enum {
  SORT_FIELD_ID,
  SORT_FIELD_MARK,
} SortField;

// sort order options
typedef enum {
  SORT_ORDER_ASC,
  SORT_ORDER_DESC,
} SortOrder;

/**
 * @brief sorts student records by specified field and order
 * @param[in,out] records array of student records to sort (modified in-place)
 * @param[in] count number of records in the array
 * @param[in] field field to sort by (ID or mark)
 * @param[in] order sort order (ascending or descending)
 * @note uses stable bubble sort algorithm
 */
void sort_records(StudentRecord *records, size_t count, SortField field,
                  SortOrder order);

#endif // SORTING_H
