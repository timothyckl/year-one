# Lecture 9 - Functions, Arrays and Strings in C

**Slides:** 66 content slides

---

## Overview

Week 9. Covers function definitions and prototypes, call-by-value, standard library
functions, scope rules (file vs. block scope), arrays (including multi-dimensional), and
characters/strings (string literals, string I/O, `<string.h>` manipulation functions).

---

## 1. Functions

### Reusability and calls

- Functions promote reusability: the same function can be used repeatedly by many other
  functions (diagram: print_student used by exam dept, lecturer, student management dept).
- A function is invoked by name + arguments, without knowing its implementation.
- Structure: `main()` may call `function_1()`, `function_2()`, ..., which may in turn call
  other functions.

### Function definitions

```
return_value_type function_name(<parameter_list>)
{
   /*definitions*/
   /*statements*/
}
```

- Parameter list: comma-separated list of parameters the function receives; **each
  parameter must be declared with an explicit type**, e.g. `int number, char grade`.

### Function prototypes

- A prototype is a function definition without a body, e.g. `int square(int);`.
- Prototypes are optional but let the compiler validate calls (prevents errors).
- Usually declared at the top of a source file or in a header file.

### Worked example - addition

```c
#include <stdio.h>

/* function prototype */
int addition(int, int);
int main() {
          int first, second, sum;

          /* read input */
          scanf("%d%d", &first, &second);

          /* invoke the addition function */
          sum = addition(first, second);

          printf("%d\n", sum);

          return 0;
}

/* function implementation */
int addition(int a, int b) {
          int result;
          result = a + b;
          return result;
}
```

### Multiple return statements

```c
#include <stdio.h>

#define POSITIVE 1
#define NEGATIVE -1
#define ZERO     0

int get_sign(int n);

int main() {

    int n;
    int sign;

    printf("Type an integer: ");
    scanf("%d", &n);

    sign = get_sign(n);
    if (sign == POSITIVE)
        printf("That is a positive number.\n");
    else if (sign == NEGATIVE)
        printf("That is a negative number.\n");
    else
        printf("That is zero.\n");

    return 0;
}

int get_sign(int n) {
    if (n < 0)
        return NEGATIVE;
    if (n > 0)
        return POSITIVE;
    return ZERO;
}
```

The function stops executing as soon as one `return` is executed.

### Calling functions by value

- **Call-by-value:** a copy of the argument's value is made and passed; changes to the
  copy do not affect the caller's original. By default all calls in C are by value.

```c
#include <stdio.h>

void call_by_value(int);

int main() {

     int a = 10;

     printf("\nBefore call_by_value, a = %d.\n\n", a);

     call_by_value(a);

     printf("After call_by_value, a = %d.\n \n ", a);

     return 0;
}

/* this function will make a copy of a */
void call_by_value(int x) {

     printf("Inside call_by_value, x = %d.\n \n ", x);
     x += 10;
     printf("After adding ten, x = %d.\n \n ", x);
}
```

Output: `a` stays 10; the copy `x` becomes 20. "Value of `a` is not updated!"

### Calling functions by reference

- Call-by-reference lets the called function modify the original value.
- Can be *simulated* in C using the address operator `&` (detailed in Lecture 10).

### Standard C library functions

- `<ctype.h>`: character functions - `isalpha()`, `isdigit()`, etc.
- `<math.h>`: `sqrt()`, `exp()`, `log()`, `sin()`, `cos()`, `tan()`.
- `<stdlib.h>`: `malloc()`, `free()`, `rand()`, `atoi()`.
- `<stdio.h>`: `printf()`, `scanf()`, `fopen()`, `fread()`, `fwrite()`.
- `<string.h>`: `strcpy()`, `strcmp()`.
- Full reference: <http://www.cplusplus.com/reference/clibrary/>

---

## 2. Scope Rules

- The **scope** of an identifier is the portion of the program in which it can be
  referenced. The same identifier can be re-used in different scopes. Tip: keep scope as
  small as possible.

### File scope

- Declared outside any function -> file scope; accessible from declaration to end of file.
- File-scope variables are often called **global variables**.

### Block scope

- Identifiers defined inside a block `{ ... }` have block scope; often called **local
  variables**.

