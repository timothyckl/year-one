import pandas as pd
import matplotlib.pyplot as plt
from sklearn.base import clone
from sklearn.model_selection import cross_validate, RandomizedSearchCV
from sklearn.compose import ColumnTransformer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    balanced_accuracy_score,
    precision_recall_curve,
    ConfusionMatrixDisplay,
    confusion_matrix,
    classification_report,
)
from imblearn.pipeline import Pipeline as ImbPipeline
from imblearn.over_sampling import (
    SMOTENC,
    BorderlineSMOTE,
    SVMSMOTE,
    ADASYN,
    KMeansSMOTE,
    RandomOverSampler,
)
from scipy.stats import loguniform


def make_preprocessor_with_extra_numeric(base_preprocessor, base_numerical_features, extra_numeric_features=None):
    """
    Build a new ColumnTransformer that mirrors `base_preprocessor` but extends
    the numerical feature list with additional columns.

    Parameters
    ----------
    base_preprocessor : ColumnTransformer
        The fitted or unfitted preprocessor whose transformer list is cloned.
    base_numerical_features : list of str
        The original numerical feature names used in the base preprocessor's
        'num' transformer step.
    extra_numeric_features : list of str, optional
        Additional numeric column names to append to the numerical features.

    Returns
    -------
    ColumnTransformer
        A new, unfitted ColumnTransformer with the extended numerical column list.
    """
    if extra_numeric_features is None:
        extra_numeric_features = []

    # build extended numerical feature list
    updated_numerical_features = base_numerical_features + extra_numeric_features

    # clone each (name, transformer, columns) triple from the base preprocessor,
    # swapping in the extended column list for the 'num' step.
    # strings like 'passthrough' and 'drop' are valid sklearn specifiers — skip clone() for them.
    updated_transformers = []
    for name, transformer, columns in base_preprocessor.transformers:
        cloned = transformer if isinstance(transformer, str) else clone(transformer)
        if name == 'num':
            updated_transformers.append((name, cloned, updated_numerical_features))
        else:
            updated_transformers.append((name, cloned, columns))

    return ColumnTransformer(transformers=updated_transformers, remainder='drop')


def get_updated_categorical_indices(preproc, X_ref):
    """
    Fit a preprocessor on X_ref and return the column indices of all
    non-numerical (categorical) output features, for use with SMOTENC.

    Parameters
    ----------
    preproc : ColumnTransformer
        An unfitted preprocessor to fit on X_ref.
    X_ref : pd.DataFrame
        Reference data used to fit the preprocessor.

    Returns
    -------
    list of int
        Column indices in the transformed matrix that correspond to
        categorical features.
    """
    preproc.fit(X_ref)
    X_t = preproc.transform(X_ref)

    n_num = len(preproc.transformers_[0][2])
    total_features = X_t.shape[1]

    return list(range(n_num, total_features))


def build_lr_pipeline_with_sampler(sampler, preprocessor, random_state):
    """
    Construct an ImbPipeline (preprocessor → sampler → LogisticRegression)
    for any pre-constructed over-sampling instance.

    Parameters
    ----------
    sampler : estimator
        A pre-constructed over-sampler instance (e.g. SMOTENC, BorderlineSMOTE,
        SVMSMOTE, ADASYN, KMeansSMOTE, or RandomOverSampler).
    preprocessor : ColumnTransformer
        An unfitted preprocessor to use as the first pipeline step.
    random_state : int
        Random seed forwarded to LogisticRegression.

    Returns
    -------
    ImbPipeline
        An unfitted pipeline ready for cross-validation.
    """
    # step name is 'sampler' (not 'smote') since this function is sampler-agnostic
    return ImbPipeline(steps=[
        ('preprocessor', preprocessor),
        ('sampler', sampler),
        ('classifier', LogisticRegression(
            random_state=random_state,
            max_iter=1000
        ))
    ])


