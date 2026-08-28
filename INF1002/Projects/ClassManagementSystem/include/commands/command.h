#ifndef COMMAND_H
#define COMMAND_H

/**
 * @file command.h
 * @brief command execution module for CMS operations
 *
 * defines operation types, status codes, and command execution functions
 * for all CMS operations including OPEN, INSERT, QUERY, UPDATE, DELETE, etc.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "database.h"

// operation types for cms operations
typedef enum {
  EXIT = 0,
  OPEN,
  SHOW_ALL,
  INSERT,
  QUERY,
  UPDATE,
  DELETE,
  SAVE,
  SORT,
  ADV_QUERY,
  STATISTICS,
  SHOW_LOG,
  CHECKSUM,
} Operation;

// operation status codes for internal cms operations
typedef enum {
  OP_SUCCESS = 0,         // operation completed successfully
  OP_ERROR_OPEN,          // failed to open file
  OP_ERROR_INPUT,         // input reading failed
  OP_ERROR_VALIDATION,    // validation failed
  OP_ERROR_DB_NOT_LOADED, // database not loaded
  OP_ERROR_GENERAL,       // general operation error
  OP_ERROR_INVALID,       // invalid operation
  OP_HELP_REQUESTED       // help menu requested (presentation layer)
} OpStatus;

// function pointer type for command execution
typedef OpStatus (*CommandFunc)(StudentDatabase *db);

/**
 * @brief executes the specified operation on the database
 * @param[in] op operation type to execute
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_operation(Operation op, StudentDatabase *db);

// individual command functions
/**
 * @brief executes OPEN operation to load database from file
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_open(StudentDatabase *db);

/**
 * @brief executes SHOW_ALL operation to display all records
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_show_all(StudentDatabase *db);

/**
 * @brief executes INSERT operation to add new record
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_insert(StudentDatabase *db);

/**
 * @brief executes QUERY operation to search for records
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_query(StudentDatabase *db);

/**
 * @brief executes UPDATE operation to modify existing record
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_update(StudentDatabase *db);

/**
 * @brief executes DELETE operation to remove record
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_delete(StudentDatabase *db);

/**
 * @brief executes SAVE operation to write database to file
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_save(StudentDatabase *db);

/**
 * @brief executes SORT operation to order records
 * @param[in,out] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_sort(StudentDatabase *db);

/**
 * @brief executes ADV_QUERY operation for advanced filtering
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_adv_query(StudentDatabase *db);

/**
 * @brief executes STATISTICS operation to compute summary stats
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_statistics(StudentDatabase *db);

/**
 * @brief executes SHOW_LOG operation to display event history
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_show_log(StudentDatabase *db);

/**
 * @brief executes CHECKSUM operation to verify data integrity
 * @param[in] db pointer to the database
 * @return OP_SUCCESS on success, appropriate error code on failure
 */
OpStatus execute_checksum(StudentDatabase *db);

#endif // COMMAND_H
