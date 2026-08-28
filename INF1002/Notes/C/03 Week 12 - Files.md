# Revision Notes - Week 12: Files

See `../../Materials/02 C/01 Lectures/` for the full lecture notes.
Use with: `../../Materials/02 C/03 Reference Guides/04 File Handling Guide.md`.

---

## 1. Files and streams

- Variables/arrays are temporary; **files give permanent storage** on disk.
- Opening a file associates it with a **stream** (a sequence of bytes).
- C views a file as a sequential stream of bytes ending at the EOF marker.
- `FILE *` is a structure type you declare a pointer to; you never access its internals.

## 2. The basic file workflow

```c
FILE *f = fopen("data.txt", "w");      /* 1. open */
if (f == NULL) { /* report; handle */ }
fprintf(f, "Hello!\n");                /* 2. read/write */
fclose(f);                             /* 3. close - always */
```

- `fopen(filename, mode)` - returns `NULL` on failure (must always check).
- `fprintf`/`fscanf` behave like `printf`/`scanf` but with a `FILE *` first argument.
- `feof(f)` - true when the position indicator is at EOF (**only meaningful after a failed
  read** - see bug note below).
- `fclose(f)` - flushes and closes; releases the OS resource.

## 3. Opening modes

Text:
| Mode | Meaning |
|------|---------|
| `"r"` | read (file must exist) |
| `"w"` | write (creates or **truncates**) |
| `"a"` | append (creates if absent) |

Binary (add `b`): `"rb"`, `"wb"`, `"ab"`, and read+write variants `"rb+"`, `"wb+"`, `"ab+"`.

## 4. Random access files

- Fixed-length records let you compute any record's offset: `offset = (key-1) * sizeof(Rec)`.
- `fseek(FILE *, long offset, int origin)` moves the position indicator; origins:
  `SEEK_SET` (start), `SEEK_CUR` (current), `SEEK_END` (end).
- `fwrite(ptr, size, count, f)` writes raw bytes from memory; `fread(ptr, size, count, f)`
  reads raw bytes into memory. Both return the number of items transferred.
- `size_t` is the unsigned integer type used for sizes (`sizeof`, etc.).

Example (Client records):
```c
fseek(f, (client.acc_num - 1) * sizeof(Client), SEEK_SET);
fwrite(&client, sizeof(Client), 1, f);
```

## 5. Command-line arguments (needed for Lab 5)

```c
int main(int argc, char **argv)
```
- `argc` = number of arguments (incl. program name); `argv[0]` = program name;
  `argv[1..]` = the real arguments, always as strings.
- Convert numbers with `atoi()`/`atol()`.
- `main` returning 0 = success, non-zero = error (readable by shell scripts/batch files).
- Omit the parameters when unused (`int main()`).

## 6. The classic `feof` bug (be ready for it in the quiz/exam)

```c
while (!feof(f)) {                    /* WRONG */
    fscanf(f, "%d%19s%lf", &account, name, &balance);
    printf("%10d%20s%10.2lf\n", account, name, balance);
}
```
`feof` is only set **after** a read attempts to go past the end, so this loop processes the
**last valid record twice** (the failed `fscanf` reuses the old values). Correct pattern:

```c
while (fscanf(f, "%d%19s%lf", &account, name, &balance) == 3)
    printf("%10d%20s%10.2lf\n", account, name, balance);
```

(Also present in the supplied `read_from_file.c` example.)

## 7. Text vs binary

- Text mode: lines delimited by newlines; fine for `.txt`, source, HTML.
- Binary mode: raw bytes; required for images/videos/databases and for tar archives.

## Common exam traps

1. Forgetting `&` with `fscanf` for non-array variables.
2. `while (!feof(f))` double-printing the last record.
3. Not checking `fopen` for `NULL`.
4. Forgetting `fclose` (leaks the file handle).
5. Using text mode when binary is required (tar).
6. Treating `fread`'s return value as bytes instead of items when `size != 1`.
7. Reading the size field of a tar header as an integer when it is an octal string.
