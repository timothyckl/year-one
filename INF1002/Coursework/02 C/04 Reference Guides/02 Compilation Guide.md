# Reference Guide - Compilation, Makefiles, and the Toolchain

This guide covers the build pipeline, command-line compilation, the Makefiles used by the
labs, and common build errors.

---

## 1. The C build pipeline

1. **Editor** - you write `program.c`.
2. **Preprocessor** - processes `#include`, `#define`, etc.
3. **Compiler** - translates to object code.
4. **Linker** - links object code with the libraries to make an executable.
5. **Loader** - loads the program into memory.
6. **CPU** - executes it.

## 2. Command-line compilation

Linux/macOS (GNU gcc or clang):

```
gcc -o hello hello.c      # compile + link -> executable "hello"
./hello                   # run it
```

Windows (Visual Studio's `cl`, from "Developer Command Prompt"):

```
cl hello.c                # creates hello.exe (and hello.obj)
hello.exe                 # run it
```

Useful flags (recommended):

```
gcc -Wall -Wextra -std=c99 -o prog prog.c   # warnings + standard
gcc -g -o prog prog.c                        # debug symbols (gdb/lldb)
gcc -fsanitize=address -g -o prog prog.c     # runtime memory checking
```

- `-Wall -Wextra`: enable warnings (the module emphasises: *pay attention to compiler
  warnings*).
- `-fsanitize=address`: catches buffer overflows and use-after-free (great for Lab 4).
- `-lm`: link the math library (`<math.h>`), only if you use it (not needed for the labs).

## 3. What the course Makefiles do

Every lab ships the same `Makefile`:

```make
CC = gcc
SRC = $(wildcard *.c)
OUT = $(basename $(SRC))

all: $(OUT)

%: %.c
	$(CC) $< -o $@

run: all
	./$(OUT)

clean:
	rm -f $(OUT)
```

- `SRC = $(wildcard *.c)` collects every `.c` file in the folder.
- `OUT = $(basename $(SRC))` derives the executable names (same basename).
- `all` builds each `%.c` into a same-named executable.
- `run` runs it; `clean` removes the binaries.

Usage:

```
make          # build
make run      # build + run
make clean    # remove executables
```

Manual equivalents (single-file labs):

```
gcc guessInteger.c -o guessInteger
./guessInteger
```

## 4. Notes / caveats about the supplied Makefiles

- No `-Wall`/`-Wextra` - warnings are not surfaced. Add flags if you want them:
  `make CC="gcc -Wall -Wextra"` or edit a copy.
- `$(wildcard *.c)` means **one `.c` file per folder is assumed**. Lab 4/5 ship exactly one
  source file, so it is fine. If a folder ever contains more than one `.c`, `OUT` will hold
  multiple targets (which also works), but `run` only executes `./OUT` (multiple words -
  would misbehave).
- No dependency tracking; a full rebuild is cheap here so it does not matter.
- `clean` uses `rm -f`, so it will not complain if binaries are absent.

## 5. Checking for leaks / memory errors (recommended for Labs 4-5)

- AddressSanitizer: compile with `-fsanitize=address -g`, then run normally.
- Valgrind (Linux/macOS via brew):
  ```
  valgrind --leak-check=full ./insertionSort
  ```

## 6. Portability

- All module programs should compile with any modern compiler.
- Avoid system-specific features. Note that `<regex.h>` (used by the supplied `tinyGrep.c`)
  is POSIX-only, not standard C (see Lab 2 README).

## 7. Common build errors and fixes

| Error | Likely cause | Fix |
|-------|-------------|-----|
| `undefined reference to 'sqrt'` | math library not linked | add `-lm` |
| `implicit declaration of function` | missing prototype/header | add prototype or `#include` |
| `ld: symbol(s) not found` / `linker command failed` | missing `main` or object | check for a `main` |
| `fatal error: 'regex.h' file not found` | POSIX header missing (MSVC) | hand-roll matching (Lab 2) |
| `warning: unused parameter` | `main(int argc, ...)` not using args | cast to void or use them |
| `permission denied` on `./prog` | binary not executable | `chmod +x prog` or rebuild |
