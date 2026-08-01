# Discrete Mathematics Tutorial Notes

This repository contains my mathematics tutorial notes in both LaTeX (`.tex`) and compiled PDF (`.pdf`) formats. Each tutorial is maintained as a pair of files with the same tutorial number.

## Requirements

To compile from source, you can use either:

- **Local:** [Tectonic](https://tectonic-typesetting.github.io/)
- **Online:** An online LaTeX editor (e.g. Overleaf)

---

## Compiling the notes

**Local (Tectonic)**

From the repository root:

```bash
cd tex
tectonic tutorial-1.tex
mv tutorial-1.pdf ../pdf/
```

To build all tutorials:

```bash
cd tex
for f in tutorial-*.tex; do
  tectonic "$f"
done
mv tutorial-*.pdf ../pdf/
```

**Online (e.g. Overleaf)**

1. Create a new project.
2. Upload the contents of `tex/` (or sync via Git if the repo is online).
3. Let Overleaf compile, then download the PDFs and place them in `pdf/`.

---

## Adding a new tutorial

1. Create `tex/tutorial-11.tex` (following the existing style).
2. Compile via Tectonic or Overleaf.
3. Save the compiled file as `pdf/tutorial-11.pdf`.

Try to keep the preamble and formatting consistent so all tutorials look uniform.

---

## Suggested usage

- Use the PDFs in `pdf/` for reading and revision.
- Edit the `.tex` files in `tex/` to fix errors, add examples, or extend the notes.

---

## Directory structure

```bash
.
├── pdf/   # Compiled notes (ready to read/print)
│   ├── tutorial-1.pdf
│   ├── tutorial-2.pdf
│   ├── ...
│   └── tutorial-10.pdf
└── tex/   # LaTeX sources
    ├── tutorial-1.tex
    ├── tutorial-2.tex
    ├── ...
    └── tutorial-10.tex
```

The naming convention is:

- `tutorial-N.tex` – LaTeX source for Tutorial N
- `tutorial-N.pdf` – compiled PDF for Tutorial N
