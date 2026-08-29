# Python Lab 1 - Python Basic

## Topics covered

1. Variables, data types, data type converters.
2. Operators.
3. Conditional logic.
4. Input/output from the keyboard (command-line arguments) and data formatting.

## Objectives

- Read command-line arguments with `sys.argv`.
- Validate input; print an exact error message when input is invalid.
- Apply arithmetic operators and conditionals.
- Format numeric output to exactly 2 decimal places.
- Train exactness: output must match the autograder byte-for-byte.

## Task structure

Three autograded tasks on Gradescope. Each file has a skeleton function you fill
in: `def AverageCalculator()`, `def BMICalculator()`,
`def WeeklyPaymentCalculator()`. Each task has 5 test cases / 5 marks. Submit all
three `.py` files together (no zip) for a max of 15 marks.

### Task 1 - Average Calculator

- Three numeric arguments `a b c`; print the average with 2 decimals.
- Invalid input prints exactly `Your input is invalid!`.
- Examples:
  - `python AverageCalculator.py 3 4 5` -> `Average:4.00`
  - `python AverageCalculator.py 60 39 92` -> `Average:63.67`
  - `python AverageCalculator.py abc 10 20` -> `Your input is invalid!`

### Task 2 - BMI Calculator

- Arguments: `metric|imperial` choice, height, weight.
- Formulas:
  - Metric: `BMI = weight(kg) / height(m)^2`
  - Imperial: `BMI = 703 * weight(lb) / height(in)^2`
- Print `%0.2f\t<Category>` (BMI, a TAB, then category).
- WHO categories: Severe Thinness <= 16; Moderate Thinness >16-17; Mild
  Thinness >17-18.5; Normal >18.5-25; Overweight >25-30; Obese Class I >30-35;
  Obese Class II >35-40; Obese Class III >40.
- Examples:
  - `python BMICalculator.py metric 1.80 78` -> `24.07\tNormal`
  - `python BMICalculator.py imperial 68.90 154.32` -> `22.85\tNormal`

### Task 3 - Weekly Payment Calculator

- Arguments: working hours, normal rate, overtime rate.
- Hours within 40 are normal hours; hours beyond 40 are overtime.
- `Normal Salary:<2dp>, Extra Salary:<2dp>, Total Salary:<2dp>` on one line.
- Examples:
  - `20 30 100` -> `Normal Salary:600.00, Extra Salary:0.00, Total Salary:600.00`
  - `60 30 200` -> `Normal Salary:1200.00, Extra Salary:4000.00, Total Salary:5200.00`
  - `10000 10 200` -> `Your input is invalid!`

## Supplied implementation analysis

The skeleton files already contain a complete, working solution. Study these
patterns:

- `sys.argv[1:]` collects the arguments; all are strings, so cast them.
- `AverageCalculator` uses `arg.isnumeric()` to validate each argument before
  `float(arg)`, then prints `f"Average:{average:.2f}"`.
- `BMICalculator` splits logic into small helpers: `bmi_metric`,
  `bmi_imperial`, `is_valid_inputs`, `get_category`, `handle_error`; checks
  `len(args) != 3`; lowers the unit choice; prints with a `\t`.
- `WeeklyPaymentCalculator` computes normal salary (capped at 40 hours) plus
  overtime salary, then the total.

## Run steps

```bash
cd <repo>/01 Python/02 Labs/Lab1   # or wherever you copied the files
python3 AverageCalculator.py 3 4 5
python3 AverageCalculator.py abc 10 20
python3 BMICalculator.py metric 1.80 78
python3 WeeklyPaymentCalculator.py 20 30 100
```

## Expected behavior

- Correct inputs -> one line of output with exactly 2 decimal places.
- Invalid inputs -> exactly `Your input is invalid!` (or the appropriate
  message) and the function returns without crashing.

## Pitfalls

- **Exact format matters**: even one space difference fails the test case
  ("TRAIN YOURSELF TO BE AN EXACT THINKER"). Watch the
  colon after `Average`, the comma+space in the Weekly Payment output, and the
  TAB between BMI and category.
- `sys.argv` entries are strings; forgetting `int()`/`float()` causes a `TypeError`.
- `int()` truncates (does not round) - not directly tested here, but a
  recurring quiz concept.
- `AverageCalculator.isnumeric()` rejects values like `3.5` (the examples only
  use integers); the lab spec text says "numbers", so decimals were never used.
- `BMICalculator` wraps `float()` without `try/except`: a genuinely
  non-numeric argument would raise `ValueError`. The invalid example
  only passes the wrong **count** of arguments, which is caught by `len(args)
  != 3`. In Gradescope, `quit()`/`exit()` are not allowed - use `return`.
- `WeeklyPaymentCalculator.is_valid_hours` returns `working_hours > 168`
  (not `> 40`); 0 or negative hours falls into the `else` branch and prints the
  invalid message.
- Do not change the file name or the function name; Gradescope calls
  `AverageCalculator()`, etc. directly.

## Lessons

- Command-line I/O is the entry point used by all later labs (Lab2+ read
  `sys.argv[1]` the same way).
- Formatting with `:.2f` and `%0.2f` is required in nearly every lab - learn
  both (Lecture 2, f-string and `%` formatting).
- Factor validation and calculation into helper functions; it keeps the graded
  function short and matches the skeleton style.
- Concatenating inputs before converting (`'3' + '4'` -> `'34'`) is exactly
  the kind of bug this lab is designed to avoid.
