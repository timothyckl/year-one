# Revision - Bytes, Encoding and Complexity

## Bits and bytes

- **Bit**: the most basic unit of information; a logical state of 0 or 1
  ("binary digit").
- **Byte**: 8 bits; represents 2^8 = 256 values (0..255, or -128..127).
- Python memory is not fixed: `sys.getsizeof(1.0)` -> 24, `sys.getsizeof(1)`
  -> 28 (reference count, type info, padding/overhead).

## ASCII

- American Standard Code for Information Interchange.
- 7-bit scheme, characters 0..127: English letters, digits, punctuation,
  control characters.
- Examples: `ord('A')` -> 65, `ord('B')` -> 66, `ord('a')` -> 97,
  `ord('b')` -> 98.
- Extended ASCII (128..255) varies across platforms; not a single standard.
- Typical display uses 8 bits (1 byte) with the high bit 0.

## Unicode

- Standard covering text in all of the world's writing systems.
- Each character has a code point: `A` = 65, the CJK char `long` (U+9F99, decimal 40857).
- The first 128 code points match ASCII.

## UTF-8

- Variable-length encoding using 1 to 4 bytes per character.
- Fully backward compatible with ASCII (ASCII text is valid UTF-8).

| Type | Unicode range | Format |
|------|---------------|--------|
| 1 byte | U+0000..U+007F | 0xxxxxxx |
| 2 bytes | U+0080..U+07FF | 110xxxxx 10xxxxxx |
| 3 bytes | U+0800..U+FFFF | 1110xxxx 10xxxxxx 10xxxxxx |
| 4 bytes | U+10000..U+10FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx |

- Example: the char `long` (U+9F99, decimal 40857) encodes as 3 UTF-8 bytes `E9 BE 99`
  (fill 4 bits into `1110xxxx`, then 6 bits into each `10xxxxxx`).
- Exam note: **"UTF-8 won't be on the exam"**.

## Binary files

- Any file containing a sequence of bytes.
- Text files (ASCII/UTF-8) are human-readable; images, audio, executables need
  specific programs to decode.
- PNG example: 8-byte signature `89 50 4E 47 0D 0A 1A 0A`; then chunks:
  IHDR (image header), IDAT (image data), IEND (image end) plus optional
  ancillary chunks.

## Big-O notation

- Big-O gives the **upper bound** of running time / space as a function of
  input size n. It describes how performance scales, not exact time.
- `O(n)`: linear - iterating a list, finding the maximum, checking every
  character of a string. Input doubles -> work roughly doubles.
- `O(n^2)`: quadratic - nested loops; input doubles -> runtime ~4x.
- `O(n log n)`: e.g. efficient sorting; `O(log n)`: halving a sorted list with
  binary search.
- Simplification rule: drop constants: `c*n -> O(n)`; `3n^2 + 10n + 20 ->
  O(n^2)`.

## Quick self-check

1. How many values can one byte hold? 256.
2. `ord('Z')` vs `ord('z')`? 90 vs 122.
3. What is the first UTF-8 byte of an ASCII character? `0xxxxxxx` (1 byte).
4. Dropping constants: `5n + 100` -> ?  (`O(n)`)
5. Is the loop `for j in range(n): for i in range(n):` O(n) or O(n^2)?
   (`O(n^2)`)
