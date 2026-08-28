#include "adv_query.h"

#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef _WIN32
#define strtok_r(str, delim, saveptr) strtok_s(str, delim, saveptr)
#endif

typedef enum {
  QUERY_FIELD_NAME = 0,
  QUERY_FIELD_PROGRAMME,
  QUERY_FIELD_MARK,
  QUERY_FIELD_INVALID
} QueryField;

#define ADV_QUERY_FIELD_COUNT 3
#define ADV_QUERY_MAX_SELECTIONS 8

// duplicate string to heap; caller frees
static char *dup_string(const char *src) {
  if (!src) {
    return NULL;
  }
  size_t len = strlen(src);
  char *copy = malloc(len + 1);
  if (copy) {
    memcpy(copy, src, len + 1);
  }
  return copy;
}

// trim leading/trailing whitespace in-place
static char *trim(char *text) {
  if (!text) {
    return text;
  }
  while (*text && isspace((unsigned char)*text)) {
    text++;
  }
  if (*text == '\0') {
    return text;
  }
  char *end = text + strlen(text) - 1;
  while (end > text && isspace((unsigned char)*end)) {
    *end = '\0';
    end--;
  }
  return text;
}

// case-insensitive string equals
static int strcaseequal(const char *a, const char *b) {
  if (!a || !b) {
    return 0;
  }
  while (*a && *b) {
    if (tolower((unsigned char)*a) != tolower((unsigned char)*b)) {
      return 0;
    }
    a++;
    b++;
  }
  return *a == '\0' && *b == '\0';
}

// map token to supported query field
static QueryField parse_field(const char *token) {
  if (strcaseequal(token, "NAME")) {
    return QUERY_FIELD_NAME;
  }
  if (strcaseequal(token, "PROGRAMME") || strcaseequal(token, "PROGRAM")) {
    return QUERY_FIELD_PROGRAMME;
  }
  if (strcaseequal(token, "MARK") || strcaseequal(token, "MARKS")) {
    return QUERY_FIELD_MARK;
  }
  return QUERY_FIELD_INVALID;
}

// flatten all records into one array for filtering
static size_t collect_records(StudentDatabase *db, StudentRecord ***out) {
  size_t total = 0;
  for (size_t t = 0; t < db->table_count; t++) {
    StudentTable *table = db->tables[t];
    if (table) {
      total += table->record_count;
    }
  }
  if (total == 0) {
    *out = NULL;
    return 0;
  }
  StudentRecord **records = malloc(total * sizeof(StudentRecord *));
  if (!records) {
    *out = NULL;
    return (size_t)-1;
  }
  size_t idx = 0;
  for (size_t t = 0; t < db->table_count; t++) {
    StudentTable *table = db->tables[t];
    if (!table) {
      continue;
    }
    for (size_t r = 0; r < table->record_count; r++) {
      records[idx++] = &table->records[r];
    }
  }
  *out = records;
  return total;
}

// substring check ignoring case
static int contains_case_insensitive(const char *haystack, const char *needle) {
  if (!haystack || !needle || *needle == '\0') {
    return 0;
  }
  size_t nlen = strlen(needle);
  for (const char *p = haystack; *p; p++) {
    size_t i = 0;
    while (p[i] && tolower((unsigned char)p[i]) ==
                      tolower((unsigned char)needle[i])) {
      if (++i == nlen) {
        return 1;
      }
    }
  }
  return 0;
}

// match a record against a GREP stage for a given field
static int grep_matches(const StudentRecord *record, QueryField field,
                        const char *pattern) {
  if (!pattern) {
    return 0;
  }
  if (field == QUERY_FIELD_NAME) {
    return contains_case_insensitive(record->name, pattern);
  }
  if (field == QUERY_FIELD_PROGRAMME) {
    return contains_case_insensitive(record->prog, pattern);
  }
  return 0;
}

static int mark_matches(const StudentRecord *record, char op, double value) {
  if (op == '<') {
    return record->mark < value;
  }
  if (op == '>') {
    return record->mark > value;
  }
  return record->mark == value;
}

static void strip_quotes(char *text) {
  size_t len = strlen(text);
  if (len >= 2 && text[0] == '"' && text[len - 1] == '"') {
    memmove(text, text + 1, len - 2);
    text[len - 2] = '\0';
  }
}

