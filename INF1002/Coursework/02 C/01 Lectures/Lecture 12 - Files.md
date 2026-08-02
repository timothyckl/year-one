# Lecture 12 - Files

**Slides:** 57 total

---

## Overview

Week 12. Covers files and streams, text-file creation/writing/reading, text vs binary
modes, random access files, `fopen`/`fprintf`/`fscanf`/`feof`/`fclose`/`fseek`/`fread`/
`fwrite`, and a full recap of the whole C portion. Ends with quiz/project admin details.

---

## 1. Files and Streams

- Storage in variables and arrays is temporary; data is lost when a program terminates.
  **Files give permanent retention of data** on secondary storage (hard disks, flash
  memory).
- When a file is opened, a **stream** is associated with the file.
- C views each file as a **sequential stream of bytes**, ending at the **end-of-file (EOF)**
  marker.
- `FILE` is a predefined structure type that holds the information to control a stream; its
  contents are not meant to be accessed directly.

---

## 2. Basic File Operations

- Create a new file and write content; open an existing file and read content.
- Functions: `fopen`, `fprintf`, `fscanf`, `feof`, `fclose`, `fseek`, `fread`, `fwrite`, ...

---

## 3. Creating and Writing a Text File

### create_file.c

```c
#include <stdio.h>

int main() {

	/* declare a pointer to a FILE structure */
	FILE *f;

	/* open the file with fopen() */
	f = fopen("create_file.txt", "w");
	if (f == NULL) {
		printf("Could not open data.txt.\n");
		return 1;
	}

	/* write to the file with fprintf() */
	fprintf(f, "Hi, this is INF1002.\n");

	/* close the file with fclose() */
	fclose(f);
	printf("The file is successfully created.\n");

	return 0;
}
```

Three steps:

1. **Open** with `fopen(filename, mode)`: `FILE *fopen(const char *filename, const char *mode);`
   Opens the file and associates a stream. The `mode` parameter defines allowed operations.
2. **Write** with `fprintf`: `int fprintf(FILE *stream, const char *format, ...);` - like
   `printf` but to a stream, with the same `%`-specifiers.
3. **Close** with `fclose`: `int fclose(FILE *stream);` - flushes and disassociates the
   stream.

### Text file opening modes

| Mode | Meaning |
|------|---------|
| `"w"`  | Create/write; if it exists it is **overwritten**; creates if not present |
| `"r"`  | Read; file must exist |
| `"a"`  | Append; creates if not present |

---

## 4. Reading from a Text File

### read_from_file.c

```c
#include <stdio.h>
int main() {
	FILE *f;
	int account;
	char name[20];
	double balance;
	/* open the file */
	f = fopen("read_from_file.txt", "r");
	if (f == NULL) {
		printf("Could not open credit.txt.\n");
		return 1;
	}
	/* read until the end of the file */
	printf("%10s\t%20s\t%10s\n", "Account", "Name", "Balance");
	while (!feof(f)) {
		/* read one record */
		fscanf(f, "%d%19s%lf", &account, name, &balance);
		/* display it to the screen */
		printf("%10d\t%20s\t%10.2lf\n", account, name, balance);
	}
	/* clean up */
	fclose(f);
	return 0;
}
```

with `read_from_file.txt`:

```
10000 George 100
20000 Daniel 200
```

- `fscanf(FILE *stream, const char *format, ...)` reads from the stream and stores into the
  pointed-to locations. Note `name` needs no `&` (array decays to pointer).
- `feof()` returns true when the file pointer is at the end of the file.

> **Unsafe pattern (bug worth flagging):** the loop `while (!feof(f))` calls `fscanf`
> first and *then* checks `feof`. Because `feof` is only set *after* a read attempt hits
> EOF, the final `fscanf` fails and the previously-held values of `account`, `name`,
> `balance` are printed a second time (duplicate trailing record). This is the classic
> "feof in the loop condition" bug. The correct pattern is to check the **return value of
> fscanf**:
> ```c
> while (fscanf(f, "%d%19s%lf", &account, name, &balance) == 3)
>     printf(...);
> ```
> Also, on a trailing-line-with-no-newline file this loop is even worse; and `%19s`
> bounds the name but `account`/`balance` have no such concern.

