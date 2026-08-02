# Lecture 11 - Dynamic Memory Allocation and Linked Lists

**Slides:** 57 content slides

---

## Overview

Week 11. Recaps pointer usage (D-I-D: Declare, Initialise, Dereference), then covers void
pointers, dynamic memory allocation (`malloc`/`calloc`/`free`), user-defined data types
(`struct`, `typedef`, `sizeof`), and linked lists (self-referential structures, search,
insert, delete).

---

## 0. Pointer Recap

- Pointers are variables whose values are memory addresses:
  ```c
  int count = 100;
  int* ptr = &count;
  ```
- How to use a pointer (D-I-D):
  - **D**eclare: `int variable; int *ptr;`
  - **I**nitialise: `int variable = 10; ptr = &variable;`
  - **D**ereference: `*ptr = 20;` (update the pointed-to value), `int a = *ptr;` (read it).
- Pointers and arrays (recap): array name is a constant pointer to the start; `bPtr[3]`
  equals `*(bPtr + 3)` equals `*(b + 3)`.
- Pointer to pointer: `int **ptrToPtr = &ptr;` (useful for arrays of pointers/strings).

---

## 1. Void Pointers

- **All pointers can be assigned to a `void *`**; a `void *` can point to a variable of any
  type:

  ```c
  int x;
  void *xPtr = &x;
  printf("xPtr: %p\n", xPtr);

  float f;
  void *fPtr = &f;
  printf("fPtr: %p\n", fPtr);
  ```

- **A pointer to void cannot be dereferenced directly** - the compiler reports
  `illegal indirection` (e.g. MSVC `void_pointers.c(16): error C2100`).
- Correct usage: cast before dereferencing:

  ```c
  float f = 123.45;
  /* incorrect */
  void *fPtr = &f;
  printf("*fPtr: %f\n", *fPtr);        /* error C2100: illegal indirection */

  /* correct */
  float *fPtr2 = (float *)fPtr;
  printf("*fPtr2: %0.2f\n", *fPtr2);
  ```

---

## 2. Dynamic Memory Allocation

### Why dynamic allocation?

- A fixed array requires the size (`#define NUM_STUDENTS 10`) to be known at compile time.
  What if you don't know how many students there are in advance? -> dynamic allocation.

### Three steps

1. `#include <stdlib.h>`
2. Use `malloc` or `calloc` to request memory.
3. Use `free` to return memory when no longer needed.

### malloc and calloc

- `malloc`: allocates a block of memory of a given number of bytes.
  `int *ptr = (int *)malloc(sizeof(int) * N);`
- `calloc`: allocates space for N elements and zeroes them.
  `int *ptr = (int *)calloc(N, sizeof(int));`
- Both return a `void *` to the start of the allocated memory; it must be explicitly cast
  to the appropriate type before use; the result is often used like an array.
- **If not enough memory is available, the pointer has the special value `NULL`.**

### free

- De-allocates memory previously allocated by `malloc`/`calloc`, allowing reuse.
- All memory allocated should eventually be `free`d; failure to do so is a **memory leak**;
  a leaking program uses more and more memory over time and eventually crashes.

### Full example

```c
#include <stdio.h>
#include <stdlib.h>

int main() {

    int *grades;
    int num_students;
    /* ask how many grades need to be stored */
    printf("How many students are in your class? ");
    scanf("%d", &num_students);

    /* allocate enough space to hold num_students integers */
    grades = (int *)malloc(num_students * sizeof(int));
    if (grades == NULL) {
        printf("Out of memory.");
        return 1;
    }

    /* read the grades */
    for (int i = 0; i < num_students; i++) {
        printf("Grade for student %d: ", i + 1);
        scanf("%d", &grades[i]);
    }

    /* de-allocate memory */
    free(grades);

    return 0;
}
```

### Strings with dynamic allocation

Self-exercise `names_dynamic.c` - allocate an array of pointers, then allocate space for
each string according to its length; free each string, then free the array:

```c
/* allocate enough space to hold num_students strings */
char **names = (char **)malloc(num_students * sizeof(char *));
if (names == NULL) { printf("Out of memory."); return 1; }
for (i = 0; i < num_students; i++) {
    /* read the name */
    printf("Name of student %d: ", i + 1);
    fgets(buf, MAX_NAME, stdin);

    /* copy the name into the array */
    int length = strchr(buf, '\n') - buf;
    names[i] = (char *)calloc(length + 1, sizeof(char));
    if (names[i] == NULL) { printf("Out of memory."); return 1; }
    strncpy(names[i], buf, length);
}

/* de-allocate memory: free each string, then the array */
for (i = 0; i < num_students; i++)
    free(names[i]);
free(names);
```

> Note: `strchr(buf, '\n') - buf` returns -1 if no newline is present (e.g. last line
> without trailing newline), and then `calloc(0 + 1, ...)` allocates 1 byte. Robust code
> checks `strchr(...) != NULL` first. Also note this pattern assumes `MAX_NAME` is large
> enough that `fgets` reads the whole line including the `'\n'`.

---

## 3. User-Defined Data Types

### Structures

- A `struct` groups related data, e.g. student info {Name, Roll, Age, Class}.
- Definition:

  ```c
  struct student {
     char name[20];
     int roll;
     int age;
     char class[12];
  };
  ```

- Declaring variables - Option 1 (separate declaration):
  ```c
  struct student student_1;
  ```