def build_lr_pipeline_with_extra_numeric(
    X_ref,
    base_preprocessor,
    base_numerical_features,
    random_state,
    extra_numeric_features=None,
    sampling_strategy='auto',
    sampler=None,
):
    """
    Construct an ImbPipeline (preprocessor → SMOTENC → LogisticRegression)
    that extends the numerical feature set of an existing preprocessor.

    Parameters
    ----------
    X_ref : pd.DataFrame
        Reference dataframe (with extra columns added) used to fit the
        updated preprocessor and derive SMOTENC categorical indices.
    base_preprocessor : ColumnTransformer
        The base preprocessor whose transformer list is cloned and extended.
    base_numerical_features : list of str
        The original numerical feature names in the base preprocessor.
    random_state : int
        Random seed forwarded to SMOTENC and LogisticRegression.
    extra_numeric_features : list of str, optional
        Additional numeric column names to include in the 'num' transformer.
    sampling_strategy : str or float, optional
        Passed directly to SMOTENC when no external sampler is provided.
        Defaults to 'auto'.
    sampler : estimator, optional
        A pre-constructed over-sampler to use instead of building SMOTENC
        internally. When None (default), SMOTENC is constructed from
        `sampling_strategy` and the derived categorical indices.

    Returns
    -------
    ImbPipeline
        An unfitted pipeline ready for cross-validation.
    """
    if extra_numeric_features is None:
        extra_numeric_features = []

    updated_preprocessor = make_preprocessor_with_extra_numeric(
        base_preprocessor, base_numerical_features, extra_numeric_features
    )
    updated_cat_idx = get_updated_categorical_indices(updated_preprocessor, X_ref)

    # use the provided sampler or construct smotenc internally as the default
    if sampler is not None:
        smote_step = sampler
    else:
        smote_step = SMOTENC(
            categorical_features=updated_cat_idx,
            sampling_strategy=sampling_strategy,
            random_state=random_state,
        )

    return ImbPipeline(steps=[
        ('preprocessor', updated_preprocessor),
        ('smote', smote_step),
        ('classifier', LogisticRegression(
            random_state=random_state,
            max_iter=1000
        ))
    ])


def run_ablation(
    experiment_no,
    description,
    change,
    model,
    X_used,
    y_train,
    skf,
    scoring,
    ablation_log,
    baseline_recall,
):
    """
    Run one ablation experiment via cross-validation and append the result
    to the shared ablation log.

    Parameters
    ----------
    experiment_no : int
        Sequential experiment identifier.
    description : str
        Human-readable description of the experiment.
    change : str
        One-line summary of what was changed from the previous configuration.
    model : estimator
        The pipeline or estimator to evaluate.
    X_used : pd.DataFrame
        Feature matrix for this experiment (may include engineered columns).
    y_train : pd.Series
        Training labels aligned with X_used.
    skf : StratifiedKFold
        Cross-validation splitter.
    scoring : dict
        Scoring dictionary passed to cross_validate.
    ablation_log : list of dict
        Mutable list that accumulates one row per experiment; mutated in place.
    baseline_recall : np.ndarray
        Per-fold recall values from Experiment 0, used to compute delta.

    Returns
    -------
    dict
        Raw cross_validate result dictionary.
    """
    res = cross_validate(
        model,
        X_used, y_train,
        cv=skf,
        scoring=scoring,
        return_train_score=False,
        n_jobs=-1
    )

    recall_vals   = res['test_recall']
    auc_vals      = res['test_roc_auc']
    bal_acc_vals  = res['test_balanced_accuracy']

    print(f"Experiment {experiment_no} — {description}")
    print(f"CV Recall          : {recall_vals.mean():.4f} ± {recall_vals.std():.4f}")
    print(f"CV ROC-AUC         : {auc_vals.mean():.4f} ± {auc_vals.std():.4f}")
    print(f"CV Balanced Acc    : {bal_acc_vals.mean():.4f} ± {bal_acc_vals.std():.4f}")
    print("Per-fold Recall:", [f'{v:.4f}' for v in recall_vals])
    print("Per-fold AUC   :", [f'{v:.4f}' for v in auc_vals])
    print()

    ablation_log.append({
        'Experiment'            : experiment_no,
        'Description'           : description,
        'Change'                : change,
        'CV Recall (mean)'      : recall_vals.mean(),
        'CV Recall (±std)'      : recall_vals.std(),
        'CV AUC (mean)'         : auc_vals.mean(),
        'CV Balanced Acc (mean)': bal_acc_vals.mean(),
        'Delta Recall'          : recall_vals.mean() - baseline_recall.mean(),
        'Delta Balanced Acc'    : bal_acc_vals.mean() - baseline_recall.mean(),
        'Decision'              : 'Keep' if recall_vals.mean() >= baseline_recall.mean() else 'Reject'
    })

    return res


