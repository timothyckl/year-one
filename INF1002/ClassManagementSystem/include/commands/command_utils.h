#ifndef COMMAND_UTILS_H
#define COMMAND_UTILS_H

/**
 * @file command_utils.h
 * @brief utility functions for command implementations
 *
 * provides helper functions for command execution including user interaction,
 * error reporting, and input validation.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "commands/command.h"

#define STUDENT_RECORDS_TABLE_INDEX 0
#define DEFAULT_DATA_FILE "data/P1_8-CMS.txt"
#define DEFAULT_FILE_MSG "No input received. Using default data file (%s).\n"

/**
 * @brief waits for user to press enter
 */
void cmd_wait_for_user(void);

/**
 * @brief reports cms error message, waits for user input, and returns status
 * @param[in] error_msg error message to display (without "CMS: " prefix or newline)
 * @param[in] status operation status to return
 * @return the status parameter value
 */
OpStatus cmd_report_error(const char *error_msg, OpStatus status);

/**
 * @brief validates that a string contains only alphabetic characters and spaces
 * @param[in] str string to validate
 * @return 1 if valid, 0 if invalid
 */
int cmd_is_alphabetic(const char *str);

#endif // COMMAND_UTILS_H
