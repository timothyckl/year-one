#include "ui.h"
#include "constants.h"
#include "utils.h"
#include <stdio.h>

#define DECLARATION_FILE_PATH "assets/declaration.txt"
#define MENU_FILE_PATH "assets/menu.txt"

/**
 * @brief displays the programme declaration from file
 * @return CMS_SUCCESS if successful, CMS_ERROR_FILE_OPEN if file cannot be opened
 */
CMSStatus ui_display_declaration(void) {
  int buf_size = INPUT_BUFFER_SIZE;
  FILE *handle = get_file_handle(DECLARATION_FILE_PATH);

  if (handle == NULL) {
    return CMS_ERROR_FILE_OPEN;
  }

  print_file_lines(handle, buf_size, false);

  return CMS_SUCCESS;
}

/**
 * @brief displays the main menu options from file
 * @return CMS_SUCCESS if successful, CMS_ERROR_FILE_OPEN if file cannot be opened
 */
CMSStatus ui_display_menu(void) {
  int buf_size = INPUT_BUFFER_SIZE;
  FILE *handle = get_file_handle(MENU_FILE_PATH);

  if (handle == NULL) {
    return CMS_ERROR_FILE_OPEN;
  }

  print_file_lines(handle, buf_size, false);

  return CMS_SUCCESS;
}

/**
 * @brief displays an error message to the user
 * @param[in] error_msg the error message to display (without "CMS: " prefix or newline)
 */
void ui_display_error(const char *error_msg) { printf("CMS: %s\n", error_msg); }