def plot_threshold_sweep(model, X, y, recall_floor=0.80):
    """
    Compute and plot the precision-recall trade-off across decision thresholds,
    then select and report the operating point that maximises F1 subject to a
    minimum recall constraint.

    Parameters
    ----------
    model : fitted estimator
        A fitted pipeline with a predict_proba method.
    X : array-like
        Feature matrix to generate probability estimates from.
    y : array-like
        True binary labels aligned with X.
    recall_floor : float, optional
        Minimum recall required for a candidate operating point (default 0.80).

    Returns
    -------
    selected_threshold : float
        Decision threshold that maximises F1 among all operating points with
        recall >= recall_floor.
    threshold_df : pd.DataFrame
        Full sweep table with columns: threshold, precision, recall, f1.
    """
    from IPython.display import display as _display

    y_proba = model.predict_proba(X)[:, 1]

    # build sweep table from sklearn's precision-recall curve
    precision_vals, recall_vals, thresholds = precision_recall_curve(y, y_proba)

    threshold_df = pd.DataFrame({
        'threshold': thresholds,
        'precision': precision_vals[:-1],
        'recall':    recall_vals[:-1],
    })

    # compute f1 for each operating point
    denom = (threshold_df['precision'] + threshold_df['recall']).replace(0, float('nan'))
    threshold_df['f1'] = 2 * threshold_df['precision'] * threshold_df['recall'] / denom

    # display candidate operating points that meet the recall floor
    print(f"Operating points with recall >= {recall_floor:.2f}:")
    _display(
        threshold_df[threshold_df['recall'] >= recall_floor]
        .sort_values('threshold')
        .reset_index(drop=True)
    )

    # plot precision and recall vs decision threshold
    fig, ax = plt.subplots(figsize=(8, 5))
    ax.plot(threshold_df['threshold'], threshold_df['recall'],    label='Recall')
    ax.plot(threshold_df['threshold'], threshold_df['precision'], label='Precision')
    ax.axvline(x=0, color='grey', linestyle='--', linewidth=0.6)
    ax.set_xlabel('Decision Threshold')
    ax.set_ylabel('Score')
    ax.set_title('Precision and Recall vs Decision Threshold')
    ax.legend()
    plt.tight_layout()
    plt.show()

    # select threshold that maximises f1 among recall >= recall_floor candidates
    candidates = threshold_df[threshold_df['recall'] >= recall_floor]
    selected_threshold = float(
        candidates.sort_values('f1', ascending=False).iloc[0]['threshold']
    )
    print(f"\nSelected threshold: {selected_threshold:.4f}")

    # show confusion matrix at selected threshold
    y_pred_adjusted = (y_proba >= selected_threshold).astype(int)

    fig, ax = plt.subplots(figsize=(5, 4))
    ConfusionMatrixDisplay(confusion_matrix(y, y_pred_adjusted)).plot(ax=ax)
    ax.set_title(f'Confusion Matrix — threshold = {selected_threshold:.2f}')
    plt.tight_layout()
    plt.show()

    print(classification_report(y, y_pred_adjusted, target_names=['No Stroke', 'Stroke']))

    return selected_threshold, threshold_df


def report_violations(label, mask, df_ref, display_cols=None):
    """
    Print a pass/fail summary for a single validation check and display
    any offending rows.

    Parameters
    ----------
    label : str
        Human-readable description of the check being run.
    mask : pd.Series of bool
        Boolean mask where True marks a violation.
    df_ref : pd.DataFrame
        The dataframe to slice for display.
    display_cols : list of str, optional
        Columns to show for violating rows. Defaults to the full feature set.
    """
    if display_cols is None:
        display_cols = [
            'age', 'avg_glucose_level', 'bmi', 'gender',
            'hypertension', 'heart_disease', 'ever_married',
            'work_type', 'Residence_type', 'smoking_status',
        ]
    n = int(mask.sum())
    if n == 0:
        print(f"  \u2713 {label}: all clear")
    else:
        print(f"  \u2717 {label}: {n} violation(s)")
        from IPython.display import display as _display
        _display(df_ref[mask][display_cols].head(10))
