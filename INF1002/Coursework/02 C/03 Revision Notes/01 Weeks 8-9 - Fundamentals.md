# Revision Notes - Weeks 8-9: C Fundamentals, Functions, Arrays, Strings

See `../01 Lectures/` for the full lecture notes.
Use with: `../04 Reference Guides/01 Syntax Reference.md`.

---

## 1. C vs Python - the big differences (W8)

- **Compiled, not interpreted.** `gcc -o hello hello.c` produces machine code executed by
  the CPU. Faster runtime, but every bug must be fixed at the source level.
- **Variables must be declared with a type** before use, and the type never changes.
  Declaring + initialising are two separate steps:
  ```c
  int count;      /* declare - holds a random value */
  count = 100;    /* initialise */
  ```
- **Statements end with `;`; blocks are `{ }`.** Whitespace is only for separating words.
- **All code lives inside functions**; execution always begins in `main()`.
- **No exceptions, no range-checking, no garbage collection, no OOP**.
- Every program needs:
  ```c
  #include <stdio.h>
  int main() {
      ...
      return 0;    /* 0 = success to the OS */
  }
  ```

## 2. Pre-processor (runs before compilation, W8)

- Directives start with `#`.
- `#include <stdio.h>` - include a header (search system dirs). `#include "myfile.h"` -
  search the current directory first.
- `#define NAME value` - symbolic constant; all later uses of `NAME` are replaced:
  ```c
  #define MAX_NUMBER 1000
  ```
- Header files can hold prototypes, constants, and type definitions.

## 3. printf / scanf cheat sheet (W8)

- `printf(format, args...)`; specifiers: `%d` int, `%ld` long, `%f` float, `%lf` double,
  `%c` char, `%s` string, `%x` hex, `%p` pointer.
- Field width / flags / precision: `conversion-spec = <flags><width><.precision><char>`.
  `%4d` right-justify in 4; `%-4d` left-justify; `%04d` zero-pad; `%.3f` 3 decimals.
- Escapes: `\n` newline, `\t` tab, `\\` backslash, `\"` quote, `\'` quote.
- `scanf(format, &var, ...)` reads from keyboard. **You must pass addresses** with `&`,
  except for arrays/strings which already decay to a pointer:
  ```c
  int a; double b; char s[20];
  scanf("%d%lf%19s", &a, &b, s);
  ```
- Unsafe: `scanf("%s", ...)` can overflow the buffer. Use width limits (`%19s`) or
  `fgets`.

## 4. Data types and expressions (W8)

- Basic types: `int`, `float`, `double`, `char`, `long`. `sizeof(int)` is 2, 4, or 8 bytes
  depending on platform.
- Operators: `+ - * / % ()`.
- Result type follows the operands: `int op int -> int`, `int op float -> float`,
  `double op float -> double`, `char op int -> char`. Watch integer division: `7/2 == 3`.
- Casts: `(int)4.5` -> 4 (truncates); `(float)4` -> 4.0.

## 5. Control structures (W8)

- `if/else`, `switch` (not in Python), `for`, `while`, `do-while` (not in Python; body
  runs at least once).
- `switch` needs `break` after each case.

## 6. Functions (W9)

- Definition: `return_type name(param_list) { body }`; each parameter needs a type.
- Prototype: `int addition(int, int);` - declare at top (or in a header) so the compiler
  can validate calls.
- **Call-by-value**: copies are passed; modifying the parameter does not change the caller's
  variable. All calls are by value by default.
- Call-by-reference is simulated with pointers (W10).
- Multiple `return` statements: function exits at the first executed `return`.
- Standard library: `<ctype.h>` (isalpha/isdigit), `<math.h>` (sqrt/log), `<stdlib.h>`
  (malloc/free/rand/atoi), `<stdio.h>`, `<string.h>`.

## 7. Scope (W9)

- **File scope** (outside all functions) = global variable; visible to end of file.
- **Block scope** (inside `{ }`) = local variable.
- Keep scope as small as possible; same name can be reused in different scopes.

## 8. Arrays (W9)

- Same name, same type, contiguous memory. Index from **0** to `size - 1`.
- Define: `int studentId[10];`; initialise: loop, or list
  `int b[5] = {1, 2, 3, 4, 5};` or `int b[] = {...};`.
- 2-D: `int b[2][3] = { {1,2,3}, {4,5,6} };` (rows grouped).
- No bounds checking in C - reading/writing past the end is undefined behaviour.

## 9. Strings (W9)

- A string is a `char` array ending in `'\0'`. `char colour[] = "blue";` -> 5 elements
  (`b l u e \0`). Always leave room for the null terminator.
- String I/O: `fgets(buf, n, stdin)` (safe line read; keeps/strips newline), `puts`,
  `getchar`, `putchar`, `sprintf` (-> use `snprintf`), `sscanf`.
- `<string.h>`: `strlen`, `strcpy` (-> prefer `strncpy`), `strcmp`/`strncmp`
  (**case-sensitive**; returns <0, 0, >0), `strchr`, `strncpy`.
- `<ctype.h>`: `isalpha`, `isdigit`, `isspace`, `tolower`, `toupper`. Pass arguments as
  `(unsigned char)`.
- Conversion: `atoi`, `atol`, `atof` from `<stdlib.h>`.

## Common exam traps (W8-9)

1. `&` in `scanf` is the address operator - forgetting it is a crash/garbage bug.
2. Off-by-one in arrays/strings: index 0..size-1, and room for `'\0'`.
3. Integer division truncation when you expect a float.
4. `strcmp` returns an int; `==` on strings compares pointers, not contents.
5. `switch` missing `break` falls through.
