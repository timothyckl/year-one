"""
data.py

This module provides data processing functionality before computing domain metrics.
"""

import pandas as pd

from src.utils.helpers import timer


def has_missing(series: pd.Series) -> bool:
    """
    Check if a time series contains any missing values (NaNs).

    Args:
        series (pd.Series): Series to check.

    Returns:
        bool: True if series contains missing values, False otherwise.
    """
    return pd.isna(series).any()


@timer
def fill_gaps(series: pd.Series) -> pd.Series:
    """
    Fill missing values in a time series using forward and backward-fill.

    Args:
        series (pd.Series): Series to impute.

    Returns:
        pd.Series: Series with imputed values.
    """
    return series.ffill().bfill()


@timer
def remove_non_trading_days(series: pd.Series) -> pd.Series:
    """
    Remove weekend rows (keep Monday–Friday) from a daily series.

    Notes:
        - Index is expected to be a DateTime object.
        - Because yfinance API doesn not include holiday periods, we only handle
          for weekends.

    Args:
        series (pd.Series): Series to process.

    Returns:
        pd.Series: Series with non-trading days removed.
    """
    # RATIONALE (dev note):
    # - Upstream joins/exports may introduce weekends; indicators should operate
    #   on trading days for consistency.

    # EFFICIENCY (dev note):
    # - O(n) boolean mask; vectorized.

    # NOTES (dev note):
    # - Converts index to DatetimeIndex if needed.
    # - Does not handle exchange holidays explicitly (usually absent in Yahoo data).
    s = series.copy()
    if not isinstance(s.index, pd.DatetimeIndex):
        s.index = pd.to_datetime(s.index)
    return s[s.index.dayofweek < 5]  # 0=Mon ... 4=Fri


def clean_data(series: pd.Series) -> pd.Series:
    """
    Cleans a series.

    Args:
        series (pd.Series): Series to be cleaned.

    Returns:
        pd.Series: Cleaned series.
    """

    # if missing values exist, we fill those gaps before removing weekends.
    if has_missing(series):
        series = fill_gaps(series)

    cleaned = remove_non_trading_days(series)
    return cleaned


@timer
def clean_outliers_iqr(
    series: pd.Series, replace_with_nan: bool = True, k: float = 3.0
) -> pd.Series:
    """
    Handle outliers using Tukey IQR fences.

    Args:
        series (pd.Series): Numeric series 
        replace_with_nan (bool): Whether to mask outliers with NaN; else clip to [Lower, Upper].
        k (float): IQR multiplier (1.5 = classic Tukey; larger is stricter).

    Returns:
        pd.Series: Cleaned series with outliers masked or clipped.
    """
    # RATIONALE (dev note):
    # - Robust to spikes and non-normal data (bad ticks, splits, glitches).

    # METHOD (dev note):
    #   Q1 = 25th pct, Q3 = 75th pct, IQR = Q3 - Q1
    #   Lower = Q1 - k * IQR
    #   Upper = Q3 + k * IQR
    #   Outlier if value < Lower or > Upper

    # EFFICIENCY (dev note):
    # - O(n): percentile estimation + vectorized mask.
    # - Returns new Series; no Python loops.

    # EDGE CASES (dev note):
    # - Constant series → IQR=0 → Lower==Upper → no flags.
    # - Heavy-tailed distributions may flag more points; tune `k`.
    q1 = series.quantile(0.25)
    q3 = series.quantile(0.75)
    iqr = q3 - q1
    lower = q1 - k * iqr
    upper = q3 + k * iqr

    if replace_with_nan:
        return series.mask((series < lower) | (series > upper))
    else:
        return series.clip(lower=lower, upper=upper)
