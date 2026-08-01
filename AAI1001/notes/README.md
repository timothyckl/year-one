# AAI1001 – Data Engineering and Visualization

Lecture notes for AAI1001, taught by Assoc. Prof. Guang Hua.

## Updating the website

The rendered site lives in `docs/` and is committed directly to `main`. GitHub Pages serves from `docs/`.

```bash
# Render the site
quarto render --output-dir docs

# Commit and push
git add docs/
git commit -m "docs: update lecture notes"
git push
```

GitHub Pages will pick up the changes automatically.
