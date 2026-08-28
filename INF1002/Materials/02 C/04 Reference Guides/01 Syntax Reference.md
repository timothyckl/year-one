# Reference Guide - C Syntax Quick Reference

See `../01 Lectures/` for the full lecture notes. A compact grammar/cheat reference.
For pointers and file I/O, see the dedicated guides.

---

## Program skeleton

```c
/* comment */
#include <stdio.h>   /* or "myfile.h" for local headers */

#define CONSTANT 100

int helper(int x);          /* prototype */

int main(void) {            /* or int main() */
    int n = 10;
    printf("%d\n", helper(n));
    return 0;
}

int helper(int x) {         /* definition */
    return x * 2;
}
```

## Pre-processor

| Directive | Purpose |
|-----------|---------|
| `#include <h>` | include standard header (system dirs) |
| `#include "h"` | include local header (current dir first) |
| `#define X v` | symbolic constant (replaced textually) |

## Declarations

```c
int a, b;              /* two ints */
int *p, *q;            /* two pointers to int */
float f = 1.5f;        /* with initialiser */
char c = 'A';          /* char constant (single quotes) */
char s[10];            /* char array (string) */
char name[] = "abc";   /* array of 4: 'a','b','c','\0' */
```

## Operators

- Arithmetic: `+ - * / %`
- Comparison: `== != < > <= >=`
- Logic: `&& || !`
- Address/deref: `&` (address-of), `*` (dereference)
- Member access: `.` (struct value), `->` (struct pointer)
- Cast: `(int)4.5`, `(float)4`
- Assignment: `=`, `+=`, `-=`, `*=`, `/=`, `%=`, `++`, `--`

## Control flow

```c
if (x > 1) { ... } else if (x == 1) { ... } else { ... }

switch (x) {
  case 1: ...; break;
  case 2: ...; break;
  default: ...;
}

for (int i = 0; i < 10; i++) { ... }

while (cond) { ... }

do { ... } while (cond);       /* runs at least once */
```

## Functions

```c
/* return_type name(parameters) */
int square(int y) { return y * y; }

/* prototype = no body */
int square(int);

/* void: no return value */
void print_num(int n) { printf("%d\n", n); }
```

Call-by-value by default; pass `&var` + a pointer parameter to modify the caller's value.

## printf specifiers

| Spec | Meaning | Example output |
|------|---------|----------------|
| `%d` | decimal int | `42` |
| `%i` | decimal int (scanf accepts hex/octal) | |
| `%x` | hex int | `2a` |
| `%ld` | long decimal | |
| `%f` | float | `3.140000` |
| `%.2f` | float, 2 decimals | `3.14` |
| `%lf` | double (scanf: `%lf`) | |
| `%c` | character | `A` |
| `%s` | string | `hello` |
| `%p` | pointer/address | `0x7fff...` |
| `%zu` | `size_t` (unsigned) | |

Flags: `-` left-justify, `+` force sign, `0` zero-pad, width `%4d`, precision `%.3f`.
Width+flags combine: `%-10.3f`, `%04d`, `%10.2lf`.

## Escape sequences

`\n` newline, `\t` tab, `\\` backslash, `\'` single quote, `\"` double quote, `\0` null.

## scanf patterns

```c
int n; double d; char word[20];
scanf("%d", &n);               /* & required for scalars */
scanf("%lf", &d);
scanf("%19s", word);           /* width limit; no & on arrays */
```

## Common library functions

`<stdio.h>`: `printf`, `scanf`, `puts`, `gets` (avoid), `fgets`, `getchar`, `putchar`,
`sprintf`/`snprintf`, `sscanf`.

`<string.h>`: `strlen`, `strcpy`/`strncpy`, `strcat` (avoid; use `strncat`),
`strcmp`/`strncmp`, `strchr`, `strstr`.

`<ctype.h>`: `isalpha`, `isdigit`, `isalnum`, `isspace`, `ispunct`, `toupper`, `tolower`.
Pass args as `(unsigned char)`.

`<stdlib.h>`: `atoi`, `atol`, `atof`, `rand`, `srand`, `malloc`, `calloc`, `free`, `exit`.

`<math.h>`: `sqrt`, `pow`, `exp`, `log`, `fabs`, `sin`, `cos`, `tan`.

## Common errors to avoid

1. Missing `;` or `}`.
2. `scanf` without `&`.
3. String buffer too small (no room for `'\0'`).
4. `strcmp(a, b) == 0` forgotten; using `a == b` for strings.
5. Comparing `char` to `int` without casting in `ctype` functions.
6. `switch` without `break`.
7. Integer division where float expected.