static int apply_text_filter(StudentRecord **records, unsigned char *keep,
                             size_t count, QueryField field,
                             const char *pattern) {
  if (!pattern || *pattern == '\0') {
    return 0;
  }
  for (size_t i = 0; i < count; i++) {
    if (keep[i] && !grep_matches(records[i], field, pattern)) {
      keep[i] = 0;
    }
  }
  return 1;
}

// apply MARK comparison to keep-mask for all records
static int apply_mark_filter(StudentRecord **records, unsigned char *keep,
                             size_t count, char op, double value) {
  for (size_t i = 0; i < count; i++) {
    if (keep[i] && !mark_matches(records[i], op, value)) {
      keep[i] = 0;
    }
  }
  return 1;
}

typedef enum { STAGE_GREP, STAGE_MARK } StageType;

typedef struct {
  StageType type;
  QueryField field; // for GREP
  char op;          // for MARK
  double value;     // for MARK
  char *pattern;    // for GREP (points into working buffer)
} QueryStage;

// parse a single pipeline segment into a structured stage
static int parse_stage(char *segment, QueryStage *out, int *field_used) {
  char *trimmed = trim(segment);
  if (*trimmed == '\0') {
    return 0;
  }

  char cmd[16] = {0};
  size_t idx = 0;
  while (trimmed[idx] && !isspace((unsigned char)trimmed[idx]) &&
         idx < sizeof(cmd) - 1) {
    cmd[idx] = trimmed[idx];
    idx++;
  }
  cmd[idx] = '\0';
  char *expr = trim(trimmed + idx);

  if (strcaseequal(cmd, "GREP")) {
    char field_buf[32] = {0};
    idx = 0;
    while (expr[idx] && !isspace((unsigned char)expr[idx]) && expr[idx] != '=' &&
           idx < sizeof(field_buf) - 1) {
      field_buf[idx] = expr[idx];
      idx++;
    }
    field_buf[idx] = '\0';
    expr = trim(expr + idx);
    if (*expr == '=') {
      expr++;
      expr = trim(expr);
    }

    QueryField field = parse_field(field_buf);
    if (field == QUERY_FIELD_INVALID || field == QUERY_FIELD_MARK ||
        field_used[field]) {
      return 0;
    }
    strip_quotes(expr);
    out->type = STAGE_GREP;
    out->field = field;
    out->pattern = expr;
    field_used[field] = 1;
    return 1;
  }

  if (strcaseequal(cmd, "MARK") || strcaseequal(cmd, "FILTER")) {
    char op = *expr;
    if (op != '<' && op != '>' && op != '=') {
      return 0;
    }
    expr = trim(expr + 1);
    if (*expr == '\0') {
      return 0;
    }
    char *endptr = NULL;
    double value = strtod(expr, &endptr);
    if (endptr == expr || *endptr != '\0') {
      return 0;
    }
    if (field_used[QUERY_FIELD_MARK]) {
      return 0;
    }
    out->type = STAGE_MARK;
    out->op = op;
    out->value = value;
    field_used[QUERY_FIELD_MARK] = 1;
    return 1;
  }

  return 0;
}

// apply a parsed stage to the keep-mask
static int apply_stage(const QueryStage *stage, StudentRecord **records,
                       unsigned char *keep, size_t count) {
  if (stage->type == STAGE_GREP) {
    return apply_text_filter(records, keep, count, stage->field,
                             stage->pattern);
  }
  return apply_mark_filter(records, keep, count, stage->op, stage->value);
}

/**
 * @brief executes a query pipeline and displays matching records
 * @param[in] db pointer to the database to query
 * @param[in] pipeline query string with filter conditions separated by '|'
 * @return ADV_QUERY_SUCCESS on success, appropriate error code on failure
 */
