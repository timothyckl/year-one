# Lecture 8b - Introduction to C

**Slides:** 74 content slides
**Recommended text:** Deitel & Deitel, *C: How to Program*, 9th ed., Pearson, 2021.

---

## Overview

First C lecture. It covers the history of C, the differences between C and Python
(compilers vs. interpreters, declarations, whitespace, functions), a first C program,
basic data types, formatted I/O (`printf`/`scanf`), and control structures. It ends with
housekeeping: assessment weights and AI-tool policy.

---

## 1. History of C

- The question "what is 37 + 45?" illustrates that natural language must be turned into
  a precise sequence of operations (define two integer variables, assign values, compute
  sum, print).
- Lineage:
  - 1943-1946: ENIAC (J. Presper Eckert & John Mauchly).
  - 1966-1967: BCPL (Martin Richards).
  - 1969: B (Ken Thompson), at Bell Labs, based on BCPL; early UNIX written in B.
  - 1972-1973: **C created by Dennis Ritchie at Bell Labs.**
  - 1973: UNIX re-written in C.
- Standardisation: 1970s "Traditional C" -> C89/ANSI C/Standard C (1989) ->
  1990 ANSI/ISO C -> C99 (1999) attempting to standardise variations.

### C vs. related languages

- Derivatives: C++, Objective-C, C#.
- Influenced: Java, Perl, Python (quite different).
- **C lacks:** exceptions, range-checking, garbage collection, object-oriented
  programming.
- C is a low-level language, which usually means faster code.

---

## 2. From Python to C

### Compilers vs. interpreters

- Python: interpreter executes the program.
- C: compiler converts the source (`program.c`) into machine code (`a.out`), which the
  CPU executes directly. C is designed so the compiler has everything it needs to
  translate the program to machine code, enabling faster runtimes.

### Variable declarations

- **C requires declarations** before use; a declared variable's type cannot change.
  A bonus: misspelled variable names are caught at compile time.
- Python needs no declaration - more convenient but you must check your own code.

### Whitespace

- Python: whitespace is significant (defines statements and blocks).
- C: whitespace is only used to separate words. Statements end with a **semicolon**,
  blocks are delimited by **braces** `{}`.

### Functions

- All C code lives inside functions; `main()` is the starting point.
- Example: a recursive GCD in C vs Python:

```c
#include <stdio.h>

int gcd(int a, int b) {
    if (b == 0)
        return a;
    else
        return gcd(b, a % b);
}

int main() {
    printf("GCD: %d\n", gcd(24, 40));
    return 0;
}
```

```python
def gcd(a, b):
    if b == 0:
        return a
    else:
        return gcd(b, a % b)

print("GCD: " + str(gcd(24, 40)))
```

### Good coding practice

- Indent blocks as in Python; format braces/whitespace consistently; comment code; use
  meaningful names. (An obfuscated IOCCC entry is a classic counter-example.)
- **Pay attention to compiler warnings** - code that is syntactically correct but likely
  to cause run-time errors.
- Avoid system-specific features; all module programs should compile and run on any
  modern compiler (portability).

---

## 3. A Simple C Program

### Program development pipeline

Editor -> source file -> Preprocessor (processes directives) -> Compiler (object code)
-> Linker (links object code with libraries, makes executable) -> Loader (loads into
memory) -> CPU executes.

### Anatomy of "Hello World"

```c
/*
 * Hello World program in C.
 */
#include <stdio.h>
int main() {
       printf("Hello world!\n");
       return 0;
}
```

- `/* ... */` : comment, ignored by the compiler.
- `#include <stdio.h>` : preprocessor directive to include the standard input/output
  header.
- `int main()` : every program must have a `main`; execution begins there.
- `\n` : newline escape; one `printf` can print several lines using multiple `\n`.
- `return 0;` : signals to the OS that the program ended with no errors.

### The pre-processor

- Runs **before** compilation. Actions include defining symbolic constants and including
  other files. Directives begin with `#`.
- `#define` : creates symbolic constants; all later occurrences are replaced.

  ```c
  #define NUM_STUDENTS 140
  int main() {
      int scores[NUM_STUDENTS];
      for (int i = 0; i < NUM_STUDENTS; i++) {
          scores[i] = 0;
      }
      return 0;
  }
  ```

- `#include` : copies a specified file in place of the directive.
  - `#include <filename>` : for standard library headers; searched in the compiler/system
    directories.
  - `#include "filename"` : for user-defined files; search starts in the directory of the
    file being compiled.
- Header files typically contain: function prototypes (declaration without
  implementation), constant definitions (`#define`), and data type definitions. A typical
  example is a `chat1002.h` header with `#define MAX_INTENT 32` and prototypes like
  `void chatbot_do_load(int inc, char **inv);` (this is a preview of the chatbot-style
  group project).

### A second program: sum.c

```c
#include <stdio.h>

int main() {
        int integer1, integer2, sum;
        printf("Enter two numbers to add\n");
        scanf("%d%d", &integer1, &integer2);

        sum = integer1 + integer2;

        printf("Sum of entered numbers = %d\n", sum);
        return 0;
}
```

- A **variable** is a named location in memory where a value is stored.
- `int integer1, integer2, sum;` declares three variables of type `int`.
- `scanf()` reads from standard input (the keyboard).
- `scanf("%d%d", &integer1, &integer2);` - three arguments: the format string plus the
  variables to fill.
- `%d` is the conversion specifier meaning "(decimal) integer".
- The ampersand `&` is the **address operator**; it tells `scanf` the memory locations
  where the values should be stored.

---

## 4. Basic Data Types and Memory

### Memory concepts

