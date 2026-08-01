#ifndef CMS_H
#define CMS_H

/**
 * @file cms.h
 * @brief class management system (cms) main application module
 *
 * provides the main program structure including initialisation,
 * menu display, and main event loop for the interactive cms application.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

typedef enum {
  CMS_SUCCESS = 0,            // operation completed successfully
  CMS_ERROR_INIT,             // cms initialisation failed
  CMS_ERROR_DB_INIT,          // database initialisation failed
  CMS_ERROR_INVALID_ARGUMENT, // invalid argument provided
  CMS_ERROR_FILE_OPEN,        // failed to open file
  CMS_ERROR_FILE_IO           // file i/o operation failed
} CMSStatus;

/**
 * @brief initialises the cms application and database
 * @return CMS_SUCCESS on success, appropriate error code on failure
 */
CMSStatus cms_init(void);

/**
 * @brief displays the main menu options to the user
 * @return CMS_SUCCESS on success, appropriate error code on failure
 */
CMSStatus display_menu(void);

/**
 * @brief runs the cms session, processing user commands
 * @return CMS_SUCCESS on success, appropriate error code on failure
 */
CMSStatus run_cms_session(void);

/**
 * @brief converts cms status code to human-readable string
 * @param[in] status the cms status code to convert
 * @return pointer to static string describing the status
 */
const char *cms_status_string(CMSStatus status);

#endif // CMS_H