AdvQueryStatus adv_query_execute(StudentDatabase *db, const char *pipeline) {
  if (!db || !pipeline) {
    return ADV_QUERY_ERROR_INVALID_ARGUMENT;
  }
  if (db->table_count == 0) {
    return ADV_QUERY_ERROR_EMPTY_DATABASE;
  }

  StudentRecord **records = NULL;
  size_t total = collect_records(db, &records);
  if (total == (size_t)-1) {
    return ADV_QUERY_ERROR_MEMORY;
  }
  if (total == 0) {
    printf("ADVQUERY: No records matched the pipeline.\n");
    return ADV_QUERY_SUCCESS;
  }

  unsigned char *keep = malloc(total);
  if (!keep) {
    free(records);
    return ADV_QUERY_ERROR_MEMORY;
  }
  memset(keep, 1, total);

  char *working = dup_string(pipeline);
  if (!working) {
    free(records);
    free(keep);
    return ADV_QUERY_ERROR_MEMORY;
  }

  int field_used[3] = {0};
  int success = 1;
  int stages = 0;
  char *ctx = NULL;
  char *stage = strtok_r(working, "|", &ctx);

  while (stage && success) {
    QueryStage parsed = {0};
    success = parse_stage(stage, &parsed, field_used) &&
              apply_stage(&parsed, records, keep, total);
    if (success) {
      stages++;
      stage = strtok_r(NULL, "|", &ctx);
    }
  }

  if (!success || stages == 0) {
    free(records);
    free(keep);
    free(working);
    return ADV_QUERY_ERROR_PARSE;
  }

  size_t match_count = 0;
  for (size_t i = 0; i < total; i++) {
    if (keep[i]) {
      match_count++;
    }
  }

  if (match_count == 0) {
    printf("ADVQUERY: No records matched the pipeline.\n");
  } else {
    printf("ID\tName\tProgramme\tMark\n");
    for (size_t i = 0; i < total; i++) {
      if (keep[i]) {
        StudentRecord *r = records[i];
        printf("%d\t%s\t%s\t%.2f\n", r->id, r->name, r->prog, r->mark);
      }
    }
    printf("Total: %zu record(s)\n", match_count);
  }

  free(records);
  free(keep);
  free(working);
  return ADV_QUERY_SUCCESS;
}

/**
 * @brief converts query status code to human-readable string
 * @param[in] status the query status code to convert
 * @return pointer to static string describing the status
 */
const char *adv_query_status_string(AdvQueryStatus status) {
  switch (status) {
  case ADV_QUERY_SUCCESS:
    return "operation succeeded";
  case ADV_QUERY_ERROR_INVALID_ARGUMENT:
    return "invalid argument provided";
  case ADV_QUERY_ERROR_EMPTY_DATABASE:
    return "database contains no records";
  case ADV_QUERY_ERROR_PARSE:
    return "advanced query parse failed";
  case ADV_QUERY_ERROR_MEMORY:
    return "memory allocation failed";
  default:
    return "unknown advanced query error";
  }
}

// ------------------------------------------------------------
// Interactive prompt helpers (kept concise)
// ------------------------------------------------------------

typedef struct {
  int field;  // 1=Name, 2=Programme, 3=Mark
  char op;    // for Mark comparisons
  char value[256];
} AdvQuerySelection;

static int read_line(const char *prompt, char *buf, size_t size) {
  printf("%s", prompt);
  if (!fgets(buf, size, stdin)) {
    return 0;
  }
  buf[strcspn(buf, "\r\n")] = '\0';
  return 1;
}

static int prompt_int(const char *prompt, int *out) {
  char buf[256];
  if (!read_line(prompt, buf, sizeof buf)) {
    return 0;
  }
  char *end = NULL;
  long v = strtol(buf, &end, 10);
  if (end == buf || *end != '\0') {
    return -1;
  }
  *out = (int)v;
  return 1;
}

static int prompt_yes_no(const char *prompt) {
  char buf[16];
  while (read_line(prompt, buf, sizeof buf)) {
    char c = (char)tolower((unsigned char)buf[0]);
    if (c == 'y')
      return 1;
    if (c == 'n')
      return 0;
    printf("Please enter Y or N.\n");
  }
  return 0;
}

static void prompt_text(const char *label, char *out, size_t size) {
  char buf[256];
  while (1) {
    char prompt[64];
    snprintf(prompt, sizeof prompt, "Enter %s to search: ", label);
    if (!read_line(prompt, buf, sizeof buf) || buf[0] == '\0') {
      printf("Input cannot be empty.\n");
      continue;
    }
    for (char *p = buf; *p; p++) {
      if (*p == '"') {
        *p = '\'';
      }
    }
    strncpy(out, buf, size - 1);
    out[size - 1] = '\0';
    break;
  }
}