- A natural next question: *"What if we want to read/write data at a random location inside
  a file?"* -> random access files.

---

## 5. Text vs Binary Modes

- **Text mode** is for text files (divided into lines by newline chars): plain text, source
  code, HTML, XML.
- **Binary mode** is for everything else - raw bytes: images, videos, databases.

---

## 6. Random Access Files

- A random access file can be read or written **in any order**; writing to one part does
  not change another part.
- The simplest random access file is a series of **fixed-length records**; every record has
  the same length, so a record's exact location can be calculated from its key and the
  record length. This enables insert/update/delete without destroying other data.
- Visualised with the "two elephants in the refrigerator" analogy.

### Step 1 - Open in binary mode

`f = fopen(filename, "wb+");`

Binary modes:

| Mode | Meaning |
|------|---------|
| `"rb"`  | Open existing binary file for reading |
| `"wb"`  | Write; creates if absent, **overwrites** if present |
| `"ab"`  | Append; creates if absent |
| `"rb+"` | Read and write (existing) |
| `"wb+"` | Read and write (create/truncate) |
| `"ab+"` | Read and write (append) |

### Step 2 - fseek

`int fseek(FILE *stream, long int offset, int origin);`

Sets the position indicator; in binary mode, the new position = `offset` added to the
`origin` reference position. Origins: `SEEK_SET` (start), `SEEK_CUR` (current),
`SEEK_END` (end).

Example: `fseek(f, (client.acc_num - 1) * sizeof(Client), SEEK_SET);`

### Step 3 - fwrite / fread

- `size_t fwrite(const void *ptr, size_t size, size_t count, FILE *stream);` - writes
  `count` elements, each `size` bytes, from memory to the current position.
- `size_t fread(void *ptr, size_t size, size_t count, FILE *stream);` - reads `count`
  elements of `size` bytes into memory.
- `size_t` is an unsigned integer type used for sizes/`sizeof` results.

### random_access_write.c

```c
#include <stdio.h>
/* this structure holds the data for one client */
typedef struct client_struct {
    int acc_num;
    char last_name[15];
    char first_name[10];
    double balance;
} Client;

int main() {
	const char *filename = "random_access_write.dat";
	FILE *f;
	Client client;
	/* open the data file */
	f = fopen(filename, "wb+");
	if (f == NULL) {
		printf("Could not open %s.\n", filename);
		return;
	}
	/* read account data from the user */
	printf("Enter account number (1-100, 0 to end)\n? ");
	while (client.acc_num != 0) {
		/* read the data for this record */
		printf("Enter last_name first_name balance\n? ");
		while (scanf("%14s%9s%lf", client.last_name, client.first_name, &client.balance) != 3)
			;
		/* go to this record's position in the file */
		fseek(f, (client.acc_num - 1) * sizeof(Client), SEEK_SET);
		/* write the client data structure */
		fwrite(&client, sizeof(Client), 1, f);
		/* ask for another record */
		printf("Enter account number (1-100, 0 to end)\n? ");
		while (scanf("%d", &client.acc_num) != 1)
			;
	}
	fclose(f);
	return 0;
}
```

> The code block above is a *lightly repaired* version. The original is identical except
> that the bare `scanf` calls have no `while (... != N)` input-recovery wrappers, and the
> original DOES contain a `scanf("%d", &client.acc_num);` immediately after the first
> account-number prompt (so the pre-loop variable IS initialised - see the last note
> below). The repairs do not change the bug analysis that follows.

