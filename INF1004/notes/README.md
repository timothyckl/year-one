# inf1004-stats-linalg

This repository contains my mathematics notes in both LaTeX (`.tex`) and compiled PDF (`.pdf`) formats.

## Requirements

To compile from source, you can use either:

- **Local:** [Tectonic](https://tectonic-typesetting.github.io/)
- **Online:** An online LaTeX editor (e.g. Overleaf)

---

## Compiling the notes

**Local (Tectonic)**

From the repository root:

```bash
cd tex/notes
tectonic lecture-1.tex
mv lecture-1.pdf ../../pdf/notes/
```

To build all notes:

```bash
./build-notes.sh
```

**Online (e.g. Overleaf)**

1. Create a new project.
2. Upload the contents of `tex/` (or sync via Git if the repo is online).
3. Let Overleaf compile, then download the PDFs and place them in the corresponding `pdf/` directory.

---

## Adding new notes

**Lecture notes:**

1. Create `tex/notes/lecture-N.tex` (following the existing style).
2. Compile via Tectonic or Overleaf.
3. Save the compiled file as `pdf/notes/lecture-N.pdf`.

**Tutorial notes:**

1. Create `tex/tutorials/tutorial-N.tex` (following the existing style).
2. Compile via Tectonic or Overleaf.
3. Save the compiled file as `pdf/tutorials/tutorial-N.pdf`.

Try to keep the preamble and formatting consistent so all notes look uniform.

---

## Suggested usage

- Use the PDFs in `pdf/` for reading and revision.
- Edit the `.tex` files in `tex/` to fix errors, add examples, or extend the notes.

---

## Directory structure

```bash
.
├── pdf/
│   ├── notes/         # Compiled lecture notes
│   │   ├── lecture-1.pdf
│   │   ├── lecture-2.pdf
│   │   └── ...
│   └── tutorials/     # Compiled tutorial notes
│       ├── tutorial-1.pdf
│       ├── tutorial-2.pdf
│       └── ...
├── tex/
│   ├── notes/         # LaTeX sources for lectures
│   │   ├── lecture-1.tex
│   │   ├── lecture-2.tex
│   │   └── ...
│   └── tutorials/     # LaTeX sources for tutorials
│       ├── tutorial-1.tex
│       ├── tutorial-2.tex
│       └── ...
└── build-notes.sh     # Build script for all notes
```

The naming convention is:

- `lecture-N.tex` / `lecture-N.pdf` – Lecture N materials
- `tutorial-N.tex` / `tutorial-N.pdf` – Tutorial N materials
