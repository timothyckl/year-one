# Python Lab 3 - Functions and Modules

## Topics covered

1. Functional abstraction.
2. Functions and modules.
3. Higher-order functions (intro).

## Objectives

- Write and import your own module (`.py` file) with reusable functions.
- Use `*args` variadic parameters and merge dictionaries.
- Find a sub-pattern in a sequence (sliding window).

## Task structure

Three autograded tasks, 5 test cases / 5 marks each; submit all four `.py`
files (`myMain.py`, `myMath.py`, `CountLetters.py`, `SearchPattern.py`)
together for 15 marks. All output must be on ONE line. Same Gradescope rule as
Lab2: no `quit()`/`exit()`/`sys.exit()` - use `return`.

### Task 1 - My own math module (myMath.py + myMain.py)

`myMath.py` must provide eight functions:

1. `add(x, y)` -> x + y
2. `subtraction(x, y)` -> x - y
3. `evenNum(x)` -> count of even numbers in list x
4. `maximum(x)` -> max value of list x
5. `minimum(x)` -> min value of list x
6. `absolute(x)` -> absolute value of number x
7. `sumTotal(x)` -> sum of all elements of list x
8. `clear(x)` -> returns a list the same length as x with all elements 0

`myMain.py` reads a comma-separated list of integers from `sys.argv[1]`, then
prints (ONE line) using `myMath.*` functions:

- difference between the biggest and smallest,
- summation of the biggest and smallest,
- summation of all inputs,
- number of even numbers,
- the list values - but if the smallest value is smaller than 5, set all values
  to 0 before printing.

Running example: `python myMain.py 12,10,11,23,25,2` ->
```
The difference is:23 The summation is:27 The summation of all input is:83 The number of even numbers is:3 The values in the list are: [0, 0, 0, 0, 0, 0]
```
(min = 2 < 5, so all values become 0).

### Task 2 - Counting Letters (CountLetters.py)

Four steps:

1. `letter_count(str)` - dict of letter frequencies for one word. Case matters:
   upper and lower case are different characters. `letter_count('Thisisit')` ->
   `{'h':1, 'T':1, 'i':3, 's':2, 't':1}`.
2. `double_count(str1, str2)` - frequencies across two words.
3. `various_count(*str)` - frequencies across any number of words (variadic).
4. `CountLetters()` - read a comma-separated list of words, count all letters,
   print in **descending ASCII order** using the given pattern:
   `print(f'{item}:{total[item]}', end=' ')`.

Running example: `python CountLetters.py Firefox,is,having,trouble,recovering,
your,windows,and,tabs` -> one line like
`y:1 x:1 w:2 ... F:1` (characters sorted by ASCII value descending).

### Task 3 - Pattern Searching (SearchPattern.py)

- Two arguments: `candidate` and `pattern`, each comma-separated numbers.
- Count how many times the pattern appears in the candidate at **consecutive
  positions**, and print `Pattern appears N time!` (note: always singular
  "time"). 0 if not found.
- Example: `python SearchPattern.py 1,2,3,1,2 1,2` -> `Pattern appears 2 time!`

## Supplied implementation analysis

- `myMath.py`: one small function per concept. `maximum`/`minimum` iterate
  manually; `clear` returns `[0] * size`; each function has a one-line
  docstring (which Lab4 warm-up later turns into `help(myMath)` output).
- `myMain.py`: imports only the functions it needs
  (`from myMath import add, clear, evenNum, maximum, minimum, subtraction,
  sumTotal`), parses with a list comprehension
  `[int(n) for n in input.split(",")]`, and formats with one `f-string`.
- `CountLetters.py`: uses `collections.Counter`; `various_count(*tmpStr)`
  joins the tuple then counts once. The final print sorts keys with
  `key=lambda c: ord(c), reverse=True`.
- `SearchPattern.py`: splits both inputs, slides a window of the pattern's
  length across the candidate, and compares `patt_split == window`. (Note the
  loop is `range(len(cand_split) - 1)`, which works for the examples but is not
  the fully general `len(cand_split) - window_size + 1` range.)

## Run steps

```bash
python3 myMain.py 12,10,11,23,25,2
python3 CountLetters.py Firefox,is,having,trouble,recovering,your,windows,and,tabs
python3 SearchPattern.py 1,2,3,1,2 1,2
```

## Expected behavior

- `myMain.py` -> exactly one line with the 5 values, in the given order and
  format (`The difference is:%d The summation is:%d ...`).
- `CountLetters.py` -> one line of `char:count` pairs in descending ASCII
  order, space-separated, `end=' '` (there is a trailing space by design).
- `SearchPattern.py` -> one line `Pattern appears N time!`.

## Pitfalls

- `myMain.py` **must** import the module (`import myMath` or `from myMath
  import ...`) and both files must be in the same directory, or the import
  fails with `ModuleNotFoundError`.
- After `split(',')`, every element is a **string**; you must convert to `int`
  before arithmetic (the instruction hint stresses this).
- `clear()` must return a **new** list (`[0] * size`), not mutate and return
  the original reference.
- In CountLetters, case is significant: `'T'` and `'t'` are separate keys.
- Descending ASCII order means `'y'` (122) prints before `'F'` (70) - the
  opposite of ascending order used in Lab2.
- `quit()`/`exit()` are forbidden; `SearchPattern` must print a count (0 is a
  valid answer) rather than exiting.
- Do not change file/function names: `myMain.py`, `myMath.py`,
  `CountLetters.py`, `SearchPattern.py`, `def myMain()`, `def CountLetters()`,
  `def SearchPattern()`.

## Lessons

- Modules are how you reuse code across files: write functions in
  one `.py`, `import` them elsewhere.
- `*args` packs any number of positional arguments into a tuple - the basis of
  `various_count`.
- The sliding-window comparison (`candidate[i:i+window] == pattern`) is a
  useful pattern that recurs in string/array problems.
- `collections.Counter` is the idiomatic frequency counter; a plain dict loop
  works too, so know both.
