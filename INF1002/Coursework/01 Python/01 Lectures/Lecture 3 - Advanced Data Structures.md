# Lecture 3 - Advanced Data Structures

## Outline

1. String (immutable sequence of Unicode characters).
2. List (mutable sequence) - updates, copies.
3. Tuple (immutable sequence).
4. Dictionary (key-value pairs, hash table).
5. List comprehension.
6. Big-O notation intro.

## Strings

- A string is an **immutable** sequence of **Unicode** characters.
- Mutable vs immutable: mutable values can change after creation
  (lists, dictionaries, sets); immutable values cannot (strings, tuples,
  integers).
- Concatenation `+` and repetition `*`:
  - `'Taylor' + 'Swift'` -> `'TaylorSwift'`.
  - `'Taylor' + ' ' + 'Swift'` -> `'Taylor Swift'`.
  - Lab warning (repeated in Lab1): if you concatenate inputs before converting
    with `float()`, e.g. `'3' + '4'` -> `'34'`, then `float('34')` is wrong.
- Sequence & membership: `in` / `not in` test membership.
- Indexing and slicing:
  - First character index 0; last index `len(s)-1`; `s[-1]` is the last char.
  - `s[start:end]` - `end` is exclusive.
  - `s[start:end:step]`.
- Comparison uses ASCII code points: `ord()` returns the Unicode
  code point; e.g. `a` -> 97, `b` -> 98, `A` -> 65, `B` -> 66.
- String built-in methods:
  - `find(str, beg=0, end=len(string))` - index of first occurrence or -1.
  - `isdigit()` - True only if all digits.
  - `lower()`, `upper()`.
  - `split(str=" ", num=...)` - split by delimiter, returns list of substrings.
  - `endswith(suffix, beg=0, end=len(string))`.
- String built-in functions: `len(string)`, `max(string)`,
  `min(string)`.
- Function vs method: `len(name)` is a function; `name.lower()` is a
  method. (Full function concept comes in Lecture 4.)

## Lists

- A list is a sequence of comma-separated items in square brackets:
  `Names = ['Saul', 'David', 'Solomon', 'Rehoboam']`.
- Items need not be the same data type.
- Concatenation and repetition: `[1,2,3]+[4,5,6]`; `[1,2,3]*4`.
- Indexing, membership, slicing: `my_list[0]`, `my_list[-1]`,
  `'abc' in my_list`, `my_list[start:end:step]`.
- Update / add / delete:
  - Update: `my_list[1] = 'efg'` (strings can't do this - immutable).
  - Add: `my_list.append('hij')`, `my_list.insert(0, 'klmn')`.
  - Delete: `del my_list[-2]`, `my_list.remove('abc')`.
  - Length: `len(my_list)`.
- Built-ins: `len`, `max`, `min`.
- Be careful when you copy a list: plain assignment `list2 = list1`
  makes both names refer to the **same** list.
- Deep copy: creates a new object and recursively copies nested
  objects; changes to the copy do not affect the original (`copy.deepcopy`).
- Shallow copy: creates a new object but inserts **references** to
  the original's objects; if the copy contains mutable items, mutating them
  affects the original. `copy.copy(list1)` is a shallow copy.
  Example:
  ```python
import copy
list1 = [[1], [2], 3, 4]
list3 = copy.copy(list1)
list3[0][0] = 100   # mutates the shared inner list -> list1 also changes
list3[0] = [200]    # rebinds list3[0] -> list1 unchanged here
```
  Checking `id(list1[0]) == id(list3[0])` shows they share the same object
  until it is rebound.

## Tuples

- A sequence of immutable Python objects, written with parentheses `()`.
- Difference from list: parentheses vs square brackets; tuples cannot be changed.

## Dictionaries

- A dictionary stores data in key-value pairs. Each key is unique and maps to a
  value. Syntax: keys separated from values by `:`, items separated by commas,
  whole thing in curly braces; empty dict is `{}`.
- Rules: keys unique; values need not be unique; values any type;
  keys must be of an **immutable** type (strings, numbers, tuples).
- Why keys must be immutable: dict is a hash table; the key's hash
  decides storage location; if a key were mutable its hash could change and the
  dict would "lose" the item.
- Access / update / add / delete:
  ```python
students = {'000': 'John', '001': 'John', '002': 'Josh', '003': 'Jack'}
students['001']                    # access -> 'John'
students['001'] = 'John_no2'       # update (overwrites old value)
students['004'] = 'Jason'          # add (new key)
del students[key]                  # delete one pair
students.clear()                   # delete all
```
- Built-in functions: `len(dict)`, `str(dict)`.
- Methods: `keys()`, `values()`, `items()` (returns (key, value)
  pairs), `update(dict2)` (adds dict2's pairs, overwriting).
- Iteration practice: `for key, value in my_dict.items():`; adding
  10 to each student's score via `keys()`; sorting with `sorted(..., key=...)`
  to sort by a custom key function.

## List comprehension

- `new_list = [expression for member in iterable]`
  - `expression`: the member itself or any valid expression, e.g. `i**2`.
  - `member`: the object/value from the iterable.
  - `iterable`: list, set, sequence, generator, or anything that yields elements
    one at a time, e.g. `list(range(10))`.
- With a conditional: `new_list = [expression for member in iterable if
  conditional]`.
- Why use it: faster (implemented in C, avoids Python-level
  overhead), concise (one line vs a `for` + `append`), readable, expressive
  (filtering, conditional expressions, nested loops), consistent across list/
  set/dict/generator.
- Caveat: efficiency sometimes costs readability.
- Set/dict comprehension forms: `{expr for member in
  iterable}` is a set; `{member: expr(member) for member in iterable}` is a
  dict.

## Topics carried over to Lectures 4 and 5

- File I/O.
- Bytes data type: ASCII, byte, bit, Unicode; beyond txt files: image, audio,
  video, npy.

## Big-O notation intro

- Big-O describes the upper bound of an algorithm's running time or space in
  terms of input size n. It measures how performance scales, not exact time.
- `O(n)`: runtime grows linearly with input size (iterating a list, finding the
  max, checking every character of a string).
- `O(n^2)`: input doubles -> runtime ~4x (nested loops).
- `O(n log n)`: e.g. finding an item in a sorted list (binary search is
  `O(log n)` for a single search).
- Simplification rule: drop the constant `c` and replace it with 1:
  `c*n -> O(n)`; `3n^2 + 10n + 20 -> O(n^2)`. Big-O focuses on order of growth,
  not constants (which depend on language, compiler, machine).
