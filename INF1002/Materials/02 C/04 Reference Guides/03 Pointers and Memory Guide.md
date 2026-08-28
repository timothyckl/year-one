# Reference Guide - Pointers and Memory

See `../01 Lectures/` for the full lecture notes. This is the deeper companion to
`02 Weeks 10-11 - Pointers Memory Linked Lists` in `../03 Revision Notes/`.

---

## 1. The mental model

A variable occupies a memory location with an address. A **pointer variable stores an
address**:

```
addr    contents
0xF200 | 100     <- int count
0xF300 | 0xF200  <- int *ptr = &count
```

- `count` directly references the value 100.
- `ptr` *indirectly* references it: `*ptr` gives 100.

Operators:

| Expression | Meaning |
|------------|---------|
| `&x`       | address of x (address-of) |
| `*p`       | the object p points to (dereference) |
| `p = &x`   | make p point at x |
| `*p = v`   | write v into the pointed-to object |

## 2. Declaring pointers

- `int *ptr;` - "ptr is a pointer to int".
- **`*` binds to the name, not the type.** `int *p, q;` -> p is a pointer, q is an int.
  Each pointer needs its own `*`.
- Pointer types matter: `int *` and `float *` are different types; assignment between
  different pointer types requires a cast (or a `void *`).

## 3. Initialisation and dereferencing (D-I-D)

```c
int variable = 10;
int *ptr;          /* declare */
ptr = &variable;   /* initialise - always point at a real object */
printf("%d", *ptr);/* dereference (read) -> 10 */
*ptr = 20;         /* dereference (write) -> variable is now 20 */
```

**Never dereference an uninitialised pointer** (garbage address): fatal crash or silent
corruption. Never assign a bare numeric address like `ptr = 0x0060FF0C;` - non-portable
and almost always a bug.

## 4. Pointers and arrays

- An array name is a constant pointer to its first element: `arr == &arr[0]`.
- `arr[i]` is exactly `*(arr + i)`. Subscripting works on pointers too: `p[1]` is valid.
- Because of this, **arrays are passed to functions by reference** - the function sees the
  caller's array and can modify it:

```c
void double_all(int b[], int size) {
    for (int i = 0; i < size; i++) b[i] *= 2;
}
```

- To prevent modification, use `const`: `void show(const int b[], int size)`.
- You must pass the array size separately; there is no built-in length.

## 5. Pointer arithmetic

- Adding an integer to a pointer scales by `sizeof(*p)`:

```c
int v[5] = {0};
int *p = v;        /* 0x3000 */
p += 2;            /* 0x3000 + 2 * sizeof(int) = 0x3008 */
p++;               /* one element forward, not one byte */
```

- Only valid within (or one-past) an array object; otherwise undefined behaviour.
- Subtracting two pointers of the same type gives the number of elements between them.

## 6. `sizeof`

- `sizeof(type)` or `sizeof expr` gives the size in bytes.
- `sizeof(char) == 1`; `sizeof(int)` is platform-dependent (commonly 4).
- Number of elements: `sizeof(arr) / sizeof(arr[0])`.
- `sizeof(struct S)` includes padding.

## 7. Pointers to pointers

```c
int n = 5;
int *ptr = &n;
int **ptrToPtr = &ptr;   /* points to the pointer */
```

Dereference chain: `*ptrToPtr` is `ptr` (an `int *`); `**ptrToPtr` is `n` (an int).
Commonly used for arrays of pointers and for functions that need to change a caller's
pointer (e.g. inserting at the head of a linked list).

## 8. Arrays of pointers (arrays of strings)

```c
char *suit[4] = { "Hearts", "Diamonds", "Clubs", "Spades" };
```
Each element is a `char *`. Print with `%s` (the pointer), not `*suit[i]` (a single char).

## 9. void pointers

- `void *` can hold any object pointer type; assigning to/from a typed pointer is allowed.
- **Cannot be dereferenced** without a cast:

```c
float f = 1.5f;
void *p = &f;
/* printf("%f", *p);  ERROR: illegal indirection */
float *fp = (float *)p;   /* cast, then dereference */
printf("%0.2f", *fp);
```

## 10. Dynamic memory (heap)

Three steps: `#include <stdlib.h>`; allocate; free.

```c
#include <stdlib.h>

int *grades = (int *)malloc(num * sizeof(int));   /* uninitialised */
int *zeroed = (int *)calloc(num, sizeof(int));    /* zero-filled */
if (grades == NULL) { /* out of memory - handle it */ }
...
free(grades);
free(zeroed);
```

- `malloc(bytes)` vs `calloc(n, size)` (also zeroes).
- Return type is `void *` - cast to your pointer type (this course's style).
- **Check for `NULL`** immediately.
- **Every allocation needs one `free`** (no double free, no leak).
- After `free`, the pointer is dangling - do not use it.

### Array of strings

```c
char **names = (char **)malloc(n * sizeof(char *));
for (int i = 0; i < n; i++) {
    names[i] = (char *)calloc(len_i + 1, sizeof(char));
    strncpy(names[i], buf, len_i);
}
...
for (int i = 0; i < n; i++) free(names[i]);
free(names);
```
Free inner blocks first, then the outer array.

## 11. Structures, typedef, and member access

```c
typedef struct node_struct {
    int data;
    struct node_struct *next;   /* self-reference needs the tag */
} Node;

Node a = { 15, NULL };          /* value */
Node *p = &a;

a.data   == (*p).data           /* dot on value */
p->data                        /* arrow on pointer (equivalent) */
```

## 12. Linked lists in one page

```c
typedef struct node_struct { int data; struct node_struct *next; } Node;

/* insert at head */
Node *insert_head(Node *head, int v) {
    Node *n = (Node *)malloc(sizeof(Node));
    if (n == NULL) return head;
    n->data = v;
    n->next = head;
    return n;
}

/* print */
void print_list(Node *head) {
    for (Node *t = head; t != NULL; t = t->next)
        printf("%d -> ", t->data);
    puts("NULL");
}

/* free all */
void free_list(Node *head) {
    while (head != NULL) {
        Node *next = head->next;   /* save before freeing */
        free(head);
        head = next;
    }
}
```

Delete: find predecessor, `prev->next = node->next;`, then `free(node);`.

## 13. Safety rules of thumb

1. Initialise pointers to a real object or `NULL`.
2. Check `NULL` before dereferencing (especially after `malloc`).
3. Keep `malloc`/`free` balanced; free exactly once per allocation.
4. Don't index past an array (no bounds checking).
5. Prefer `strncpy`/`snprintf`/`fgets` over `strcpy`/`sprintf`/`gets`.
6. When modifying a caller's pointer, pass `T **` (pointer to pointer).
7. `const` on array/pointer parameters documents intent and catches bugs.
