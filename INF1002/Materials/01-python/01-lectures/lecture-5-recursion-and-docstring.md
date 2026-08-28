# Lecture 5 - Recursion and Docstring

## Outline

1. Packing and unpacking.
2. Set/dict comprehensions (brief).
3. Recursion.
4. Docstrings.
5. Bytes, ASCII, Unicode, UTF-8.

## Tuple packing and unpacking

- A function can appear to "return multiple values"; actually Python packs them
  into a tuple, and you unpack the tuple into variables.
- Unpacking assigns each element to a separate variable.
- Use `_` for a single unused value; `*var` collects the "rest"; `*_` skips a
  block of values.
- Multiple assignment: on the right, values are packed into a tuple; on the
  left, values are unpacked into variables.

## Set and dict comprehensions

- `{expression for member in iterable}` builds a **set**.
- `{member: exp(member) for member in iterable}` builds a **dictionary**.

## Recursion

- Recursion: solving a problem by relying on solutions to **smaller instances
  of the same problem**.
- Factorial definition:
  - n! = 1, if n = 0 (base case)
  - n! = n * (n-1)!, if n > 0 (recursive case)
- The three parts:
  - **Decomposition**: turn the problem into a smaller problem of the same
    kind (3! = 3 * 2!).
  - **Base case**: the point where the answer is known without recursing
    (0! = 1).
  - **Composition**: combine results of smaller problems to get the larger
    answer (1*1 = 1, 2*1 = 2, 3*2 = 6).
- Implementation:
  ```python
  def factorial(n):
      # Base case: if n is 0, return 1
      if n == 0:
          return 1
      # Recursive case: n! = n * (n-1)!
      else:
          return n * factorial(n - 1)
  ```
- How it works: the call stack - last in, first out; each call pushes a
  **frame** (function name, local variables, global variables, instruction
  pointer, previous frame). pythontutor.com visualises this.
- Key points: figure out the formula for how a big problem can be
  solved by a similar smaller problem; assume you know the solution for n-1 and
  ask how to use it for n; identify the base case.
- Fibonacci:
  - Base cases: F0 = 0, F1 = 1.
  - Recursive formula: Fn = F(n-1) + F(n-2).
  - Sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21, ...
  - Naive recursion recomputes the same subproblems repeatedly (the recursion
    tree shows duplicated subtrees) - motivation for improvement (dynamic
    programming).
- General template:
  ```python
  def func(big_problem):
      if base_case:
          return value
      else:
          # recursive formula
          tmp = func(smaller_problem)
          return recursive_formula(tmp)
  ```
- Practice: reverse a list `[70, 63, 98, 85, 22]` - find the base case and the
  recursive formula; related LeetCode: reverse linked list.
- Recursion vs iteration: recursive problems can be written
  iteratively and vice versa; the choice depends on the problem nature, context,
  performance, and readability (practice: factorial without recursion).

## Docstrings

- A docstring is a comment that appears as the **first line** of a new piece of
  code; used by the `help()` function.
- In a module: put the module description at the top, surrounded by `'''` or
  `"""`. In each function: write the comment as the first line inside the body.
- Lab4 warm-up uses this: add docstrings to `myMath.py` (from Lab3), put the
  module into your Python `Lib` folder, then `import myMath` and
  `help(myMath)`.

## Bytes, ASCII, Unicode, UTF-8

- Bytes: a byte = 8 bits; a bit is 0 or 1 (the most basic unit of
  information). Value range of a byte: 2^8 = 256 -> 0..255 (or -128..127).
- Python int/float memory is not fixed:
  ```python
  import sys
  sys.getsizeof(1.0)   # 24
  sys.getsizeof(1)     # 28
  ```
  due to reference count, type info, and padding/management overhead.
- ASCII: 7-bit encoding defining 128 characters (0-127): English
  upper/lower case letters, digits, punctuation, control characters. Extended
  ASCII (128-255) varies by platform and is not standardised.
- Unicode: needed for other languages (e.g. Chinese, Tamil). Code
  points: `A` = 65; the CJK char `long` = 40857 (0x9F99).
- Unicode vs UTF-8:
  - `A` -> 65 -> binary `1000001` -> one byte `0100 0001`.
  - the char `long` (code point 40857) needs 3 UTF-8 bytes: E9 BE 99.
  - UTF-8 is variable-length: 1 to 4 bytes per character; ASCII characters use
    1 byte (fully backward compatible), others use 2-4 bytes.
  - UTF-8 format table:
    | Type | Unicode range | Format |
    |------|---------------|--------|
    | 1 byte | U+0000..U+007F | 0xxxxxxx |
    | 2 bytes | U+0080..U+07FF | 110xxxxx 10xxxxxx |
    | 3 bytes | U+0800..U+FFFF | 1110xxxx 10xxxxxx 10xxxxxx |
    | 4 bytes | U+10000..U+10FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx |
- Binary files: a binary file is any file containing a sequence of
  bytes; images/audio/executables need specific programs to decode. PNG
  structure example: 8-byte signature `89 50 4E 47 0D 0A 1A 0A`, then chunks
  (IHDR image header, IDAT image data, IEND image end).
- Key definitions: bit; byte (8 bits, 256 values); ASCII (7 bits,
  typically stored in 1 byte); UTF-8 (variable-length, backward compatible);
  binary files.
- Exam note: **"UTF-8 won't be on the exam."**
