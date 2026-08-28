# String and File Processing

Focused guide for text-processing tasks (Labs 2-4).

## String methods at a glance

| Method | What it does |
|--------|--------------|
| `s.find(sub, beg=0, end=len(s))` | index of first occurrence, or -1 |
| `s.isdigit()` | True if all characters are digits |
| `s.lower()` / `s.upper()` | case conversion |
| `s.split(sep)` | list of substrings split on `sep` (default whitespace) |
| `s.endswith(suffix, beg=0, end=len(s))` | True if it ends with suffix |
| `s.strip()` | removes surrounding whitespace/newline |
| `s.zfill(width)` | zero-pads left (e.g. `str(4).zfill(3)` -> `'004'`) |
| `ord(c)` / `chr(n)` | code point <-> character |

Built-in functions: `len(s)`, `max(s)`, `min(s)`. Plus `"+".join(list_of_str)`
for joining (used to make CSV output).

## Counting frequencies

Plain dict:

```python
counts = {}
for ch in text:
    counts[ch] = counts.get(ch, 0) + 1
```

Or `Counter`:

```python
from collections import Counter
counts = Counter(text)          # Counter is dict-like
```

## Top-N with tie-breaking

```python
# top 5 by descending frequency, ties ascending by key (ASCII)
top = sorted(counts.items(), key=lambda kv: (-kv[1], kv[0]))[:5]

# descending ASCII order (Lab3): sort keys by ord(), reversed
chars = sorted(counts.keys(), key=ord, reverse=True)
```

## Reading a file

```python
with open("data.txt", "r", encoding="utf-8") as f:
    for line in f:                      # one line at a time
        line = line.strip()
        if line:
            process(line)
```

```python
with open("data.txt", "r") as f:
    lines = [l.strip() for l in f.readlines() if l.strip()]
```

## Writing a file

```python
with open("out.txt", "w", encoding="utf-8") as f:
    for item in results:
        f.write(str(item) + "\n")       # add newline yourself

with open("data.txt", "a") as f:        # append mode
    f.write("new line\n")
```

## CSV-style parsing (Lab2/3 pattern)

Each line is keywords separated by commas:

```python
words = line.split(",")                 # list of strings
nums = [int(n) for n in line.split(",") if n.strip()]
```

Remember: `split` returns strings; cast to `int`/`float` before arithmetic.

## Command-line input pattern (all labs)

```python
import sys
def main():
    args = sys.argv[1:]                 # everything after script name
    first = args[0]                     # all strings!
```

## Practical file workflow (append a record)

```python
students = []
with open("student.txt", "r") as f:
    for line in f:
        if line.strip():
            students.append(line.strip())

max_id = max(int(s.split(" ")[0]) for s in students)
new_id = str(max_id + 1).zfill(3)
students.append(f"{new_id} Jack 99")

with open("student.txt", "w") as f:
    for s in students:
        f.write(s + "\n")
```

## Pitfalls

- `input()` and `sys.argv` return strings - always cast.
- Slicing `s[start:end]` excludes `end`.
- `strip()` before `if line:` to skip blank lines.
- `writelines` does not add newlines.
- `split()` with no argument splits on whitespace; `split(",")` splits on
  commas (CSV).
- `int(" 3 ")` works, but `int("3.0")` raises ValueError - validate with
  try/except or `.isnumeric()`.
