/*
 * example_menu_adv_query.c
 *
 * Interactive menu harness for testing QUERY and ADV QUERY operations.
 * Provides a simple menu interface to test both basic and advanced query
 * functionality with user input.
 *
 * Build command:
 *   clang -Iinclude -Wall -Wextra -g
 * \src/{adv_query,cms,database,parser,sorting,utils}.c
 * \tests/example_menu_adv_query.c \-o build/example_menu_adv_query.exe
 *
 * Run command:
 *   ./build/example_menu_adv_query.exe
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "adv_query.h"
#include "database.h"

#define MENU_BUF 256
#define DEFAULT_DATA_FILE "data/P1_8-CMS.txt"

static void wait_for_enter(void) {
  char buffer[MENU_BUF];
  printf("\nPress Enter to continue...");
  (void)fgets(buffer, sizeof buffer, stdin);
}

static void print_menu(void) {
  puts("==============================================================");
  puts("Class Management System (Query Harness)");
  puts("");
  puts("Main Menu");
  puts("");
  puts("[1] OPEN: Opens the database file and loads records.");
  puts("[2] QUERY: Search for a single record by ID.");
  puts("[3] ADV QUERY: Run the advanced search pipeline.");
  puts("[4] EXIT: Exit the harness.");
  puts("==============================================================");
}

static void trim_newline(char *text) {
  if (!text) {
    return;
  }
  size_t len = strcspn(text, "\r\n");
  text[len] = '\0';
}

static int prompt_path(char *buffer, size_t size) {
  printf("Enter a file path (press ENTER for default data file): ");
  fflush(stdout);
  if (!fgets(buffer, (int)size, stdin)) {
    return 0;
  }
  trim_newline(buffer);
  if (buffer[0] == '\0') {
    snprintf(buffer, size, "%s", DEFAULT_DATA_FILE);
  }
  return 1;
}

static int prompt_id(int *out_id) {
  char buf[MENU_BUF];
  printf("Enter student ID to search: ");
  fflush(stdout);
  if (!fgets(buf, sizeof buf, stdin)) {
    return 0;
  }
  trim_newline(buf);
  if (buf[0] == '\0') {
    return -1;
  }
  char *endptr = NULL;
  long parsed = strtol(buf, &endptr, 10);
  if (endptr == buf || *endptr != '\0') {
    return -1;
  }
  *out_id = (int)parsed;
  return 1;
}

static void run_query(StudentDatabase *db) {
  if (!db || db->table_count == 0) {
    printf("CMS: Please OPEN the database before querying.\n");
    wait_for_enter();
    return;
  }

  int id = 0;
  int rc = prompt_id(&id);
  if (rc <= 0) {
    printf("CMS: Invalid student ID input.\n");
    wait_for_enter();
    return;
  }

  // search for record with matching ID
  StudentRecord *record = NULL;
  for (size_t t = 0; t < db->table_count; t++) {
    StudentTable *table = db->tables[t];
    if (!table) {
      continue;
    }

    for (size_t r = 0; r < table->record_count; r++) {
      if (table->records[r].id == id) {
        record = &table->records[r];
        break;
      }
    }
    if (record) {
      break;
    }
  }

  if (!record) {
    printf("CMS: The record with ID=%d does not exist.\n", id);
    wait_for_enter();
    return;
  }

  printf("CMS: Record found.\n\n");
  printf("ID\tName\tProgramme\tMark\n");
  printf("%d\t%s\t%s\t%.2f\n", record->id, record->name, record->prog,
         record->mark);
  wait_for_enter();
}

static void run_adv_query(StudentDatabase *db) {
  if (!db || db->table_count == 0) {
    printf("CMS: Please OPEN the database before running advanced query.\n");
    wait_for_enter();
    return;
  }

  // guided prompt that builds and executes the pipeline for the user
  AdvQueryStatus status = adv_query_run_prompt(db);
  if (status != ADV_QUERY_SUCCESS) {
    printf("CMS: Advanced query failed: %s\n", adv_query_status_string(status));
  }

  wait_for_enter();
}

int main(void) {
  StudentDatabase *db = db_init();
  if (!db) {
    fprintf(stderr, "Failed to initialise database.\n");
    return EXIT_FAILURE;
  }

  int running = 1;
  while (running) {
    print_menu();
    printf("Select an option: ");
    fflush(stdout);

    char buf[MENU_BUF];
    if (!fgets(buf, sizeof buf, stdin)) {
      break;
    }
    int choice = atoi(buf);

    switch (choice) {
    case 1: {
      char path[MENU_BUF];
      if (!prompt_path(path, sizeof path)) {
        printf("CMS: Failed to read file path.\n");
        wait_for_enter();
        break;
      }
      DBStatus status = db_load(db, path, NULL);
      if (status != DB_SUCCESS) {
        printf("CMS: Failed to load database: %s\n", db_status_string(status));
      } else {
        db->is_loaded = true;
        strncpy(db->filepath, path, sizeof db->filepath);
        db->filepath[sizeof db->filepath - 1] = '\0';
        printf("CMS: The database file \"%s\" is successfully opened.\n", path);
      }
      wait_for_enter();
      break;
    }
    case 2:
      run_query(db);
      break;
    case 3:
      run_adv_query(db);
      break;
    case 4:
      running = 0;
      break;
    default:
      printf("CMS: Invalid option. Please select between 1 and 4.\n");
      wait_for_enter();
      break;
    }
  }

  db_free(db);
  return EXIT_SUCCESS;
}
