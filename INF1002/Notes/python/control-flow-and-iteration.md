# Revision - Control Flow and Iteration

## Control flow structures

- Sequential: statements run top to bottom.
- Conditional: `if` / `if-else` / `if-elif-else`.
- Loop: `while` and `for`.
- Also: function call, exception handling.

## Boolean conditions

- `and`, `or`, `not` with standard truth tables.
- Comparisons always produce True/False: `if x == 5`, `if a < b`. `if x = 5`
  is an error.
- Truthiness: `if 0:` False, `if 10:` True, `if []:` False, `if [1,2]:` True,
  empty string/dict/set falsy, `if x is None:` for None.
- `bool` is a subclass of int: `True == 1`, `False == 0`.

## if / elif / else

```python
if expr:
    statements
elif expr2:
    statements
else:
    statements
```

- Colon `:` required; indentation defines the block (4 spaces or one tab,
  consistent).
- `elif` chains: only the first matching branch runs.
- Nested `if`: indentation determines which `else` belongs to which `if` - the
  coffee example (drink + size) shows how a misplaced block changes the
  output.

## Conditional expression

```python
value_if_true if condition else value_if_false
```

## match-case (Python 3.10+)

```python
match variable:
    case 'p1':
        ...
    case 'p2':
        ...
    case _:
        default
```

`case _` is the wildcard/default.

## while loop

```python
while expression:
    statement1
    statement2
```

- Condition is a boolean expression; statements should eventually make it
  False, or you get an infinite loop (dead loop). Quit with Ctrl+C.
- `pass` is a placeholder that does nothing.
- Body may never run if the condition is initially False.
- Useful `while True:` loops: input until quit command, server/event listener,
  game loop.

## for loop

```python
for iterating_var in sequence:
    statements
```

- Iterates over each item of a sequence: list, tuple, string, range.
- `range(stop)`, `range(start, stop)`, `range(start, stop, step)` - `stop` is
  exclusive. `range(20)` covers push-ups 0..19.
- `range()` returns a lazy range object (not a list): memory efficient, supports
  indexing/slicing.

## break / continue / else

- `break`: exit the loop immediately.
- `continue`: skip the rest of the current iteration, start the next.
- `for ... else` / `while ... else`: the `else` block runs if the loop finishes
  normally (no `break`).
- Dead loop in `for`: iterate over a list while appending to it (the loop keeps
  finding new items).

## Console I/O

- `input(prompt)` reads a line as a **string**; cast with `int()`/`float()`.
- `sys.argv` is the command-line argument list: `sys.argv[0]` is the script
  name, `sys.argv[1:]` the arguments (all strings).
- `__name__ == "__main__"` guard:
  ```python
  if __name__ == "__main__":
      main()
  ```
  `__name__` is `"__main__"` when run directly; otherwise the module name.

## Formatting quick reference

- f-string: `f"{x:.2f}"`, alignment `f"{x:>8}"` (right), `:<` (left), `:^`
  (center).
- `%`-style: `"%0.2f" % x`, `"%5d" % n`, `"%10.2f" % x`.

## Common exam patterns

- Count with a loop + accumulator.
- Sum even numbers / filter with `% 2`.
- Loop over `range(len(list))` when you need indices; `for item in list` when
  you only need values.
- Leap-year, even/odd, and top-N frequency tasks (see Labs 2 and 3).
