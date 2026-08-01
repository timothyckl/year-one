#ifndef TEST_UTILS_H
#define TEST_UTILS_H

#include "../include/database.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// test result tracking
extern int g_tests_run;
extern int g_tests_passed;
extern int g_tests_failed;

// colour codes for terminal output
#define COLOR_GREEN "\033[0;32m"
#define COLOR_RED "\033[0;31m"
#define COLOR_YELLOW "\033[0;33m"
#define COLOR_BLUE "\033[0;34m"
#define COLOR_RESET "\033[0m"

// assertion macros
#define ASSERT_TRUE(condition, message)                                        \
  do {                                                                         \
    g_tests_run++;                                                             \
    if (condition) {                                                           \
      g_tests_passed++;                                                        \
      printf(COLOR_GREEN "  ✓ " COLOR_RESET "%s\n", message);                  \
    } else {                                                                   \
      g_tests_failed++;                                                        \
      printf(COLOR_RED "  ✗ " COLOR_RESET "%s\n", message);                    \
      printf("    File: %s, Line: %d\n", __FILE__, __LINE__);                  \
    }                                                                          \
  } while (0)

#define ASSERT_FALSE(condition, message) ASSERT_TRUE(!(condition), message)

#define ASSERT_EQUAL_INT(expected, actual, message)                            \
  do {                                                                         \
    g_tests_run++;                                                             \
    if ((expected) == (actual)) {                                              \
      g_tests_passed++;                                                        \
      printf(COLOR_GREEN "  ✓ " COLOR_RESET "%s\n", message);                  \
    } else {                                                                   \
      g_tests_failed++;                                                        \
      printf(COLOR_RED "  ✗ " COLOR_RESET "%s\n", message);                    \
      printf("    Expected: %d, Got: %d\n", (expected), (actual));             \
      printf("    File: %s, Line: %d\n", __FILE__, __LINE__);                  \
    }                                                                          \
  } while (0)

#define ASSERT_EQUAL_FLOAT(expected, actual, epsilon, message)                 \
  do {                                                                         \
    g_tests_run++;                                                             \
    if (fabs((expected) - (actual)) < (epsilon)) {                             \
      g_tests_passed++;                                                        \
      printf(COLOR_GREEN "  ✓ " COLOR_RESET "%s\n", message);                  \
    } else {                                                                   \
      g_tests_failed++;                                                        \
      printf(COLOR_RED "  ✗ " COLOR_RESET "%s\n", message);                    \
      printf("    Expected: %.2f, Got: %.2f\n", (expected), (actual));         \
      printf("    File: %s, Line: %d\n", __FILE__, __LINE__);                  \
    }                                                                          \
  } while (0)

#define ASSERT_EQUAL_STRING(expected, actual, message)                         \
  do {                                                                         \
    g_tests_run++;                                                             \
    if (strcmp((expected), (actual)) == 0) {                                   \
      g_tests_passed++;                                                        \
      printf(COLOR_GREEN "  ✓ " COLOR_RESET "%s\n", message);                  \
    } else {                                                                   \
      g_tests_failed++;                                                        \
      printf(COLOR_RED "  ✗ " COLOR_RESET "%s\n", message);                    \
      printf("    Expected: \"%s\", Got: \"%s\"\n", (expected), (actual));     \
      printf("    File: %s, Line: %d\n", __FILE__, __LINE__);                  \
    }                                                                          \
  } while (0)

#define ASSERT_NOT_NULL(pointer, message)                                      \
  do {                                                                         \
    g_tests_run++;                                                             \
    if ((pointer) != NULL) {                                                   \
      g_tests_passed++;                                                        \
      printf(COLOR_GREEN "  ✓ " COLOR_RESET "%s\n", message);                  \
    } else {                                                                   \
      g_tests_failed++;                                                        \
      printf(COLOR_RED "  ✗ " COLOR_RESET "%s\n", message);                    \
      printf("    Pointer was NULL\n");                                        \
      printf("    File: %s, Line: %d\n", __FILE__, __LINE__);                  \
    }                                                                          \
  } while (0)

#define ASSERT_NULL(pointer, message)                                          \
  do {                                                                         \
    g_tests_run++;                                                             \
    if ((pointer) == NULL) {                                                   \
      g_tests_passed++;                                                        \
      printf(COLOR_GREEN "  ✓ " COLOR_RESET "%s\n", message);                  \
    } else {                                                                   \
      g_tests_failed++;                                                        \
      printf(COLOR_RED "  ✗ " COLOR_RESET "%s\n", message);                    \
      printf("    Pointer was not NULL\n");                                    \
      printf("    File: %s, Line: %d\n", __FILE__, __LINE__);                  \
    }                                                                          \
  } while (0)

// test suite macros
#define RUN_TEST(test_func)                                                    \
  do {                                                                         \
    printf(COLOR_BLUE "\n▶ Running: " COLOR_RESET "%s\n", #test_func);         \
    test_func();                                                               \
  } while (0)

#define TEST_SUITE_START(suite_name)                                           \
  do {                                                                         \
    printf(COLOR_YELLOW "\n╔═════════════════════════════════════════════════" \
                        "══════════╗\n" COLOR_RESET);                          \
    printf(COLOR_YELLOW "║  Test Suite: %-45s║\n" COLOR_RESET, suite_name);    \
    printf(COLOR_YELLOW "╚═══════════════════════════════════════════════════" \
                        "════════╝\n" COLOR_RESET);                            \
    g_tests_run = 0;                                                           \
    g_tests_passed = 0;                                                        \
    g_tests_failed = 0;                                                        \
  } while (0)

#define TEST_SUITE_END()                                                       \
  do {                                                                         \
    printf(COLOR_YELLOW "\n══════════════════════════════════════════════════" \
                        "═════════\n" COLOR_RESET);                            \
    printf("Tests Run: %d | ", g_tests_run);                                   \
    printf(COLOR_GREEN "Passed: %d" COLOR_RESET " | ", g_tests_passed);        \
    printf(COLOR_RED "Failed: %d\n" COLOR_RESET, g_tests_failed);              \
    printf(COLOR_YELLOW "════════════════════════════════════════════════════" \
                        "═══════\n\n" COLOR_RESET);                            \
    return (g_tests_failed == 0) ? 0 : 1;                                      \
  } while (0)

// helper functions for creating test data
StudentRecord create_test_record(int id, const char *name, const char *prog,
                                 float mark);
StudentRecord create_invalid_id_record(int id);
StudentRecord create_invalid_mark_record(float mark);
StudentRecord create_empty_name_record(void);
StudentRecord create_empty_prog_record(void);

// helper functions for database setup/teardown
StudentDatabase *create_empty_test_database(void);
StudentDatabase *create_test_database_with_records(int count);
void cleanup_test_database(StudentDatabase *db);

// helper functions for table setup
StudentTable *create_empty_test_table(const char *name);
StudentTable *create_test_table_with_records(const char *name, int count);

// test file path helpers
#define TEST_FIXTURES_DIR "tests/fixtures/"
const char *get_test_file_path(const char *filename);

#endif // TEST_UTILS_H
