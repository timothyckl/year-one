# Lecture 8a - Set Up Integrated Development Environment (IDE) for C

**Slides:** 6 pages
**Lecturer context:** INF1002 - Programming Fundamentals, Week 8.

---

## Overview

This topic is practical setup guidance only. It tells you how to get a C compiler
running on Windows, macOS, and Linux, plus one cross-platform IDE (Code::Blocks). There is
no C language content; the only code shown is the classic "Hello World" program used as a
compile-and-run smoke test.

---

## Setup by Platform

### Windows: Microsoft Visual Studio 2022 Community

Two ways are described:

1. **Within the IDE:**
   - Download and install Visual Studio 2022 Community from
     <https://visualstudio.microsoft.com/vs/community/>
   - Create a new solution and an "Empty Project" with a meaningful name.
   - Add a new "C++ File (.cpp)" under the "Source Files" folder.
   - Right-click the new file and rename it to end in `.c`.
   - Right-click the project and select "Build".
   - Find the resulting `.exe` in the "Debug" folder of the solution.

2. **From the command line:**
   - Open "Developer Command Prompt for Visual Studio" (search it from the taskbar).
   - Change directory to the folder holding your `.c` files.
   - Run `cl filename.c` - this produces both an `.exe` and an `.obj`.
   - Open a normal `cmd` window, go to the exe folder, and run the exe by name.

> Note: Visual Studio is a C++ IDE, so you have to create an "Empty Project" and rename
> the source file to a `.c` extension for it to be treated as C (see
> `02 Labs/Lab 1 - guessInteger`).

### macOS

- Download and install Xcode from <https://developer.apple.com/xcode/>
- Tutorials: <http://help.apple.com/xcode/mac/>
- Xcode bundles Apple's clang toolchain (which includes the GNU-style `gcc`/`clang`
  command line). On a terminal you can compile with `gcc` just like on Linux.

### Linux

- Most distributions already ship the GNU C Compiler (`gcc`).
- Compile from the command line:
  ```
  gcc -o hello hello.c
  ```

### Cross-platform: Code::Blocks

- Free, open source, cross-platform IDE for C/C++/Fortran.
- Download: <http://www.codeblocks.org/downloads>
- Windows users are directed to choose `codeblocks-17.12mingw-setup.exe` (the MinGW
  bundle so a compiler is included).

### Online options

- <https://repl.it/>
- <https://pythontutor.com/c.html#mode=edit>
- <https://www.hackerrank.com/domains/c>

---

## The Test Program

```c
/*
 * A simple C program.
 */
#include <stdio.h>

int main() {

           printf("Hello world!\n");

           return 0;

}
```

Compile and run this in whatever environment you chose to confirm the toolchain works.
Expected output:

```
Hello world!
```

---

## Observations

- All module programs should compile with any modern compiler and be portable; choosing a
  plain command-line `gcc`/`clang` toolchain (as used by every Lab Makefile in this
  course) is the most consistent choice.
- Every Lab's supplied `Makefile` in this module compiles with `gcc` using
  `%: %.c ; $(CC) $< -o $@` (no extra flags), so a working `gcc` command line is the
  only real requirement.

---

## Key Takeaways

1. C has no single "official" IDE; pick a compiler + editor you like.
2. The course labs assume a command-line `gcc`-style compiler on a Unix-like shell.
3. The `gcc -o hello hello.c` pattern is what all the Lab Makefiles reproduce.
