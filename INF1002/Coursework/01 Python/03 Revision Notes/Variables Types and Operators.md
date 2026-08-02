# Revision - Variables, Types and Operators

## Variables and assignment

- Syntax: `<name> = <expression>`.
- A variable is a name (reference) plus a memory location; `id(var)` returns
  the memory address; `type(var)` returns the type.
- Values can be reassigned (reset).
- Swap without a library:
  ```python
temp = first
first = second
second = temp
```

## Naming rules

- Start with a letter or `_`; then letters, digits, underscores.
- Case sensitive (`Var1` and `var1` differ).
- Keywords are not allowed: `if, else, for, while, class, def, return, try,
  except, ...`
- Valid: `_myvar`, `Var1`, `good`; invalid: `1var`, `-var`, `return`.
- Style: snake_case variables (`total_sum`); UPPERCASE_WITH_UNDERSCORES
  constants (`PI`, `MAX_LIMIT`); meaningful names; single letters only for
  loop counters (`i, j, k`). Follow PEP 8.

## Core types

| Type | Samples | Mutability |
|------|---------|------------|
| int | 8, 12, 1024 | immutable |
| float | 2.3, 3.1415926 | immutable |
| bool | True, False | immutable (subclass of int: `True == 1`, `True + True == 2`) |
| str | 'Hello', '3.1415926' | immutable |
| None | None (NoneType singleton) | - |
| list | [1, 'a', 2.5] | mutable |
| tuple | (1, 'a', 2.5) | immutable |
| set | {1, 2, 3} | mutable |
| dict | {'k': 'v'} | mutable |
| byte | b'...' | - |

Mutable = value can be changed after creation (list, dict, set; "you can change
the individual items, not the entire object at once"). Immutable = cannot be
changed (str, tuple, int, float).

## Truthiness and None

- Falsy objects: `False`, `None`, zero (`0`, `0.0`, `0j`), empty containers
  (`""`, `[]`, `{}`, `set()`, `range(0)`). Everything else is truthy.
- Test `None` with `is None` / `is not None` (identity), never `==`.
- `is` / `is not` compare object identity (same memory location); `==` compares
  value.

## Type casting

- Explicit conversion: `int(x)`, `float(x)`, `str(x)`, `bool(x)`.
- `int(5.7)` -> 5: `int()` **truncates toward zero**, it does not round.
  (`int(70.9)` -> 70, not 71; `int(-100.5)` -> -100, not -101.) "Round vs
  truncation" is a quiz-favorite concept.
- `int('5')` works; `int('5.7')` raises `ValueError`.
- Strings from `input()`/`sys.argv` must be cast before arithmetic.

## Arithmetic operators

| Op | Meaning | Notes |
|----|---------|-------|
| `+ - *` | add, subtract, multiply | |
| `/` | division | always float: `4/2` -> 2.0; `10/0` -> ZeroDivisionError |
| `//` | floor division | `13.9//2` -> 6.0 |
| `%` | modulus | `11%3` -> 2; `11.0%3.0` -> 2.0 |
| `**` | exponent | `2**4` -> 16 |
| `+= -= *= ...` | augmented assignment | `a += 1` is `a = a + 1` |

## Strings: quoting and escaping

- `'...'` and `"..."` are interchangeable.
- Embed the other quote type directly, or escape: `print("It's a
  \"beautiful\" day!")`.
- Escape characters: `\n` newline, `\t` tab, `\\` backslash.
- Raw strings `r"..."` disable escapes: `print(r"C:\Users\Name")`.

## Quick self-check

1. What is `int(-3.9)`? (Answer: -3, truncation toward zero.)
2. `type(True)` is? (bool.)
3. Is `''` truthy or falsy? (falsy.)
4. `10/4` vs `10//4`? (2.5 vs 2.)
5. Why must keys of a dict be immutable? (Hash-stability; see data structures
   revision.)
