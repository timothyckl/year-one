# Lecture 4 - Functions

## Outline

1. File I/O (read / write).
2. Functions (default, positional/keyword args, mutability, scope, annotations).
3. Modules.
4. Higher-order functions.

## File I/O

- Why files: data in memory is lost when the program exits; files keep data
  permanent (survives reboots).
- Workflow: open a file -> process the data (load from disk to memory) ->
  write data to disk -> save the file.
- `open()`: creates a file object. Syntax:
  `file_object = open(file_name[, access_mode] [, encoding])`.
  - `file_name`: string path.
  - `access_mode`: read/write/append; optional, default `r`.
  - `encoding`: default depends on OS; utf-8.
- Modes:

  | Mode | Description |
  |------|-------------|
  | `r` | Read only (default). |
  | `r+` | Read and write. |
  | `w` | Write only; overwrites if the file exists, creates if not. |
  | `w+` | Write and read; overwrites existing file. |
  | `a` | Append; creates if not exists. |
  | `b` | Binary mode: `rb`, `wb`, `ab`, ... |

- Reading a file line by line:
  ```python
# while + readline
while True:
    line = file.readline()          # empty string -> end of file
    if not line:
        break
    if line.strip():
        students.append(line.strip())
# or: for line in file
for line in file:
    line = line.strip()
    if line:
        students.append(line)
```
  One-line alternative: `students = [line.strip() for line in file.readlines()
  if line.strip()]`.
- Read methods:
  - `readline(size)`: size caps the number of characters read (until newline or
    `size` chars).
  - `readlines(hint)`: returns a list of lines; hint suggests an approximate
    character budget but complete lines are still returned.
  - `read(size)`: read a block.
- Working example: a `student.txt` with lines `000 John 100`
  etc.; append a new student whose id = max id + 1, zero-padded to 3 digits
  with `str(new_id).zfill(3)`, then write:
  ```python
with open('student.txt', 'w', encoding='utf-8') as file:
    for student in students:
        file.write(student + '\n')
```
- Write methods: `write(str)`; `writelines(sequence)` (note whether
  lines include newline characters).
- File pointer: indicates the current position. After
  `file.read(4)` the pointer advances, so a second `file.read(4)` reads the
  next 4 characters (`f'{seg1=}'` shows the value).
- `with open(...) as file:`: automatically opens and closes the
  file, even if an exception occurs. Multiple files:
  ```python
with open('input.txt', 'r') as infile, open('output.txt', 'w') as outfile:
    ...
```

## Functions

- A function is a block of organized, reusable code that performs a single,
  related action (like functions of a smartphone).
- Definition:
  ```python
def function_name(arguments):
    '''function_docstring'''     # optional comment
    function_suite
    return [expression]          # optional
```
  - Function name: the identifier.
  - Arguments: inputs; formal arguments (names used inside) vs actual arguments
    (real values at the call site).
  - Docstring: comment; optional.
  - Return: optional; exits the function, passing a value back to the caller.
- When calling, the number of arguments must match the definition.
- Example - an `AverageCalculator` that reads `sys.argv[1:3]`,
  validates with a helper `is_number(s)` using `try: float(s)`/`except
  ValueError`, prints `Average:{average:.2f}` or `Your input is invalid!`.
- Positional vs keyword arguments:
  - Positional: passed in order; order matters.
  - Keyword: passed by name; order does not matter.
  - `open('student.txt', 'r', 'utf-8')` (positional) vs
    `open('student.txt', 'r', encoding='utf-8')` (keyword).
- Positional-only / keyword-only arguments: robustness and
  readability when there are many parameters.
- `*args`: accept any number of positional arguments, packed into a
  tuple:
  ```python
def sum_numbers(*args):
    total = 0
    for number in args:
        total += number
    return total
sum_numbers(10, 20)      # 30
sum_numbers(1, 2, 3, 4, 5)  # 15
```
- `**kwargs`: accept any number of keyword arguments, packed into a dictionary.
- Do function calls change the caller's variables?
  - Immutable (str, int, float, tuple): NOT changed. Example
    `AverageModifier(average)` does `average += 10` inside, but the caller's
    `average` stays `0.0` (the local rebinding gives a new id).
  - Mutable (list, dict): CAN be changed. Example `AddAScore(scores)` does
    `scores.append(10)`; the caller's list becomes `[0.0, 10]` and keeps the
    same id.
