# Lecture 2 - Python Basics II

## Outline

1. Control flow: conditional control (`if`), loop control (`while`, `for`).
2. Input/output from the console.
3. String formatting.
4. Git basics.

## Control flow

- Control flow is the order in which statements/instructions/function calls of a
  program are executed.
- Main structures: sequential, conditional, loop, function call, exception
  handling.
- Sequential control: execute instructions top to bottom, in written order.

## Conditional control - truth values

- Decisions are based on the truth value of conditions.
- `bool` is a subclass of `int`: `True == 1`, `False == 0`, and `True + True`
  evaluates to `2`.
- Logical operators: `and`, `or`, `not` with the standard truth
  tables (`and` is True only when both are True; `or` is True when at least one
  is True; `not` flips the value).
- Objects evaluated as False in a boolean context:
  - `False` itself, `None`,
  - zero of any numeric type (`0`, `0.0`, `0j`, ...),
  - empty containers: `""`, `[]`, `{}`, `set()`, `range(0)`.
  Everything else is truthy.
- `None`: singleton of type `NoneType`, represents "no value"; the
  default return value of a function without a `return`; evaluates to False in
  a boolean context; always test with `is None` / `is not None`, not `==`.
- Identity operators: `is` / `is not` check whether two variables
  refer to the same object in memory; commonly used for `None`.

## if / if-else / if-elif-else

```python
if expr:
    statement(s)
```

- Expressions in `if`:
  - Comparisons: `if x == 5:`, `if a < b:` - always True/False. `if x = 5:`
    is an error (assignment is not a comparison).
  - Numbers: `if 0:` -> False; `if 10:` -> True.
  - Containers: `if []:` -> False; `if [1, 2, 3]:` -> True; same for strings,
    dicts, sets.
  - None: check explicitly with `if x is None:`.
- `if-else` and `if-elif-else`.
- Coffee-shop example: a chain of `elif` matching `drink ==
  "Americano"/"Latte"/"Tea"/"Hot Chocolate"`, setting a price, with an `else`
  printing "Sorry, we don't have that option."
- Details: a colon `:` is required at the end of the `if`/`else`
  lines; indentation defines the block (4 spaces or one tab, consistent).
- Nested `if`: be careful with indentation - the example nests a
  size check inside a drink check, and the price print is outside the inner
  `else`, which changes the message layout.

## Conditional expression

```python
value_if_true if condition else value_if_false
```

Use when a condition should produce a value.

## match-case

```python
match variable_name:
    case 'pattern 1':
        statement1
    case 'pattern 2':
        statement2
    ...
    case _:
        print('default')
```

Introduced in Python 3.10; `case _` is a wildcard like `switch`'s `default`.

## while loop

```python
while expression:
    statement1
    statement2
```

- The expression is a boolean expression deciding whether to stay in the loop;
  the statements change the result of the expression.
- `pass` does nothing when executed; it is a placeholder to keep code
  syntactically correct.
- Infinite loops: forgetting `pushup_count += 1` causes a dead loop,
  the most common cause of a program "hanging"; quit with restart or Ctrl+C.
  Useful `while True:` cases: continuous input loop until a quit command, server/
  event listener, game loop.
- The loop body may never execute if the condition is False at the start.

## for loop

```python
for iterating_var in sequence:
    statements
```

- Loops over the items of any sequence (list, tuple, string, range).
- `range()`: `range(stop)`, `range(start, stop)`,
  `range(start, stop, step)`; `stop` is exclusive.
- `range()` returns a range object, not a list - lazy (produces numbers on
  demand), memory efficient (stores only start, stop, step), supports indexing
  and slicing.
- Compare `for` and `while`: `while` continues while a boolean
  condition holds; `for` iterates over a sequence/iterable.
- Dead loop in `for`: iterating over a mutable sequence while appending to it.
- `break`: stops the loop when a condition is satisfied.
- `continue`: skips the remaining statements of the current iteration
  and starts the next one (example: "I don't do exercise on Sunday").
- `for ... else ...`: the `else` runs if the loop finished without
  hitting `break`.

## Console input

- Two input manners: keyboard and files.
- `input()` reads one line from standard input and returns it as a string; you
  can pass a prompt text that appears before the cursor.
- Convert the returned string with type casting if needed.

## Running files in Colab

- Create `first.py`, edit it, run with `!python first.py` (the `!` prefix is
  required in Colab).

## sys.argv

- `sys.argv` is a list of the command-line arguments.
- `sys.argv[0]` is the script name; `sys.argv[1]` .. `sys.argv[n]` are the
  additional arguments. All are strings - cast as needed.

## __name__ and __main__

- `__name__` is a special variable set by the interpreter telling how the file
  is being used.
- When the file is run directly, `__name__ == "__main__"` (the entry point).
- When imported, `__name__` is the module's name (e.g. `"first"`).
- Guard with:
  ```python
def main():
    pass  # put your code here

if __name__ == "__main__":
    main()  # call your main function here
```

## Output and print

- Concatenate strings with `+`: `print("Your age is: " + str(age))`.
- For non-string variables, convert with `str(...)`.

## f-string formatting

- Print variables: `f"{name}"`; expressions inside braces too.
- Alignment:
  - `{variable:>width}` right-aligned,
  - `{variable:<width}` left-aligned,
  - `{variable:^width}` centered.
  - `width` is the total number of characters.
- Float precision: `{variable:>[width].[precision]f}` - width is total number
  of digits, precision is the number of decimal digits; don't forget the `f`
  (without it you get something different).

## % formatting

- Format strings: `%<width>s`; integers: `%<width>d`.
- Floats: `%<width>.<precision>f` (percent sign, width, dot, precision, f).
  Width is the total length including the decimal point; precision is the
  number of decimal places; if width < real length, the real length is printed.

## Git

- Git != GitHub; Git is version control.
- Configure once: `git config --global user.name "Mona Lisa"` (quotes needed if
  the name contains spaces) and `git config --global user.email your@email.com`.
- Basics: `git init project1`, `git add file1.txt`, `git commit -m "message"`.
- GitHub flow: clone a repo, `git branch`, `git checkout -b main`, edit files,
  `git add *.py`, `git commit -m "..."`, `git remote add origin <url>`,
  `git push origin main`.
- SSH: generate a key with `ssh-keygen -t rsa -b 4096 -C your@email.com`, add
  the public key at github.com/settings/keys, then use the SSH repository URL.
