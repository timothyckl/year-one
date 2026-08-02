# Lecture 10 - Pointers

**Slides:** 63 content slides

---

## Overview

Week 10. Recaps functions/arrays/strings, then covers pointers: pointer variables,
definition/initialisation/dereferencing, pointers and arrays, pointer arithmetic,
`sizeof`, pointers to pointers, arrays of pointers, call-by-reference, passing arrays to
functions, and `const` on array parameters.

---

## 1. Variables and Memory Recap

- Every variable has a name, type, and value; you declare, initialise, then use it.
- `int numberOfStudents_INF1002 = 600;` is really: (1) declare `int numberOfStudents_INF1002;`
  (a random value initially), then (2) assign 600.
- Declaring a variable automatically allocates memory; assigning updates that memory.
- `printf("Memory starting address: %p", &numberOfStudents_INF1002);` prints e.g.
  `0x0060FF05`; `sizeof(numberOfStudents_INF1002)` prints 4 (an `int` on that platform).
- Copying copies values: `numberOfStudents_ICT2107 = numberOfStudents_INF1002;` makes both
  600; the assignment does not relink memory.
- The **address-of operator `&`** returns the address of its operand.

---

## 2. Pointer Variables

**Pointers are variables whose values are memory addresses.**

```c
int count = 100;
int* ptr = &count;
```

Memory picture:

```
  0xF200  | 100    |  count : int
  0xF300  | 0xF200 |  ptr   : int*
```

### Definition

- `int *ptr;` reads as "ptr is a pointer to an int"; the `*` marks a pointer declaration.
- **The `*` does NOT distribute** across names: `int *ptr1, *ptr2;` declares two pointers,
  but `int *a, b;` declares a pointer and a plain int. Prefix each pointer name with `*`.

### Initialisation

- `int *countPtr = &count;` - countPtr stores the address of count, then *indirectly*
  references count's value.
- Demo:

  ```c
  #include <stdio.h>

  int main() {
      int y = 5;
      int *yPtr;

      yPtr = &y;

      printf("Address of y: %p\n", &y);      // y address
      printf("Value of yPtr: %p\n", yPtr);   // yPtr's stored address
      printf("Address of yPtr: %p\n", &yPtr);
      printf("Value to which yPtr points: %d\n", *yPtr);
      return 0;
  }
  ```

- Question: why not initialise a pointer with a direct value like
  `yPtr = 0060FF0C;`? Because the address must refer to a real object; hard-coded
  addresses are not portable and are almost always a bug.

### Dereferencing

- Declare: `int count; int *ptr;`
- Initialise: `int count = 100; ptr = &count;`
- Use (dereference - retrieve/update the pointed-to value):
  ```c
  int a = *ptr;    // read: a = 100
  *ptr = 200;      // write: count becomes 200
  ```
- Pointer operators:
  - `&` address-of.
  - `*` dereference: returns the value of the object the pointer points to.
  - `*ptr = 10;` updates the object.
- **Unsafe pattern:** *"Dereferencing a pointer which has
  not been properly initialised or that has not been assigned to point to a specific
  location in memory is an error. This could cause a fatal run time error, or it could
  accidentally modify important data and allow the program to run to completion with
  incorrect results."*

### Exercise

```c
int a = 5, b = 2;
int *p = &a, *q = &b;

(*p) *= 2;        // a = 10
*q = *p - 1;      // b = 9
p = &b;           // p now points at b
b = *p + 3;       // b = 12
```

Walk through: `(*p) *= 2` doubles a (10). `*q = *p - 1` writes 9 into b. `p = &b` retargets
p. `b = *p + 3` => b = 9 + 3 = 12. Final: a = 10, b = 12.

---

## 3. Pointers and Arrays

- Pointers and arrays are intimately related:
  - **An array name can be thought of as a constant pointer to the start (first element)
    of the array.**
  - Array subscripts can be applied to pointers: `p[1]` works on a pointer `p`.
  - Pointer arithmetic can navigate arrays.
- The name of an array evaluates to the address of its first element:

  ```c
  int main() {
      char charArray[] = {'a', 'b', 'c', 'd', 'e' };
      printf("charArray: \t%p\n", charArray);
      printf("&chararray[0]: \t%p\n", &(charArray[0]));
      printf("&charArray: \t%p\n", &charArray);
      return 0;
  }
  ```

- Subscripting and pointer arithmetic are interchangeable:

  ```c
  int main() {
      char b[] = {'a', 'b', 'c', 'd', 'e' };  // ptr to start
      char *bPtr = b;                          // points to start of array b
      printf("*(bPtr + 3): \t%c\n", *(bPtr + 3));  // start + 3
      printf("*(b + 3): \t%c\n", *(b + 3));        // start + 3
      printf("bPtr[3]: \t%c\n", bPtr[3]);          // 3 subscript like array
      return 0;
  }
  ```

  Output: `*(bPtr + 3): d`, `*(b + 3): d`, `bPtr[3]: d`.

- The fourth element can be referenced as `*(bPtr + 3)`, `*(b + 3)`, or `bPtr[3]`.

### Exercise

```c
int a[] = { 1, -1, 4, 5, 4, -3 };
int *p = a + 5;

*p = -(*p);        // a[5] = 3
p -= 2;            // p -> a[3]
*p = *p + 1;       // a[3] = 6
*(p + 1) = *p * 2; // a[4] = 12
```

Final array: `{ 1, -1, 4, 6, 12, 3 }`.

---

## 4. Pointer Expressions and Arithmetic

### The `sizeof` operator

