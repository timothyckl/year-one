# Reference Guide - File Handling in C

This guide covers opening modes, reading/writing text and binary files, random access
files, and file copy/argument patterns, drawing on the supplied sample programs
(`create_file.c`, `read_from_file.c`, `random_access_write.c`, `random_access_read.c`).

---

## 1. Concept

- Files give permanent storage (variables/arrays are temporary).
- Opening a file creates a **stream**; C views the file as a sequential stream of bytes
  ending at the **EOF marker**.
- `FILE` is the opaque struct controlling the stream; use `FILE *` pointers only.

## 2. Open / write / close (text)

```c
#include <stdio.h>

int main(void) {
    FILE *f = fopen("data.txt", "w");
    if (f == NULL) {
        printf("Could not open data.txt.\n");
        return 1;
    }
    fprintf(f, "Hello! This is a new file.\n");
    fclose(f);
    return 0;
}
```

- `fopen(filename, mode)` returns `NULL` on failure - **always check**.
- `fprintf(stream, format, ...)` - like `printf` with a `FILE *` first argument.
- `fclose(stream)` - flush + close; call for every successful open.

## 3. Open modes

| Mode | Meaning |
|------|---------|
| `"r"`  | read (must exist) |
| `"w"`  | write (create / **truncate**) |
| `"a"`  | append (create if absent) |
| `"rb"`  `"wb"`  `"ab"`  | binary variants of the above |
| `"r+"`  `"w+"`  `"a+"`  | read+write text |
| `"rb+"` `"wb+"` `"ab+"`  | read+write binary |

Binary mode = raw bytes (images, video, databases, tar archives).
Text mode = lines delimited by newlines.

## 4. Reading (text) - and the feof pitfall

Sample input file `read_from_file.txt`:

```
10000 George 100
20000 Daniel 200
```

The classic buggy pattern:

```c
while (!feof(f)) {                    /* BUG: double-prints last record */
    fscanf(f, "%d%19s%lf", &account, name, &balance);
    printf("%10d\t%20s\t%10.2lf\n", account, name, balance);
}
```

**Why it is wrong:** `feof` only becomes true *after* a read reaches the end. The final
`fscanf` fails but leaves `account/name/balance` holding the last record's values, which
are printed again.

**Correct pattern - check the read's return value:**

```c
while (fscanf(f, "%d%19s%lf", &account, name, &balance) == 3)
    printf("%10d\t%20s\t%10.2lf\n", account, name, balance);
```

- `fscanf` returns the number of successfully assigned items (EOF at end).
- `name` (a char array) needs no `&`; scalars `account`/`balance` do.
- `%19s` bounds the string read to 19 chars + null.

## 5. Random access files (fixed-length records)

The simplest random access file is a series of records of the same length, so any record's
offset is `key * sizeof(Record)`.

```c
typedef struct client_struct {
    int acc_num;
    char last_name[15];
    char first_name[10];
    double balance;
} Client;
```

### fseek - position the stream

```c
int fseek(FILE *stream, long offset, int origin);
```
Origins: `SEEK_SET` (start), `SEEK_CUR` (current), `SEEK_END` (end).
For binary streams the new position = `origin + offset` exactly.

```c
fseek(f, (client.acc_num - 1) * sizeof(Client), SEEK_SET);
```

### fwrite / fread - raw record I/O

```c
size_t fwrite(const void *ptr, size_t size, size_t count, FILE *stream);
size_t fread(void *ptr, size_t size, size_t count, FILE *stream);
```

- Write/read `count` elements, each `size` bytes.
- Return value = number of elements actually transferred; check it.
- `size_t` is the unsigned type used for sizes/`sizeof`.

Example write:

```c
f = fopen(filename, "wb+");
...
fseek(f, (client.acc_num - 1) * sizeof(Client), SEEK_SET);
fwrite(&client, sizeof(Client), 1, f);
fclose(f);
```

Example read (robust version - check `fread`):

```c
f = fopen(filename, "rb");
while (fread(&client, sizeof(Client), 1, f) == 1) {
    if (client.acc_num != 0)
        printf("%d %s %s %.2f\n", client.acc_num, client.last_name, client.first_name, client.balance);
}
fclose(f);
```

## 6. Finding a file's length

```c
fseek(f, 0, SEEK_END);
long n = ftell(f);      /* size in bytes */
fseek(f, 0, SEEK_SET);
```

## 7. Copying a file (streaming, memory-efficient)

```c
char buf[8192];
size_t bytes;
while ((bytes = fread(buf, 1, sizeof(buf), in)) > 0) {
    if (fwrite(buf, 1, bytes, out) != bytes) { /* write error */ }
}
```
Use the *returned* byte count, not the buffer size, when writing the tail.

## 8. Command-line arguments (Lab 5)

```c
int main(int argc, char *argv[]) {
    /* argv[0] = program name; argv[1..argc-1] = arguments (strings) */
}
```
- Convert numeric args with `atoi()`/`atol()`/`strtol()`.
- `main` returns an int to the OS: 0 = success, non-zero = error.

## 9. Tar header basics (Lab 5)

- `struct header` with `name[100]` and `size[12]` (plus ignored fields); header length is
  `sizeof(struct header)`.
- `size` is stored as an **octal string**, zero-padded to 11 chars:

```c
snprintf(h.size, sizeof(h.size), "%011lo", (unsigned long)file_size);
```
- Archive layout: `header | file bytes | header | file bytes | ...`
- Convert the size back with `atoi(h.size)` when reading.
- Open/read/write the archive in **binary** mode.

## 10. Safety rules

1. Check every `fopen` for `NULL`.
2. Close every successfully opened file (`fclose`).
3. Check `fscanf`/`fread`/`fwrite` return values instead of relying on `feof`.
4. Use width limits on `%s`/`%[...]` in `fscanf` (`%19s`) to avoid overflow.
5. Binary files: use binary modes; don't `fprintf` into a binary stream.
6. When reading whole files, stream with a buffer - do not trust input sizes.
7. Beware text/binary newline translation differences when moving files across platforms.
