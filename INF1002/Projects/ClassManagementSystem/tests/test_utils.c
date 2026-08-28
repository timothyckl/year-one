#include "test_utils.h"

// global test counters
int g_tests_run = 0;
int g_tests_passed = 0;
int g_tests_failed = 0;

// create a test record with given values
StudentRecord create_test_record(int id, const char *name, const char *prog,
                                 float mark) {
  StudentRecord record;
  record.id = id;
  strncpy(record.name, name, sizeof(record.name) - 1);
  record.name[sizeof(record.name) - 1] = '\0';
  strncpy(record.prog, prog, sizeof(record.prog) - 1);
  record.prog[sizeof(record.prog) - 1] = '\0';
  record.mark = mark;
  return record;
}

// create a record with invalid id
StudentRecord create_invalid_id_record(int id) {
  return create_test_record(id, "Test Name", "Test Programme", 75.0f);
}

// create a record with invalid mark
StudentRecord create_invalid_mark_record(float mark) {
  return create_test_record(2500100, "Test Name", "Test Programme", mark);
}

// create a record with empty name
StudentRecord create_empty_name_record(void) {
  return create_test_record(2500100, "", "Test Programme", 75.0f);
}

// create a record with empty prog
StudentRecord create_empty_prog_record(void) {
  return create_test_record(2500100, "Test Name", "", 75.0f);
}

// create an empty test database
StudentDatabase *create_empty_test_database(void) { return db_init(); }

// create a test database with n records
StudentDatabase *create_test_database_with_records(int count) {
  StudentDatabase *db = db_init();
  if (!db)
    return NULL;

  StudentTable *table = table_init("TestTable");
  if (!table) {
    db_free(db);
    return NULL;
  }

  // create test column headers
  char **headers = malloc(4 * sizeof(char *));
  if (headers) {
    headers[0] = strdup("ID");
    headers[1] = strdup("Name");
    headers[2] = strdup("Programme");
    headers[3] = strdup("Mark");
    table_set_column_headers(table, headers, 4);
  }

  // add test records
  for (int i = 0; i < count; i++) {
    char name[50];
    char prog[50];
    snprintf(name, sizeof(name), "Student%d", i + 1);
    snprintf(prog, sizeof(prog), "Programme%d", (i % 3) + 1);

    StudentRecord record =
        create_test_record(2500100 + i, name, prog, 50.0f + (i % 50));

    table_add_record(table, &record);
  }

  db_add_table(db, table);
  return db;
}

// cleanup test database
void cleanup_test_database(StudentDatabase *db) {
  if (db) {
    db_free(db);
  }
}

// create an empty test table
StudentTable *create_empty_test_table(const char *name) {
  return table_init(name);
}

// create a test table with n records
StudentTable *create_test_table_with_records(const char *name, int count) {
  StudentTable *table = table_init(name);
  if (!table)
    return NULL;

  // create test column headers
  char **headers = malloc(4 * sizeof(char *));
  if (headers) {
    headers[0] = strdup("ID");
    headers[1] = strdup("Name");
    headers[2] = strdup("Programme");
    headers[3] = strdup("Mark");
    table_set_column_headers(table, headers, 4);
  }

  // add test records
  for (int i = 0; i < count; i++) {
    char name_buf[50];
    char prog[50];
    snprintf(name_buf, sizeof(name_buf), "Student%d", i + 1);
    snprintf(prog, sizeof(prog), "Programme%d", (i % 3) + 1);

    StudentRecord record =
        create_test_record(2500100 + i, name_buf, prog, 50.0f + (i % 50));

    table_add_record(table, &record);
  }

  return table;
}

// get test file path
const char *get_test_file_path(const char *filename) {
  static char path[256];
  snprintf(path, sizeof(path), "%s%s", TEST_FIXTURES_DIR, filename);
  return path;
}
