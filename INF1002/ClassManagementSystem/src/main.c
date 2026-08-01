#include "cms.h"
#include <stdlib.h>

/**
 * @brief main entry point for the cms application
 * @return EXIT_SUCCESS on successful completion, EXIT_FAILURE on error
 */
int main(void) {
  CMSStatus status = run_cms_session();
  return (status == CMS_SUCCESS) ? EXIT_SUCCESS : EXIT_FAILURE;
}
