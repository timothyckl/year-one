# Python Lab 2 - Loops and Advanced Data Structures

## Topics covered

1. Advanced data structures: list, tuple, dictionary.
2. For/while loops.
3. List comprehensions.
4. File I/O.

## Objectives

- Count frequencies of characters/items and pick the top N by a defined sort.
- Compute sums, differences, counts, and a "centered average" from a list.
- Apply the leap-year rule across a range.
- (Warm-up) read a CSV-style file and write results to a new file.

## Task structure

Three autograded tasks, 5 test cases / 5 marks each; submit all three `.py`
files together for 15 marks. All output must be on ONE line.

Important Gradescope rule: **do not use `quit()`,
`exit()` or `sys.exit()`** - they cause test-case failure. Use `return`.

### Task 1 - Count Popular Characters

- Read one string argument, lowercase it, count each character's frequency.
- Print the **top 5** characters in descending order of frequency; ties broken
  by **ascending ASCII order**.
- Example:
  - `python CountPopularChars.py sdsERwweYxcxeewHJesddsdskjjkjrFGe21DS2145o9003gDDS`
    -> `d:7,s:7,e:6,j:4,w:3`

### Task 2 - Even Odd Calculator

- Input: a comma-separated series of integers, e.g. `12,2,8,7,100`.
- Output (one line):
  - sum of even numbers, sum of odd numbers,
  - difference between biggest and smallest,
  - count of even numbers, count of odd numbers,
  - the "centered average": mean after removing the **largest and smallest
    values** (if there are multiple copies of the smallest/largest, remove just
    one copy of each).
- Non-integer input -> `Please enter valid integers.`
- Examples:
  - `[12,2,8,7,100]` -> centered average 9.
  - `[2,2,8,11,100]` -> centered average 7 (remove one `2` and the `100`;
    `(2+8+11)/3 = 7`).
  - `1,2,abcd,8,11,200,301` -> `Please enter valid integers.`

### Task 3 - Leap Year Calculator

- Input: start year and end year (two arguments).
- Rule: leap if divisible by 4, except centuries (divisible by 100) that are
  not also divisible by 400. So 1600 and 2000 are leap; 1700, 1800, 1900 are not.
- Output (one line): `The number of Leap Years is N, the Leap Years are ...`
- Invalid input -> `Your input is invalid!`
- Example: `1989 2000` -> `The number of Leap Years is 3, the Leap Years are
  1992, 1996, 2000`

## Warm-up exercises (not submitted)

Several useful drills, most importantly **exercise 9**: read
`Lab2_testData.txt` (each line is keywords separated by `,`), count keyword
frequencies, print the **top 5** keywords and write them to a new file
`top_5.txt`. The instructions stress: "You must try exercise 8 to learn how to
do the Files I/O!" The full File I/O workflow appears again in Lab3's
`myMain`/`myMath` (see the File I/O revision note).

## Supplied implementation analysis

- `CountPopularChars`: lowercases the input, counts with a dict, then sorts:
  `sorted(counts.items(), key=lambda x: (-x[1], x[0]))` - negative frequency
  gives descending order, the character gives ascending ASCII tie-break; then
  slices `[:5]` and joins with commas. This is the canonical "top N" pattern.
- `EvenOddCalculator`: list comprehensions to split even/odd, `sum()`,
  `max()/min()`, and the centered average
  `(sum(nums) - min(nums) - max(nums)) // (len(nums) - 2)` (integer floor
  division). Invalid input is caught with `try/except ValueError`.
- `LeapYearCalculator`: validates with `.isnumeric()`, defines the leap rule as
  a lambda, builds the list with a list comprehension over `range(start,
  end+1)`, and joins with `', '`.

## Run steps

```bash
python3 CountPopularChars.py sdsERwweYxcxeewHJesddsdskjjkjrFGe21DS2145o9003gDDS
python3 EvenOddCalculator.py 12,2,8,7,100
python3 EvenOddCalculator.py 1,2,abcd,8,11,200,301
python3 LeapYearCalculator.py 1989 2000
```

## Expected behavior

- `CountPopularChars` prints exactly 5 `char:count` pairs separated by commas,
  in one line, no trailing space.
- `EvenOddCalculator` prints the full sentence on one line; invalid integers
  print the short error.
- `LeapYearCalculator` prints one line with the count and comma-space separated
  years.

## Pitfalls

- **The centered average removes only ONE copy** of min and ONE copy of max,
  even when duplicates exist (`[2,2,8,11,100]` -> 7, not 13). The supplied
  formula subtracts a single `min` and a single `max`, so it is correct.
- `//` (floor division) for the centered average: the example outputs are
  integers; keep the same operator to match the autograder.
- Ties in CountPopularChars must go ascending by ASCII (uppercase `A`=65
  before lowercase `a`=97) - the `(-count, char)` key handles this.
- Always lowercase the input string first (the example output has no uppercase).
- Use `return` instead of `quit()`/`exit()`/`sys.exit()`.
- Argument parsing: the whole series is ONE argument (`12,2,8,7,100`), so call
  `split(",")` on `sys.argv[1]`, not on the list of args.

## Lessons

- The `(-count, key)` sort idiom is reused in Lab3 (`CountLetters` reverse
  ASCII order) and in the revision notes; it is the standard way to get "top N
  by frequency, ties by value".
- List comprehensions for filtering (even/odd, leap years) replace verbose
  loops and are a required skill.
- File I/O will be graded from Lab2's warm-up onward; make sure you can read a
  text file, count with a dict, and write output to a new file before the quiz.
