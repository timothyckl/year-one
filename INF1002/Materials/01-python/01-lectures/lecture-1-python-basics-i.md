# Lecture 1 - Python Basics I

## Outline

1. Python introduction (why Python).
2. Software-engineering concepts (problem solving, requirement analysis, design,
   implementation/debug, testing, deployment).
3. Python basic I: variables, assignment, naming, types, type casting,
   arithmetic operators.
4. Command line vs IDE, interpreters vs compilers.

## Python introduction

- Why Python: simple, powerful (community + ecosystem), strong application in
  machine learning; cross-platform. Popularity per the TIOBE index.
- "The Zen of Python" (PEP 20 style maxims) - highlights:
  - Beautiful is better than ugly. Explicit is better than implicit.
  - Simple is better than complex. Flat is better than nested.
  - Readability counts. Errors should never pass silently.
  - "There should be one - and preferably only one - obvious way to do it."
  - Now is better than never.
  - Namespaces are one honking great idea.

## First program, quotes, escape characters

- `print("Hello World!")` or `print('Hello World!')` - single and double quotes
  are generally interchangeable.
- Mixing quotes lets you embed the other kind:
  - `print('He said, "Python is awesome!"')`
  - `print("It's a beautiful day!")`
- To print `It's a "beautiful" day.` escape the quote with a backslash:
  - `print('It\'s a "beautiful" day!')`
  - `print("It's a \"beautiful\" day!")`
- Escape characters: `\n` newline, `\t` tab, `\\` backslash.
- Raw strings: prefix with `r` or `R` so backslashes are not escapes:
  - `print(r'it\n1\t2')` prints `it\n1\t2` literally,
  - `print(r"C:\Users\Name")` prints a Windows path without escapes.

## Case sensitivity and indentation

- Python is case sensitive: `print != Print != PRINT`.
- Indentation matters and defines blocks, especially in nested `if`/`for`/`while`.

## Comments

- Single line: `# comment`.
- Multi-line / docstring: `''' ... '''` (three quotes). A docstring in the first
  line of a module/function is used by `help()` (expanded in Lecture 5).

## Problem solving and software engineering

- A problem is defined by its inputs and the desired property of the output.
- Polya's method: understand the problem -> devise a plan ->
  carry out the plan -> look back.
- Software development methodology: Agile is one example.
- Requirement analysis: e.g., "store files into a database" - ask
  what kind of files, size, numbers, read/write frequency, stability/HA/perf.
- Design: design patterns are reusable solutions promoting
  maintainability, scalability, reusability.
- Implementation & debug: a logic error produces incorrect results
  (a "bug"). In VS Code: Run = F5, breakpoint pauses at a line, Step over = F10,
  Step into = F11.
- Testing: checking software satisfies expectations; automated
  testing; levels: unit, integration, system; use test cases to make programs
  robust.
- Deployment: local machine -> staging server -> production
  server -> monitor. Key concepts: CI/CD, version control (Git), rollback.
- Look back: monitor, get feedback, evaluate with numbers.

## Variables and assignment

- Syntax: `<variable name> = expression`. E.g. `firstName = 'Taylor'` then
  `print(firstName)`.
- A variable is a name we can reference later; values can be reset.
- Naming rules:
  - Start with a letter or underscore `_`; subsequent characters can be
    letters, digits, underscores.
  - Case sensitive. Keywords cannot be used (`if, else, for, while, class,
    def, return, try, except, ...`).
  - Valid: `_myvar`, `Var1`, `good`. Invalid: `1var`, `-var`, `return`.
- Style: snake_case for variables (`my_variable`, `total_sum`);
  UPPERCASE_WITH_UNDERSCORES for constants (`PI`, `MAX_LIMIT`); meaningful
  names; avoid single letters except loop counters (`i, j, k`).
- PEP 8: the Python community style guide; Python 3 allows Unicode
  identifiers but keep them readable/consistent.
- Memory: a variable is a reserved memory location; `id(var)` shows
  its memory address.
- Multiple assignment / swap: swap two values using a temp:
  ```python
  temp = first
  first = second
  second = temp
  ```

## Types and type casting

- `type(x)` reports the type. Basic types:
  `int` (8, 12, 1024), `float` (2.3, 3.1415926), `bool` (True, False),
  `str` ('Hello, World!', '3.1415926'), `None`, plus list, tuple, set,
  dictionary, byte.
- Type casting = explicitly converting a value from one type to another;
  dynamic typing = the variable's type is determined at runtime.
- Casting examples: numbers to string, string to numbers, precision adjustment.
- Casting `int`/`float` truncates toward zero - a common check asks "Why 70,
  not 71?" and "Why -100, not -101?" for `int(70.9)`-style conversions: `int()`
  truncates (rounds toward zero), it does **not** round. Round vs truncation
  is a recurring quiz concept.

## Arithmetic operators

| Operator | Name | Example / note |
|----------|------|----------------|
| `+` | Addition | |
| `-` | Subtraction | |
| `*` | Multiplication | |
| `/` | Division | always returns float; `10/0` -> ZeroDivisionError; `4/2` -> `2.0` |
| `//` | Floor division | `13.9//2` -> `6.0` |
| `%` | Modulus | `11%3` -> 2; `11.0%3.0` -> 2.0 |
| `**` | Exponent | `2**4` -> 16 |
| `+=` | Augmented addition | `a += 1` means `a = a + 1`; also `-=`, `*=` ... |

## Command line and IDE

- Python shell: interactive, results shown immediately; cons - hard to manage
  large projects, hard to refactor.
- IDE: a code editor + compiler + debugger + (often) GUI builder.
- Tools used in class: Google Colab and Jupyter notebooks.

## How Python runs

- High-level code is compiled to assembly, then assembled to machine code
  (compiled languages). Python instead compiles to bytecode.
- CPython is the interpreter that reads, parses, and executes Python code:
  translates high-level code to bytecode, then executes the bytecode.
- Compiler vs interpreter:
  - Compiler: translates the whole source file to machine code before running;
    produces an independent executable; faster at run time; errors detected at
    compile time. Examples: GCC, MSVC, Clang.
  - Interpreter: translates and executes line by line at runtime; source must be
    present; slower; immediate feedback for small snippets. Examples: Python,
    Ruby, JS engines (V8).

## Assignment

Install Python 3 (try `pip install`; Conda/Miniconda) and an IDE (PyCharm or
VS Code); create a simple program; try run, debug, breakpoint, and inspect
variable values. First lab from week 2.