- Scope:
  - Local variable: defined inside a function, exists only while the function
    runs.
  - Global variable: defined at module level; readable inside functions, but to
    reassign it you need `global` (otherwise you get `UnboundLocalError: local
    variable 'marks' referenced before assignment`).
  - Nonlocal variables: an advanced topic.
- Function annotations / type hints: annotate parameters and return
  values (`def f(x: int) -> str:`); they improve readability and IDE support,
  allow type checking, and do not affect runtime logic.

## Modules

- A module is a file (`.py`) containing definitions of functions, classes,
  variables, constants. `import` loads it.
- Create your own module: write functions in a `.py` file saved in the same
  directory as the script, give it a descriptive name.
- Use: `import module_name` (no `.py` extension), call
  `module_name.function_name()`; optionally `import module_name as short_name`.
- `from module_name import load_data, process_data` lets you call the function
  name directly; useful when a module has many functions (saves loading time).
- `help(module_name)` shows the module docstring and function docs;
  `dir(module_name)` lists its names.
- Built-in modules mentioned: `math`, `datetime`, `random`, `os`, `urllib2`.
- File path helpers: `os.path.join()`, `os.path.split()`,
  `os.path.splitext()`, `os.path.exists()`, `os.mkdir()`.
- Hierarchy: Library -> Package -> Module (module1.py, module2.py,
  subpackage with more modules).
- Data-science libraries: Scrapy, BeautifulSoup, NumPy, SciPy,
  Pandas (Series + DataFrame), Matplotlib, Seaborn, PyTorch, Transformers,
  SciKit-Learn, TensorFlow.
- Benefits of modules: code organization, reuse, namespace
  isolation (no naming conflicts), maintainability.

## Higher-order functions

- Functions are objects in Python; the only special thing is they are callable.
- A higher-order function takes a function as an argument and/or returns a
  function. Functions can be assigned to variables, passed, and returned like
  any reference.
- Returning a function:
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
add_fn = operation_factory('add')     # add_fn(10, 5) -> 15
```
- `sorted(iterable, key=func)`: custom sort. `key` is a function
  that takes one value and returns the value to sort by; e.g. sort by absolute
  value with `key=abs`, or by last character with a custom function.
- `map(function, iterable, *iterables)`: applies `function` to
  each item and returns an iterator of results; same order, same length as the
  (shortest) input. With multiple iterables the function must take the same
  number of arguments; result length = length of the shortest iterable.
  - Iterators: loop over values one at a time with `next()`; raise
    `StopIteration` when exhausted. Lazy (values produced on demand, memory
    efficient); one-time use, forward only, no indexing, cannot restart.
  - Example: square a list with `map(square, numbers)`; extract scores from
    `['John\t100', 'Josh\t90', ...]`.
- `filter(func, sequence)`: keeps items for which `func` returns
  True; returns an iterator.
- Map/filter vs list comprehension:
  - `map`/`filter` are lazy (compute on demand via `list()`, a `for` loop, or
    `next()`); list comprehension is eager (immediate full list).
  - Equivalent forms shown:
  ```python
result = map(double, numbers)          # lazy iterator
result = [double(x) for x in numbers]  # eager list
result = filter(is_even, numbers)
result = [x for x in numbers if is_even(x)]
```
- `reduce(func, seq)`: takes a sequence and returns a single
  value. `func` takes two values and returns one: consume first two -> return a
  value -> consume next with the returned value -> repeat -> return final value.
  In Python 3 it is not a built-in; import from `functools`:
  `from functools import reduce`.
- Note: these functions do not change the original sequence; a new
  sequence is created for the output. (The Lab5 `sort()` implementation is an
  exception because it mutates in place - see the Lab5 README.)
