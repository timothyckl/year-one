#include "utils.h"
#include <sys/stat.h>

/**
 * @brief validates command-line arguments
 * @param[in] argc argument count from main()
 * @param[in] argv argument vector from main()
 * @return 0 if arguments are valid, non-zero otherwise
 */
int check_args(int argc, char *argv[]) {
  if (argc != 2) {
    fprintf(stderr, "Usage: %s <path-to-file>\n", argv[0]);
    return -1;
  }
  return 0;
}

/**
 * @brief opens a file and returns file handle
 * @param[in] file_path path to the file to open
 * @return file pointer on success, NULL on failure
 */
FILE *get_file_handle(const char *file_path) {
  FILE *file_handle = fopen(file_path, "r");

  if (file_handle == NULL) {
    fprintf(stderr, "Error opening file.\n");
    return NULL;
  }

  return file_handle;
}

/**
 * @brief checks if a directory exists
 * @param[in] dir_path path to the directory to check
 * @return CMS_SUCCESS if directory exists, CMS_ERROR_FILE_OPEN otherwise
 */
CMSStatus check_directory_exists(const char *dir_path) {
  struct stat path_stat;

  // check if path exists
  if (stat(dir_path, &path_stat) != 0) {
    return CMS_ERROR_FILE_OPEN; // path does not exist
  }

  // check if path is a directory
  if (!S_ISDIR(path_stat.st_mode)) {
    return CMS_ERROR_FILE_OPEN; // path exists but is not a directory
  }

  return CMS_SUCCESS; // directory exists
}

/**
 * @brief prints file contents line by line with optional line numbers
 * @param[in] handle file handle to read from
 * @param[in] buffer_size size of buffer for reading lines
 * @param[in] show_line_num whether to display line numbers
 */
void print_file_lines(FILE *handle, int buffer_size, bool show_line_num) {
  char buffer[buffer_size];
  int current_line = 0;

  // print content line by line
  while (fgets(buffer, buffer_size, handle) != NULL) {
    if (show_line_num) {
      current_line++;
      printf("Line %i: %s", current_line, buffer);
    } else {
      printf("%s", buffer);
    }
  }

  // close file after usage
  fclose(handle);
}