- Every variable has a name, a type, and a value; names map to memory addresses.
- Illustration: `integer1` at `0x0060FF03`, `integer2` at `0x0060FF05`, `sum` at
  `0x0060FF07`. Entering "45" and "37" stores those values; `sum = integer1 + integer2`
  stores 82. Placing a new value **replaces** the previous value in that location.

### The basic data types

```c
#include <stdio.h>
int main() {

        int      a = 3000;         /* integer data type */
        float    b = 4.5345;       /* floating point data type */
        char     c = 'A';          /* character data type */
        long     d = 31456;        /* long integer data type */
        double   e = 5.1234567890; /* double-precision floating point data type */

        printf("Here is the list of the basic data types\n");
        printf("\n1. This an integer (int): %d", a);
        printf("\n2. This is a floating point number (float): %f", b);
        printf("\n3. This is a character (char): %c", c);
        printf("\n4. This is a long integer (long): %ld", d);
        printf("\n5. This is a double-precision float (double): %.10f", e);
        printf("\n6. This is a sequence of characters: %s",
                 "Hello INF1002 students");

        return 0;
}
```

Exercise: try changing the `10` in `%.10f` and observe the output.

### C expressions

- Operators: `+ - * / % ()`.
- Result type depends on operand types:
  - `int op int -> int`
  - `int op float -> float`
  - `float op float -> float`
  - `double op float -> double`
  - `char op int -> char`
- Casting changes an expression's type explicitly: `(int)4.5` rounds down; `(float)4`
  "promotes".

---

## 5. C Formatted I/O

### Streams

- All I/O in C is done with **streams** (a stream is a sequence of bytes).
- Input stream from keyboard/disk/network; output stream to screen/disk/printer/network.
- `<stdio.h>` provides `printf`, `scanf`, `puts`, `getchar`, `putchar`.

### printf

- `printf(format-control-string, other-arguments);`
- Common conversion specifiers:
  - `%c` character, `%s` string, `%d` decimal integer, `%x` hex integer, `%ld` long
    decimal integer, `%f` single-precision float, `%lf` double.
- General form: `conversion-specifier = <flags><field width><precision><literal char>`,
  e.g. `printf("%-10s%-10d%-10c%-10.3f\n", "hello", 7, 'a', 1.23);`.
- Flags: `-` left-justify; `+` plus sign before positives; ` ` (space) before
  positive numbers; `0` zero-pad to the field width.

### printf flags example - fieldwidth.c

```c
#include <stdio.h>

int main() {

       printf("%4d\n", 1);
       printf("%04d\n", 1);
       printf("%-4d\n", 1);
       printf("%4d\n", 12);
       printf("%4d\n", 123);
       printf("%4d\n", 1234);
       printf("%4d\n", 12345);

       return 0;
}
```

Output (right column):

```
   1
0001
1
  12
 123
1234
12345
```

### Escape sequences

- `\n` newline, `\t` tab, `\'` single quote, `\"` double quote, `\\` backslash.

### scanf

- `scanf(format-control-string, other-arguments);`
- Example:

  ```c
  printf("Enter seven integers: ");
  scanf("%d%i%i%i%o%u%x", &a, &b, &c, &d, &e, &f, &g);
  ```

  Input `-70 -70 070 0x70 70 70 70` prints back as `-70 -70 56 112 56 70 112`
  (note `%i`/`%o`/`%x` interpretations of the same-looking inputs).

---

## 6. Control Structures

C has the same control structures as other languages, but `switch` and `do-while` are
**not** available in Python.

```c
/* if-else */
if (x > 1) {
    printf("More than one.");
} else {
    printf("Not more than one.");
}

/* switch */
switch (x) {
    case 1:
        printf("x is 1.");
        break;
    case 2:
        printf("x is 2.");
        break;
    ...
}

/* for */
int i;
for (i = 0; i < 10; i++) {
    printf("i = %d\n", i);
}

/* while */
int i = 0;
while (i < 10) {
    printf("i = %d\n", i);
    i++;
}

/* do-while (body executes at least once) */
i = 0;
do {
    printf("i = %d\n", i);
    i++;
} while (i <= 10);
```

---

## End-of-Week Checklist

C development environment, basic C program structure, comments, pre-processor
directives (`#include`, `#define`), `printf()`, `scanf()`, basic data types, variables
and memory, streams, if/else, switch/case, for/while/do..while, coding conventions,
format control strings.

---

## Administration & Assessment

- Weighting: Lab Assignments (5) 5%; Group Project 25%; Test (Week 13) 20%; Sub-total 50%.
- Group project spec uploaded in Week 9; same groupings as Python.
- Test (Week 13) is online.
- AI tools: allowed for reference in Labs (hands-on learning recommended); NOT allowed to
  generate code directly for the Group Project (a declaration is needed from each team);
  NOT allowed in the Test.
- Labs start Week 8 (Friday), in the classroom; "Learning happens not in the answer, but
  in the path to the answer."

---

## Key Takeaways

1. C: compiled, static declarations, semicolons + braces, no exceptions/GC/range checks.
2. `printf`/`scanf` with conversion specifiers and the `&` address operator for input.
3. `#include` / `#define` run at the pre-processing stage.
4. Control structures mirror Python except `switch` and `do-while`.
5. Assessment: 5 labs (5%), project (25%), online test (20%).

---

## Safety / Correctness Notes

- Nothing here is unsafe by itself, but note for later labs:
  - `scanf("%d", ...)` without width limits on `%s` (e.g. `scanf("%9s", ...)` for char
    arrays) is buffer-unsafe.
  - Prefer `fgets` + `sscanf` for interactive input.
