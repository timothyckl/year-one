#include "commands/command_utils.h"
#include "constants.h"
#include "ui.h"
#include <ctype.h>
#include <stdio.h>

/**
 * @brief waits for user to press enter
 */
void cmd_wait_for_user(void) {
  char continue_buf[INPUT_BUFFER_SIZE];
  printf("\nPress Enter to continue...");
  (void)fgets(continue_buf, sizeof continue_buf, stdin);
  fflush(stdout);
}

/**
 * @brief reports cms error message, waits for user input, and returns status
 * @param[in] error_msg error message to display (without "CMS: " prefix or newline)
 * @param[in] status operation status to return
 * @return the status parameter value
 */
OpStatus cmd_report_error(const char *error_msg, OpStatus status) {
  ui_display_error(error_msg);
  cmd_wait_for_user();
  return status;
}

/**
 * @brief validates that a string contains only alphabetic characters and spaces
 * @param[in] str string to validate
 * @return 1 if valid, 0 if invalid
 */
int cmd_is_alphabetic(const char *str) {
  if (!str) {
    return 0;
  }

  // check each character is either alphabetic or a space
  for (const char *p = str; *p != '\0'; p++) {
    if (!isalpha((unsigned char)*p) && *p != ' ') {
      return 0;
    }
  }

  return 1;
}