> **Bugs/unsafe patterns in the supplied file** (`random_access_write.c`):
> - `return;` inside `int main()` when `fopen` fails - `main` returns `int`, so this is
>   non-standard (use `return 1;`).
> - **No validation of the account number.** The prompt claims "1-100", but the program
>   accepts any value; a negative or huge `acc_num` makes `fseek` go to a nonsense offset
>   (and the `int` arithmetic `(acc_num - 1) * sizeof(Client)` can overflow). A value of 0
>   ends the loop.
> - **Non-numeric input spins forever.** With the original plain `scanf("%d", ...)`, a
>   non-numeric reply fails to update `acc_num`, so the loop condition never changes and
>   the same (stale) record is re-written forever. (This is exactly the bug the `while
>   (... != 1)` wrappers fix.)
> - No check that `fwrite` returned 1 element.
> - `fseek` offsets are `long`, but the expression `(client.acc_num - 1) * sizeof(Client)`
>   is computed in `int`; fine for small record counts, overflows for large ones.
> - `"wb+"` truncates the file on open, so a fresh run discards previous data and unentered
>   accounts are simply absent (no zero-filled records are written). The reader tolerates
>   that by skipping `acc_num == 0`.
> - The record written is the raw padded struct; that is fine only because the reader uses
>   the identical struct type.

### random_access_read.c

```c
#include <stdio.h>

typedef struct client_struct {
    int acc_num;
    char last_name[15];
    char first_name[10];
    double balance;
} Client;

int main() {

	const char *filename = "random_access_write.dat";
	FILE *f;
	Client client;

	/* open the data file */
	f = fopen(filename, "rb");
	if (f == NULL) {
		printf("Could not open %s.\n", filename);
		return;
	}
	/* print title */
	printf("%-6s%-16s%-11s%10s\n", "Acct", "Last Name", "First Name", "Balance");
	/* read one record at a time until we reach EOF */
	fread(&client, sizeof(Client), 1, f);
	while (!feof(f)) {
		if (client.acc_num != 0)
			printf("%-6d%-16s%-11s%10.2lf\n", client.acc_num, client.last_name, client.first_name, client.balance);
		fread(&client, sizeof(Client), 1, f);
	}
	fclose(f);
}
```

> Note: the `while (!feof(f))` pattern here is applied to `fread`, not `fscanf`; because
> `fread` is called both before the loop and inside it, and `client` retains the last
> successfully-read record, the classic double-print issue is mostly avoided here (the
> final failed `fread` does not overwrite `client`, but the loop exits right after via the
> top-of-loop `feof` check). It still relies on checking `feof` only after a read, which
> is fragile; checking `fread`'s return value (`!= 1`) is the robust idiom.

---

## 7. Recap of the Whole Module

The recap covers: Hello World structure and `main`; `#define`/`#include`; `printf` format
strings; `scanf` conversions; control structures; functions (definition, prototype,
return value); scope (global vs local); arrays; strings (char array + `'\0'`); pointers
(`int* ptr = &count;`, D-I-D usage); pointers and arrays; pointers to pointers;
call-by-value vs call-by-reference; structures; dynamic memory allocation (3 steps) and
`free`; linked lists and operations.

---

## 8. Admin

- No lecture 24 Nov 2025. Online quiz 24 Nov 2025 (Respondus LockDown Browser, web camera,
  blank paper, calculator allowed) - 45 minutes, MCQ + short answer, closed-book, during
  the normal lecture time slot. Make-up in-person quiz is harder; missing both = 0.
- Project submission 25 Nov 2025 23:59. No labs 28 Nov (study time).
- End-of-module feedback: anonymous; constructive and respectful.

---

## Key Takeaways

1. Files = permanent storage; opened via `fopen(mode)` -> stream -> `FILE *`.
2. Text mode for lines; binary mode for raw bytes (records).
3. Random access = fixed-length records + `fseek` + `fread`/`fwrite`.
4. Always check `fopen` for `NULL`; always `fclose`.
5. Avoid the `while (!feof(f))` + `fscanf` bug: check `fscanf`'s return value instead.
6. `main(int argc, char **argv)` and command-line arguments are needed for Lab 5 (see
   Lab 5 README).
