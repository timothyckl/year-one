#include "checksum.h"
#include "commands/command_utils.h"
#include <stdio.h>
#include <string.h>

// crc32 lookup table
static unsigned long crc32_table[256];
static int table_initialised = 0;

// initialise crc32 lookup table
static void init_crc32_table(void) {
  if (table_initialised)
    return;

  unsigned long polynomial = 0xEDB88320;
  for (unsigned long i = 0; i < 256; i++) {
    unsigned long crc = i;
    for (unsigned long j = 0; j < 8; j++) {
      if (crc & 1) {
        crc = (crc >> 1) ^ polynomial;
      } else {
        crc >>= 1;
      }
    }
    crc32_table[i] = crc;
  }
  table_initialised = 1;
}

// compute crc32 of a buffer
static unsigned long crc32(const unsigned char *data, size_t length) {
  init_crc32_table();

  unsigned long crc = 0xFFFFFFFF;
  for (size_t i = 0; i < length; i++) {
    unsigned char byte = data[i];
    crc = (crc >> 8) ^ crc32_table[(crc ^ byte) & 0xFF];
  }
  return crc ^ 0xFFFFFFFF;
}

/**
 * @brief computes CRC32 checksum of a single record
 * @param[in] record pointer to the student record to checksum
 * @return CRC32 checksum value of the record
 */
unsigned long compute_record_checksum(const StudentRecord *record) {
  if (!record)
    return 0;

  // create buffer with all record fields
  unsigned char buffer[1024];
  size_t offset = 0;

  // add id with bounds checking
  if (offset + sizeof(record->id) > sizeof(buffer))
    return 0;
  memcpy(buffer + offset, &record->id, sizeof(record->id));
  offset += sizeof(record->id);

  // add name with bounds checking
  size_t name_len = strlen(record->name);
  if (offset + name_len > sizeof(buffer))
    return 0;
  memcpy(buffer + offset, record->name, name_len);
  offset += name_len;

  // add programme with bounds checking
  size_t prog_len = strlen(record->prog);
  if (offset + prog_len > sizeof(buffer))
    return 0;
  memcpy(buffer + offset, record->prog, prog_len);
  offset += prog_len;

  // add mark with bounds checking
  if (offset + sizeof(record->mark) > sizeof(buffer))
    return 0;
  memcpy(buffer + offset, &record->mark, sizeof(record->mark));
  offset += sizeof(record->mark);

  return crc32(buffer, offset);
}

/**
 * @brief computes CRC32 checksum of entire database
 * @param[in] db pointer to the database to checksum
 * @return CRC32 checksum value of the database
 */
unsigned long compute_database_checksum(const StudentDatabase *db) {
  if (!db || !db->is_loaded || db->table_count == 0)
    return 0;

  // get the student records table
  StudentTable *table = db->tables[STUDENT_RECORDS_TABLE_INDEX];
  if (!table || table->record_count == 0)
    return 0;

  init_crc32_table();

  unsigned long combined_crc = 0xFFFFFFFF;

  // compute checksum of each record and combine them
  for (size_t i = 0; i < table->record_count; i++) {
    unsigned long record_crc = compute_record_checksum(&table->records[i]);
    // combine checksums using xor
    combined_crc ^= record_crc;
  }

  return combined_crc;
}

/**
 * @brief computes CRC32 checksum of file on disk
 * @param[in] filepath path to the file to checksum
 * @return CRC32 checksum value of the file, 0 on error
 */
unsigned long compute_file_checksum(const char *filepath) {
  if (!filepath)
    return 0;

  FILE *fp = fopen(filepath, "r");
  if (!fp)
    return 0;

  init_crc32_table();

  unsigned long crc = 0xFFFFFFFF;
  int byte;

  while ((byte = fgetc(fp)) != EOF) {
    crc = (crc >> 8) ^ crc32_table[(crc ^ (unsigned char)byte) & 0xFF];
  }

  fclose(fp);
  return crc ^ 0xFFFFFFFF;
}
