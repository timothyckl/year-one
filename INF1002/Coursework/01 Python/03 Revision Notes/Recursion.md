# Revision - Recursion

## Definition

Recursion solves a problem by solving **smaller instances of the same problem**.

## The three ingredients

1. **Decomposition**: express the big problem as a smaller problem of the same
   kind.
2. **Base case**: the smallest case, answered directly (no recursive call).
3. **Composition**: combine the result(s) of the smaller problems to get the
   answer for the big problem.

## Recipe

```python
def func(big_problem):
    if base_case:
        return value
    else:
        tmp = func(smaller_problem)
        return recursive_formula(tmp)
```

Step-by-step: assume you know the answer for `n-1` (or `n//10`, etc.) and ask
"how do I build the answer for `n` from it?"; then identify the base case.

## Factorial

```python
def factorial(n):
    if n == 0:              # base case
        return 1
    else:                   # recursive case: n! = n * (n-1)!
        return n * factorial(n - 1)
```

## Sum 1..x

```python
def sum_recursive(x):
    if x == 1:              # base case
        return 1
    return x + sum_recursive(x - 1)
```

## Count digits

```python
def digit_recursive(x):
    if x < 10:              # base case
        return 1
    return 1 + digit_recursive(x // 10)
```

## Fibonacci

- Base cases: F0 = 0, F1 = 1.
- Formula: Fn = F(n-1) + F(n-2).
- Sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21, ...
- Naive recursion recomputes overlapping subproblems (the recursion tree shows
  the duplication); that motivates dynamic programming.

## Elfish (reduce two inputs at once)

```python
def is_elfish(word, letters="elf"):
    if letters == "":        # all letters found
        return True
    if word == "":           # ran out of word
        return False
    if letters[0] in word:
        return is_elfish(word, letters[1:])   # found this letter
    return is_elfish(word[1:], letters)       # skip first char
```

## How it works underneath

- Call stack: last in, first out; push a frame per call, pop on return.
- A frame stores the function name, local variables, global variables, the
  instruction pointer, and a pointer to the previous frame.
- Python has a recursion limit; no base case (or a wrong one) ->
  `RecursionError: maximum recursion depth exceeded`.

## Recursion vs iteration

- Every recursive problem can be written iteratively and vice versa
  (Lab 4 makes you implement both and check they agree).
- Choose based on the problem nature, performance, and readability.

## Practice problems

- Factorial (recursive + iterative).
- Sum 1..x (recursive + iterative).
- Count digits (recursive + iterative).
- Fibonacci.
- Reverse a list `[70, 63, 98, 85, 22]` - base case (empty/single-element list)
  and recursive formula (first element + reversed rest).
- Elfish.
