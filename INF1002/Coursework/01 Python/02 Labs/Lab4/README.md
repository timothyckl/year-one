# Python Lab 4 - Recursion and Docstring

## Topics covered

1. Recursion.
2. Docstrings.

## Objectives

- Write a recursive function and an equivalent iterative function that give the
  same answer.
- Identify the base case and the recursive formula for each problem.
- Write proper docstrings and view them with `help()`.

## Task structure

Three autograded tasks, 5 test cases / 5 marks each; submit all three `.py`
files together for 15 marks. All output must be on ONE line. Same Gradescope
rule as before: no `quit()`/`exit()`/`sys.exit()` - use `return`.

### Task 1 - Sum Calculator (SumCalculator.py)

- `sum_recursive(x)`: SUM(x) = 1 + 2 + ... + x, computed recursively.
  Base case `x == 1` -> 1; recursive case `x + sum_recursive(x - 1)`.
- `sum_iterative(x)`: same value using a `for` loop over `range(1, x + 1)`.
- Main program reads one number and prints:
  `The SUM value calculated by recursive is 6 and by iterative is 6.`

### Task 2 - Count Digits (CountDigits.py)

- `digit_recursive(x)`: number of digits of a positive number. Base case:
  `x < 10` -> 1 digit; recursive case: `1 + digit_recursive(x // 10)`
  (repeatedly divide by 10, dropping the remainder, until less than 10).
- `digit_iterative(x)`: same with a `while x > 0` loop.
- Example: `python CountDigits.py 789` ->
  `The number of digit(s) calculated by recursive is 3 and by iterative is 3.`

### Task 3 - Searching elfish (elfish.py)

- A word is "elfish" if it contains the letters `e`, `l`, `f` in any order
  (e.g. tasteful, whiteleaf, unfriendly, waffles).
- Implement recursively; hint: "recursively reduce both the elfish letters and
  input word."
- Examples:
  - `python elfish.py waffles` -> `waffles is one elfish word!`
  - `python elfish.py instance` -> `instance is not an elfish word!`

## Warm-up exercises (not submitted)

1. `fac(n)` recursive factorial.
2. `fac_iterative(n)` factorial with a `for` loop.
3. Docstrings: add proper comments to the `myMath` module from Lab3, put the
   module file into your Python installation's `Lib` folder, then in IDLE:
   ```python
import myMath
help(myMath)
```
4. Optional: implement the Fibonacci and string-reversal programs.

## Supplied implementation analysis

- All three files share the same skeleton style: the recursive function
  declares a base case first, then the recursive case; the iterative version
  uses a loop; the main function validates input (`try/except ValueError`,
  positivity checks) before calling both functions and printing ONE line.
- `elfish.py` reduces both the letter set and the word: if the first elfish
  letter is present in the word, drop that letter; otherwise drop the first
  character of the word. The word is lowercased before checking.
- Note: `SumCalculator.py` imports `sys` twice (harmless redundancy in the
  supplied file).

## Run steps

```bash
python3 SumCalculator.py 3
python3 CountDigits.py 789
python3 elfish.py waffles
python3 elfish.py instance
```

## Expected behavior

- Each program validates input and prints exactly one line with the recursive
  and iterative results; they must agree.
- `elfish` prints the input word (lowercased) followed by the verdict.

## Pitfalls

- Recursion must have a base case that is eventually reached, or you get
  `RecursionError: maximum recursion depth exceeded` (tied to the call stack /
  frames).
- The SUM base case in the supplied code is `x == 1`, not `x <= 1`; inputs like
  `0` are rejected by the main function (prints a "positive number" message),
  so it is never reached with a bad value.
- `digit_iterative` uses `while x > 0`, so `x = 0` would return 0; again the
  main function rejects non-positive input first.
- Keep output on ONE line with the exact wording from the running examples; a
  stray period or capital letter fails the test case.
- Do not change file/function names (`def SumCalculator()`, `def CountDigits()`,
  `def elfish()`).
- Docstring placement: the module docstring is the first statement of the file;
  each function's docstring is the first statement of the function body -
  otherwise `help()` will not show it.

## Lessons

- The recursion recipe: write down the base case, then assume
  you can solve `n-1` (or `n//10`) and express the answer for `n` in terms of
  it.
- Recursion and iteration are interchangeable; labs deliberately make you
  implement both and confirm identical output.
- Elfish is the classic "reduce two inputs at once" recursion: one argument
  shrinks when a needed letter is found, the other shrinks when it is not.
- Docstrings + `help()` are how Python documents modules, and they are needed
  again for the group project's report quality.
