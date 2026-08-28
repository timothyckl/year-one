# INF1002 - C Study Library

Organised notes for the C portion of INF1002 Programming Fundamentals (Weeks 8-12).

## Structure

```
02 C/
|-- README.md                    <- this index
|-- 01 Lectures/                 one substantive note per lecture document (6 files)
|-- 02 Labs/
|   |-- Lab 1 - guessInteger/    README per lab
|   |-- Lab 2 - tinyGrep/
|   |-- Lab 3 - guessWord/
|   |-- Lab 4 - insertionSort/
|   `-- Lab 5 - miniTar/
|-- 03 Revision Notes/           consolidated cross-lecture notes + quiz checklist
`-- 04 Reference Guides/         syntax, compilation, pointers/memory, file handling
```

## How to use this library

1. **Reading order:** `01 Lectures/` in week order (8a, 8b, 9, 10, 11, 12).
2. **Before each lab:** read the matching `02 Labs/Lab N/README.md`, which summarises
   the objectives, the exact spec, how the supplied code behaves (or what is missing),
   how to build/run it, and the bugs/safety issues worth knowing.
3. **Revision:** `03 Revision Notes/` condenses the whole module; `04 Quiz Checklist.md`
   is the exam-topic checklist. The quiz is closed-book and allows a calculator.
4. **Quick lookups while coding:** `04 Reference Guides/` (syntax, compiler + Makefile,
   pointers/memory, file I/O).

## Notes on the supplied lab code

The supplied lab files are as-found and are **not** all complete or correct:

- **Lab 1 `guessInteger.c`** - complete, working solution (verified). Minor nits only:
  `atoi` overflow path, an uncast `isdigit` argument, and `exit()` inside a helper.
- **Lab 2 `tinyGrep.c`** - complete but **deviates from the spec**: it uses POSIX regex
  (`<regex.h>`) instead of hand-written matching, only translates `_` when it is the whole
  pattern (a real bug - multi-character patterns containing `_` fail), and treats regex
  metacharacters as regex rather than literally. Details in its README.
- **Lab 3 `guessWord.c`** - complete, working solution (verified). Duplicate win check
  and long-input buffering rough edges noted.
- **Lab 4 `insertionSort.c`** - **unimplemented skeleton** (empty `main`). You must write
  the linked-list insertion sort yourself.
- **Lab 5 `miniTar.c`** - **unimplemented skeleton** (struct + empty `main`). You must
  implement the tar archiving yourself.

All five compile cleanly with `gcc -Wall -Wextra` (as supplied); Lab 1/3 transcripts and
Lab 2's printed examples were re-run and match. See each lab README for the verified
behaviour and the flagged defects.

## Validation

- Compilation checked with `gcc -Wall -Wextra` (macOS clang-based gcc).
- Behaviour of `guessInteger`, `guessWord`, `tinyGrep` checked against the sample
  transcripts.
- `File1.txt`/`File2.txt` sizes (31 and 62 bytes) confirmed to match the octal sizes
  (`00000000037`, `00000000076`) quoted for Lab 5.

## Conventions

- Plain ASCII throughout (no em-dashes or smart quotes).
- Bugs and unsafe patterns in supplied code are called out explicitly rather than taught
  as good practice.

## Related sections

- Python portion: `../01 Python/`
- Other material (tools, algorithms/complexity): `../03 Misc/`
- Assessments (project, quiz, report): `../04 Assessments/`
