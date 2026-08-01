#include "cms.h"
#include "commands/command.h"
#include "constants.h"
#include "database.h"
#include "ui.h"
#include "utils.h"
#include <ctype.h>
#include <errno.h>
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * @brief initialises the cms application and database
 * @return CMS_SUCCESS on success, appropriate error code on failure
 */
CMSStatus cms_init() {
  // validate required directories exist
  if (check_directory_exists("assets") != CMS_SUCCESS) {
    fprintf(stderr, "\nError: Required directory 'assets/' not found.\n");
    fprintf(stderr, "Ensure the executable (main or main.exe) is in the build/ "
                    "directory AND you are running it from the project root "
                    "directory (not from inside build/).\n");
    fprintf(stderr, "Example: ./build/main (Unix/macOS) or build\\main.exe "
                    "(Windows)\n\n");
    return CMS_ERROR_FILE_OPEN;
  }

  if (check_directory_exists("data") != CMS_SUCCESS) {
    fprintf(stderr, "\nError: Required directory 'data/' not found.\n");
    fprintf(stderr, "Ensure the executable (main or main.exe) is in the build/ "
                    "directory AND you are running it from the project root "
                    "directory (not from inside build/).\n");
    fprintf(stderr, "Example: ./build/main (Unix/macOS) or build\\main.exe "
                    "(Windows)\n\n");
    return CMS_ERROR_FILE_OPEN;
  }

  // display declaration
  CMSStatus status = ui_display_declaration();
  if (status != CMS_SUCCESS) {
    return status;
  }

  // wait for user and clear screen
  char buf[INPUT_BUFFER_SIZE];
  printf("\nPress Enter to continue...");
  (void)fgets(buf, sizeof buf, stdin);
  fflush(stdout);
  // NOTE: use "cls" for windows
  system("clear");

  return CMS_SUCCESS;
}

/**
 * @brief displays the main menu options to the user
 * @return CMS_SUCCESS on success, appropriate error code on failure
 */
CMSStatus display_menu(void) { return ui_display_menu(); }

// parses user command string and maps to operation enum
// returns OP_SUCCESS if valid command found (sets *op)
// returns OP_HELP_REQUESTED if help command detected
// returns OP_ERROR_INVALID if command not recognised
static OpStatus parse_command(const char *input, Operation *op) {
  if (!input) {
    return OP_ERROR_INVALID;
  }

  // create working copy and strip leading whitespace
  char cmd[INPUT_BUFFER_SIZE];
  const char *start = input;
  while (*start == ' ' || *start == '\t') {
    start++;
  }

  // copy to buffer
  size_t i = 0;
  while (*start && i < INPUT_BUFFER_SIZE - 1) {
    cmd[i++] = *start++;
  }
  cmd[i] = '\0';

  // strip trailing whitespace
  while (i > 0 && (cmd[i - 1] == ' ' || cmd[i - 1] == '\t')) {
    cmd[--i] = '\0';
  }

  // handle empty input
  if (i == 0) {
    return OP_ERROR_INVALID;
  }

  // convert to uppercase for case-insensitive matching
  for (size_t j = 0; j < i; j++) {
    cmd[j] = toupper((unsigned char)cmd[j]);
  }

  // map commands to operations (case-insensitive)
  if (strcmp(cmd, "OPEN") == 0) {
    *op = OPEN;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "SHOW ALL") == 0) {
    *op = SHOW_ALL;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "INSERT") == 0) {
    *op = INSERT;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "QUERY") == 0) {
    *op = QUERY;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "UPDATE") == 0) {
    *op = UPDATE;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "DELETE") == 0) {
    *op = DELETE;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "SAVE") == 0) {
    *op = SAVE;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "SORT") == 0) {
    *op = SORT;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "ADV QUERY") == 0) {
    *op = ADV_QUERY;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "STATISTICS") == 0) {
    *op = STATISTICS;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "SHOW LOG") == 0) {
    *op = SHOW_LOG;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "CHECKSUM") == 0) {
    *op = CHECKSUM;
    return OP_SUCCESS;
  }
  if (strcmp(cmd, "EXIT") == 0) {
    *op = EXIT;
    return OP_SUCCESS;
  }

  // special presentation layer command
  if (strcmp(cmd, "HELP") == 0) {
    return OP_HELP_REQUESTED;
  }

  // unrecognised command
  return OP_ERROR_INVALID;
}

static OpStatus get_user_input(char *buf, size_t buf_size, Operation *op) {
  printf("P1_8 > ");
  fflush(stdout);

  if (fgets(buf, buf_size, stdin) == NULL) {
    // handle EOF (ctrl+d) as exit command
    *op = EXIT;
    return OP_SUCCESS;
  }

  // strip trailing newline/carriage return
  size_t len = strcspn(buf, "\r\n");
  buf[len] = '\0';

  // handle empty input
  if (len == 0) {
    printf("CMS: Invalid input. Please enter a command.\n");
    return OP_ERROR_INVALID;
  }

  // parse command string to operation
  OpStatus status = parse_command(buf, op);

  if (status == OP_ERROR_INVALID) {
    printf("CMS: Unknown command. Type HELP for available commands.\n");
  }

  return status;
}

/**
 * @brief runs the cms session, processing user commands
 * @return CMS_SUCCESS on success, appropriate error code on failure
 */
CMSStatus run_cms_session(void) {
  CMSStatus status;

  status = cms_init();
  if (status != CMS_SUCCESS) {
    fprintf(stderr, "Failed to display menu: %s\n", cms_status_string(status));
    return status;
  }

  // init db
  StudentDatabase *db = db_init();
  if (!db) {
    fprintf(stderr, "Failed to initialise database\n");
    return CMS_ERROR_DB_INIT;
  }

  // display menu once at startup
  status = display_menu();
  if (status != CMS_SUCCESS) {
    fprintf(stderr, "Failed to display menu: %s\n", cms_status_string(status));
    db_free(db);
    return status;
  }

  char inp_buf[INPUT_BUFFER_SIZE];
  Operation op;
  OpStatus op_status;

  // main loop - continue until user successfully exits
  // (exit can be cancelled if there are unsaved changes)
  do {
    op_status = get_user_input(inp_buf, sizeof inp_buf, &op);

    // handle help request (presentation layer only)
    if (op_status == OP_HELP_REQUESTED) {
      display_menu();
      continue;
    }

    // handle other errors
    if (op_status != OP_SUCCESS) {
      continue;
    }

    op_status = execute_operation(op, db);
  } while (op != EXIT || op_status != OP_SUCCESS);

  db_free(db);
  return CMS_SUCCESS;
}

/**
 * @brief converts cms status code to human-readable string
 * @param[in] status the cms status code to convert
 * @return pointer to static string describing the status
 */
const char *cms_status_string(CMSStatus status) {
  switch (status) {
  case CMS_SUCCESS:
    return "operation succeeded";
  case CMS_ERROR_INIT:
    return "CMS initialisation failed";
  case CMS_ERROR_DB_INIT:
    return "database initialisation failed";
  case CMS_ERROR_INVALID_ARGUMENT:
    return "invalid argument provided";
  case CMS_ERROR_FILE_OPEN:
    return "failed to open file";
  case CMS_ERROR_FILE_IO:
    return "file I/O operation failed";
  default:
    return "unknown error";
  }
}
