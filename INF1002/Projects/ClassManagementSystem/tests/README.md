# Test Suite Documentation

Comprehensive test suite for the Class Management System (CMS).

## Overview

The test suite provides extensive coverage of all core modules with ~220 test cases across 9 test files.

### Test Structure

```bash
tests/
├── test_utils.h           # Test framework utilities and assertions
├── test_utils.c           # Helper functions and test fixtures
├── test_parser.c          # Parser and validation tests (51 tests)
├── test_database.c        # Database CRUD and memory tests (53 tests)
├── test_sorting.c         # Sorting algorithm tests (14 tests)
├── test_statistics.c      # Statistics calculation tests (12 tests)
├── test_event_log.c       # Event logging tests (14 tests)
├── test_commands.c        # Command precondition tests (30 tests)
├── test_checksum.c        # CRC32 integrity checking tests (29 tests)
├── test_adv_query.c       # Advanced query pipeline tests (12 tests)
├── test_query.c           # Basic query search tests (4 tests)
└── fixtures/              # Test data files
    ├── test_valid.txt     # Well-formed database
    ├── test_invalid.txt   # Database with invalid records
    ├── test_empty.txt     # Empty database
    └── test_boundary.txt  # Boundary value test data
```

## Running Tests

### Compile All Tests

```bash
make tests
```

### Run All Tests

```bash
make test
```
```bash
$cmdSrc = Get-ChildItem src\commands\*.c; Get-ChildItem tests\test_*.c | Where-Object Name -ne 'test_utils.c' | ForEach-Object { gcc -std=c11 -Wall -Wextra -g $_.FullName tests/test_utils.c src/adv_query.c src/cms.c src/database.c src/parser.c src/sorting.c src/utils.c src/event_log.c src/checksum.c src/statistics.c src/ui.c @cmdSrc -Iinclude -o ("build/" + $_.BaseName + ".exe") }
```

### Run Individual Test

```bash
./build/test_parser
./build/test_database
./build/test_sorting
./build/test_statistics
./build/test_event_log
./build/test_commands
./build/test_checksum
./build/test_adv_query
./build/test_query
```

## Test Coverage

### Parser Module (`test_parser.c`) - 51 tests

**validate_record()** - 15 tests

- Valid record validation
- NULL pointer handling
- ID boundary values (2500000, 2600000, 2499999, 2600001)
- Mark boundary values (0.0, 100.0, -0.01, 100.01)
- Empty name/programme validation
- Long names and programmes

**parse_metadata()** - 8 tests

- Valid metadata parsing
- Missing colons
- Empty values
- NULL pointer handling
- Multiple colons in value

**parse_record_line()** - 9 tests

- Valid record line parsing
- Incomplete records (1-3 fields)
- NULL pointer handling
- Extra fields handling
- Newline stripping

**parse_column_headers()** - 6 tests

- Standard headers
- NULL pointer handling
- Empty lines
- Single column headers

**parse_file()** - 7 tests

- Valid file parsing
- Empty files
- Invalid records (skipping)
- Nonexistent files
- Boundary value files

### Database Module (`test_database.c`) - 53 tests

**table_init()** - 3 tests

- Valid initialisation
- Empty table name
- Long table name truncation

**table_free()** - 3 tests

- Empty table cleanup
- Table with records cleanup
- NULL pointer handling

**table_add_record()** - 9 tests

- Adding to empty table
- Multiple additions
- Capacity growth (10 → 20)
- NULL pointer handling
- Boundary ID and mark values

**table_remove_record()** - 9 tests

- Removing existing records
- Nonexistent IDs
- First, last, middle record removal
- Only record removal
- NULL pointer handling

**db_init()** - 1 test

- Valid database initialisation

**db_add_table()** - 5 tests

- Adding first table
- Multiple tables
- Capacity growth (2 → 4)
- NULL pointer handling

**db_load()** - 5 tests

- Valid file loading
- Nonexistent files
- Empty files
- NULL pointer handling

**db_save()** - 4 tests

- Valid saving
- Empty database
- NULL pointer handling

**db_update_record()** - 8 tests

- Update name, programme, mark individually
- Update all fields
- Nonexistent ID
- Invalid data
- NULL pointer handling

### Sorting Module (`test_sorting.c`) - 14 tests

- Empty/NULL array handling
- Single record (no-op)
- Sort by ID (ascending/descending)
- Sort by Mark (ascending/descending)
- Already sorted data
- Reverse sorted data
- Duplicate marks with tie-breaking
- All same values
- Boundary ID and mark values
- Large dataset (100 records)

### Statistics Module (`test_statistics.c`) - 12 tests

