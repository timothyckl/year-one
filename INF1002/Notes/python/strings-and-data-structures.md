# Revision - Strings and Data Structures

## Strings

- Immutable sequence of Unicode characters.
- Sequence operations: `+` concatenation, `*` repetition, `in`/`not in`
  membership, indexing `s[0]`, `s[-1]`, slicing `s[start:end]` (end exclusive),
  `s[start:end:step]`.
- Indexing: first char 0, last `len(s)-1`.
- Concatenating before casting is a classic bug: `'3' + '4'` -> `'34'`, then
  `float('34')` is wrong (Lab1 warning).
- Comparison uses code points (ASCII/Unicode): `ord('a')` -> 97, `ord('A')` ->
  65; `'A' < 'a'`.
- Common methods: `find`, `isdigit`, `lower`, `upper`, `split`, `endswith`.
- Built-ins: `len`, `max`, `min`.
- Function vs method: `len(name)` is a function; `name.lower()` is a method.

## List

- Mutable sequence in `[ ]`; items may be of mixed types.
- `+` and `*` (concatenation/repetition), indexing/slicing like strings.
- Update: `my_list[1] = 'efg'`.
- Add: `append(item)` (end), `insert(i, item)` (at index i).
- Delete: `del my_list[-2]`, `remove('abc')` (by value).
- Built-ins: `len`, `max`, `min`, `sum`.

### Copying lists - the important trap

- `list2 = list1` makes both names refer to the **same** list; mutations
  through either name are visible through the other.
- Shallow copy (`copy.copy`): new outer list, but nested mutable items are
  **shared**. Mutating a nested item affects the original.
- Deep copy (`copy.deepcopy`): new list and recursively new copies of nested
  objects; fully independent.
- Example:
  ```python
  import copy
  list1 = [[1], [2], 3, 4]
  list3 = copy.copy(list1)
  list3[0][0] = 100     # list1[0] is now [100] too (shared inner list)
  list3[0] = [200]      # rebinding list3[0] does not affect list1
  ```

## Tuple

- Immutable sequence in `( )`. Cannot be changed once created.
- Use for data that must not change; hashable -> can be a dict key.

## Dictionary

- Key-value pairs in `{ }`; `key: value` separated by commas.
- Keys unique, must be immutable (str, int, tuple); values any type.
- Why immutable keys: dict is a hash table; the key's hash decides storage; a
  mutable key could change its hash and the dict would "lose" the item.
- Access: `d[key]`; update/add: `d[key] = value` (overwrites existing, adds
  new); delete: `del d[key]`, `d.clear()`.
- Methods: `keys()`, `values()`, `items()` (key/value pairs), `update(d2)`.
- Iterate: `for key, value in d.items():`.

## Set and dict comprehensions

- Set: `{expression for member in iterable}`.
- Dict: `{member: exp(member) for member in iterable}`.

## List comprehension

```python
new_list = [expression for member in iterable]
new_list = [expression for member in iterable if conditional]
```

- Builds a new list in one line; faster (implemented in C), concise, readable.
- Examples: `[i**2 for i in range(10)]`; `[x for x in range(20) if x % 2 == 0]`;
  `["Even" if i % 2 == 0 else "Odd" for i in range(10)]`.
- Efficiency sometimes costs readability.

## Top-N frequency idiom

```python
counts = {}
for ch in text:
    counts[ch] = counts.get(ch, 0) + 1
# or: from collections import Counter; counts = Counter(text)

top = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))[:5]
```

Descending frequency, ties ascending by key (Lab2); use `reverse=True` /
`-ord(c)` for descending-ASCII output (Lab3).

## Quick self-check

1. `[1,2,3]*3` -> ?  (`[1,2,3,1,2,3,1,2,3]`)
2. `s = 'abcdef'`; `s[1:4]` -> ?  (`'bcd'`)
3. Can a list be a dict key? No (mutable).
4. After `b = a[:]`, do `a` and `b` share elements? They are different lists
   (slicing copies the top level).
5. `{x: x*x for x in range(3)}` -> ?  (`{0:0, 1:1, 2:4}`)