```c
#include <stdio.h>

int i = 1;                       /* i has file scope (global) */

int main() {
    int x = 4;                   /* x has block scope in main */
    printf("add_i outputs %d\n", add_i(x));
    printf("i is %d\n", i);
    printf("x is %d\n", x);
    return 0;
}

int add_i(int n) {
    int x = n + i;               /* x has block scope in add_i */
    i++;                         /* updates the global i */
    return x;
}
```

Output:

```
add_i outputs 5
i is 2
x is 4
```

---

## 3. Arrays

- An array is a group of memory locations with the **same name** and **same type**.
- The number in square brackets is the **subscript/index**; the index starts at **zero**
  and ends at `number of elements - 1`.
- The index can be any integer expression, e.g. `c[a+2] += 2;`.

### Define / initialise / use

```c
#include <stdio.h>

#define MAX_STUDENTS 10

int main() {

    int studentId[MAX_STUDENTS];                     /* define */

    for (int i = 0; i < MAX_STUDENTS; i++)           /* initialise via loop */
        studentId[i] = i + 1;

    printf("%7s%13s\n", "Element", "Value");

    for (int i = 0; i < MAX_STUDENTS; i++)           /* use */
        printf("%7d%13d\n", i, studentId[i]);

    return 0;
}
```

### Defining arrays

Need: the type of elements, the name, the number of elements.

- `int b[100], x[27];`  vs  `int a, y;` (scalar declarations).

### Initialiser list

```c
int studentId[MAX_STUDENTS] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
```

### Multi-dimensional arrays

- Two-dimensional ("double-subscripted") arrays are most common; an array with `m` rows
  and `n` columns is an m-by-n array.

```c
int b[2][3] = { {1, 2, 3}, {4, 5, 6} };
```

- `b` is a 2-by-3 array with rows `{1,2,3}` and `{4,5,6}`. Initialiser values are grouped
  by row.

---

## 4. Characters and Strings

### Fundamentals of characters

- A character constant uses single quotes and has an integer value per the character set:
  `'z'` is 122, `'\n'` is 10 in ASCII.
- Arithmetic/comparison operators work on characters (`+`/`-` move up/down the set;
  `<`, `>`, `==`, `!=` compare by the character set).

### Character handling library `<ctype.h>`

- Functions to classify/convert characters (e.g. `isalpha`, `isdigit`, `tolower`,
  `toupper`).

### Fundamentals of strings

- A string is an **array of characters ending in the null character `'\0'`**.
- String literals are written in double quotes, e.g. `"SIT-DNA"`.

### String initialisation

- `char colour[] = "blue";` creates a 5-element array `b l u e \0`. If no size is given,
  the size is derived from the initialisers (here 5).
- Array must be large enough to hold the string **plus** its terminating null.
- Reading a string with scanf:
  ```c
  char word[10];
  scanf("%9s", word);
  ```
  - `word` is an array (a memory address), so **no `&` is needed**.
  - `scanf` reads until a space, tab, newline, or EOF - so an unbounded `scanf("%s", word)`
    can overflow; as noted: *"It is possible that the user could exceed 9 characters
    and your program might crash."*
  - Good practice: use `%9s` to read at most 9 characters and leave room for the null.

### Standard I/O functions for strings/characters `<stdio.h>`

- `fgets()` reads into a char array until a newline, EOF, or max characters:

  ```c
  #include <stdio.h>
  #include <string.h>

  #define MAX_LENGTH 80

  /* print the characters of a string in reverse order */
  void print_reverse(const char[]);

  int main() {

      char sentence[MAX_LENGTH];
      printf("Enter a line of text:\n");
      fgets(sentence, MAX_LENGTH, stdin);
      printf("The input line written backwards:\n");
      print_reverse(sentence);

      return 0;
  }
  ```

- `puts()` takes a string, prints it and appends a newline. `getchar()` reads one char
  from stdin and returns it as an int.

  ```c
  #include <stdio.h>
  #define MAX_LENGTH 80

  int main() {

      char sentence[MAX_LENGTH];
      char c;
      int index = 0;
      puts("Enter a line of text: ");
      while ((c = getchar()) != '\n' && index < MAX_LENGTH - 1)
          sentence[index++] = c;
      sentence[index] = '\0';
      puts("The input line was: ");
      puts(sentence);

      return 0;
  }
  ```

  > Note on the `getchar()` loop: `getchar()` returns `int`, and comparing the result to
  > `'\n'` misses the EOF case. The loop here would treat EOF as a character. The code is
  > slightly unsafe/incomplete; using `int c;` and also testing
  > `c != EOF` is the more robust form.