- Normal calculation (average, min, max)
- NULL pointer handling
- Empty table
- Single record
- All same marks
- Tie for highest/lowest (first occurrence)
- Boundary marks (0.0, 100.0)
- Large dataset
- Floating-point precision
- NULL records array

### Event Log Module (`test_event_log.c`) - 14 tests

**event_log_init()** - 1 test

- Valid initialisation

**event_log_free()** - 3 tests

- Empty log cleanup
- Log with events cleanup
- NULL pointer handling

**log_event()** - 10 tests

- First event logging
- Multiple events
- NULL log handling
- Fill to initial capacity (50)
- Capacity growth (50 → 100 → 200 → ... → 1000)
- Max capacity (1000)
- Circular buffer behaviour (overflow)
- Different operations
- Different statuses

### Commands Module (`test_commands.c`) - 30 tests

**Note:** Command tests focus on database preconditions and error handling. Full testing requires stdin mocking infrastructure.

- NULL database handling (all commands)
- Database not loaded errors (all commands)
- Empty table handling
- Success cases (where testable without input)
- `is_alphabetic()` validation (4 tests)
- Operation/status name mapping (2 tests)

### Checksum Module (`test_checksum.c`) - 29 tests

- CRC32 calculation accuracy
- Database checksum generation
- File checksum verification
- NULL pointer handling
- Empty database handling
- Checksum comparison and validation
- Different database content checksums
- File I/O error handling

### Advanced Query Module (`test_adv_query.c`) - 12 tests

**Pipeline-based filtering system with GREP and MARK filters**

- NULL database handling
- NULL pipeline handling
- Empty database errors
- Empty pipeline errors
- Unknown command parsing
- Duplicate filter detection (mark, name)
- Disallowed ID field validation
- Invalid mark operators
- Valid GREP operations (NAME, PROGRAMME)
- Valid MARK filters (>, <, =, >=, <=)
- Combined pipeline filters

### Query Module (`test_query.c`) - 4 tests

**Basic ID-based search functionality**

- NULL database pointer handling
- Empty/unloaded database handling
- Valid ID lookups (existing records)
- Nonexistent ID handling

## Test Framework

### Assertion Macros

```c
ASSERT_TRUE(condition, message)
ASSERT_FALSE(condition, message)
ASSERT_EQUAL_INT(expected, actual, message)
ASSERT_EQUAL_FLOAT(expected, actual, epsilon, message)
ASSERT_EQUAL_STRING(expected, actual, message)
ASSERT_NOT_NULL(pointer, message)
ASSERT_NULL(pointer, message)
```

### Test Suite Macros

```c
TEST_SUITE_START(suite_name)
RUN_TEST(test_function)
TEST_SUITE_END()
```

### Helper Functions

```c
// record creation
StudentRecord create_test_record(int id, const char *name,
                                  const char *programme, float mark);
StudentRecord create_invalid_id_record(int id);
StudentRecord create_invalid_mark_record(float mark);
StudentRecord create_empty_name_record(void);
StudentRecord create_empty_programme_record(void);

// database creation
StudentDatabase *create_empty_test_database(void);
StudentDatabase *create_test_database_with_records(int count);
void cleanup_test_database(StudentDatabase *db);

// table creation
StudentTable *create_empty_test_table(const char *name);
StudentTable *create_test_table_with_records(const char *name, int count);

// test file paths
const char *get_test_file_path(const char *filename);
```

## Writing New Tests

### Basic Test Structure

```c
void test_my_function(void) {
    // setup
    StudentTable *table = create_test_table_with_records("Test", 5);

    // execute
    int result = my_function(table);

    // assert
    ASSERT_EQUAL_INT(expected_value, result, "Result should match expected");

    // cleanup
    table_free(table);
}
```

### Adding to Test Suite

```c
int main(void) {
    TEST_SUITE_START("My Module Tests");

    RUN_TEST(test_my_function);

    TEST_SUITE_END();
}
```

## Test Output

### Successful Test

```
✓ Test description
```

### Failed Test

```
✗ Test description
    Expected: 5, Got: 3
    File: test_file.c, Line: 42
```

### Suite Summary

```
═══════════════════════════════════════════════════════════
Tests Run: 51 | Passed: 51 | Failed: 0
═══════════════════════════════════════════════════════════
```

## Limitations

### Command Testing

Full command testing requires:

- stdin mocking/redirection
- stdout capture
- Interactive input simulation

Current tests cover:

- Database precondition validation
- Error handling
- Success cases without user input

## Troubleshooting

### Test Compilation Fails

```bash
# Ensure all dependencies are compiled
make clean
make tests
```
