# C Lab 2 - tinyGrep

## Objectives

1. Write functions to implement some features.
2. Understand and apply array concepts.
3. Understand C strings and use the standard string library for manipulating strings.

---

## Lab Requirements

**In-lab exercises:**
- Exercise 1: given array declarations, print values of expressions such as `a[3]`,
  `b[3]`, `b[a[1]]`.
- Exercise 2: character/string expressions using `strlen(a)`, `strlen(b)`, `strcmp(a, b)`,
  etc.
- Exercise 3: read a sentence of up to 255 chars, split into words (space/punctuation
  delimited) and print each word with its length line by line. Extra: recognise a hidden
  "magic word" and print "You said the magic word!". Hints: `fgets(buffer, n, stdin)`,
  `<ctype.h>`.

**Graded assignment (Gradescope, 10 test cases, max 5 marks):**
- Write `tinyGrep.c`: a mini version of the Unix `grep` searching keyboard input.
  1. Ask for a line of text (up to 255 chars).
  2. Ask for a pattern (up to 255 chars).
  3. Ask whether matching is case-sensitive ("Y"/"N").
  4. Print whether the pattern occurs and the index of the first occurrence.

**Pattern rules:**
- A letter matches itself (case-sensitive: exact case; case-insensitive: any case).
- A dot `.` matches any character.
- An underscore `_` matches any form of whitespace (i.e. any char for which `isspace()`
  returns true).
- All other characters match only themselves.

**Formatting:** no whitespace after `:`; every print ends with `\n`; no `sys.argv`; use
`scanf`/`fgets`/`fgetc`; implement the match logic yourself (a suggested approach is to
start with `strncmp`, then add case handling, then dot/underscore).

---

## Supplied Implementation (`tinyGrep.c`)

The supplied file is a **complete solution that does NOT follow the suggested approach**:
instead of hand-rolling the match loop, it uses the POSIX **regular expression engine**
(`<regex.h>`):

- `get_input(prompt, buffer, size)` - prints the prompt and reads a line with `fgets`,
  stripping the trailing newline.
- `first_regex_match(text, pattern, case_sensitive, out_start)` - compiles the pattern
  with `regcomp` (flags `REG_EXTENDED | REG_NEWLINE`, plus `REG_ICASE` when case-insensitive),
  runs `regexec`, and stores the match start offset `rm_so` in `*out_start`. If the whole
  pattern is exactly `"_"` it substitutes `"[[:space:]]"`.
- `main()` - reads text, pattern, and the Y/N answer; if the answer is not exactly "Y" or
  "N" it returns `EXIT_FAILURE`; prints "Matches at position %zu." or "No match.".

Compile status: verified with `gcc -Wall -Wextra` (no warnings). Behaviour matches the two
examples (verified below).

### Verified transcripts

Example 1 (input `The cat sat on the mat.` / `cat` / `N`):

```
Matches at position 4.
```

Example 2 (input `The cat sat on the mat.` / `rat` / `N`):

```
No match.
```

Case-sensitive "the" in "The cat sat on the mat." matches at position 15 (matches the
table in the source file's header comment).

---

## Compile / Run

```
make            # builds tinyGrep
./tinyGrep      # run it (or: make run)
make clean
```

Manual: `gcc tinyGrep.c -o tinyGrep`

Note: `<regex.h>` is POSIX. It exists on macOS/Linux/BSD but is **not** part of standard C
and is absent from MSVC's `<regex.h>` (Windows users on the course toolchain may need a
POSIX layer or to hand-roll the matching instead).

---

## Expected Behaviour

- Positions are 0-based (e.g. "the" inside "The cat..." is at position 4 case-insensitively).
- Case-insensitive matching via `REG_ICASE`.
- `.` in the pattern matches any character.
- `_` as the whole pattern matches any whitespace.
- Non-Y/N answer exits the program (the supplied code does this rather than re-prompting).

---

## Pitfalls

- **Autograder exactness**: prompts and output strings must match exactly (no space after
  the colons, `\n` on every line, "Matches at position N." with the period).
- **0-based vs 1-based indexing**: the expected answer is the index of the first matching
  character in the string, starting at 0.
- **`fgets` keeps the trailing newline**: remember to strip it before searching, or a
  pattern ending in a letter will never match the end of the line.
- **The "first occurrence" rule**: report only the first match position.

---

## Safety / Correctness Issues in the Supplied Code (important)

The supplied solution is elegant but **deviates from the spec in several ways**, and the
requirements explicitly ask you to *implement the matching yourself*
("Replace `strncmp()` with a new function..."). Treat this file as a reference, not as the
expected approach.

1. **Underscore handling is incomplete (a real bug).** The spec says `_` matches any
   whitespace *anywhere in the pattern*. The code only translates the pattern when the
   *entire* pattern equals `"_"`:
   ```c
   pattern = (strcmp(pattern, "_") == 0) ? "[[:space:]]" : pattern;
   ```
   A pattern like `a_at` (which per spec should match "a at" or "a\tat") returns
   "No match." (verified). Inside a regex, `_` is a literal underscore, not whitespace.
   This fails the spec for any multi-character pattern containing `_`.
2. **Other pattern characters are NOT escaped.** The spec says "All other characters match
   only themselves." But the pattern is fed straight to `regcomp`, so regex metacharacters
   such as `*`, `+`, `?`, `(`, `)`, `[`, `{`, `^`, `$`, `|`, and `\` are interpreted as
   regex syntax rather than matched literally. For example, pattern `a*b` matches "ab",
   "aab", etc., not the literal text `a*b`.
3. **Case-insensitive matching is delegated to `REG_ICASE`**, which is POSIX regex
   behaviour and can differ subtly from the letter-by-letter rule in the spec (e.g. locale
   handling of non-ASCII letters).
4. **Non-Y/N input exits** with `EXIT_FAILURE` rather than re-prompting. The spec does not
   define this behaviour; a hand-rolled loop that re-prompts on bad input would re-ask.
5. **`get_input` prints the prompt with `printf("%s\n", prompt)`**, which adds a newline
   *after* the prompt (the spec wants the prompt text to end with `:` and a `\n` for the
   transcript - fine - but note the prompts in the sample output appear with the text and
   a newline, which this matches).
6. `strlen`/`strncpy` are used on `pattern` after a possible substitution - fine; no memory
   issue since the substituted literal is a compile-time string.

**Honest recommendation for the graded task:** implement the matcher yourself, as
suggested - iterate over the text with a candidate start index, compare characters position
by position honouring case-sensitivity, `.` (any char), and `_` (`isspace`), and report the
first start index found. That is deterministic, portable (no `<regex.h>`), and exactly
matches the spec.

---

## Lessons

- Even a "supplied solution" can deviate from the spec; always test against the sample
  table in the header, including edge patterns (`_` inside a pattern, special characters).
- Using a library (regex) trades exactness for brevity - here it is a mismatch with the
  requirements.
- Reading whole lines with `fgets` and stripping the newline is the safe way to handle
  input containing spaces.
- Breaking the problem into small functions (read input, one match function with
  parameters for case-sensitivity) matches the pattern the spec recommends.

