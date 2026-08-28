#ifndef STATISTICS_H
#define STATISTICS_H

/**
 * @file statistics.h
 * @brief statistics module for computing summary data on student records
 *
 * calculates aggregate statistics including count, average mark, highest mark,
 * and lowest mark with associated student details.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "database.h"
#include <stddef.h>

// epsilon for floating-point comparisons
#define FLOAT_EPSILON 0.0001f

/**
 * structure to hold summary statistics for student records
 *
 * contains aggregated information including:
 * - total student count
 * - average mark
 * - highest mark with student details
 * - lowest mark with student details
 */
typedef struct {
  size_t total_count;            // total number of students
  float average_mark;            // mean of all marks
  float highest_mark;            // maximum mark value
  float lowest_mark;             // minimum mark value
  char highest_student_name[50]; // name of student with highest mark
  char lowest_student_name[50];  // name of student with lowest mark
  int highest_student_id;        // id of student with highest mark
  int lowest_student_id;         // id of student with lowest mark
} StudentStatistics;

/**
 * calculates summary statistics for all student records in a table
 *
 * computes total count, average mark, highest mark with student details,
 * and lowest mark with student details.
 *
 * tie-breaking policy: when multiple students share the same highest or
 * lowest mark, the first occurrence in the table is reported.
 *
 * @param table pointer to student table (must not be NULL)
 * @param stats pointer to statistics structure to populate (must not be NULL)
 * @return DB_SUCCESS on success
 *         DB_ERROR_NULL_POINTER if table or stats is NULL
 *         DB_ERROR_INVALID_DATA if table is empty or records array is NULL
 */
DBStatus calculate_statistics(StudentTable *table, StudentStatistics *stats);

#endif // STATISTICS_H
