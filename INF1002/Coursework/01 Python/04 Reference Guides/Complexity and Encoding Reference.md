# Complexity and Encoding Reference

Quick reference for the Big-O and bytes/encoding material.

## Big-O summary

| Notation | Growth | Typical shape | Examples |
|----------|--------|---------------|----------|
| O(1) | constant | one operation | dict/list index lookup (amortised) |
| O(log n) | logarithmic | halving each step | binary search on sorted data |
| O(n) | linear | one pass | iterate a list, find max, check each char |
| O(n log n) | linearithmic | divide and conquer | efficient sort (merge/quick) |
| O(n^2) | quadratic | nested loops | double loop over n |

- O(n): input doubles -> work roughly doubles.
- O(n^2): input doubles -> work ~4x.
- Simplify by dropping constants and lower-order terms: `c*n -> O(n)`,
  `3n^2 + 10n + 20 -> O(n^2)`.
- Big-O is an upper bound on how performance scales, not exact timing.

## Bit, byte

- bit: 0 or 1.
- byte: 8 bits -> 2^8 = 256 values (0..255, or -128..127).
- Python objects are not fixed size: `sys.getsizeof(1)` -> 28,
  `sys.getsizeof(1.0)` -> 24 (reference count, type info, overhead).

## ASCII

- 7-bit, characters 0..127: letters, digits, punctuation, control characters.
- `ord('A')` = 65, `ord('B')` = 66, `ord('a')` = 97, `ord('b')` = 98.
- Extended ASCII (128..255) is not standardised across platforms.

## Unicode and UTF-8

- Unicode assigns a code point to every character: `A` = U+0041 (65),
  the CJK char `long` = U+9F99 (40857). First 128 code points == ASCII.
- UTF-8: variable length, 1-4 bytes, backward compatible with ASCII.

| Bytes | Unicode range | First byte pattern |
|-------|---------------|--------------------|
| 1 | U+0000..U+007F | 0xxxxxxx |
| 2 | U+0080..U+07FF | 110xxxxx 10xxxxxx |
| 3 | U+0800..U+FFFF | 1110xxxx 10xxxxxx 10xxxxxx |
| 4 | U+10000..U+10FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx |

- the char `long` = U+9F99 = 3 UTF-8 bytes: E9 BE 99.
- Exam note: "UTF-8 won't be on the exam."

## Binary files

- A binary file is any sequence of bytes; images/audio/executables need
  programs to decode; text files (ASCII/UTF-8) are human-readable.
- PNG: signature `89 50 4E 47 0D 0A 1A 0A` (8 bytes) + chunks:
  IHDR (header), IDAT (image data), IEND (end).
- Python reads/writes binary with mode `'b'` (`rb`, `wb`).

## Memory model reminders

- `id(x)` = object identity/address; `is` compares identity, `==` compares
  value.
- Mutable objects (list/dict/set) can be changed in place; immutable objects
  (int/str/float/tuple) cannot.
- Passing a mutable object to a function lets the function mutate it; passing
  an immutable one does not.
