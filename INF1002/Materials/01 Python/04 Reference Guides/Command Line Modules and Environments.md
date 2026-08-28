# Command Line, Modules and Environments

How programs get input, how modules work, and how to set up an environment.

## Running Python

```bash
python3 script.py            # run a file
python3 script.py arg1 arg2  # pass arguments
python3                      # interactive shell
```

In Google Colab notebooks use `!python script.py` (the `!` prefix runs a shell
command).

## sys.argv

```python
import sys
print(sys.argv)          # list; [0] is the script name
first = sys.argv[1]      # first user argument (a string!)
nums = [int(n) for n in sys.argv[1].split(",")]
```

- Always strings - cast with `int()`/`float()`.
- Guard against missing arguments with `len(sys.argv)`.

## input() from keyboard

```python
name = input("Enter name: ")   # returns a string
age = int(input("Enter age: "))
```

## __name__ and __main__

- `__name__ == "__main__"` only when the file is run directly.
- When imported, `__name__` is the module's name.
- Use the guard so imports do not execute your test code.

## Modules

```python
import myMath                      # whole module
myMath.add(1, 2)

import myMath as m                 # alias
m.add(1, 2)

from myMath import add, evenNum    # direct names
add(1, 2)

from myMath import *               # everything (use sparingly)
```

- Import searches the current directory first, then `PYTHONPATH`, then the
  standard library / site-packages.
- Module file must be a `.py` in the same directory (or on the path).
- `help(myMath)` shows module + function docstrings; `dir(myMath)` lists names.
- If you move a module elsewhere (e.g. into the Python `Lib` folder - Lab 4
  warm-up), `import myMath` works from anywhere.

## Built-in / standard modules

`math`, `datetime`, `random`, `os` (+ `os.path`), `sys`, `functools`
(for `reduce`), `collections` (for `Counter`).

## os.path helpers

```python
os.path.join("a", "b.txt")      # 'a/b.txt' (platform-safe)
os.path.split(p)                # ('a', 'b.txt')
os.path.splitext("a.txt")       # ('a', '.txt')
os.path.exists(p)               # True/False
os.mkdir("folder")              # create directory
```

## Installing Python and packages

- Python 3 via official installer, or Conda/Miniconda.
- `pip install <package>` installs packages into the active environment.
- Conda: create an environment, activate it, then `pip install` inside it.
- IDEs used in class: VS Code, PyCharm. Try run, debug (F5), breakpoint,
  Step over (F10), Step into (F11), and inspect variable values.

## Git quick start

```bash
git config --global user.name "Your Name"
git config --global user.email you@example.com
git init project1
cd project1
git add file1.txt
git commit -m "my first commit"
```

GitHub workflow: `git clone <url>`, `git branch`, `git checkout -b main`,
edit, `git add *.py`, `git commit -m "..."`, `git remote add origin <url>`,
`git push origin main`. For SSH: `ssh-keygen -t rsa -b 4096 -C your@email.com`,
add the `.pub` key at github.com/settings/keys.

## Compiler vs interpreter

- Compiler (GCC, Clang, MSVC): whole file -> machine code before running;
  independent executable; errors at compile time.
- Interpreter (Python, Ruby, V8): line by line at runtime; source must be
  present; immediate feedback. CPython compiles to bytecode then executes it.
