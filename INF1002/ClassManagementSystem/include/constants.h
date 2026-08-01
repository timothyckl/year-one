#ifndef CONSTANTS_H
#define CONSTANTS_H

/**
 * @file constants.h
 * @brief global constants for buffer sizes, constraints, and limits
 *
 * defines application-wide constants including buffer sizes, field constraints,
 * validation limits, and cryptographic parameters.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

// buffer sizes for user input
#define INPUT_BUFFER_SIZE 256  // standard user input buffer
#define ERROR_MESSAGE_SIZE 256 // error message formatting
#define TEMP_BUFFER_SIZE 256   // temporary string operations
#define SMALL_INPUT_SIZE 10    // single-char input with buffer

// file parsing constants
#define MAX_LINE_LENGTH 512    // maximum line length in data files
#define MAX_METADATA_VALUE 200 // maximum metadata value size in parser

// field size constraints (must match database.h struct sizes)
#define MAX_NAME_LENGTH 50       // student name field size
#define MAX_PROGRAMME_LENGTH 50  // programme field size
#define MAX_TABLE_NAME_LENGTH 50 // table name field size
#define MAX_DB_NAME_LENGTH 100   // database name field size
#define MAX_AUTHORS_LENGTH 200   // authors field size

// student id validation constraints
#define MIN_STUDENT_ID 2500000 // minimum valid student id
#define MAX_STUDENT_ID 2600000 // maximum valid student id

// cryptographic constants
#define CRC32_TABLE_SIZE 256 // standard crc32 lookup table size

#endif // CONSTANTS_H
