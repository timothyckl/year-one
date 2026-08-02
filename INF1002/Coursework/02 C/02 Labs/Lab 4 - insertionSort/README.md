# C Lab 4 - insertionSort

## Objectives

1. To understand and use dynamic memory allocation and linked lists.

---

## Lab Requirements

**In-lab exercises:**
- Exercise 1: use `typedef` (with macros and `struct` where appropriate) to create:
  - `INTL_MONEY_VALUE` - a floating-point money value plus a 3-char currency string
    (e.g. "SGD").
  - `INTL_MONEY_VALUE_PTR` - a pointer to the type above.
  - Then declare a few variables of each type and confirm it compiles.
- Exercise 2: build a linked list of students in `main()`:
  1. Declare `head` pointing to the start of the list; initial value `NULL`.
  2. Create a node {surname "Adams", grade 85.0} and place it at the start.
  3. Create {surname "Pritchard", grade 66.5} and append it at the end.
  4. Create {surname "Jones", grade 91.5} and insert it between Adams and Pritchard so the
     list is alphabetical. (Start from the type declarations given.)

**Graded assignment (Gradescope, 5 test cases, max 5 marks):**
- Write `insertionSort.c`: sort words with a **linked-list insertion sort**.
  - Each node stores one word (<= 32 chars), lower-case letters plus apostrophes and
    hyphens; no spaces/quotes/other chars.
  - Repeatedly ask "Please enter a word:"; convert upper-case to lower-case; reject words
    with invalid characters with "Invalid word." (do not insert them).
  - Insert each valid word into its correct alphabetical position using `strcmp()`
    (hyphens/apostrophes sort by ASCII value).
  - Stop when the user enters `***`.
  - Print "All the entered words in order:" then the words, one per line.
  - Finally, de-allocate ALL allocated memory.
  - Check for allocation failures, report an error, but continue executing.

---

## Supplied Implementation (`insertionSort.c`)

**The supplied file is an unimplemented skeleton.** It contains the task description
comment block and a bare `main()`:

```c
#include <stdio.h>

int main()
{
    /* code here */
    return 0;
}
```

There is **no** node struct, no memory allocation, no linked-list logic. The entire graded
task must be written by you. (Compiles cleanly as-is with `gcc -Wall -Wextra`, but does
nothing.)

---

## What You Need to Implement

1. **Node type** (self-referential structure, per Lecture 11):
   ```c
   typedef struct node_struct {
       char word[33];                 /* up to 32 chars + '\0' */
       struct node_struct *next;
   } Node;
   ```
2. **Read input** with `fgets` (or `scanf("%32s", ...)`), strip the newline, convert
   upper-case to lower-case.
3. **Validate**: only `a-z`, `'`, and `-` are allowed; otherwise print "Invalid word." and
   re-prompt. (Watch Example 2: "invalid word" contains a space -> Invalid
   word.; `"quote"` contains quotes -> Invalid word.; `good-bye` and `it's` are valid.)
4. **Insert sorted**: walk the list comparing with `strcmp(node->word, new_word)`; insert
   before the first node that sorts after the new word. Handle the empty-list and
   insert-at-front cases.
5. **Allocation failures**: `malloc` returning `NULL` should print an error and the
   program should continue (i.e. skip that word) - as required.
6. **Termination**: input `***` ends the loop.
7. **Print** the sorted list one word per line.
8. **Free everything**: walk the list with a temporary pointer, `free` each node. No leaks.

---

## Expected Behaviour

Example 1 transcript:

```
Please enter a word:
cat
Please enter a word:
dog
Please enter a word:
monkey
Please enter a word:
elephant
Please enter a word:
***
All the entered words in order:
cat
dog
elephant
monkey
```

Example 2 (invalid inputs are rejected and not inserted; output remains sorted):

```
...
invalid word
Invalid word.
...
"quote"
Invalid word.
...
All the entered words in order:
another
good-bye
hello
it's
valid
```

---

## Pitfalls

- **Exact prompt and output**: "Please enter a word:" (no space after the colon),
  "Invalid word." (with the period), "All the entered words in order:".
- **`***` must not be inserted** and must not be printed.
- **Insertion must keep the list sorted at all times** (that is the whole point of
  insertion sort).
- **`strcmp` return value semantics**: `< 0` means the first argument sorts before the
  second.
- **Memory**: every successfully allocated node must eventually be `free`d; forgetting the
  de-allocation step loses marks and is a memory leak.
- **Length**: words up to 32 chars need a 33-byte buffer (`+1` for the null).

---

## Safety / Correctness Notes

- **`malloc` failure handling**: check the return value of every `malloc`; on `NULL`,
  print an error and continue (explicitly required) rather than `exit`.
- **Unbounded input**: prefer `fgets` with a fixed buffer (33+ bytes) or `scanf("%32s", ...)`
  so a long word cannot overflow the array. A bare `scanf("%s", ...)` into a 32-byte array
  is a classic buffer overflow.
- **`fgets` newline**: strip the trailing `'\n'` before validation or the last letter will
  fail validation.
- **Insert before first, in middle, or at end**: three distinct cases; get the "insert at
  head" and "empty list" cases right or the list logic breaks.
- **Free order**: free from the head forward using a `next` saved *before* freeing the
  current node (do not read `->next` after `free`).
- The grading script may feed several words then `***`; make sure your program consumes
  exactly one line per prompt.

---

## Lessons

- Linked lists give dynamic sizing (vs fixed arrays) and this lab is the first place you
  must write the whole thing yourself.
- Insertion sort on a linked list: O(n^2) comparisons but simple, in-place, and the 
  natural way to keep a sorted list as you add items.
- The discipline of "every malloc has a matching free" is the core of C memory management
  (failure to free is a memory leak; long-running programs crash).
- Validation-first-then-convert keeps the word set canonical (lower-case letters,
  apostrophes, hyphens) so `strcmp` ordering is predictable.