- `sizeof` returns the number of bytes needed to hold a type.
  - `sizeof(char)` is 1; `sizeof(int)` is 2, 4 or 8 depending on the compiler's word size.
- `int size = sizeof(int) * 4;` -> "size of 4 integers is: 16 bytes" on a 4-byte-int
  platform.

### Valid pointer operands

- Pointers are valid in assignment, arithmetic, and comparison expressions, but not all
  operators normally used there are valid with pointers.

### Pointer assignment

- A pointer can be assigned to another pointer of the **same type**; `anotherPtr = ptr;`
  makes anotherPtr point wherever ptr points.

### Pointer arithmetic scales by the pointed-to type

```c
int v[5] = {0};
int *vPtr = v;      // 0x3000
vPtr += 2;          // 0x3008, i.e. 0x3000 + 2 * sizeof(int)
```

- "When an integer is added or subtracted from a pointer, the pointer is incremented or
  decremented by that integer times the size of the object to which the pointer refers."
- `vPtr -= 4;` back to 0x3000; `vPtr++;` moves one element (0x3004), not one byte.
- A common "Confused? This bytes!" trap: `3000 + 2 = 3002` in
  conventional arithmetic does **not** apply to pointers - it is `3000 + 2*4 = 3008`.

---

## 5. Pointers to Pointers

```c
int n = 5;
int *ptr = &n;
int **ptrToPtr = &ptr;
```

- `(int *)` : pointer to an integer; `(int *)*` : pointer to a pointer to an integer.
- Demo:

  ```c
  #include <stdio.h>

  int main() {

      int n = 5;                          // value 5 at some address
      int *ptr = &n;                      // address of n
      int **ptrToPtr = &ptr;              // address of ptr

      printf("&n = %p\n", &n);
      printf("ptr = %p\n", ptr);
      printf("&ptr = %p\n", &ptr);
      printf("ptrToPtr = %p\n", ptrToPtr);

      printf("*ptr = %d\n", *ptr);        // 5
      printf("*ptrToPtr = %p\n", *ptrToPtr);  // address of ptr (= value of ptrToPtr deref once)
      printf("ptr = %p\n", ptr);
      printf("**ptrToPtr = %d\n", **ptrToPtr);  // 5 - double dereference

      return 0;
  }
  ```

- Uses: arrays of pointers, arrays of strings.

---

## 6. Arrays of Pointers

```c
char * suit[4] = { "Hearts", "Diamonds", "Clubs", "Spades" };
```

Each element is of type "pointer to char" (an array of 4 pointers). Full card-deck example:

```c
#include <stdio.h>

int main() {
    char *suit[4] = { "Hearts", "Diamonds", "Clubs", "Spades" };
    char *face[13] = {
         "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10",
         "Jack", "Queen", "King"
    };

    for (int i = 0; i < 4; i++) {
         char *card_suit = suit[i];
         for (int j = 0; j < 13; j++) {
               printf("%s of %s\n", face[j], card_suit);
               // note: %s needs the address; *face[j] would only print one char
         }
    }
    return 0;
}
```

---

## 7. Call-by-Reference

### Recall call-by-value

- A copy is passed; changes don't affect the caller; all calls are by value by default.

### Simulating call-by-reference with a pointer

```c
#include <stdio.h>

/* cube a number in-place */
void cubeByReference(int *);

int main() {
    int number = 5;
    cubeByReference(&number);        /* pass the address */
    printf("number = %d\n", number);
    return 0;
}

void cubeByReference(int *ptr) {
    *ptr = (*ptr) * (*ptr) * (*ptr);
}
```

Output: `number = 125`.

### Passing arrays to functions

- Call with the array name without brackets: `modifyArray(a, 5);`
- Parameter: `void modifyArray(int b[], int size)` - size between `[]` is not required.
- **C automatically passes arrays by reference** (the array name decays to a pointer), so
  the function can modify the caller's elements.

```c
int main() {
    int a[5] = {0, 1, 2, 3, 4};
    printArray(a, 5);
    modifyArray(a, 5);
    printArray(a, 5);
    return 0;
}

void modifyArray(int b[], int size) {
    int j;
    for (j = 0; j < size; j++)
        b[j] *= 2;
}

void printArray(int b[], int size) {
    int j;
    printf("[Array] = ");
    for (j = 0; j < size; j++)
        printf("%d ", b[j]);
    printf("\n");
}
```

Output:

```
[Array] = 0 1 2 3 4
[Array] = 0 2 4 6 8
```

### Using `const` to prevent modification

- If you do not want a function to change an array, use `const int b[]`:

  ```c
  void tryToModifyArray(const int b[], int size) {
      int j;
      for (j = 0; j < size; j++)
          b[j] *= 2;   /* compile-time error: l-value specifies const object */
  }
  ```

  The compiler rejects this with an error such as: `const_array.c(28): error C2166: l-value specifies
  const object`. Elements become constant in the function body; any modification is a
  compile-time error.

---

## End-of-Week 10 Checklist

Pointer declarations, address operator, pointer dereferencing, pointer assignment, void
pointers (listed but not detailed here), pointers to pointers, pointer arithmetic, arrays
and pointers, arrays of pointers, call by reference, passing arrays to functions, using
`const`.

---

## Key Takeaways

1. A pointer stores an address; `*` dereferences, `&` takes an address.
2. Array names are constant pointers to the first element; `arr[i] == *(arr + i)`.
3. Pointer arithmetic is scaled by `sizeof(type)`.
4. Simulate call-by-reference by passing `&var` and taking an `int *`/array parameter.
5. Arrays always pass by reference; guard against mutation with `const`.
6. Never dereference an uninitialised/unassigned pointer.
