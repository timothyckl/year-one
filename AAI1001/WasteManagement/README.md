# AAI1001 Waste Management Project

Data engineering, visualisation, and analysis of Singapore's waste-management and recycling data. The project investigates what the national recycling rate hides across individual waste streams.

## Project structure

| File | Description |
|---|---|
| `proposal.qmd` | Original project proposal |
| `presentation.qmd` | Reveal.js slide deck (1600×900) |
| `assets/presentation.scss` | Presentation theme and slide layout |
| `assets/proposal.scss` | Proposal and analysis styling |
| `assets/` | Source visuals (`.webp`, `.png`) and `inter-variable.ttf` font |
| `data/waste_management.csv` | Data.gov.sg / NEA source dataset |
| `references/` | Assignment specification and example proposal material |

Rendered HTML outputs are gitignored.

## Requirements

- [Quarto](https://quarto.org/)
- R with packages: `dplyr`, `ggplot2`, `gt`, `htmlwidgets`, `plotly`, `scales`, `stringr`, `tidyr`

## Rendering

```bash
quarto render proposal.qmd
quarto render presentation.qmd
```

Preview the presentation:

```bash
quarto preview presentation.qmd
```
