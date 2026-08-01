# INF1002-P1-08-CMS

**A Terminal-Based Class Management System for Student Records**

A comprehensive command-line application for managing student records with robust data integrity features, advanced querying capabilities, and detailed operation logging. The system provides 13 commands organised into database operations, record management, analysis tools, and system utilities.

## Key Features

- **Complete CRUD Operations** - Create, read, update, and delete student records
- **Data Integrity** - CRC32 checksums for change detection and file verification
- **Advanced Querying** - Multi-criteria filter pipeline for complex searches
- **Operation Logging** - Automatic session history tracking with timestamps
- **Statistical Analysis** - Real-time calculations (count, average, highest, lowest)
- **Flexible Sorting** - Sort by ID or mark in ascending or descending order
- **Robust Validation** - Multi-layer input validation and error handling
- **Dynamic Memory** - Efficient memory management with automatic capacity growth

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Command Reference](#command-reference)
   - [Getting Started](#getting-started)
   - [Daily Operations](#daily-operations)
   - [Data Management](#data-management)
   - [Advanced Features](#advanced-features)
   - [System Tools](#system-tools)
3. [Database File Format](#database-file-format)
4. [Architecture & Technical Details](#architecture--technical-details)
5. [Testing](#testing)
6. [Project Structure](#project-structure)

---

## Quick Start

### Prerequisites

- A C compiler (gcc or clang)
- `make` installed (optional but recommended)
- Unix-like environment (Linux, macOS) or Windows with MinGW/Cygwin

### Build

To compile the programme:
```bash
make
```

Or manually:
```bash
mkdir -p build
gcc -Iinclude -Wall -Wextra -g src/*.c src/commands/*.c -o build/main
```

### Building for Multiple Platforms

The project supports cross-compilation for macOS and Windows:

**Build for macOS (native):**
```bash
make build-macos
```
Creates `build/main` executable for macOS.

**Build for Windows (cross-compile):**
```bash
make build-windows
```
Creates `build/main.exe` executable for Windows. Requires MinGW cross-compiler (`x86_64-w64-mingw32-gcc`).

**Build for both platforms:**
```bash
make build-all
```

**Manual cross-compilation for Windows:**
```bash
mkdir -p build
x86_64-w64-mingw32-gcc -Iinclude -Wall -Wextra -g src/*.c src/commands/*.c -o build/main.exe
```

**Installing MinGW on macOS:**
```bash
brew install mingw-w64
```

### Run

```bash
./build/main
```

### Your First Session

Here's a complete workflow to get started:

1. **Start the programme:**
   ```bash
   ./build/main
   ```
   Press ENTER at the declaration screen to continue.

2. **Open the default database:**
   ```
   P1_8 > OPEN
   Enter file path (or press ENTER for default): [Press ENTER]
   ```
   You'll see: `CMS: Database loaded successfully from 'data/P1_8-CMS.txt'`

3. **View all records:**
   ```
   P1_8 > SHOW ALL
   ```
   A formatted table displays all student records.

4. **Search for a specific student:**
   ```
   P1_8 > QUERY
   Enter Student ID to search: 2501234
   ```

5. **Add a new student:**
   ```
   P1_8 > INSERT
   Enter Student ID: 2505000
   Enter Name: Alice Wong
   Enter Programme: Computer Science
   Enter Mark: 88.5
   ```

6. **Save your changes:**
   ```
   P1_8 > SAVE
   ```

7. **Exit the programme:**
   ```
   P1_8 > EXIT
   ```

---

## Command Reference

Commands are organised by typical workflow and usage patterns.

### Getting Started

#### OPEN

**Purpose:** Load a database file from disk into memory

**Syntax:** `OPEN`

**Requirements:** None (can be run at any time)

**Interactive Workflow:**

1. **Unsaved Changes Check:**
   - If a database is already loaded and the user exits with unsaved changes, you'll see:
     ```
     Warning: You have unsaved changes!
     ```

2. **File Path Prompt:**
   ```
   Enter file path (or press ENTER for default):
   ```
   - Press ENTER for default: `data/P1_8-CMS.txt`
   - Or enter a custom path (relative or absolute)

**Output:**

Success:
```
CMS: Database loaded successfully from 'data/P1_8-CMS.txt'
Total records: 10 loaded, 0 skipped
```

With errors:
```
CMS: Database loaded with errors from 'data/P1_8-CMS.txt'
Total: 10 attempted, 8 loaded, 2 skipped
Skipped: 1 validation error(s), 1 parse error(s)
```

**Error Scenarios:**
- File not found: `CMS: Failed to read database.`
- Invalid format: Records skipped with error breakdown
- Memory allocation failure: System error message

**Example:**
```
P1_8 > OPEN
Enter a file path (press ENTER for default data file): abc.txt
CMS: Error - Cannot open file 'abc.txt'
CMS: Failed to load database: file not found

Press ENTER to continue...
```

---

#### HELP

**Purpose:** Display command reference menu

**Syntax:** `HELP`

**Requirements:** None

**Output:** Displays the contents of `assets/menu.txt` showing all available commands organised by category:
- Database Operations
- Record Management
- Analysis & Tools
- System Commands

**Example:**
```
P1_8 > HELP
==============================================================
                 Class Management System
                    Command Reference
==============================================================
[Command categories and descriptions displayed]
```

---

#### SHOW ALL

**Purpose:** Display all student records in a formatted table

**Syntax:** `SHOW ALL`

**Requirements:** Database must be loaded

**Features:**
- Dynamic column width calculation
- Left-aligned text (ID, Name, Programme)
- Right-aligned numbers (Mark)
- Mark formatted to 2 decimal places
- Proper spacing and visual alignment

**Output Example:**
```
Table Name: StudentRecords

ID       Name              Programme              Mark
2501234  Joshua Chen       Software Engineering   70.50
2504567  John Levoy        Digital Supply Chain   85.90
2500619  Hasif             CS                     67.00
2501234  Isaac Teo         Computer Science       63.40
```

**Error Scenarios:**
- Database not loaded: `CMS: Database not loaded.`

---

### Daily Operations

#### INSERT

**Purpose:** Add a new student record to the database

**Syntax:** `INSERT`

**Requirements:** Database must be loaded

**Interactive Workflow:**

1. **Student ID:**
   ```
   Enter Student ID:
   ```
   - Must be exactly 7 digits
   - Range: 2500000 to 2600000
   - Cannot already exist in database

2. **Name:**
   ```
   Enter Name:
   ```
   - Maximum 49 characters
   - Alphabetic characters and spaces only
   - Cannot be empty

3. **Programme:**
   ```
   Enter Programme:
   ```
   - Maximum 49 characters
   - Alphabetic characters and spaces only
   - Cannot be empty

4. **Mark:**
   ```
   Enter Mark:
   ```
   - Float value
   - Typically range: 0.0 to 100.0

**Validation:**
- Duplicate ID checked before prompting for other fields
- Each field validated as entered
- Full record validation after all fields collected
- Dynamic array growth if capacity reached

**Output:**

Success:
```
CMS: Record with ID 2505000 successfully inserted.
```

Errors:
```
CMS: Invalid input. Student ID must be exactly 7 digits between 2500000 and 2600000.
CMS: Invalid input. Record with ID 2505000 already exists.
CMS: Invalid input. Name must be non-empty and contain only alphabetic characters.
CMS: Failed to insert record.
```

**Example:**
```
P1_8 > INSERT
Enter Student ID: 2505000
Enter Name: Alice Wong
Enter Programme: Computer Science
Enter Mark: 88.5
CMS: Record with ID 2505000 successfully inserted.

Press ENTER to continue...
```

---

#### QUERY

**Purpose:** Search for a single student record by ID

**Syntax:** `QUERY`

**Requirements:** Database must be loaded

**Interactive Workflow:**
```
Enter Student ID to search:
```

**Algorithm:** Linear search through all records (O(n) complexity)

**Output:**

Success (record found):
```
CMS: Record with ID 2501234 found in table StudentRecords

ID       Name              Programme              Mark
2501234  Joshua Chen       Software Engineering   70.50
```

Not found:
```
CMS: Record does not exist.
```

**Error Scenarios:**
- Database not loaded: `CMS: Database not loaded.`
- Invalid ID format: `CMS: Invalid input.`

**Example:**
```
P1_8 > QUERY
Enter Student ID to search: 2501234
CMS: Record with ID 2501234 found in table StudentRecords

ID       Name              Programme              Mark
2501234  Joshua Chen       Software Engineering   70.50

Press ENTER to continue...
```

---

#### UPDATE

**Purpose:** Modify a field in an existing student record

**Syntax:** `UPDATE`

**Requirements:** Database must be loaded

**Interactive Workflow:**

1. **Student ID:**
   ```
   Enter Student ID to update:
   ```

2. **Field Selection:**
   ```
   Select field to update:
     [1] Name
     [2] Programme
     [3] Mark
   Enter your choice:
   ```

3. **New Value:**
   Based on selected field:
   - `[1]` Name: `Enter new name:` (same validation as INSERT)
   - `[2]` Programme: `Enter new programme:` (same validation as INSERT)
   - `[3]` Mark: `Enter new mark:` (float validation)

**Features:**
- Only updates the selected field
- Other fields remain unchanged
- Full validation of new value
- Maintains data integrity

**Output:**

Success:
```
CMS: Record with ID 2501234 successfully updated.
```

Errors:
```
CMS: Record does not exist.
CMS: Invalid input. [Field-specific error message]
CMS: Failed to update record.
```

**Example:**
```
P1_8 > UPDATE
Enter Student ID to update: 2501234
Select field to update:
  [1] Name
  [2] Programme
  [3] Mark
Enter your choice: 3
Enter new mark: 75.0
CMS: Record with ID 2501234 successfully updated.

Press ENTER to continue...
```

---

#### DELETE

**Purpose:** Remove a student record from the database

**Syntax:** `DELETE`

**Requirements:** Database must be loaded

**Interactive Workflow:**

1. **Student ID:**
   ```
   Enter Student ID to delete:
   ```

2. **Confirmation:**
   ```
   Are you sure you want to delete record with ID 2501234? (Y/N):
   ```
   - Enter `Y` or `y` to confirm deletion
   - Enter `N`, `n`, or any other input to cancel

**Safety Features:**
- Mandatory confirmation before deletion
- Can cancel at confirmation stage
- Uses `memmove()` for safe array shifting
- Zeroes out deleted memory slot
- Irreversible operation (no undo)

**Output:**

Success:
```
CMS: Record with ID 2501234 successfully deleted.
```

Cancelled:
```
CMS: Deletion cancelled.
```

Errors:
```
CMS: Record does not exist.
CMS: Failed to delete record.
```

**Example:**
```
P1_8 > DELETE
Enter Student ID to delete: 2501234
Are you sure you want to delete record with ID 2501234? (Y/N): Y
CMS: Record with ID 2501234 successfully deleted.

Press ENTER to continue...
```

---

### Data Management

#### SAVE

**Purpose:** Write in-memory database to disk

**Syntax:** `SAVE`

**Requirements:** Database must be loaded

**Behaviour:**
- Saves to the file path used in the most recent OPEN command
- Creates file if it doesn't exist
- Overwrites existing file
- Updates internal checksum for change detection
- Preserves database metadata (name, authors, table name)

**File Format:**
- Tab-separated values
- Metadata header (Database Name, Authors)
- Table header (Table Name, Column names)
- Data rows (one per record)

**Output:**

Success:
```
CMS: Database saved successfully to 'data/P1_8-CMS.txt'
```

Errors:
```
CMS: Database not loaded.
CMS: Failed to save database.
```

**Example:**
```
P1_8 > SAVE
CMS: Database saved successfully to 'data/P1_8-CMS.txt'

Press ENTER to continue...
```

**Note:** Always SAVE before EXIT if you want to keep your changes!

---

#### CHECKSUM

**Purpose:** Verify data integrity and detect unsaved changes

**Syntax:** `CHECKSUM`

**Requirements:** Database must be loaded

**Features:**
- **In-memory checksum:** CRC32 of current database state
- **File checksum:** CRC32 of file on disk
- **Last saved checksum:** Baseline from last SAVE/OPEN operation
- **File metadata:** Size (bytes), last modified timestamp
- **Match status:** MATCH (consistent) or MISMATCH (unsaved changes)

**Algorithm:** CRC32 with 256-entry lookup table

**Output Example:**

No changes:
```
==============================================================
                  Database Integrity Check
==============================================================
In-memory checksum: 0x1A2B3C4D
File checksum:      0x1A2B3C4D (data/P1_8-CMS.txt)
Last saved checksum: 0x1A2B3C4D

Status: MATCH - Database is consistent with last save

Records: 4
File size: 512 bytes
Last modified: 2025-11-24 16:30:00
==============================================================
```

With unsaved changes:
```
==============================================================
                  Database Integrity Check
==============================================================
In-memory checksum: 0x9E8F7A6B
File checksum:      0x1A2B3C4D (data/P1_8-CMS.txt)
Last saved checksum: 0x1A2B3C4D

Status: MISMATCH - Unsaved changes detected!

Records: 5
File size: 512 bytes
Last modified: 2025-11-24 16:30:00
==============================================================
```

**Use Cases:**
- Verify data hasn't been corrupted
- Check if you have unsaved changes
- Confirm successful save operations
- Compare in-memory state to file state

---

### Advanced Features

#### SORT

**Purpose:** Sort records in-place by ID or mark

**Syntax:** `SORT`

**Requirements:** Database must be loaded with at least one record

**Interactive Workflow:**

1. **Field Selection:**
   ```
   Select field to sort by:
     [1] ID
     [2] Mark
   Enter your choice (or press ENTER to cancel):
   ```
   - Press ENTER to cancel

2. **Order Selection:**
   ```
   Select sort order:
     [A] Ascending
     [D] Descending
   Enter your choice (or press ENTER to cancel):
   ```
   - Enter `A` or `a` for ascending
   - Enter `D` or `d` for descending
   - Press ENTER to cancel

**Algorithm:** Bubble sort (stable sort)

**Features:**
- In-place modification (modifies database directly)
- Can cancel at either prompt stage
- Stable sort preserves relative order for equal values
- Handles single record gracefully (no-op)

**Output:**

Success:
```
CMS: 10 records successfully sorted by Mark in descending order.
```

Cancelled:
```
CMS: Sort operation cancelled.
```

Errors:
```
CMS: Database not loaded.
CMS: No records available for sorting.
CMS: Invalid input.
```

**Example:**
```
P1_8 > SORT
Select field to sort by:
  [1] ID
  [2] Mark
Enter your choice (or press ENTER to cancel): 2
Select sort order:
  [A] Ascending
  [D] Descending
Enter your choice (or press ENTER to cancel): D
CMS: 4 records successfully sorted by Mark in descending order.

Press ENTER to continue...
```

**Before Sort (by ID):**
```
ID       Name              Mark
2500619  Hasif             67.00
2501234  Joshua Chen       70.50
2501234  Isaac Teo         63.40
2504567  John Levoy        85.90
```

**After Sort (by Mark, Descending):**
```
ID       Name              Mark
2504567  John Levoy        85.90
2501234  Joshua Chen       70.50
2500619  Hasif             67.00
2501234  Isaac Teo         63.40
```

---

#### ADV QUERY

**Purpose:** Execute advanced multi-criteria searches using a filter pipeline

**Syntax:** `ADV QUERY`

**Requirements:** Database must be loaded

**Filter Pipeline Syntax:**
```
GREP <field> = "<pattern>" | MARK <op> <value>
```

**Supported Filters:**

1. **GREP (Text Search):**
   - **Fields:** `NAME`, `PROGRAMME`
   - **Matching:** Case-insensitive substring search
   - **Syntax:** `GREP NAME = "John"` or `GREP PROGRAMME = "Computer"`
   - **Pattern:** Quotes optional (will be normalised)

2. **MARK (Numeric Comparison):**
   - **Operators:** `<` (less than), `>` (greater than), `=` (equals)
   - **Syntax:** `MARK > 70` or `MARK = 85.5`
   - **Type:** Floating-point comparison

**Interactive Guided Mode:**

The system provides a user-friendly guided interface:

1. **Field Selection:**
   ```
   Pick a field to filter:
    1) Name
    2) Programme
    3) Mark
    0) Cancel
   Select option:
   ```

2. **Multiple Fields:**
   ```
   Add another field? (Y/N):
   ```

3. **Pattern Entry:**
   - For Name/Programme: `Enter <field> to search:`
   - For Mark: Operator selection menu, then value entry

**Pipeline Rules:**
- Multiple filters separated by `|` (pipe)
- All filters must match (AND logic)
- Each field can only appear once
- ID field not supported in filters

**Output:**

Success with results:
```
ID       Name              Programme          Mark
2501234  Joshua Chen       Computer Science   70.50
2501267  Alice Wong        Computer Science   88.00
Total: 2 record(s)
```

No matches:
```
Total: 0 record(s)
No records matched your query.
```

**Interactive Example:**
```
P1_8 > ADV QUERY

Pick a field to filter:
 1) Name
 2) Programme
 3) Mark
 0) Cancel
Select option: 2
Add another field? (Y/N): Y

Pick a field to filter:
 1) Name
 2) Programme
 3) Mark
 0) Cancel
Select option: 3

Enter Programme to search: Computer
Mark comparison
 1) Greater than
 2) Less than
 3) Equal to
Select option: 1
Enter mark value: 70

ID       Name              Programme          Mark
2501234  Joshua Chen       Computer Science   70.50
2501267  Alice Wong        Computer Science   88.00
Total: 2 record(s)

Press ENTER to continue...
```

**Advanced Pipeline Examples:**

Find all students named "John":
```
GREP NAME = "John"
```

Find Computer Science students with mark > 70:
```
GREP PROGRAMME = "Computer" | MARK > 70
```

Find high-achieving students named Alice:
```
MARK >= 80 | GREP NAME = "Alice"
```

**Error Scenarios:**
- Database not loaded: `CMS: Database not loaded.`
- Invalid pipeline syntax: `CMS: Invalid query syntax.`
- Duplicate field: `CMS: Each field can only be used once.`

---

#### STATISTICS

**Purpose:** Calculate and display summary statistics for all students

**Syntax:** `STATISTICS`

**Requirements:** Database must be loaded with at least one record

**Calculations:**

1. **Total Count:** Number of students in database
2. **Average Mark:** Mean of all marks (2 decimal places)
3. **Highest Mark:** Maximum mark with student ID and name
4. **Lowest Mark:** Minimum mark with student ID and name

**Features:**
- Floating-point precision calculations
- Tie-breaking: First occurrence used for highest/lowest
- Formatted output with alignment

**Output Example:**
```
Table Name: StudentRecords

Total students: 4
Average mark: 71.70
Highest mark: 85.90 (ID: 2504567, Name: John Levoy)
Lowest mark: 63.40 (ID: 2501234, Name: Isaac Teo)
```

**Error Scenarios:**
- Database not loaded: `CMS: Database not loaded.`
- No records: `CMS: No records available.`

**Example:**
```
P1_8 > STATISTICS
Table Name: StudentRecords

Total students: 4
Average mark: 71.70
Highest mark: 85.90 (ID: 2504567, Name: John Levoy)
Lowest mark: 63.40 (ID: 2501234, Name: Isaac Teo)

Press ENTER to continue...
```

**Calculation Details:**
- Average: Sum of all marks ÷ count
- Comparison: Uses floating-point epsilon (0.0001f) for equality checks
- Tie handling: If multiple students share highest/lowest mark, first occurrence displayed

---

### System Tools

#### SHOW LOG

**Purpose:** Display operation history for the current session

**Syntax:** `SHOW LOG`

**Requirements:** None (works independently of database state)

**Features:**
- Timestamps for all logged operations (ISO 8601 format: `YYYY-MM-DD HH:MM:SS`)
- Operation names and status codes (SUCCESS/FAILURE)
- Circular buffer (maximum 1000 entries)
- Session-scoped (cleared when programme exits)
- Shows complete history or most recent 1000 if exceeded

**Logged Operations:**
- OPEN - Database file loading
- INSERT - New record creation
- QUERY - Record searches
- UPDATE - Record modifications
- DELETE - Record deletions
- SAVE - Database file writes
- SORT - Record sorting
- ADV_QUERY - Advanced queries

**Not Logged (View-Only Operations):**
- SHOW ALL
- STATISTICS
- SHOW LOG
- CHECKSUM
- EXIT
- HELP

**Output Example:**
```
==============================================================
          Operation History for Current Session
Database File: data/P1_8-CMS.txt
==============================================================
Timestamp            Operation    Status
2025-11-24 14:30:15  OPEN         SUCCESS
2025-11-24 14:31:22  INSERT       SUCCESS
2025-11-24 14:32:10  QUERY        SUCCESS
2025-11-24 14:33:45  UPDATE       SUCCESS
2025-11-24 14:35:00  DELETE       SUCCESS
2025-11-24 14:36:20  SAVE         SUCCESS
2025-11-24 14:37:15  SORT         SUCCESS
2025-11-24 14:38:30  ADV_QUERY    SUCCESS
==============================================================
```

**Use Cases:**
- Review what operations you've performed
- Verify successful operations
- Track when changes were made
- Debug issues with timing information

---

#### EXIT

**Purpose:** Exit the programme gracefully

**Syntax:** `EXIT`

**Requirements:** None

**Safety Features:**

**Unsaved Changes Detection:**
- Automatically compares current database checksum to last saved checksum
- If changes detected, displays warning menu:

```
Warning: You have unsaved changes!
What would you like to do?
  [1] Save and exit
  [2] Discard and exit
  [3] Cancel (return to menu)
Enter your choice:
```

**Behaviour:**
- **Choice [1]:** Saves database then exits
- **Choice [2]:** Exits without saving (changes lost)
- **Choice [3]:** Cancels exit, returns to main menu
- **Invalid choice:** Cancels exit

**No Changes Detected:**
- Exits immediately with goodbye message

**Output:**

Clean exit:
```
Goodbye!
```

With save:
```
CMS: Database saved successfully to 'data/P1_8-CMS.txt'
Goodbye!
```

Cancelled:
```
[Returns to command prompt]
```

**Example (with unsaved changes):**
```
P1_8 > EXIT
Warning: You have unsaved changes!
What would you like to do?
  [1] Save and exit
  [2] Discard and exit
  [3] Cancel (return to menu)
Enter your choice: 1
CMS: Database saved successfully to 'data/P1_8-CMS.txt'
Goodbye!
```

**Important:** Always save your work before exiting to prevent data loss!

---

## Database File Format

The system uses a **tab-separated text file** format for database storage. This format is strictly enforced and must be followed exactly.

### File Structure

```
Database Name: <database name>
Authors: <author1>, <author2>, ...

Table Name: <table name>
ID	Name	Programme	Mark
<id>	<name>	<programme>	<mark>
<id>	<name>	<programme>	<mark>
...
```

### Example Database File

From `data/P1_8-CMS.txt`:

```
Database Name: Sample-CMS
Authors: Assistant Prof Oran Zane Devilly

Table Name: StudentRecords
ID	Name	Programme	Mark
2501234	Joshua Chen	Software Engineering	70.50
2501234	Isaac Teo	Computer Science	63.40
2504567	John Levoy	Digital Supply Chain	85.90
2500619	Hasif	CS	67.00
```

### Field Specifications

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| **ID** | Integer | 2500000 - 2600000 (7 digits) | Unique student identifier |
| **Name** | String | Max 49 chars, alphabetic + spaces | Student's full name |
| **Programme** | String | Max 49 chars, alphabetic + spaces | Programme of study |
| **Mark** | Float | 0.0 - 100.0 | Student's mark/grade |

### Data Validation Rules

#### Student ID Validation
- Must be exactly 7 digits
- Range: 2500000 to 2600000 (inclusive)
- Must be unique within database
- Validated using `strtol()` with overflow detection
- No leading zeros beyond required digits

#### Name Validation
- Cannot be empty or whitespace-only
- Maximum 49 characters (50 bytes with null terminator)
- Alphabetic characters only (A-Z, a-z)
- Spaces allowed between words
- No numbers, punctuation, or special characters
- Leading/trailing whitespace trimmed during parsing

#### Programme Validation
- Cannot be empty or whitespace-only
- Maximum 49 characters (50 bytes with null terminator)
- Alphabetic characters only (A-Z, a-z)
- Spaces allowed between words
- No numbers, punctuation, or special characters
- Leading/trailing whitespace trimmed during parsing

#### Mark Validation
- Must be valid floating-point number
- Typical range: 0.0 to 100.0
- Supports one or two decimal places
- Validated using `strtof()` with conversion verification
- Displayed with 2 decimal places in output

### File Format Details

**Separator:** Tab character (`\t`)
- **NOT** comma-separated (CSV)
- **NOT** space-separated
- Must be tab character between fields

**Metadata Header:**
```
Database Name: <name>
Authors: <author1>, <author2>, ...
```
- Required at file start
- One blank line after metadata
- Authors can be comma-separated list

**Table Header:**
```
Table Name: <name>
ID	Name	Programme	Mark
```
- Column names tab-separated
- Must match exactly (case-sensitive)

**Data Rows:**
- One row per student record
- Tab-separated fields
- No quotes around text fields
- Empty lines ignored during parsing

### Creating Custom Database Files

To create your own database file:

1. Start with metadata header
2. Add blank line
3. Add table header with column names
4. Add data rows (tab-separated)
5. Save as `.txt` file

**Template:**
```
Database Name: Your-Database-Name
Authors: Your Name

Table Name: StudentRecords
ID	Name	Programme	Mark
2501000	Student Name	Programme Name	85.50
```

**Important:**
- Use actual tab characters (not spaces)
- Follow validation rules strictly
- Invalid records will be skipped during OPEN
- System displays parse/validation error counts

---

## Architecture & Technical Details

This section provides insight into the system's design for markers evaluating code quality and architecture.

### Module Organisation

#### Core Modules

**cms.c / cms.h**
- Main application entry point
- Command prompt loop
- Input parsing and dispatch
- User interface coordination
- Links all modules together

**database.c / database.h**
- Table and record management
- Dynamic array implementation
- Memory allocation and growth
- File I/O operations (read/write)
- CRUD operation implementations
- Record validation coordination

**parser.c / parser.h**
- Database file parsing
- CSV/TSV tokenisation
- Field validation
- Error tracking and reporting
- Whitespace handling
- Type conversion (string to int/float)

**ui.c / ui.h**
- Menu display
- Message formatting
- Screen clearing
- Wait for user prompts
- Table rendering with dynamic widths
- Status message formatting

#### Command Modules (`src/commands/`)

**operation_registry.c**
- Command dispatcher
- Operation enum to function pointer mapping
- Event logging integration
- Status code propagation
- Command precondition checking

**command_utils.c**
- Shared utilities for command implementations
- Input validation helpers
- Error reporting helpers
- Common prompt functions
- Alphabetic validation
- Confirmation prompts

**Individual Command Files:**
- `open_command.c` - Database loading
- `show_all_command.c` - Table display
- `insert_command.c` - Record creation
- `query_command.c` - Basic search
- `update_command.c` - Record modification
- `delete_command.c` - Record deletion
- `save_command.c` - Database persistence
- `sort_command.c` - Record sorting
- `adv_query_command.c` - Advanced queries
- `statistics_command.c` - Statistical calculations
- `event_log_command.c` - Operation history
- `checksum_command.c` - Integrity checking

Each command file contains:
- Command implementation function
- Input prompts and validation
- Business logic
- Error handling
- User feedback

#### Feature Modules

**sorting.c / sorting.h**
- Bubble sort algorithm (stable)
- Comparison functions for ID and Mark
- In-place array sorting
- Order control (ascending/descending)

**statistics.c / statistics.h**
- Aggregate calculations
- Average, count, min, max
- Floating-point precision handling
- Tie-breaking for extrema

**adv_query.c / adv_query.h**
- Filter pipeline parser
- GREP and MARK filter implementations
- Query execution engine
- Interactive guided mode
- Result collection and display

**checksum.c / checksum.h**
- CRC32 algorithm with lookup table
- Database state checksumming
- File content checksumming
- Change detection logic

**event_log.c / event_log.h**
- Circular buffer implementation
- Dynamic capacity growth
- Timestamp capture (ISO 8601)
- Operation and status logging
- Display formatting

**utils.c / utils.h**
- General helper functions
- String manipulation utilities
- Input handling helpers

### Memory Management Strategy

#### Dynamic Arrays

**Tables (Database Level):**
- Initial capacity: 2 tables
- Growth strategy: Doubles capacity when full
- Allocation: `malloc()` for initial, `realloc()` for growth
- Storage: Array of `Table*` pointers

**Records (Table Level):**
- Initial capacity: 10 records
- Growth strategy: Doubles capacity when full
- Allocation: `malloc()` for initial, `realloc()` for growth
- Storage: Array of `Record` structures

**Event Log:**
- Initial capacity: 50 entries
- Growth strategy: Doubles capacity when full
- Maximum capacity: 1000 entries (circular overflow)
- Allocation: `malloc()` for initial, `realloc()` for growth
- Circular behaviour: Overwrites oldest when exceeding max

#### Allocation Strategy

**Robust Error Handling:**
```c
new_array = realloc(old_array, new_capacity * sizeof(Type));
if (new_array == NULL) {
    // handle allocation failure
    // preserve old_array (not freed yet)
    return error_status;
}
old_array = new_array;
```

**Key Principles:**
- Check all allocation results
- Preserve old pointer if realloc fails
- Use appropriate status codes
- Free resources on error paths

#### Deallocation

**Proper Cleanup Functions:**
- `table_free(Table* table)` - Frees record array and table structure
- `db_free(Database* db)` - Frees all tables and database structure
- `event_log_free()` - Frees event log entries and array
- Called on: EXIT, OPEN (reload), and errors

**Memory Safety:**
- NULL pointer checks before operations
- Explicit null termination for strings
- `memmove()` for safe array element removal
- Zeroing of deleted memory slots

#### No Memory Leaks

The system has been tested extensively for memory leaks:
- All allocated memory properly freed
- No orphaned allocations
- Proper cleanup on all exit paths
- Tested with valgrind (no leaks detected)

### Error Handling Architecture

#### Multi-Layer Validation

**1. Input Layer (Commands):**
- Empty input detection
- String length validation
- Numeric format validation (strtol/strtof)
- Range validation
- Character type validation (alphabetic check)

**2. Business Logic (Database):**
- Duplicate ID detection
- Record existence verification
- State consistency checks
- Operation precondition validation

**3. Schema Layer (Parser):**
- Field format validation
- Constraint enforcement
- Type conversion verification
- Boundary value checking

#### Status Code System

**OpStatus (Operation Level):**
```c
typedef enum {
    OP_SUCCESS,
    OP_FAILURE
} OpStatus;
```

**DBStatus (Database Operations):**
```c
typedef enum {
    DB_SUCCESS,
    DB_FAILURE,
    DB_NOT_LOADED,
    DB_EMPTY,
    DB_DUPLICATE,
    DB_NOT_FOUND,
    // ... additional status codes
} DBStatus;
```

**ValidationStatus (Schema):**
```c
typedef enum {
    VALID,
    INVALID_ID,
    INVALID_NAME,
    INVALID_PROGRAMME,
    INVALID_MARK,
    // ... additional validation codes
} ValidationStatus;
```

**Status Code Propagation:**
- Commands call database functions
- Database functions return status codes
- Commands interpret status and display messages
- Operation registry logs status
- User sees descriptive error message

#### Error Reporting

**Consistent Message Format:**
```c
CMS: <descriptive error message>
```

**User-Friendly Messages:**
- Specific about what went wrong
- Suggest corrective action when possible
- No technical jargon in user-facing messages
- Wait for acknowledgement (Press ENTER)

**Error Categories:**
- Input errors (validation failures)
- State errors (database not loaded)
- Resource errors (file not found, memory allocation)
- Logic errors (duplicate ID, record not found)

### Key Algorithms

#### CRC32 Checksum

**Implementation:**
- 32-bit cyclic redundancy check
- 256-entry lookup table (precomputed)
- Polynomial: 0xEDB88320 (IEEE 802.3)
- Processes data byte-by-byte
- Initial value: 0xFFFFFFFF
- Final XOR: 0xFFFFFFFF

**Usage:**
- Database state integrity
- File content verification
- Change detection
- Unsaved changes warning

**Performance:** O(n) where n is data size

#### Bubble Sort

**Implementation:**
- Stable sorting algorithm
- In-place (no extra array allocation)
- Comparison-based
- Adaptive (early termination if sorted)

**Complexity:**
- Best case: O(n) - already sorted
- Average case: O(n²)
- Worst case: O(n²)
- Space: O(1) - in-place

**Comparison Functions:**
- ID: Integer comparison
- Mark: Float comparison with ID tie-breaker

**Choice Rationale:**
- Simplicity and correctness
- Stability preserves relative order
- Adequate for small to medium datasets
- In-place (minimal memory overhead)

### Design Decisions

#### Circular Event Log Buffer

**Rationale:**
- Bounded memory usage (max 1000 entries)
- Automatic old entry eviction
- No manual cleanup required
- Session-scoped (lifetime = programme execution)

**Implementation:**
- Head pointer for write position
- Size counter (capped at capacity)
- Wraparound when reaching capacity
- Display sorted chronologically

#### Dynamic Array Growth

**Strategy:** Double capacity when full

**Advantages:**
- Amortised O(1) insertion
- Reduces reallocation frequency
- Minimises memory copying
- Standard practice in dynamic collections

**Trade-offs:**
- Potential over-allocation
- Memory temporarily doubled during realloc
- Acceptable for typical dataset sizes

#### Tab-Separated Format

**Rationale:**
- Simple parsing (no quote handling)
- Human-readable and editable
- Cross-platform compatible
- No escape sequence complexity
- Direct field extraction via tokenisation

**Trade-offs:**
- No tab characters allowed in data
- Less flexible than CSV
- Manual editing requires care
- Acceptable given controlled field constraints

#### Multi-Layer Validation

**Rationale:**
- Defence in depth
- Catch errors early (fail fast)
- Clear error attribution
- Maintainable (separation of concerns)

**Layers:**
1. Input syntax (parser)
2. Schema constraints (validator)
3. Business rules (database)

#### Command Dispatch Pattern

**Implementation:** Operation registry with function pointers

**Advantages:**
- Extensibility (easy to add commands)
- Separation of concerns
- Centralised logging
- Uniform precondition checking

**Structure:**
```c
typedef OpStatus (*CommandFunction)(Database*);

// Registry maps operation enum to function
CommandFunction get_command_handler(Operation op);
```

---

## Testing

Comprehensive test suite ensuring system reliability and correctness.

### Running Tests

**All tests:**
```bash
make test-all
```

**Run specific test executable:**
```bash
./build/test_parser
./build/test_database
# ... etc
```

### Test Coverage

**Total:** ~220 test cases across 9 test modules

| Module | Tests | Focus Areas |
|--------|-------|-------------|
| **test_parser.c** | 51 | Field validation, file parsing, error handling |
| **test_database.c** | 53 | CRUD operations, memory management, edge cases |
| **test_sorting.c** | 14 | Sort algorithms, order correctness, stability |
| **test_statistics.c** | 12 | Calculations, edge cases, floating-point precision |
| **test_event_log.c** | 14 | Logging, circular buffer, capacity growth |
| **test_commands.c** | 30 | Preconditions, error handling, state validation |
| **test_checksum.c** | 29 | CRC32 correctness, change detection, file I/O |
| **test_adv_query.c** | 12 | Pipeline parsing, filter execution, result accuracy |
| **test_query.c** | 4 | Basic search, linear search correctness |

### Test Framework

**Custom assertion macros:**
```c
ASSERT_TRUE(condition, message)
ASSERT_FALSE(condition, message)
ASSERT_EQUAL(expected, actual, message)
ASSERT_NOT_EQUAL(value1, value2, message)
ASSERT_NULL(pointer, message)
ASSERT_NOT_NULL(pointer, message)
```

**Test utilities:**
- Record creation helpers
- Database setup/teardown
- Fixture file loading
- Comparison helpers

### Test Fixtures

Located in `tests/fixtures/`:

- **test_valid.txt** - Well-formed database file
- **test_invalid.txt** - Records with validation errors
- **test_empty.txt** - Empty database (no records)
- **test_boundary.txt** - Boundary values for all fields

### Test Categories

**Unit Tests:**
- Individual function testing
- Isolated module behaviour
- Edge case validation
- Error condition handling

**Integration Tests:**
- Multi-module workflows
- End-to-end operations
- State consistency
- File I/O with database operations

**Boundary Tests:**
- Minimum/maximum values
- Empty collections
- Single element operations
- Capacity limits

### Limitations

**Full Command Testing:**
- Complete command workflows require stdin mocking
- Current tests cover:
  - Precondition validation
  - Error handling paths
  - Database operation correctness
- Success paths requiring interactive input tested manually

**Future Enhancements:**
- Mock stdin for full command integration tests
- Performance benchmarking suite
- Stress testing (large datasets)
- Concurrent operation testing (if applicable)

### Test Output

**Success:**
```
Running test_parser...
[PASS] Test validate_id_valid
[PASS] Test validate_id_out_of_range
...
All tests passed: 51/51
```

**Failure:**
```
Running test_parser...
[PASS] Test validate_id_valid
[FAIL] Test validate_id_out_of_range
  Expected: INVALID_ID
  Actual: VALID
...
Tests passed: 50/51
```

For detailed testing information, see [tests/README.md](tests/README.md).

---

## Project Structure

```bash
INF1002-P1-08-CMS/
├── src/                       # source code
│   ├── main.c                 # programme entry point
│   ├── cms.c                  # main application loop and dispatch
│   ├── database.c             # table/record management and CRUD
│   ├── parser.c               # file parsing and validation
│   ├── ui.c                   # user interface and display
│   ├── sorting.c              # bubble sort implementation
│   ├── statistics.c           # statistical calculations
│   ├── event_log.c            # operation logging system
│   ├── adv_query.c            # advanced query engine
│   ├── checksum.c             # CRC32 integrity checking
│   ├── utils.c                # general utility functions
│   └── commands/              # command implementations
│       ├── operation_registry.c    # command dispatcher
│       ├── command_utils.c         # shared command utilities
│       ├── open_command.c          # OPEN command
│       ├── show_all_command.c      # SHOW ALL command
│       ├── insert_command.c        # INSERT command
│       ├── query_command.c         # QUERY command
│       ├── update_command.c        # UPDATE command
│       ├── delete_command.c        # DELETE command
│       ├── save_command.c          # SAVE command
│       ├── sort_command.c          # SORT command
│       ├── adv_query_command.c     # ADV QUERY command
│       ├── statistics_command.c    # STATISTICS command
│       ├── event_log_command.c     # SHOW LOG command
│       └── checksum_command.c      # CHECKSUM command
│
├── include/                   # header files
│   ├── cms.h                  # main application interface
│   ├── database.h             # database structures and operations
│   ├── parser.h               # parsing functions
│   ├── ui.h                   # UI functions
│   ├── sorting.h              # sorting functions
│   ├── statistics.h           # statistics functions
│   ├── event_log.h            # event log interface
│   ├── adv_query.h            # advanced query interface
│   ├── checksum.h             # checksum functions
│   ├── utils.h                # utility functions
│   └── commands/
│       ├── command.h          # command definitions and registry
│       └── command_utils.h    # shared command utilities
│
├── tests/                     # test suite
│   ├── test_parser.c          # parser and validation tests
│   ├── test_database.c        # database operation tests
│   ├── test_sorting.c         # sorting tests
│   ├── test_statistics.c      # statistics tests
│   ├── test_event_log.c       # event log tests
│   ├── test_commands.c        # command precondition tests
│   ├── test_checksum.c        # checksum tests
│   ├── test_adv_query.c       # advanced query tests
│   ├── test_query.c           # basic query tests
│   ├── test_utils.h           # test utilities header
│   ├── test_utils.c           # test utilities implementation
│   ├── fixtures/              # test data files
│   │   ├── test_valid.txt
│   │   ├── test_invalid.txt
│   │   ├── test_empty.txt
│   │   └── test_boundary.txt
│   └── README.md              # detailed test documentation
│
├── data/                      # database files
│   ├── P1_8-CMS.txt           # default database
│   ├── 100-records.txt        # benchmark dataset (100 records)
│   ├── 500-records.txt        # benchmark dataset (500 records)
│   └── 1000-records.txt       # benchmark dataset (1000 records)
│
├── assets/                    # UI text files
│   ├── menu.txt               # command reference menu
│   └── declaration.txt        # team declaration
│
├── build/                     # compiled binaries (created by make)
│   ├── main                   # main executable (macOS)
│   ├── main.exe               # main executable (Windows)
│   ├── test_parser
│   ├── test_database
│   └── ...                    # other test executables
│
├── Makefile                   # build configuration
├── README.md
```

### Module Responsibilities

**Core System:**
- `cms.c` - Application coordination, command loop
- `database.c` - Data storage, CRUD operations
- `parser.c` - File format handling, validation
- `ui.c` - Display formatting, user interaction

**Features:**
- `sorting.c` - Record sorting functionality
- `statistics.c` - Aggregate calculations
- `adv_query.c` - Complex query processing
- `checksum.c` - Data integrity verification
- `event_log.c` - Operation history tracking

**Commands:**
- Each command in separate file for maintainability
- `operation_registry.c` - Centralized command dispatch
- `command_utils.c` - Shared command functionality

**Testing:**
- One test file per module
- Test utilities for common operations
- Fixtures for reproducible test data

---

## Constants & Limits

### Student Record Constraints

| Constant | Value | Description |
|----------|-------|-------------|
| **MIN_STUDENT_ID** | 2500000 | Minimum valid student ID |
| **MAX_STUDENT_ID** | 2600000 | Maximum valid student ID |
| **ID_DIGITS** | 7 | Required number of digits for ID |

### Field Size Limits

| Field | Size (bytes) | Characters | Notes |
|-------|--------------|------------|-------|
| **Name** | 50 | 49 + null | Maximum name length |
| **Programme** | 50 | 49 + null | Maximum programme length |
| **Table Name** | 50 | 49 + null | Database table name |
| **Database Name** | 100 | 99 + null | Database metadata |
| **Authors** | 200 | 199 + null | Author list metadata |
| **File Path** | 260 | 259 + null | Maximum file path |

### Capacity Settings

| Component | Initial | Growth | Maximum | Notes |
|-----------|---------|--------|---------|-------|
| **Tables** | 2 | 2× | unlimited | Database table array |
| **Records** | 10 | 2× | unlimited | Records per table |
| **Event Log** | 50 | 2× | 1000 | Circular after max |

### Buffer Sizes

| Buffer | Size (bytes) | Purpose |
|--------|--------------|---------|
| **Input Buffer** | 256 | User input reading |
| **Line Buffer** | 512 | File line reading |
| **Temp Buffer** | 256 | Temporary operations |
| **Small Input** | 10 | Single character inputs |

### Algorithm Parameters

| Parameter | Value | Description |
|-----------|-------|-------------|
| **CRC32 Table Size** | 256 | Lookup table entries |
| **Float Epsilon** | 0.0001f | Floating-point comparison tolerance |
| **Default Data File** | `data/P1_8-CMS.txt` | Default database path |

### Mark Range

| Boundary | Value | Notes |
|----------|-------|-------|
| **Minimum Mark** | 0.0 | Typical minimum (not enforced) |
| **Maximum Mark** | 100.0 | Typical maximum (not enforced) |
| **Display Precision** | 2 decimal places | Output formatting |