static char prompt_mark_op(void) {
  int choice = 0;
  while (1) {
    printf("\nMark comparison\n 1) Greater than\n 2) Less than\n 3) Equal to\n");
    int rc = prompt_int("Select option: ", &choice);
    if (rc == 1 && choice >= 1 && choice <= 3) {
      return (choice == 1) ? '>' : (choice == 2) ? '<' : '=';
    }
    printf("Please enter 1, 2, or 3.\n");
  }
}

static const char *field_token(int field) {
  return (field == 1) ? "NAME" : (field == 2) ? "PROGRAMME" : "MARK";
}

static const char *field_label(int field) {
  return (field == 1) ? "Name" : (field == 2) ? "Programme" : "Mark";
}

static int collect_fields(AdvQuerySelection *sel, size_t *count) {
  size_t n = 0;
  while (n < ADV_QUERY_FIELD_COUNT && n < ADV_QUERY_MAX_SELECTIONS) {
    printf("\nPick a field to filter:\n 1) Name\n 2) Programme\n 3) Mark\n 0) Cancel\n");
    int choice = 0;
    int rc = prompt_int("Select option: ", &choice);
    if (rc == 0) {
      return 0;
    }
    if (rc == 1 && choice == 0) {
      if (n == 0) {
        printf("Cancelled advanced search.\n");
        return 0;
      }
      break;
    }
    if (rc == 1 && choice >= 1 && choice <= 3) {
      int dup = 0;
      for (size_t i = 0; i < n; i++) {
        if (sel[i].field == choice) {
          dup = 1;
          break;
        }
      }
      if (dup) {
        printf("You already selected %s. Pick another field.\n",
               field_label(choice));
        continue;
      }
      sel[n].field = choice;
      sel[n].op = '=';
      sel[n].value[0] = '\0';
      n++;
      if (n >= ADV_QUERY_FIELD_COUNT) {
        printf("All available fields have been selected.\n");
        break;
      }
      if (!prompt_yes_no("Add another field? (Y/N): ")) {
        break;
      }
    } else {
      printf("Invalid choice. Try again.\n");
    }
  }
  *count = n;
  return n > 0;
}

static void collect_values(AdvQuerySelection *sel, size_t count) {
  for (size_t i = 0; i < count; i++) {
    if (sel[i].field == 3) {
      sel[i].op = prompt_mark_op();
      prompt_text("mark value", sel[i].value, sizeof sel[i].value);
    } else {
      prompt_text(field_label(sel[i].field), sel[i].value,
                  sizeof sel[i].value);
    }
  }
}

static void build_pipeline(const AdvQuerySelection *sel, size_t count,
                           char *pipeline, size_t size) {
  pipeline[0] = '\0';
  for (size_t i = 0; i < count; i++) {
    char stage[256];
    if (sel[i].field == 3) {
      snprintf(stage, sizeof stage, "MARK %c %s", sel[i].op, sel[i].value);
    } else {
      snprintf(stage, sizeof stage, "GREP %s = \"%s\"",
               field_token(sel[i].field), sel[i].value);
    }
    if (pipeline[0]) {
      strncat(pipeline, " | ", size - strlen(pipeline) - 1);
    }
    strncat(pipeline, stage, size - strlen(pipeline) - 1);
  }
}

/**
 * @brief runs interactive query prompt with guided help
 * @param[in] db pointer to the database to query
 * @return ADV_QUERY_SUCCESS on success, appropriate error code on failure
 */
AdvQueryStatus adv_query_run_prompt(StudentDatabase *db) {
  if (!db) {
    return ADV_QUERY_ERROR_INVALID_ARGUMENT;
  }
  if (!db->is_loaded || db->table_count == 0) {
    printf("CMS: Please OPEN the database before running advanced query.\n");
    return ADV_QUERY_ERROR_EMPTY_DATABASE;
  }

  AdvQuerySelection selections[ADV_QUERY_MAX_SELECTIONS];
  size_t selection_count = 0;
  if (!collect_fields(selections, &selection_count)) {
    return ADV_QUERY_SUCCESS;
  }

  collect_values(selections, selection_count);

  char pipeline[256 * ADV_QUERY_MAX_SELECTIONS] = {0};
  build_pipeline(selections, selection_count, pipeline, sizeof pipeline);

  return adv_query_execute(db, pipeline);
}
