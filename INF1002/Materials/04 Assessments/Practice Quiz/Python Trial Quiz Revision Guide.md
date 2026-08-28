# Python Trial Quiz - Topic Revision Guide and Question Index

## About this guide

The trial quiz contains 37 open questions with no answer key. This guide is
a topic-oriented revision aid: for each topic it lists the questions that
test it, what each question asks you to know, what to review, and common
pitfalls. It deliberately does NOT provide answers to the trial questions -
work through each question yourself and verify by running Python or checking
your lecture/lab notes. Suggested self-checks are marked "[self-check]".

## Contents
1. Topic index (topic -> questions)
2. Question index (question -> topic)
3. Topic-by-topic revision notes

---

## 1. Topic index (topic -> questions)

| #  | Topic | Questions |
|----|-------|-----------|
| A  | Strings, escape characters, printing | 1, 2 |
| B  | Variable naming rules | 3, 4 |
| C  | Arithmetic operators | 5 |
| D  | Boolean expressions and truthiness | 6, 7 |
| E  | Loops (while vs for, break/continue, for...else) | 8, 9, 10 |
| F  | Command-line arguments (sys.argv) | 11 |
| G  | String formatting (f-strings) | 12, 13 |
| H  | String operators and slicing | 14, 15, 16 |
| I  | Characters and ASCII (ord vs int) | 17, 18 |
| J  | Lists (mutation, methods, aliasing) | 19, 20, 21, 22 |
| K  | Tuples | 23 |
| L  | Dictionaries | 24, 25 |
| M  | Conditional expressions and list comprehensions | 26 |
| N  | File I/O | 27, 28, 29, 30 |
| O  | Functions: mutation of arguments and scope | 31, 32 |
| P  | Higher-order functions (map, filter) | 33, 34 |
| Q  | Recursion | 35 |
| R  | Packing and unpacking | 36 |
| S  | Lab questions | 37 |

## 2. Question index (question -> topic)

