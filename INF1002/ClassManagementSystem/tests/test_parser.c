#include <math.h>
#include "../include/database.h"
#include "../include/parser.h"
#include "test_utils.h"

// =============================================================================
// validate_record() tests
// =============================================================================

void test_validate_record_valid(void) {
  StudentRecord record =
      create_test_record(2500100, "John Doe", "Computer Science", 75.5f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(VALID_RECORD, status, "Valid record should pass validation");
}

void test_validate_record_null(void) {
  ValidationStatus status = validate_record(NULL);
  ASSERT_EQUAL_INT(INVALID_FIELD_COUNT, status,
                   "NULL record should return INVALID_FIELD_COUNT");
}

void test_validate_record_id_boundary_zero(void) {
  StudentRecord record = create_test_record(2500000, "Test", "Programme", 75.0f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(VALID_RECORD, status, "ID = 2500000 should be valid (min boundary)");
}

void test_validate_record_id_boundary_max(void) {
  StudentRecord record =
      create_test_record(2600000, "Test", "Programme", 75.0f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(VALID_RECORD, status,
                   "ID = 2600000 should be valid (max boundary)");
}

void test_validate_record_id_negative(void) {
  StudentRecord record = create_invalid_id_record(-1);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(INVALID_ID_RANGE, status, "Negative ID should be invalid");
}

void test_validate_record_id_overflow(void) {
  StudentRecord record = create_invalid_id_record(2600001);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(INVALID_ID_RANGE, status,
                   "ID >= 2600001 should be invalid");
}

void test_validate_record_mark_boundary_zero(void) {
  StudentRecord record = create_test_record(2500100, "Test", "Programme", 0.0f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(VALID_RECORD, status,
                   "Mark = 0.0 should be valid (boundary)");
}

void test_validate_record_mark_boundary_max(void) {
  StudentRecord record = create_test_record(2500100, "Test", "Programme", 100.0f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(VALID_RECORD, status,
                   "Mark = 100.0 should be valid (max boundary)");
}

void test_validate_record_mark_negative(void) {
  StudentRecord record = create_invalid_mark_record(-0.01f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(INVALID_MARK_RANGE, status,
                   "Negative mark should be invalid");
}

void test_validate_record_mark_overflow(void) {
  StudentRecord record = create_invalid_mark_record(100.01f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(INVALID_MARK_RANGE, status,
                   "Mark > 100.0 should be invalid");
}

void test_validate_record_empty_name(void) {
  StudentRecord record = create_empty_name_record();
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(INVALID_EMPTY_NAME, status, "Empty name should be invalid");
}

void test_validate_record_empty_prog(void) {
  StudentRecord record = create_empty_prog_record();
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(INVALID_EMPTY_PROGRAMME, status,
                   "Empty programme should be invalid");
}

void test_validate_record_name_with_spaces(void) {
  StudentRecord record =
      create_test_record(2500100, "John Doe", "Computer Science", 75.0f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(VALID_RECORD, status, "Name with spaces should be valid");
}

void test_validate_record_long_name(void) {
  char long_name[50];
  memset(long_name, 'A', 49);
  long_name[49] = '\0';
  StudentRecord record =
      create_test_record(2500100, long_name, "Programme", 75.0f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(VALID_RECORD, status, "49-character name should be valid");
}

void test_validate_record_long_programme(void) {
  char long_prog[50];
  memset(long_prog, 'B', 49);
  long_prog[49] = '\0';
  StudentRecord record = create_test_record(2500100, "Test", long_prog, 75.0f);
  ValidationStatus status = validate_record(&record);
  ASSERT_EQUAL_INT(VALID_RECORD, status,
                   "49-character programme should be valid");
}

// =============================================================================
// parse_metadata() tests
// =============================================================================

void test_parse_metadata_valid(void) {
  char key[256], value[256];
  ParseStatus status =
      parse_metadata("Database Name: Test Database", key, value);
  ASSERT_EQUAL_INT(PARSE_SUCCESS, status,
                   "Valid metadata should parse successfully");
  ASSERT_EQUAL_STRING("Database Name", key, "Key should be 'Database Name'");
  ASSERT_EQUAL_STRING("Test Database", value,
                      "Value should be 'Test Database'");
}

void test_parse_metadata_with_spaces(void) {
  char key[256], value[256];
  ParseStatus status = parse_metadata("Authors: John Doe", key, value);
  ASSERT_EQUAL_INT(PARSE_SUCCESS, status, "Metadata with spaces should parse");
  ASSERT_EQUAL_STRING("John Doe", value,
                      "Value with spaces should be preserved");
}

void test_parse_metadata_no_colon(void) {
  char key[256], value[256];
  ParseStatus status = parse_metadata("Invalid Line Without Colon", key, value);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status,
                   "Line without colon should fail");
}

void test_parse_metadata_empty_value(void) {
  char key[256], value[256];
  ParseStatus status = parse_metadata("Key: ", key, value);
  ASSERT_EQUAL_INT(PARSE_ERROR_EMPTY, status, "Empty value should fail");
}

void test_parse_metadata_null_line(void) {
  char key[256], value[256];
  ParseStatus status = parse_metadata(NULL, key, value);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status, "NULL line should fail");
}

void test_parse_metadata_null_key_buffer(void) {
  char value[256];
  ParseStatus status = parse_metadata("Key: Value", NULL, value);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status, "NULL key buffer should fail");
}

void test_parse_metadata_null_value_buffer(void) {
  char key[256];
  ParseStatus status = parse_metadata("Key: Value", key, NULL);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status, "NULL value buffer should fail");
}

void test_parse_metadata_multiple_colons(void) {
  char key[256], value[256];
  ParseStatus status = parse_metadata("Key: Value: Extra", key, value);
  ASSERT_EQUAL_INT(PARSE_SUCCESS, status, "Multiple colons should parse");
  ASSERT_EQUAL_STRING("Value: Extra", value,
                      "Value should include extra colons");
}

// =============================================================================
// parse_record_line() tests
// =============================================================================

void test_parse_record_line_valid(void) {
  StudentRecord record;
  ParseStatus status =
      parse_record_line("2500100\tJohn Doe\tComputer Science\t75.50", &record);
  ASSERT_EQUAL_INT(PARSE_SUCCESS, status, "Valid record line should parse");
  ASSERT_EQUAL_INT(2500100, record.id, "ID should be 2500100");
  ASSERT_EQUAL_STRING("John Doe", record.name, "Name should match");
  ASSERT_EQUAL_STRING("Computer Science", record.prog,
                      "Programme should match");
  ASSERT_EQUAL_FLOAT(75.50f, record.mark, 0.01f, "Mark should be 75.50");
}

void test_parse_record_line_null_line(void) {
  StudentRecord record;
  ParseStatus status = parse_record_line(NULL, &record);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status, "NULL line should fail");
}

void test_parse_record_line_null_record(void) {
  ParseStatus status = parse_record_line("2500100\tName\tProg\t75.0", NULL);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status, "NULL record should fail");
}

void test_parse_record_line_empty(void) {
  StudentRecord record;
  ParseStatus status = parse_record_line("", &record);
  ASSERT_EQUAL_INT(PARSE_ERROR_EMPTY, status, "Empty line should fail");
}

void test_parse_record_line_incomplete_one_field(void) {
  StudentRecord record;
  ParseStatus status = parse_record_line("2500100", &record);
  ASSERT_EQUAL_INT(PARSE_ERROR_INCOMPLETE, status,
                   "Line with 1 field should fail");
}

void test_parse_record_line_incomplete_two_fields(void) {
  StudentRecord record;
  ParseStatus status = parse_record_line("2500100\tJohn", &record);
  ASSERT_EQUAL_INT(PARSE_ERROR_INCOMPLETE, status,
                   "Line with 2 fields should fail");
}

void test_parse_record_line_incomplete_three_fields(void) {
  StudentRecord record;
  ParseStatus status = parse_record_line("2500100\tJohn\tCS", &record);
  ASSERT_EQUAL_INT(PARSE_ERROR_INCOMPLETE, status,
                   "Line with 3 fields should fail");
}

void test_parse_record_line_with_newline(void) {
  StudentRecord record;
  ParseStatus status = parse_record_line("2500100\tJohn\tCS\t75.0\n", &record);
  ASSERT_EQUAL_INT(PARSE_SUCCESS, status, "Line with newline should parse");
  ASSERT_EQUAL_INT(2500100, record.id, "ID should parse correctly");
}

void test_parse_record_line_extra_fields(void) {
  StudentRecord record;
  ParseStatus status =
      parse_record_line("2500100\tJohn\tCS\t75.0\tExtra", &record);
  ASSERT_EQUAL_INT(PARSE_SUCCESS, status, "Extra fields should be ignored");
}

// =============================================================================
// parse_column_headers() tests
// =============================================================================

void test_parse_column_headers_standard(void) {
  char **headers = NULL;
  size_t count = 0;
  ParseStatus status =
      parse_column_headers("ID\tName\tProgramme\tMark", &headers, &count);
  ASSERT_EQUAL_INT(PARSE_SUCCESS, status, "Standard headers should parse");
  ASSERT_EQUAL_INT(4, count, "Should have 4 headers");
  if (headers) {
    ASSERT_EQUAL_STRING("ID", headers[0], "First header should be ID");
    ASSERT_EQUAL_STRING("Name", headers[1], "Second header should be Name");
    ASSERT_EQUAL_STRING("Programme", headers[2],
                        "Third header should be Programme");
    ASSERT_EQUAL_STRING("Mark", headers[3], "Fourth header should be Mark");
    // free headers
    for (size_t i = 0; i < count; i++)
      free(headers[i]);
    free(headers);
  }
}

void test_parse_column_headers_null_line(void) {
  char **headers = NULL;
  size_t count = 0;
  ParseStatus status = parse_column_headers(NULL, &headers, &count);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status, "NULL line should fail");
}

void test_parse_column_headers_null_headers(void) {
  size_t count = 0;
  ParseStatus status = parse_column_headers("ID\tName", NULL, &count);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status,
                   "NULL headers output should fail");
}

void test_parse_column_headers_null_count(void) {
  char **headers = NULL;
  ParseStatus status = parse_column_headers("ID\tName", &headers, NULL);
  ASSERT_EQUAL_INT(PARSE_ERROR_FORMAT, status, "NULL count output should fail");
}

void test_parse_column_headers_empty(void) {
  char **headers = NULL;
  size_t count = 0;
  ParseStatus status = parse_column_headers("", &headers, &count);
  ASSERT_EQUAL_INT(PARSE_ERROR_EMPTY, status, "Empty line should fail");
}

void test_parse_column_headers_single(void) {
  char **headers = NULL;
  size_t count = 0;
  ParseStatus status = parse_column_headers("ID", &headers, &count);
  ASSERT_EQUAL_INT(PARSE_SUCCESS, status, "Single column should parse");
  ASSERT_EQUAL_INT(1, count, "Should have 1 header");
  if (headers) {
    for (size_t i = 0; i < count; i++)
      free(headers[i]);
    free(headers);
  }
}

// =============================================================================
// parse_file() tests
// =============================================================================

void test_parse_file_valid(void) {
  StudentDatabase *db = create_empty_test_database();
  ParseStatistics stats = {0};

  DBStatus status =
      parse_file(get_test_file_path("test_valid.txt"), db, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Valid file should parse successfully");
  ASSERT_TRUE(db->table_count > 0, "Database should have at least one table");
  if (db->table_count > 0) {
    ASSERT_EQUAL_INT(5, db->tables[0]->record_count, "Should have 5 records");
    ASSERT_EQUAL_INT(5, stats.records_loaded,
                     "Statistics should show 5 records loaded");
  }

  cleanup_test_database(db);
}

void test_parse_file_empty(void) {
  StudentDatabase *db = create_empty_test_database();
  ParseStatistics stats = {0};

  DBStatus status =
      parse_file(get_test_file_path("test_empty.txt"), db, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Empty file should parse successfully");
  ASSERT_TRUE(db->table_count > 0, "Database should have at least one table");
  if (db->table_count > 0) {
    ASSERT_EQUAL_INT(0, db->tables[0]->record_count, "Should have 0 records");
  }

  cleanup_test_database(db);
}

void test_parse_file_invalid_records(void) {
  StudentDatabase *db = create_empty_test_database();
  ParseStatistics stats = {0};

  DBStatus status =
      parse_file(get_test_file_path("test_invalid.txt"), db, &stats);

  // should succeed but skip invalid records
  ASSERT_EQUAL_INT(DB_SUCCESS, status,
                   "File with invalid records should parse");
  ASSERT_TRUE(stats.validation_errors > 0, "Should have validation errors");
  ASSERT_TRUE(stats.records_skipped > 0, "Should have skipped records");

  cleanup_test_database(db);
}

void test_parse_file_nonexistent(void) {
  StudentDatabase *db = create_empty_test_database();
  ParseStatistics stats = {0};

  DBStatus status = parse_file("nonexistent_file_xyz.txt", db, &stats);

  ASSERT_EQUAL_INT(DB_ERROR_FILE_NOT_FOUND, status,
                   "Nonexistent file should fail");

  cleanup_test_database(db);
}

void test_parse_file_null_filename(void) {
  StudentDatabase *db = create_empty_test_database();
  ParseStatistics stats = {0};

  DBStatus status = parse_file(NULL, db, &stats);

  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status, "NULL filename should fail");

  cleanup_test_database(db);
}

void test_parse_file_null_database(void) {
  ParseStatistics stats = {0};

  DBStatus status =
      parse_file(get_test_file_path("test_valid.txt"), NULL, &stats);

  ASSERT_EQUAL_INT(DB_ERROR_NULL_POINTER, status, "NULL database should fail");
}

void test_parse_file_boundary_values(void) {
  StudentDatabase *db = create_empty_test_database();
  ParseStatistics stats = {0};

  DBStatus status =
      parse_file(get_test_file_path("test_boundary.txt"), db, &stats);

  ASSERT_EQUAL_INT(DB_SUCCESS, status, "Boundary values file should parse");
  if (db->table_count > 0 && db->tables[0]->record_count > 0) {
    ASSERT_EQUAL_INT(2500000, db->tables[0]->records[0].id,
                     "First record should have ID=2500000");
    ASSERT_EQUAL_INT(2600000, db->tables[0]->records[1].id,
                     "Second record should have ID=2600000");
    ASSERT_EQUAL_FLOAT(0.0f, db->tables[0]->records[0].mark, 0.01f,
                       "First record should have mark=0.0");
    ASSERT_EQUAL_FLOAT(100.0f, db->tables[0]->records[1].mark, 0.01f,
                       "Second record should have mark=100.0");
  }

  cleanup_test_database(db);
}

// =============================================================================
// test suite runner
// =============================================================================

int main(void) {
  TEST_SUITE_START("Parser Module Tests");

  // validate_record tests
  RUN_TEST(test_validate_record_valid);
  RUN_TEST(test_validate_record_null);
  RUN_TEST(test_validate_record_id_boundary_zero);
  RUN_TEST(test_validate_record_id_boundary_max);
  RUN_TEST(test_validate_record_id_negative);
  RUN_TEST(test_validate_record_id_overflow);
  RUN_TEST(test_validate_record_mark_boundary_zero);
  RUN_TEST(test_validate_record_mark_boundary_max);
  RUN_TEST(test_validate_record_mark_negative);
  RUN_TEST(test_validate_record_mark_overflow);
  RUN_TEST(test_validate_record_empty_name);
  RUN_TEST(test_validate_record_empty_prog);
  RUN_TEST(test_validate_record_name_with_spaces);
  RUN_TEST(test_validate_record_long_name);
  RUN_TEST(test_validate_record_long_programme);

  // parse_metadata tests
  RUN_TEST(test_parse_metadata_valid);
  RUN_TEST(test_parse_metadata_with_spaces);
  RUN_TEST(test_parse_metadata_no_colon);
  RUN_TEST(test_parse_metadata_empty_value);
  RUN_TEST(test_parse_metadata_null_line);
  RUN_TEST(test_parse_metadata_null_key_buffer);
  RUN_TEST(test_parse_metadata_null_value_buffer);
  RUN_TEST(test_parse_metadata_multiple_colons);

  // parse_record_line tests
  RUN_TEST(test_parse_record_line_valid);
  RUN_TEST(test_parse_record_line_null_line);
  RUN_TEST(test_parse_record_line_null_record);
  RUN_TEST(test_parse_record_line_empty);
  RUN_TEST(test_parse_record_line_incomplete_one_field);
  RUN_TEST(test_parse_record_line_incomplete_two_fields);
  RUN_TEST(test_parse_record_line_incomplete_three_fields);
  RUN_TEST(test_parse_record_line_with_newline);
  RUN_TEST(test_parse_record_line_extra_fields);

  // parse_column_headers tests
  RUN_TEST(test_parse_column_headers_standard);
  RUN_TEST(test_parse_column_headers_null_line);
  RUN_TEST(test_parse_column_headers_null_headers);
  RUN_TEST(test_parse_column_headers_null_count);
  RUN_TEST(test_parse_column_headers_empty);
  RUN_TEST(test_parse_column_headers_single);

  // parse_file tests
  RUN_TEST(test_parse_file_valid);
  RUN_TEST(test_parse_file_empty);
  RUN_TEST(test_parse_file_invalid_records);
  RUN_TEST(test_parse_file_nonexistent);
  RUN_TEST(test_parse_file_null_filename);
  RUN_TEST(test_parse_file_null_database);
  RUN_TEST(test_parse_file_boundary_values);

  TEST_SUITE_END();
}
