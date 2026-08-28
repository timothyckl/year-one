# Revision - File I/O

## Why files

- Data in memory is lost when the program exits; files persist it.
- Workflow: open -> load/process -> write -> close.

## open() and modes

```text
file = open(file_name[, access_mode] [, encoding])
```

- Default mode is read `r`; default encoding depends on the OS (utf-8 common).
- Modes:

| Mode | Meaning |
|------|---------|
| `r` | read only (default) |
| `r+` | read and write |
| `w` | write only; overwrites existing file, creates if missing |
| `w+` | write and read; overwrites existing file |
| `a` | append; creates if missing |
| `b` | binary: `rb`, `wb`, `ab`, ... |

## Reading

```python
file = open('student.txt', 'r')
for line in file:          # iterate lines directly
    line = line.strip()
    if line:
        students.append(line)
file.close()
```

- `readline(size)`: reads one line (optionally limited to `size` chars); returns
  `''` at end of file.
- `readlines(hint)`: list of lines; `hint` is an approximate character budget,
  complete lines always returned.
- `read(size)`: read a block of `size` characters.
- `strip()` removes the trailing newline; guard with `if line:` to skip blanks.
- One-liner: `students = [line.strip() for line in file.readlines() if
  line.strip()]`.

## Writing

- `write(str)`: write one string.
- `writelines(sequence)`: write a sequence of strings (you add the newlines).
- Remember `\n` at the end of each line, or lines run together.

## File pointer

- `open(...)` positions the pointer at the start.
- Each read advances the pointer: two `file.read(4)` calls return consecutive
  4-character blocks.

## with statement (preferred)

```python
with open('student.txt', 'w', encoding='utf-8') as file:
    for student in students:
        file.write(student + '\n')
```

- Auto-closes the file, even on exceptions.
- Multiple files:
  ```python
with open('in.txt', 'r') as infile, open('out.txt', 'w') as outfile:
    ...
```

## Worked pattern (append a record)

1. Read all lines into a list.
2. Find `max_id` (parse first field, e.g. `int(line.split(' ')[0])`).
3. `new_id = max_id + 1`; zero-pad with `str(new_id).zfill(3)`.
4. Append `f'{new_id} Name score'` to the list.
5. Rewrite the whole file in `'w'` mode.

## Binary vs text

- Text files: ASCII/UTF-8, human readable.
- Binary files (images, audio, executables): sequences of bytes; read/write in
  `'b'` mode; need specific decoders. (See bytes revision note.)

## Lab links

- Lab2 warm-up: count keyword frequencies from `Lab2_testData.txt` and write
  top-5 to `top_5.txt`.
- Lab3: the `myMath.load_data`/`process_data` pattern uses
  `with open(...) as file` and `readlines()`.