| Q | Topic |
|---|-------|
| 1  | A - Strings, escape characters |
| 2  | A - Strings, printing quotes |
| 3  | B - Variable naming rules |
| 4  | B - Valid identifiers |
| 5  | C - Arithmetic operators (//, %, **) |
| 6  | D - Boolean expressions (and, or, not) |
| 7  | D - Truthiness of containers |
| 8  | E - while vs for loop |
| 9  | E - break and continue |
| 10 | E - for...else |
| 11 | F - sys.argv |
| 12 | G - f-string alignment (>5) |
| 13 | G - f-string format (^6.2f) |
| 14 | H - String operators (+ and *) |
| 15 | H - Slicing start/end/step inclusivity |
| 16 | H - Slicing with steps and negatives |
| 17 | I - ASCII values of characters |
| 18 | I - ord('a') vs int('a') |
| 19 | J - Valid list of mixed types |
| 20 | J - Updating list vs string items |
| 21 | J - append, del, len on lists/dicts |
| 22 | J - Aliasing (B = A) risk |
| 23 | K - Tuples vs lists |
| 24 | L - Building and querying a dictionary |
| 25 | L - Dictionary methods (keys, values, items, update) |
| 26 | M - Conditional expression and list comprehension |
| 27 | N - Reading lines from a file |
| 28 | N - strip() and split() |
| 29 | N - readline() vs readlines() |
| 30 | N - File pointer |
| 31 | O - Does a function change the caller's variable |
| 32 | O - Variable scope in a function |
| 33 | P - map with str.upper |
| 34 | P - filter with is_even |
| 35 | Q - Recursion (fibonacci) |
| 36 | R - Packing and unpacking |
| 37 | S - Lab questions |

## 3. Topic-by-topic revision notes

### A. Strings, escape characters, printing (Q1, Q2)
- Q1 asks for the definition of an escape character; Q2 asks how to print a
  string that contains both single and double quotes.
- Review: escape sequences (`\\`, `\'`, `\"`, `\n`, `\t`); how to embed
  quotes inside strings by mixing quote types or escaping them.
- Pitfall: forgetting that backslash sequences must be escaped inside normal
  strings.
- [self-check] Print several strings containing quotes both ways (mix quotes,
  escape quotes) and confirm the output matches what you predicted.

### B. Variable naming rules (Q3, Q4)
- Q3 asks for the naming rules; Q4 asks you to pick valid names from
  `_myvar, Var1, 1var, -var, return, good`.
- Review: identifiers must start with a letter or underscore; the rest can be
  letters, digits, or underscores; case sensitivity; reserved keywords cannot
  be used; style conventions (PEP 8: snake_case for variables).
- Pitfall: forgetting reserved words (e.g. `return`) are invalid even though
  they look valid, and that `-var` is not an identifier (minus is an
  operator).

### C. Arithmetic operators (Q5)
- Q5 asks what `//`, `%`, and `**` do.
- Review: floor division, modulo/remainder, and exponentiation; how they
  behave with negative numbers (floor vs truncation).
- Pitfall: `//` floors toward negative infinity, so results with negatives
  may surprise.

### D. Boolean expressions and truthiness (Q6, Q7)
- Q6 asks what a Boolean expression is and the meaning of `and`, `or`,
  `not`; Q7 asks whether empty containers (`A = []`, `B = ''`) evaluate as
  True or False.
- Review: comparison operators producing bools; `and`/`or` short-circuit and
  return operands (not always a bool); truthiness of empty containers,
  `0`, `None`, empty string.
- Pitfall: thinking `and`/`or` always return True/False - they return one of
  the operands.

### E. Loops (Q8, Q9, Q10)
- Q8 asks the differences between `while` and `for`; Q9 when to use `break`
  and `continue`; Q10 what `for...else` means.
- Review: `for` iterates over a sequence (finite, known count); `while`
  repeats while a condition holds (unknown/conditional); `break` exits the
  loop; `continue` skips to the next iteration; the `else` clause runs when
  the loop completes without `break`.
- Pitfall: forgetting the `else` on a loop only runs when no `break`
  happened; infinite loops when the `while` condition never becomes False.

### F. Command-line arguments (Q11)
- Q11 gives `python AverageCalculator.py 3 4 5` and asks about `sys.argv[0]`,
  `sys.argv[1]`, and the type of `sys.argv[1]`.
- Review: `sys.argv` is a list of strings; `sys.argv[0]` is the script name;
  following elements are the arguments, all strings (convert with `int()`
  where needed).
- Pitfall: forgetting that `sys.argv[0]` is the script name, and that all
  argv values are strings.

### G. String formatting with f-strings (Q12, Q13)
- Q12 asks the output of `f'{A:>5}'` with `A = 'test'`; Q13 asks the output
  of `f'{B:^6.2f}'` with `B = 3.1415`.
- Review: format spec `[[fill]align][width][.precision][type]`; `<` left,
  `>` right, `^` center; width includes the fill; `.2f` rounds to 2 decimal
  places and pads; what happens when width is smaller than the value.
- Pitfall: mixing up alignment characters and forgetting the type suffix
  (`f` for float).
- [self-check] Reproduce the exact outputs in your own REPL before moving on.

### H. String operators and slicing (Q14, Q15, Q16)
- Q14 asks the result of `A + '3'`, `A * 3`, and `A * '3'` with `A = '2'`,
  and whether lists have similar operators; Q15 asks about `start, end, step`
  inclusivity; Q16 asks the value of `A[::2]` and `A[:-1:2]` with
  `A = 'abcdefg'`.
- Review: string concatenation with `+`; repetition with `*`; that `*`
  requires an integer on one side; slicing syntax `[start:stop:step]` where
  `start` inclusive and `stop` exclusive; negative indices and negative
  steps; the same `+`/`*`/slicing behaviour on lists.
- Pitfall: `A * '3'` is a TypeError (string times string); exclusive stop;
  forgetting negative step reverses direction.

### I. Characters and ASCII (Q17, Q18)
- Q17 asks the ASCII values of `A, B, a, b` and which is greater; Q18 asks
  the output of `ord('a')` and `int('a')`.
- Review: uppercase letters before lowercase in ASCII; `ord()` returns the
  code point of a single character; `int('a')` raises ValueError (not a
  valid integer literal).
- Pitfall: `int()` and `ord()` are different operations; `int('a')` fails.

### J. Lists (Q19, Q20, Q21, Q22)
- Q19 asks whether a mixed-type list is valid; Q20 asks how to update an
  item (`A[index] = value`) and what happens with strings; Q21 asks about
  `append`, `del`, `len` for lists and dictionaries; Q22 asks the risk of
  `B = A` for `A = [1,2,3,4]`.
- Review: lists can hold mixed types; lists are mutable (item assignment
  works) but strings are immutable (cannot do `A[index] = ...`); `append`
  adds to lists; `del` removes items; `len` works on sequences and
  collections; assignment `B = A` does NOT copy - both names reference the
  same list (aliasing), so mutating through one affects the other.
- Pitfall: mutating via an alias unexpectedly; trying to mutate a string.

### K. Tuples (Q23)
- Q23 asks the type of `T = (1,2,3,4)` and the difference between tuple and
  list.
- Review: tuples are immutable sequences; created with parentheses (comma is
  the real tuple-maker); can be unpacked; used where immutability matters
  (e.g. dict keys).
- Pitfall: a single-element tuple needs a trailing comma; thinking tuples
  cannot be nested or indexed (they can be read).

### L. Dictionaries (Q24, Q25)
- Q24 asks to define a dictionary keyed by student names with scores and get
  one student's score; Q25 asks what `keys()`, `values()`, `items()`,
  `update()` mean.
- Review: `{key: value}`; access by key with `d[key]` or `d.get(key)`;
  `keys()`, `values()`, `items()` return views; `update()` merges another
  mapping or iterable of key-value pairs; keys must be hashable.
- Pitfall: accessing a missing key raises KeyError unless using `.get()`.

### M. Conditional expressions and list comprehensions (Q26)
- Q26 asks what a conditional expression is, what a list comprehension is,
  and the value of `Scores = ['A' if s > 85 else 'B' for s in B]` with
  `B = [40,60,80,100]`.
- Review: `x if cond else y`; comprehension syntax
  `[expr for item in iterable if filter]`; conditional expressions inside
  comprehensions are evaluated per item.
- Pitfall: operator precedence of the conditional expression inside a
  comprehension; forgetting the comprehension iterates every element.
- [self-check] Trace the value of Scores by hand, then run it.

### N. File I/O (Q27, Q28, Q29, Q30)
- Q27 asks for the value of the second line of a file (content shown in the
  quiz); Q28 asks what `strip()`/`split()` do; Q29 asks what
  `readline()`/`readlines()` do and whether they take arguments; Q30 asks
  what a file pointer is.
- Review: opening files with `open()`; reading with `.read()`,
  `.readline()` (returns next line, includes `\n`), `.readlines()` (returns
  a list of lines); `strip()` removes leading/trailing whitespace and
  newlines; `split()` splits a string on whitespace (or a separator) into a
  list; the file pointer tracks the current position in the file.
- Pitfall: lines read with `.readline()` keep the trailing newline unless
  stripped; `split()` default splits on any whitespace and discards empty
  strings.

### O. Functions: mutation of arguments and scope (Q31, Q32)
- Q31 shows a function that does `s += 'ing'` and returns `s`, called with
  `update(A)` where `A = 'test'`, and asks for the value of A now; Q32 shows
  a function reading `marks` before assigning it (`marks = marks + 20`) and
  asks for the output.
- Review: strings are immutable, so rebinding the parameter inside a function
  does not change the caller's variable (pass-by-object-reference /
  call-by-value semantics); a variable assigned anywhere inside a function
  becomes local to that function, so reading it before assignment raises
  UnboundLocalError unless declared `global`.
