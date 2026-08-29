# C Lab 3 - guessWord

## Objectives

1. Understand and apply pointer concepts.
2. Understand and apply the relationship between pointers and arrays in C.

---

## Lab Requirements

**In-lab exercises:**
- Exercise 1: find and fix the errors in pointer fragments (e.g. `number = zPtr;`
  assigning a pointer to an int; `number = *zPtr[2];` wrong dereference of an element;
  a `for (i = 0; i <= 5; i++)` loop reading past a 5-element array).
- Exercise 2: pointer basics with `long` variables - declare `long *lPtr`, assign the
  address of `value1` to `lPtr`, print the pointed-to value, copy it to `value2`, and print
  both the address of `value1` and the address stored in `lPtr` (they should match).

**Graded assignment (Gradescope, 5 test cases, max 5 marks):**
- Write `guessWord.c`: a two-player word-guessing game (Hangman-like).
  1. Player 1 enters a word of up to 12 letters, letters only.
     - Upper-case letters are converted to lower-case.
     - Punctuation/digits -> "Sorry, the word must contain only English letters." and
       re-prompt.
     - No dictionary check needed.
  2. Player 2 guesses one letter at a time, max 7 wrong guesses.
     - Each round prints a row with one underscore per letter; previously-guessed letters
       show in place.
     - Upper-case guesses are converted to lower-case; a punctuation/digit guess counts as
       a wrong guess.
     - Correct letter reveals *every* occurrence at the next round.
  3. Game ends when all letters are revealed ("Player 2 wins.") or after 7 wrong guesses
     ("Player 1 wins.").

**Formatting:** macros for constants; no `sys.argv`; `ctype.h`/`string.h` allowed; helper
functions encouraged; no space after `:`; every print ends with `\n`; "guess" singular
when 1 remains; comment each section.

---

## Supplied Implementation (`guessWord.c`)

The supplied file is a **complete, working solution**. Structure:

- `read_line(buf, n)` - `fgets` wrapper that strips the trailing newline.
- `validate_and_to_lower(s)` - checks length (0 < len <= 12) and that every char is a
  letter; lower-cases each char via `tolower`; returns 0 on failure.
- `print_progress(word, revealed)` - prints one char per slot (letter if revealed, else
  `_`) separated by single spaces.
- `reveal_letter(word, revealed, guess)` - reveals all positions of a letter, returns how
  many were newly revealed.
- `all_revealed(revealed, len)` - true when every slot is revealed.
- `main()` - loops: print progress; if all revealed -> "Player 2 wins."; ask for a letter;
  letters are lower-cased and revealed (0 new => wrong_guesses++), non-letters also count
  as wrong; then re-check win/loss.

Compile status: verified with `gcc -Wall -Wextra` (no warnings). Behaviour matches
Example 1 (verified below). Note the extra defensive `word_len = MAX_WORD_LEN`
cap and the `word` buffer of `MAX_WORD_LEN + 64` bytes (extra room for safety).

### Verified transcript

Input `Topsy-turvy` (rejected), `Cat`, then guesses `e a c t`:

```
Player 1, enter a word of no more than 12 letters:
Sorry, the word must contain only English letters.
Player 1, enter a word of no more than 12 letters:
Player 2 has so far guessed:
_ _ _
Player 2, you have 7 guesses remaining. Enter your next guess:
Player 2 has so far guessed:
_ _ _
Player 2, you have 6 guesses remaining. Enter your next guess:
Player 2 has so far guessed:
_ a _
Player 2, you have 6 guesses remaining. Enter your next guess:
Player 2 has so far guessed:
c a _
Player 2, you have 6 guesses remaining. Enter your next guess:
Player 2 has so far guessed:
c a t
Player 2 wins.
```

---

## Compile / Run

```
make            # builds guessWord
./guessWord     # run it (or: make run)
make clean
```

Manual: `gcc guessWord.c -o guessWord`

---

## Expected Behaviour

- The progress row prints each round *before* prompting, with single spaces between slots
  (`_ a _`), ending with `\n`.
- "Player 2, you have N guesses remaining. Enter your next guess:" on one line.
- A correct guess that completes the word prints the final row and "Player 2 wins."
- After the 7th wrong guess the last row is printed, then "Player 1 wins."
- Example 2 has a typo (a line missing the trailing colon); the
  code always prints the colon.

---

## Pitfalls

- **Exact strings**: "Player 2 has so far guessed:", "Player 2 wins.", "Player 1 wins.",
  "Sorry, the word must contain only English letters." - copy them exactly, including
  periods.
- **Pluralisation**: "7 guesses ... Enter your next guess:" vs "1 guess ...".
- **Uppercase conversion**: Player 1's word *and* Player 2's guesses must be lower-cased.
- **Non-letter guesses count as wrong** but are *not* converted; they must not reveal
  anything.
- **Reveal every occurrence**: guessing `a` in "banana" reveals all three `a`s.
- **Input longer than the buffer**: `read_line` reads up to `n-1` chars; a huge input line
  is truncated and the leftovers remain in stdin for the next read - a known rough edge in
  the supplied code (see safety notes).

---

## Safety / Correctness Issues in the Supplied Code

1. **Buffer handling on long input**: `read_line(word, sizeof(word))` reads at most 75
   chars. If Player 1 types a longer line, `validate_and_to_lower` fails (len > 12) and
   the leftover characters are consumed by the *next* `fgets`, producing a confusing
   second rejection. Not a crash, but not clean behaviour for hostile input. A `while
   (getchar() != '\n');` flush after an over-long line would fix it.
2. **`validate_and_to_lower` returns 0 for `len > MAX_WORD_LEN`** and the program re-asks.
   That matches the spec ("no more than 12 letters") but note a 13-letter word is silently
   treated like a punctuation error (same message).
3. **Duplicate win check**: the win condition is evaluated both at the top of the loop and
   right after each guess, so the win path prints the progress row twice (once via the
   top-of-loop print, once via the dedicated win print). The transcript matches the
   expected output only because the examples show the same duplication. It works, but the
   control flow is redundant.
4. **`strlen` on a truncated word**: with the buffer approach this is safe; with a
   hand-rolled loop it would not be.
5. `isalpha((unsigned char)g)` and `tolower((unsigned char)g)` are correctly cast - a good
   example to copy.

---

## Lessons

- Pointer/array concepts (Objectives 1-2) are exercised by passing the `revealed` array
  into helpers; note that arrays decay to pointers, so `revealed` is modified in place.
- A small set of focused helper functions (validate, reveal, progress, all-revealed) keeps
  the game logic readable - this is the recommended pattern.
- `tolower`/`isalpha` on `unsigned char` values is the portable way to use `<ctype.h>`.
- Testing the full transcript (including rejected input and the exact win/loss strings)
  is essential for the exact-output autograder.
