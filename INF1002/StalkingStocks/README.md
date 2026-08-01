# 👁️ 📈 Stalking Stocks

Beginner-friendly financial analysis—delivered via a clean, interactive Streamlit dashboard.

![Python](https://img.shields.io/badge/Python-3.13+-blue)
![Streamlit](https://img.shields.io/badge/Streamlit-app-brightgreen)
![Tests](https://img.shields.io/badge/tests-pytest-informational)

Hosted App:
https://stalking-stocks.streamlit.app/

---

## Table of Contents
- [Features](#features)
- [Quick Start](#quick-start)
- [Unit Tests](#unit-tests)
- [Project Structure](#project-structure)

---

## Features

- 🧭 Interactive Streamlit dashboard with price/volume charts
- 🔎 Ticker lookup with validated historical data fetching
- 🗂️ Sector and symbol filtering for quick market slicing
- 📊 Basics: returns, highs/lows, drawdowns, moving averages
- 📈 Configurable chart options
- 🧩 Modular codebase (`models`, `services`, `ui`, `utils`)
- 🧪 Pytest-covered core for reliability

> [!NOTE]
> See `src/ui/` for chart components and `src/services/` for data/business logic.

---

## Quick Start

### Prerequisites
- Python **3.13+**
- `pip` (or `conda`) for package management

### Clone

```bash
git clone https://github.com/MuhammadHasifF/INF1002-P1-08-StalkingStocks.git
cd INF1002-P1-08-StalkingStocks
```

### Create & Activate a Virtual Environment

```bash
# Linux/macOS
python3 -m venv .venv
source .venv/bin/activate

# Windows (PowerShell)
python -m venv .venv
. .\.venv\Scripts\Activate.ps1

# Deactivate when you're done
deactivate
```

### Install Dependencies

```bash
pip install -r requirements.txt
```

### Run the App

```bash
streamlit run app.py
```
> The app will open in your browser. If not, visit the URL printed in your terminal.

## Unit Tests

```bash
# run all tests
pytest

# run tests in a specific file
pytest tests/test_core.py

# run tests by keyword
pytest -k "ticker"

# run a specific test function
pytest tests/test_finance.py::test_get_ticker_data

# verbose output
pytest -v
```

## Project Structure

```bash
.
├── app.py                      # Streamlit entry point
├── src/                        # Application source
│   ├── dashboard.py
│   ├── constants/              # Static values and domain-specific mappings
│   │   └── sectors.py
│   ├── models/                 # Data models and dataframe schemas
│   │   ├── base.py
│   │   └── dataframe.py
│   ├── services/               # Business logic and data processing
│   │   ├── core.py
│   │   ├── data.py
│   │   └── finance.py
│   ├── ui/                     # Visualization & UI components
│   │   ├── adapters.py
│   │   ├── charts.py
│   │   ├── filters.py
│   │   └── overview.py
│   └── utils/                  # General-purpose helpers
│       ├── helpers.py
│       └── parsers.py
├── tests/                      # Unit tests
│    ├── conftest.py
│    ├── test_core.py
│    ├── test_finance.py
│    ├── test_edge_cases.py
│    └── test_models.py
├── docs/
├── notebook/
├── README.md
└── requirements.txt
```
