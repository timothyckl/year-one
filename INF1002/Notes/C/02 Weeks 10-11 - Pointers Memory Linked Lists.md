# Revision Notes - Weeks 10-11: Pointers, Dynamic Memory, Linked Lists

See `../../Materials/02 C/01 Lectures/` for the full lecture notes.
Use with: `../../Materials/02 C/03 Reference Guides/03 Pointers and Memory Guide.md`.

---

## 1. Pointer fundamentals (W10)

- A pointer is a variable whose **value is a memory address**.
- `int *ptr;` reads "ptr is a pointer to int". The `*` does NOT distribute:
  `int *a, b;` = pointer + int.
- Address-of `&` returns an address; dereference `*` reads/writes the pointed-to value:
  ```c
  int y = 5;
  int *yPtr = &y;
  printf("%d", *yPtr);   /* 5 */
  *yPtr = 10;            /* y is now 10 */
  ```
- D-I-D recipe: **D**eclare -> **I**nitialise -> **D**ereference.
- **Never dereference an uninitialised or unassigned pointer** - fatal error or silent
  data corruption.

## 2. Pointers and arrays (W10)

- An array name is a **constant pointer to the first element**.
- Interchangeable:
  ```c
  char b[] = {'a','b','c','d','e'};
  char *bPtr = b;
  bPtr[3] == *(bPtr + 3) == *(b + 3)   /* all 'd' */
  ```
- Passing an array to a function passes it **by reference** (the called function can modify
  the caller's array). Size must be passed separately.
- Guard with `const`: `void f(const int b[], int size)` makes modifications a compile error.

## 3. Pointer arithmetic (W10)

- Adding/subtracting an integer scales by `sizeof(*ptr)`:
  `vPtr += 2` on `int*` advances `2 * sizeof(int)` bytes.
- So `arr[i]` == `*(arr + i)`; `p++` moves one element, not one byte.
- Pointer assignment requires **same type**: `anotherPtr = ptr;`.
- `sizeof` operator: `sizeof(int)` bytes for a type, `sizeof arr / sizeof arr[0]` for an
  element count.

## 4. Call-by-reference (W10)

- Simulate it by passing addresses:
  ```c
  void cube(int *ptr) { *ptr = (*ptr)*(*ptr)*(*ptr); }
  int n = 5;
  cube(&n);            /* n becomes 125 */
  ```

## 5. Pointers to pointers and arrays of pointers (W10)

- `int **ptrToPtr = &ptr;` - pointer to a pointer; double dereference `**ptrToPtr`.
- `char *suit[4] = { "Hearts", ... };` - array of pointers (to string literals). Useful for
  arrays of strings.

## 6. Void pointers (W11)

- `void *` can point to anything; **all** pointers convert to it.
- **Cannot be dereferenced** - must cast first:
  ```c
  float *f2 = (float *)fPtr;
  ```

## 7. Dynamic memory allocation (W11)

- Three steps: `#include <stdlib.h>`, allocate with `malloc`/`calloc`, free with `free`.
- `malloc(n_bytes)` - uninitialised block; `calloc(n, size)` - zeroed block.
- Both return `void *`; cast to the target type; result used like an array.
- **Always check for `NULL`** (out of memory).
- **Every `malloc`/`calloc` must eventually have a matching `free`**; leaks grow until the
  program crashes.

```c
int *grades = (int *)malloc(num_students * sizeof(int));
if (grades == NULL) { /* report and handle */ }
...
free(grades);
```

For an array of strings: allocate `char **` (array of pointers), then a block per string,
then `free` each string and finally the array.

## 8. Structures and typedef (W11)

- `struct` groups fields; declared/initialised like arrays:
  ```c
  struct student { char name[20]; int roll; int age; char class[12]; };
  struct student stud1 = { "Sachin Kumar", 101, 16, "INF1002" };
  ```
- Access: `.` for struct values, `->` for pointers to structs (`s->roll` == `(*s).roll`).
- `typedef` alias: `typedef struct student Student;` or the anonymous-struct form:
  ```c
  typedef struct { ... } Student;
  ```
- `sizeof(Student)` works on user-defined types (includes padding).

## 9. Linked lists (W11)

- A linear collection of **self-referential nodes** linked by pointers; last `next` = NULL;
  accessed via `head`.
  ```c
  typedef struct node_struct {
      int data;
      struct node_struct *next;
  } Node;
  ```
- Link two nodes:
  ```c
  Node node1 = { 15, NULL }, node2 = { 10, NULL };
  node1.next = &node2;
  ```
- **Search/traverse**: `temp = temp->next;` until target or NULL.
- **Insert at head** (with malloc'd node):
  ```c
  new_node->next = head;
  head = new_node;
  ```
- **Delete a node**: unlink with the predecessor, then `free`:
  ```c
  prev->next = node_ptr->next;
  free(node_ptr);
  ```
- Arrays need contiguous memory; linked lists use scattered nodes linked together - at the
  cost of an extra pointer per node and no random access.

## Common exam traps (W10-11)

1. Dereferencing NULL/uninitialised pointers.
2. `free` twice, or using a pointer after `free` (dangling pointer).
3. Forgetting `free` (leak) - especially in a loop.
4. Confusing `&ptr` (address of the pointer) with `ptr` (the stored address) and `*ptr`
   (the pointed-to value).
5. `*` does not distribute in declarations.
6. Off-by-one when walking a list: stop at `->next == NULL` before dereferencing `next`.
7. Casting `void *` result of `malloc` - required by this course's style (and by C++), not
   required by ISO C.
