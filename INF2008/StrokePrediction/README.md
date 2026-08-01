# Stroke Risk Prediction

Binary classification project to predict stroke occurrence in patients using demographic and clinical features, following the CRISP-DM methodology.

## Dataset

[Healthcare Stroke Prediction Dataset](https://www.kaggle.com/datasets/fedesoriano/stroke-prediction-dataset) — 5,110 patient records with 12 features (age, BMI, glucose level, hypertension, smoking status, etc.).

## Structure

```
├── stage-01-notebook.ipynb   # business understanding, EDA, baseline models (LR, DT)
├── stage-02-notebook.ipynb   # leak-proof pipelines, ensemble models (RF, GBT), tuning
├── utils.py                  # shared preprocessing and pipeline utilities
└── data/
    ├── healthcare-dataset-stroke-data.csv
    └── prepared_data.pkl
```

## Methodology

Two-stage CRISP-DM workflow:

- **Stage 1** — Data understanding, exploratory analysis, and baseline models (Logistic Regression, Decision Tree).
- **Stage 2** — Strict sklearn/imblearn pipelines with SMOTE for class imbalance, hyperparameter tuning, and ensemble methods (Random Forest, Gradient Boosting).

## Setup

```bash
source .venv/bin/activate
jupyter notebook
```

**Dependencies:** `scikit-learn`, `pandas`, `numpy`, `matplotlib`, `seaborn`