- `sprintf()` formats data into a char array using the same specifiers as `printf`:

  ```c
  #include <stdio.h>
  #define MAX_LENGTH 80

  int main() {

      char s[MAX_LENGTH];
      int x;
      double y;

      printf("Enter an integer and a double: ");
      scanf("%d%lf", &x, &y);

      sprintf(s, "integer: %d, double: %f\n", x, y);
      printf("The formatted string stored in the array is: %s", s);

      return 0;
  }
  ```

  > `sprintf` has no size argument; if the formatted text exceeds `MAX_LENGTH - 1` it
  > overflows the buffer. `snprintf(s, MAX_LENGTH, ...)` is the safe alternative.

### String manipulation functions `<string.h>`

- Classic example (username generator): `strncpy`, `strlen`, `rand`/`srand`:

  ```c
  #include <stdio.h>
  #include <stdlib.h>
  #include <string.h>
  #include <time.h>

  #define MAX_FULLNAME 80
  #define MAX_USERNAME 9
  #define RANDOM_DIGITS 3

  int main() {

      char name[MAX_FULLNAME];
      char userID[MAX_USERNAME];
      int n, i;

      printf("Enter your name: ");
      scanf("%79s", name);
      strncpy(userID, name, MAX_USERNAME - RANDOM_DIGITS - 1);
      userID[MAX_USERNAME - RANDOM_DIGITS - 1] = '\0';   /* ensure null termination */

      n = strlen(userID);
      for (i = 0; i < RANDOM_DIGITS; i++)
          userID[n + i] = '0' + rand() % 10;
      userID[n + RANDOM_DIGITS] = '\0';
      printf("Your username is: %s\n", userID);

      return 0;
  }
  ```

  Sample output:
  ```
  Enter your full name: Cristal Ngo Minh Ngoc
  Your username is: Crist610
  Enter your full name: Rachel Green
  Your username is: Rache822
  ```

  > Note: `srand()`/`rand()` appear in the explanation but the code block does not
  > actually call `srand(time(0))`, so `rand()` will produce the same sequence every run
  > (predictable output). That is a genuine defect in the example - a real
  > "random" suffix needs `srand(time(NULL))` once at startup.

- **String comparison:** `strcmp()` / `strncmp()` are **case-sensitive**. Return value is
  negative / zero / positive.

  ```c
  #include <stdio.h>
  #include <string.h>

  int main() {

      char word1[20], word2[20];

      printf("Enter two words, separated by a space: ");
      scanf("%19s%19s", word1, word2);

      int c = strcmp(word1, word2);
      if (c < 0)
          printf("\"%s\" comes first.\n", word1);
      else if (c > 0)
          printf("\"%s\" comes first.\n", word2);
      else
          printf("Those two words are the same.\n");

      return 0;
  }
  ```

- **String conversion functions** `<stdlib.h>`: `atoi()`, `atol()`, `atof()`,
  `strtol()`, etc. Example: `long l = atol("123456789");` converts the string to the long
  integer 123456789.

---

## End-of-Week Checklist

Function prototypes, function definitions, call by value, scope rules, array declarations,
array initialiser lists, multi-dimensional arrays, characters, strings, string
manipulation/comparison/I/O functions.

---

## Administration

- Group project specs uploaded to LMS this week; plagiarism strictly not allowed; do not
  share code/project info on GitHub or elsewhere.
- AI tools may NOT generate project code directly (declaration needed per team); AI not
  allowed in the Test.

---

## Key Takeaways

1. Declare prototypes; by default functions receive **copies** (call-by-value).
2. Arrays index from 0; sizes fixed at declaration.
3. Strings = char arrays + `'\0'`; always leave room for the null terminator.
4. `fgets` + `strncpy`/`strncmp`/`snprintf` are the safer tools; watch for the unsafe
   patterns flagged above.
