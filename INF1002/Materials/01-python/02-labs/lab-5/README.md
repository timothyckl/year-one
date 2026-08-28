# Python Lab 5 - High-Order Functions (Lambda, Map, Filter, Reduce)

## Topics covered

1. Lambda expressions.
2. Map, filter, and reduce.

## Objectives

- Write and pass functions as arguments (higher-order functions).
- Compose operations with `lambda`, `map`, `filter`, `sorted`, `reduce`.
- Write functions whose bodies fit in 1-2 lines using these tools.

## Task structure

Only **two** tasks in this lab. 4 test cases / max 5 marks for DoTwiceGame, and
5 test cases / 5 marks for SalesAnalytics; submit both files together for a max
of 10. All output on ONE line. Same Gradescope rule: no `quit()`/`exit()`/
`sys.exit()`.

### Task 1 - Do-Twice Game (DoTwiceGame.py)

- `double(x)` returns `2 * x`.
- `square(x)` returns `x * x`.
- `cube(x)` returns `x * x * x`.
- A higher-order function `doTwice(func, x)` applies `func` twice:
  `return func(func(x))`.
- Main program reads a number and an option: 1 = double, 2 = square,
  3 = cube; applies the chosen operation **twice**; anything else prints
  `It cannot be supported!`.
- Examples:
  - `python DoTwiceGame.py 4 1` -> `16` (double(double(4)) = 2*(2*4))
  - `python DoTwiceGame.py 4 2` -> `256` (square(square(4)) = 4^2^2)
  - `python DoTwiceGame.py 4 4` -> `It cannot be supported!`

### Task 2 - Sales Analytics (SalesAnalytics.py)

Given a list of sales numbers and a scale factor, implement:

- `scale(list1, x)` -> each number multiplied by the scale factor
  (`[10,20,30,40]*2` -> `[20, 40, 60, 80]`).
- `sort(list1)` -> sort by the **last digit** of each number
  (`sort([55,70,61,34,72,59])` -> `[70, 61, 72, 34, 55, 59]`).
- `goodSales(list1)` -> keep only sales strictly **above the average**
  (`[10,20,40,60,20]` -> `[40, 60]`).
- The main program prints three lists on ONE line:
  `The scaled numbers are:  [...] The sorted sales numbers are:  [...] The good
  sales numbers are:  [...]`

Running example: `python SalesAnalytics.py 10,20,30,40,50,60 2` ->
```
The scaled numbers are:  [20, 40, 60, 80, 100, 120] The sorted sales numbers are:  [10, 20, 30, 40, 50, 60] The good sales numbers are:  [40, 50, 60]
```
Note the two spaces after each colon in the supplied implementation.

## Supplied implementation analysis

- `DoTwiceGame.py` passes the *function itself* (no parentheses) into
  `doTwice`: `doTwice(double, input)`. `double`/`square`/`cube` use the `**`
  operator for powers. Type aliases (`Decimal = int | float`) and full
  docstrings show good style.
- `SalesAnalytics.py`:
  - `scale` uses a list comprehension `[x * num for num in list1]`.
  - `sort` uses `list1.sort(key=lambda x: x % 10)` - sorts **in place** by last
    digit.
  - `goodSales` uses `list(filter(lambda x: x > avg, list1))`.
  - Each fits in 1-2 lines as requested.
- The warm-ups ask you to rewrite a `getBonus(Salary)` lambda
  (`lambda x: x + 100(salary)`) into a doubling lambda and confirm identical
  results - practice reading lambda composition.

## Run steps

```bash
python3 DoTwiceGame.py 4 1
python3 DoTwiceGame.py 4 2
python3 DoTwiceGame.py 4 4
python3 SalesAnalytics.py 10,20,30,40,50,60 2
```

## Expected behavior

- `DoTwiceGame` prints just the number (or `It cannot be supported!`).
- `SalesAnalytics` prints the three lists on one line, in the order scaled,
  sorted, good sales.

## Pitfalls

- Pass the **function, not the call**: `doTwice(double, input)`, never
  `doTwice(double(input), input)`.
- `sort()` in the supplied code **mutates the input list in place** and returns
  it, unlike `map`/`filter`/`sorted` (which "do not change the original
  sequence"). Because `sort(input_list)` runs before
  `goodSales(input_list)`, `goodSales` actually receives the *sorted* list. In
  the running example the sort is a no-op (10..60 already ordered by last
  digit), so this hidden behavior does not show up in the output - worth
  knowing if the autograder feeds other data.
- The example output has two spaces after each colon (`numbers are:  \[20, ...`).
  Keep the exact spacing.
- `goodSales` uses `>` (strictly above average), not `>=`; the example average
  is 35, so 40/50/60 are kept and 30 is not.
- In `DoTwiceGame`, invalid option prints `It cannot be supported!` - do not
  crash on option 4+.
- Do not change file/function names (`def DoTwiceGame()`, `def SalesAnalytics()`).

## Lessons

- Functions are first-class objects: they can be stored, passed, and returned.
  `doTwice` is the minimal example.
- `lambda` + `map`/`filter`/`sorted`/`reduce` replaces short loops; for anything
  longer, a named function or list comprehension is more readable.
- Watch the difference between `sorted(x)` (returns a new list) and
  `x.sort()` (mutates in place).
- `reduce` is not used here but is possible quiz material:
  `from functools import reduce`.
