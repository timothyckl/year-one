# Revision - Higher-Order Functions

## Functions are objects

- In Python a function is an object just like a string; the only special thing
  is that it is **callable**.
- Functions can be assigned to variables, stored in lists/dicts, passed as
  arguments, and returned from functions.

## Higher-order functions

A higher-order function takes a function as an argument and/or returns a
function.

Returning a function:

```python
def operation_factory(operation):
    def add(a, b):
        return a + b
    def subtract(a, b):
        return a - b
    if operation == 'add':
        return add
    elif operation == 'subtract':
        return subtract
```

`doTwice` (Lab 5) - pass a function, apply it twice:

```python
def doTwice(func, x):
    return func(func(x))
```

## sorted() with a key

- `sorted(iterable, key=func)` sorts using `func(item)` as the sort key.
- Sort by absolute value: `sorted([-4, 2, -7], key=abs)`.
- Sort by last character with a custom function, or by last digit:
  `list1.sort(key=lambda x: x % 10)`.
- Note: `sorted(x)` returns a new list; `x.sort()` sorts in place (and returns
  None).

## map(function, iterable, *iterables)

- Applies `function` to each item; returns a **lazy iterator** of results.
- Same order, same length as the (shortest) input.
- With multiple iterables the function takes one arg per iterable; result
  length = length of the shortest iterable.
- Example: `map(square, [1, 2, 3])`; extract scores from `['John\t100', ...]`
  with a lambda.

## filter(function, iterable)

- Keeps items for which `function(item)` is True; returns a lazy iterator.
- Example: `filter(is_even, [1, 2, 3, 4])` -> keeps 2, 4.

## reduce(func, seq)

- Reduces a sequence to a single value.
- `func` takes two values and returns one: consume first two -> combine ->
  combine result with the next value -> ... -> final single value.
- Not a built-in in Python 3: `from functools import reduce`.
- Example: sum all numbers: `reduce(lambda a, b: a + b, numbers)`.

## Lambda

```python
lambda x: x + 1
lambda a, b: a + b
```

Anonymous single-expression functions; used where a named function is
overkill (mostly as `key=` or inside `map`/`filter`).

## Lazy vs eager

- `map()` and `filter()` return **iterators**: values are produced on demand
  (lazy), one-time use, forward only, memory efficient. Consume with `list()`,
  a `for` loop, or `next()`; exhausted iterators raise `StopIteration`.
- List comprehensions are **eager**: they immediately build the whole list.
- Equivalent forms:

```python
list(map(double, numbers))         # lazy iterator -> list
[double(x) for x in numbers]       # eager list

list(filter(is_even, numbers))
[x for x in numbers if is_even(x)]
```

## Side-effect note

- `map`/`filter`/`sorted`/`reduce` do not modify the original sequence - a new
  sequence is created. (The Lab5 supplied `sort()` is the exception: it sorts
  the input list in place.)

## Quick self-check

1. What is `list(map(lambda x: x * x, [1, 2, 3]))`? (`[1, 4, 9]`)
2. `list(filter(lambda x: x > 3, [1, 4, 2, 5]))`? (`[4, 5]`)
3. Why wrap `map(...)` in `list(...)`? Because `map` is lazy and returns an
   iterator.
4. `reduce(lambda a, b: a + b, [1, 2, 3, 4])`? (`10`)
5. `doTwice(square, 3)`? (`81`, i.e. square(square(3)))
