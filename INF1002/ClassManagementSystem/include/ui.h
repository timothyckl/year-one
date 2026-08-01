#ifndef UI_H
#define UI_H

/**
 * @file ui.h
 * @brief user interface module for displaying menus and messages
 *
 * provides functions for displaying the programme declaration, main menu,
 * and error messages to the user.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "cms.h"

/**
 * @brief displays the programme declaration from file
 * @return CMS_SUCCESS if successful, CMS_ERROR_FILE_OPEN if file cannot be opened
 */
CMSStatus ui_display_declaration(void);

/**
 * @brief displays the main menu options from file
 * @return CMS_SUCCESS if successful, CMS_ERROR_FILE_OPEN if file cannot be opened
 */
CMSStatus ui_display_menu(void);

/**
 * @brief displays an error message to the user
 * @param[in] error_msg the error message to display (without "CMS: " prefix or newline)
 */
void ui_display_error(const char *error_msg);

#endif // UI_H
