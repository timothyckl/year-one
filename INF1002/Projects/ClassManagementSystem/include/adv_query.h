#ifndef ADV_QUERY_H
#define ADV_QUERY_H

/**
 * @file adv_query.h
 * @brief advanced query module for filter-based record searching
 *
 * provides a filter-based query system that allows chaining multiple
 * conditions. supports filtering by id, name, programme, and mark with various
 * operators. uses a pipeline syntax where filters are separated by '|'.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "database.h"

typedef enum {
  ADV_QUERY_SUCCESS = 0,            // operation completed successfully
  ADV_QUERY_ERROR_INVALID_ARGUMENT, // invalid argument provided
  ADV_QUERY_ERROR_EMPTY_DATABASE,   // database is empty
  ADV_QUERY_ERROR_PARSE,            // failed to parse query
  ADV_QUERY_ERROR_MEMORY            // memory allocation failed
} AdvQueryStatus;

/**
 * @brief executes a query pipeline and displays matching records
 * @param[in] db pointer to the database to query
 * @param[in] pipeline query string with filter conditions separated by '|'
 * @return ADV_QUERY_SUCCESS on success, appropriate error code on failure
 */
AdvQueryStatus adv_query_execute(StudentDatabase *db, const char *pipeline);

/**
 * @brief converts query status code to human-readable string
 * @param[in] status the query status code to convert
 * @return pointer to static string describing the status
 */
const char *adv_query_status_string(AdvQueryStatus status);

/**
 * @brief runs interactive query prompt with guided help
 * @param[in] db pointer to the database to query
 * @return ADV_QUERY_SUCCESS on success, appropriate error code on failure
 */
AdvQueryStatus adv_query_run_prompt(StudentDatabase *db);

#endif // ADV_QUERY_H 
