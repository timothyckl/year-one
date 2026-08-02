# Revision Notes - Quiz / Exam Checklist

A condensed, one-page-style checklist of the topics the module declares examinable. The
Week 12 quiz is 45 minutes, closed-book, MCQ + short answers, online (Respondus LockDown
Browser).

---

## Week 8 - Introduction to C
- [ ] C development environment (compiler, `gcc -o out in.c`)
- [ ] Basic program structure (`main`, `return 0`)
- [ ] Comments `/* */`
- [ ] Pre-processor: `#include`, `#define`
- [ ] `printf()` / `scanf()` format control strings
- [ ] Basic data types (`int`, `long`, `float`, `double`, `char`) and sizes
- [ ] Variables and memory (name/type/value; `&` address-of)
- [ ] Streams (sequence of bytes)
- [ ] Control structures: `if/else`, `switch/case`, `for`, `while`, `do-while`
- [ ] Coding conventions; compiler warnings; portability

## Week 9 - Functions, Arrays, Strings
- [ ] Function prototypes vs definitions
- [ ] Call by value (copies)
- [ ] Scope rules: file (global) vs block (local)
- [ ] Array declarations, initialiser lists, multi-dimensional arrays
- [ ] Characters (ASCII values, arithmetic)
- [ ] Strings: `'\0'` termination; room for the null
- [ ] String I/O: `fgets`, `puts`, `getchar`, `sprintf`
- [ ] `<string.h>`: `strlen`, `strncpy`, `strcmp`/`strncmp` (case-sensitive)
- [ ] `<ctype.h>`: `isalpha`, `isdigit`, `tolower`, ...
- [ ] `<stdlib.h>` conversions: `atoi`, `atol`, `atof`

## Week 10 - Pointers
- [ ] Pointer declarations (`int *ptr;` - `*` doesn't distribute)
- [ ] Address operator `&`; dereference `*`
- [ ] Pointer initialisation and pointer assignment
- [ ] Void pointers (`void *`)
- [ ] Pointers to pointers (`int **`)
- [ ] Pointer arithmetic (scales by `sizeof(type)`)
- [ ] `sizeof` operator
- [ ] Arrays and pointers (name == &arr[0]; subscripting pointers)
- [ ] Arrays of pointers (e.g. `char *suit[4]`)
- [ ] Call by reference (simulated with pointers)
- [ ] Passing arrays to functions (by reference); `const` arrays

## Week 11 - Dynamic Memory and Linked Lists
- [ ] Dynamic memory allocation: `#include <stdlib.h>`
- [ ] `malloc` / `calloc` (return `void *`; cast; `NULL` check)
- [ ] `free` (every allocation freed; memory leaks)
- [ ] `sizeof` with user-defined types
- [ ] Self-referential structures (Node with `next`)
- [ ] `struct` and `typedef` (dot `.` vs arrow `->`)
- [ ] Linked lists: search/update, insert, delete (with `free`)

## Week 12 - Files
- [ ] Files and streams; `FILE *`
- [ ] `fopen` modes (`"r"`, `"w"`, `"a"`; binary `"b"` variants)
- [ ] `fprintf`, `fscanf`, `fclose`, `feof`
- [ ] Sequential access files
- [ ] Random access files: `fseek`, `fread`, `fwrite`, fixed-length records
- [ ] `main(int argc, char **argv)` command-line arguments

---

## Quick-fire facts most likely to be tested

1. `int *a, b;` declares a pointer `a` and an int `b` (`*` doesn't distribute).
2. `scanf` needs `&`; arrays/strings don't.
3. Strings must have room for `'\0'`; `char s[10]` holds 9 chars + null.
4. `strcmp` returns <0/0/>0 and is case-sensitive; never compare strings with `==`.
5. Arrays pass to functions by reference (elements can be modified).
6. `malloc`/`calloc` return `void *`; check for `NULL`; `free` every allocation.
7. Pointer arithmetic is scaled by the pointed-to type.
8. `->` dereferences a struct pointer; `.` accesses struct values.
9. A linked list's last `next` is `NULL`; traverse with `temp = temp->next`.
10. `while (!feof(f))` + `fscanf` double-processes the last record - check `fscanf`'s
    return value instead.
11. Tar file sizes in headers are octal strings (`%011lo`), converted with `atoi`.
12. `do-while` and `switch` exist in C but not in Python.

## Labs mapping (for revision by exercise)
- Lab 1 `guessInteger`: `fgets` + validation, ranges, pluralisation.
- Lab 2 `tinyGrep`: strings, case sensitivity, pattern matching.
- Lab 3 `guessWord`: `ctype.h` conversion, arrays of revealed letters, helper functions.
- Lab 4 `insertionSort`: linked lists, `malloc`/`free`, sorted insertion.
- Lab 5 `miniTar`: `argv`, `fopen`/`fread`/`fwrite` in binary mode, tar header structs.