- Pitfall: thinking strings mutate in place; forgetting local scope rules
  that cause UnboundLocalError.
- [self-check] Run both snippets and explain each output line.

### P. Higher-order functions: map and filter (Q33, Q34)
- Q33 asks the output of `map(str.upper, names)` printed with
  `list(result)`; Q34 asks the output of `filter(is_even, numbers)`.
- Review: `map(func, iterable)` applies func to each element and returns an
  iterator; `filter(func, iterable)` keeps elements where func returns
  truthy; results must be converted (e.g. `list(...)`) to see them.
- Pitfall: forgetting map/filter return lazy iterators, not lists; a
  function (not a call) must be passed as the first argument.

### Q. Recursion (Q35)
- Q35 shows a recursive `fibonacci` with base cases `n == 0` and `n == 1`
  and asks for `print(fibonacci(4))`.
- Review: base cases stop recursion; each call reduces the problem toward a
  base case; trace the call tree to compute small values by hand.
- Pitfall: off-by-one in the trace; forgetting the base cases lead to
  infinite recursion.
- [self-check] Compute fibonacci(4) by hand as a call tree, then run it.

### R. Packing and unpacking (Q36)
- Q36 asks you to explain/use packing and unpacking (topic header, no code
  shown).
- Review: `*args`/`**kwargs` in function definitions (packing) and calls
  (unpacking); tuple/list unpacking (`a, b = (1, 2)`); star unpacking in
  assignment (`first, *rest = ...`).
- Pitfall: mixing positional and keyword packing; forgetting the number of
  values must match the number of targets (unless starred).

### S. Lab questions (Q37)
- Q37 refers to questions from the module labs.
- Review: redo the lab worksheets; the quiz signals that lab material is in
  scope.

---

## How to use this guide

1. Attempt each question in the quiz first.
2. For the questions you get wrong or are unsure about, jump to the topic in
   section 3 and re-review the concepts, then re-attempt.
3. Verify any code-output questions (12, 13, 14, 16, 18, 26, 31, 32, 33, 34,
   35) by running the snippet in Python and comparing with your prediction.
4. Redo the labs (see topic S) since lab-style questions are explicitly
   referenced.
