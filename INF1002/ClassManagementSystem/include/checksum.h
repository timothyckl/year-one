#ifndef CHECKSUM_H
#define CHECKSUM_H

/**
 * @file checksum.h
 * @brief checksum module for data integrity verification
 *
 * provides CRC32 checksum computation for databases, files, and individual
 * records to detect changes and verify data integrity.
 *
 * @author Group P1-08 (Timothy, Aamir, Hasif, Dalton, Gin)
 */

#include "database.h"

/**
 * @brief computes CRC32 checksum of entire database
 * @param[in] db pointer to the database to checksum
 * @return CRC32 checksum value of the database
 */
unsigned long compute_database_checksum(const StudentDatabase *db);

/**
 * @brief computes CRC32 checksum of file on disk
 * @param[in] filepath path to the file to checksum
 * @return CRC32 checksum value of the file, 0 on error
 */
unsigned long compute_file_checksum(const char *filepath);

/**
 * @brief computes CRC32 checksum of a single record
 * @param[in] record pointer to the student record to checksum
 * @return CRC32 checksum value of the record
 */
unsigned long compute_record_checksum(const StudentRecord *record);

#endif // CHECKSUM_H
