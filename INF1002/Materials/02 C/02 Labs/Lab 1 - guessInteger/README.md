# C Lab 1 - guessInteger

## Objectives

1. Choose, install, and configure a development environment suitable for C programming.
2. Comprehend C expressions and programs.
3. Write, compile, and debug C programs.

---

## Lab Requirements

**In-lab exercises (for understanding, not graded):**
- Exercise 1: type in, compile, and run "Hello World".
- Exercise 2: with given variable declarations, write programs to print the values of
  expressions such as `a / b`, `(b * 3) % 4`, `y / x`, and to print results using format
  specifiers `%4d`, `%x`, `%.2f`, `%10.1f`, `c =\t%c`.
- Exercise 3: BMI calculator - read weight (kg) and height (m), print BMI to one decimal
  place, and classify underweight/overweight per the given table.

**Graded assignment (Gradescope, 5 test cases, max 5 marks):**
- Write `guessInteger.c`: a two-player number guessing game.
  1. Player 1 enters a number between 1 and 1000 inclusive; out-of-range input prints
     "That number is out of range." and repeats.
  2. Player 2 has 10 rounds. Each round starts with "Player 2, you have n guess(es)
     remaining."
  3. "Enter your guess:" then evaluate: too high -> "Too high.", too low -> "Too low.",
     correct -> "Player 2 wins." and stop; out of range -> "That number is out of range."
     and re-prompt **without decrementing the guess count**.
  4. If not guessed within 10 rounds -> "Player 1 wins."

**Formatting rules (autograder is exact):** no whitespace after the colon in prompts; every
print ends with `\n`; use "guess" (singular) when 1 remains; use `#define` macros for all
constants; do **not** use `sys.argv` (that's Python); use `scanf`/`fgets`/`fgetc` etc.;
comment each section of the code.

---

## Supplied Implementation (`guessInteger.c`)

The supplied file already contains a complete, working solution. Structure:

- `trim(char *str)` - strips leading/trailing whitespace from a string.
- `is_numeric(const char *str)` - returns true only if every char is a digit.
- `is_in_range(int n, int a, int b)` - bounds check (inclusive).
- `is_match(int guess, int n)` - equality check.
- `higher_or_lower(int guess, int n)` - prints "Too high."/"Too low.".
- `announce_winner(int player)` - prints "Player i wins." then `exit(EXIT_SUCCESS)`.
- `main()` - reads Player 1's number with `fgets` + validation loop, then runs Player 2's
  10-round loop with an inner re-prompt loop for invalid/out-of-range guesses.

Compile status: verified with `gcc -Wall -Wextra` (no warnings) and behaviour matches the
sample transcripts (see the test transcript below).

### Verified transcript

Input `1500 500 750 250 500`:

```
Player 1, enter a number between 1 and 1000:
That number is out of range.
Player 1, enter a number between 1 and 1000:
Player 2, you have 10 guesses remaining.
Enter your guess:
Too high.
Player 2, you have 9 guesses remaining.
Enter your guess:
Too low.
Player 2, you have 8 guesses remaining.
Enter your guess:
Player 2 wins.
```

---

## Compile / Run

The `Makefile` compiles every `*.c` in the folder into a same-named executable:

```
make            # builds guessInteger
./guessInteger  # run it (or: make run)
make clean      # removes the binary
```

Equivalent manual command: `gcc guessInteger.c -o guessInteger`

---

## Expected Behaviour

- Player 1 input is range-checked (1..1000, inclusive); non-numeric or out-of-range input
  is rejected with "That number is out of range."
- Player 2 has exactly 10 guess slots; out-of-range guesses do not consume a slot.
- Correct guess => "Player 2 wins."; 10 failed guesses => "Player 1 wins.".
- Prompt text, spacing, and pluralisation are exact.

---

## Pitfalls

- **Exact output matching**: the Gradescope autograder fails on any extra/missing space or
  wrong newline. Keep prompts exactly as specified (no space after `:`).
- **Singular/plural**: "1 guess remaining" vs "n guesses remaining".
- **Out-of-range guesses do not count**: this differs from normal guess-game behaviour and
  is easy to get wrong.
- **Range check before evaluating high/low**: an out-of-range guess must print "That number
  is out of range." and *not* "Too high./Too low.".
- **Trim + numeric check**: the solution reads via `fgets`, strips the newline, trims
  spaces, then validates digits. If you instead `scanf("%d", ...)` directly, invalid input
  can leave the input stream in a bad state.

---

## Safety / Correctness Issues in the Supplied Code

The supplied solution is functionally correct but has minor nits worth knowing:

1. **`exit(EXIT_SUCCESS)` inside `announce_winner`**: the function never returns to
   `main`. If you later refactor to reuse the function, this is a hidden control-flow
   surprise. (It works for the assignment because Player 1's win is also routed through
   it.)
2. **`atoi` overflow**: `is_numeric` accepts arbitrarily long digit strings; `atoi` on an
   out-of-range integer is undefined behaviour. For the 1..1000 range this is not
   reachable in practice, but `strtol` is the safer conversion.
3. **`isdigit(str[i])` without an `(unsigned char)` cast**: technically undefined
   behaviour for negative `char` values (C requires an `unsigned char` argument). It is
   benign here because digits/ASCII are positive, but `isdigit((unsigned char)str[i])` is
   the portable form. (Note `trim` does cast correctly; `is_numeric` does not.)
4. **`main()` takes no arguments and never returns a value at the end** - it falls off the
   end after `announce_winner(1)`, but `announce_winner` always exits first, so the missing
   `return 0` is unreachable.
5. The header comment contains typos (stray double quotes after the Player 1 prompt).

None of these affect correctness for the autograder; they are noted so you recognise them
in other people's code.

---

## Lessons

- `fgets` + validation is more robust than bare `scanf` for interactive numeric input.
- Structuring the game into small helper functions (`is_numeric`, `is_in_range`,
  `is_match`, `higher_or_lower`) mirrors how later labs (tinyGrep, guessWord) ask you to
  factor logic, and makes the autograder's edge cases easy to reason about.
- Whitespace handling matters: trim the input and validate digits before converting.
- This lab is your first exposure to the exact-output autograder workflow: "train yourself
  to be an exact thinker".
