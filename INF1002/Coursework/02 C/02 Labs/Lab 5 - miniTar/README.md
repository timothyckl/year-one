# C Lab 5 - miniTar

## Objectives

To understand the structure of files, and to open, read from, write to, and close files
in C.

---

## Pre-Reading

- Command-line arguments: `int main(int argc, char **argv)`. `argc` counts arguments
  including the program name; `argv[0]` is the program name, `argv[1]` the first argument,
  etc. They are always strings, so convert with `atoi()` and friends when you need numbers.
- `main`'s return value is handed to the invoking shell; 0 = success, non-zero = error.
- Most compilers let you omit the arguments if unused (which is why earlier labs use
  `int main()`).

---

## Lab Requirements

**Graded assignment (Gradescope, 1 test case, max 5 marks):**
- Write `miniTar` that accepts input file names on the command line
  (`miniTar File1.txt File2.txt`) and produces an archive `Result.tar`.
- Tar archive layout: for each input file, a **fixed-length header** (`struct header`)
  followed by the file's raw data. Header sections always have length `sizeof(struct
  header)`; each file's data length is given by the `size` field in its header.
- Only the `name` and `size` fields matter for this exercise; ignore the rest.
- The `size` field is a **string**, not an integer; convert with `atoi()` to do arithmetic.
- **Flexible**: accept any number of input files.
- **Use `sys.argv[]`** for user inputs (this is the one lab that uses command-line args).
- **Binary mode** for `Result.tar` open/write (`"wb"`).
- File size written in **octal**, zero-padded to 11 chars: `snprintf(... ,"%011lo", ...)`.
  (Example given: File1.txt size octal `00000000037`, File2.txt `00000000076`.)
- Add error handlers and comments.
- Hint: copying file-to-file via a small buffer is more memory-efficient than reading the
  whole file at once (see `fread`/`fwrite` reference).

**Expected console output:**

```
miniTar File1.txt File2.txt
Archive 'Result.tar' created successfully.
```

---

## Supplied Implementation (`miniTar.c`)

**The supplied file is an unimplemented skeleton** - it provides the header struct and
constants, then an empty `main(int argc, char *argv[])`:

```c
#define RECORDSIZE 512
#define NAMSIZ 100
#define TUNMLEN 32
#define TGNMLEN 32

struct header {
	char name[NAMSIZ];
	char mode[8];
	char uid[8];
	char gid[8];
	char size[12];
	char mtime[12];
	char chksum[8];
	char linkflag;
	char linkname[NAMSIZ];
	char magic[8];
	char uname[TUNMLEN];
	char gname[TGNMLEN];
	char devmajor[8];
	char devminor[8];
};

int main(int argc, char *argv[]) {
    /* code here */
    return 0;
}
```

You must implement the archiving logic. As shipped it compiles with only
"unused parameter" warnings and does nothing.

---

## What You Need to Implement

1. Loop over `argv[1] .. argv[argc-1]` (skip `argv[0]` = program name).
2. For each input file:
   - Open it in **binary read** mode `"rb"`; check for `NULL` (print an error and skip or
     exit - error handlers are required).
   - Determine its size in bytes (e.g. `fseek(f, 0, SEEK_END); long n = ftell(f);`
     `fseek(f, 0, SEEK_SET);`).
   - Fill `struct header h = {0};`, copy the filename into `h.name` (careful: `NAMSIZ` is
     100), and write the size as an octal string: `snprintf(h.size, sizeof(h.size),
     "%011lo", (unsigned long)n);` (the `%011lo` convention).
   - `fwrite(&h, sizeof(struct header), 1, out)` to the archive.
   - Copy the file's bytes into the archive with a small buffer loop:
     `while ((bytes = fread(buf, 1, sizeof(buf), in)) > 0) fwrite(buf, 1, bytes, out);`
     (this avoids loading the whole file into memory, per the hint).
   - Close the input file.
3. Open `Result.tar` once in binary write mode `"wb"`; close it after all files.
4. Print `Archive 'Result.tar' created successfully.`

Note: `sizeof(struct header)` is the size of the full struct (with padding) - that is what
the reader/grader expects, since header length is defined as `sizeof(struct header)`.

---

## Expected Behaviour

Running `./miniTar File1.txt File2.txt` creates `Result.tar` in the current directory and
prints the success line. The file sizes in the two headers should be the octal values:

- `File1.txt` is 31 bytes -> octal `00000000037` (verified).
- `File2.txt` is 62 bytes -> octal `00000000076` (verified).

Gradescope tests the binary contents of `Result.tar` (name + size fields), so byte
exactness matters: header first, then raw file bytes, for each file in command-line order.

---

## Pitfalls

- **`argv[0]` is the program name** - start your loop at index 1.
- **`size` is a string**: write it with `snprintf`, read it back with `atoi`. Never assign
  an integer directly into the `char size[12]` field.
- **Octal, not decimal**: `%011lo`, zero-padded to 11 characters. Wrong base or padding
  fails the grader.
- **Binary mode**: `"wb"` / `"rb"` - `Result.tar` must be
  opened in binary mode.
- **Filename length**: `name` field is 100 chars; the lab file names are short, but guard
  against truncation.
- **Output message**: exactly `Archive 'Result.tar' created successfully.` (single line).
- **Any number of files**: the grader may pass 1..N files, so the loop must be general.

---

## Safety / Correctness Issues

- **Never trust `fopen` to succeed**: every open needs a `NULL` check; report the failing
  filename.
- **Don't read the whole file into memory** (a hint worth following): use a fixed-size
  buffer loop so large inputs don't exhaust memory.
- **`ftell`/`fseek` type**: file positions are `long`; for files > 2GB this overflows on
  some platforms, but large-file handling is out of scope here.
- **`sizeof(struct header)` includes struct padding** - that is fine here because the
  grader defines header length as `sizeof(struct header)`; just never assume it equals the
  sum of the member sizes.
- **Buffer sizes**: if you read with `fread(buf, 1, BUFSIZ, in)`, always write back the
  actual bytes returned (`bytes`), not the buffer size.
- The skeleton's `int main(int argc, char *argv[])` with unused parameters is fine; add
  a usage check (e.g. if `argc < 2`, print a message and return non-zero).

---

## Lessons

- Command-line arguments (`argc`/`argv`) are the first-class way to pass file names; this
  is the only lab that requires them.
- Files are streams of bytes: open, read/write, close; every resource must be released.
- Binary files are handled with `fread`/`fwrite` on raw bytes; text vs binary mode matters.
- Fixed-length record headers (like tar's) let you compute and jump to offsets - the basis
  of random access files from Lecture 12.
- Streaming copy (buffer loop) beats read-whole-file for memory efficiency and is the
  pattern professional code uses.