- Option 2 (variables after the closing brace):
  ```c
  } student_2, student_3;
  ```
- Use with the **dot operator** for struct values:

  ```c
  #include <stdio.h>

  struct student {
           char name[20];
           int roll;
           int age;
           char class[12];
  };

  int main() {
           struct student stud1 = { "Sachin Kumar", 101, 16, "INF1002" };
           printf("\n Name : %s", stud1.name);
           printf("\n Roll : %d", stud1.roll);
           printf("\n Age : %d", stud1.age);
           printf("\n Class: %s", stud1.class);
           return 0;
  }
  ```

  Output: Name : Sachin Kumar / Roll : 101 / Age : 16 / Class: INF1002.

### Passing structures by reference

- Use the **arrow operator `->`** to dereference a pointer to a struct:

  ```c
  #include <stdio.h>

  void print_student(Student *s);

  int main() {
       Student stud1 = { "Sachin Kumar", 101, 16, "INF1002" };
       print_student(&stud1);
       return 0;
  }

  void print_student(Student *s) {
       printf("\n Name : %s", s->name);
       printf("\n Roll : %d", s->roll);
       printf("\n Age : %d", s->age);
       printf("\n Class: %s", s->class);
  }
  ```

  > Note: this example uses `Student` without first defining it
  > (`typedef struct student Student;` is missing). The code will not compile as-is;
  > a `typedef` makes it work.

### typedef

- Motivation: writing `struct student stud1 = {...}` repeatedly is verbose.
- `typedef <type> <new_type>` creates an alias:

  ```c
  typedef float salary;
  salary wages_of_month;    /* wages_of_month is a float */
  ```

  ```c
  struct student {
     char name[20];
     int roll;
     int age;
     char class[12];
  };
  typedef (struct student) Student;
  Student student_1 = { "Sachin Kumar", 101, 16, "INF1002" };
  ```

  Even more concise - tag the anonymous struct:

  ```c
  typedef struct {
      char name[20];
      int roll;
      int age;
      char class[12];
  } Student;
  ```

### sizeof with user-defined types

```c
typedef struct {
     int id;
     char name[25];
} Student;

printf("The size of a Student record is: %d bytes\n", sizeof(Student));
```

---

## 4. Linked Lists

### Why linked lists?

- Arrays need contiguous memory. A linked list links data at different memory locations to
  optimise memory use.
- Life example: the professor left a bottle; students A..E each know the next
  student's address; knowing only A's address lets you recover the bottle by following
  links.

### Definition

- A linked list is a linear collection of **self-referential structures (nodes)**
  connected by **pointers (links)**:

  ```
  node   node   node   node
  [12] ->[13] ->[5] ->[7] -> X(NULL)
  ```

### Self-referential structure

```c
typedef struct node_struct {
    int data;
    struct node_struct *next;
} Node;
```

- Contains a pointer member that points to a structure of the same type.
- Self-referential structures can form linked lists, queues, stacks, trees.

### Creating two nodes and linking them

```c
int main() {

    Node node1 = { 15, NULL };
    Node node2 = { 10, NULL };
    node1.next = &node2;

    printf("node1.data = %d\n", node1.data);
    printf("node1.next = %p\n", node1.next);

    Node *node_ptr = &node1;
    printf("node1.data = %d\n", node_ptr->data);
    node_ptr = node_ptr->next;
    printf("node2.data = %d\n", node_ptr->data);
}
```

- Use the **dot `.`** operator for non-pointer (struct value) access.
- Use the **arrow `->`** operator for pointer access (de-reference).
- By convention the **last node's `next` points to `NULL`**.
- The list is accessed via a pointer to the first node, called **head**.

### Operations: search / update

- Move a temp pointer through the list with `temp = temp->next;` until the target is found.

### Insert at the beginning

```c
Node *new_node = (Node *)malloc(sizeof(Node));
new_node->data = 100;
new_node->next = NULL;

/* 1. link the new node to the old head */
new_node->next = head;
/* 2. move head to the new node */
head = new_node;
```

### Delete a node

```c
prev_ptr->next = node_ptr->next;   /* unlink */
free(node_ptr);                    /* don't forget to free malloc'd memory */
```

Helper to find the predecessor:

```c
Node* Find_Pre_Node() {
    Node *temp = head;
    while (temp->next != NULL) {
        if (temp->next == node_ptr) {
            return temp;
        }
        temp = temp->next;
    }
}
```

> Note: `Find_Pre_Node()` references globals `head`/`node_ptr` and has no
> declared parameters and no `return` on the fall-through path (it is a sketch, not
> production code). A production version would take the list head and the target node as
> arguments and return `NULL` when no predecessor exists.

---

## End-of-Week Checklist

Dynamic memory allocation, the `sizeof` operator, `malloc()` and `free()`, self-
referential structures, user-defined data types, dot operators, linked lists, linked lists
vs arrays, searching & updating lists, inserting into linked lists, deleting from linked
lists.

---

## Key Takeaways

1. `malloc`/`calloc` return `void *` (cast it); check for `NULL`; always `free` (memory
   leaks crash long-running programs).
2. `struct` groups data; `typedef` gives convenient names; `.` for values, `->` for
   pointers.
3. Linked lists chain `Node` structs via `next` pointers; insert at head is 2 pointer
   ops; delete requires a predecessor pointer plus `free`.
4. Never dereference a `void *` without casting.
