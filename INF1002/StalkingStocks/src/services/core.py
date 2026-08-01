"""
core.py

This module provides the core functionality of the project, including
the main processing functions and algorithms for our application.

Notes:
    - All computation is done manually with Python.
    - Pandas/Numpy modules are only used for input/output compatibility.
"""

import numpy as np
import pandas as pd

from src.utils.helpers import timer


@timer
def compute_sma(close: pd.Series, window: int = 5) -> pd.Series:
    """
    Computes the simple moving average with sliding window.

    Args:
        close (pd.Series): Closing price
        window (int): Window size

    Returns:
        pd.Series: Computed SMA values based on input series.
    """
    # RATIONALE (dev note):
    # - SMA smooths noise by averaging the last N closes (e.g., 5, 20, 50).
    # - Typical use: trend detection / denoising.

    # EFFICIENCY (dev note):
    # - O(n) time: single pass with a running sum (add new, drop old).
    # - O(n) space: output series; extra space is O(1).
    #   Avoids recomputing sums for each window.

    # Convert pandas input to NumPy array for computation (manual algorithms)
    values: np.ndarray = close.values
    n: int = len(values)
    out: np.ndarray = np.full(n, np.nan, dtype=float)

    # Handle edge cases
    if window <= 0 or n == 0:
        return pd.Series(out, index=close.index)

    # Calculate SMA using sliding window approach
    running_sum: float = 0.0
    for i, current_price in enumerate(values):
        # Add current price to running sum
        running_sum += float(current_price)

        # Remove price that's now outside the window (if we have enough data)
        if i >= window:
            running_sum -= float(values[i - window])

        # Calculate SMA only when we have enough data points
        if i >= window - 1:
            out[i] = running_sum / window

    # Convert Python result back to pandas Series for output
    return pd.Series(out, index=close.index)


@timer
def compute_streak(close: pd.Series) -> tuple[int, int, pd.Series]:
    """
    Computes the longest up, down streaks, and a daily direction mask.

    Args:
        close (pd.Series): Closing price

    Returns:
        tuple[int, int, pd.Series]: Longest up streak, downstreak, and mask.
    """
    # RATIONALE (dev note):
    # - Up: Close_t > Close_{t-1}; Down: < ; Flat: == (breaks streaks).
    # - Useful for momentum diagnostics and run-length visualization.

    # EFFICIENCY (dev note):
    # - O(n) time; O(1) auxiliary state (mask is intended output).
    # - Single pass, minimal counters.

    # Convert pandas input to NumPy array for computation (manual algorithms)
    values: np.ndarray = close.values
    n: int = len(values)

    # Handle edge case: need at least 2 prices to determine direction
    if n <= 1:
        return (0, 0, pd.Series(np.zeros(n, dtype=int), index=close.index))

    # Initialize counters and mask
    longest_up_streak: int = 0
    longest_down_streak: int = 0
    current_streak: int = 0
    current_direction: int = 0  # 0 = no direction, 1 = up, -1 = down
    mask: np.ndarray = np.zeros(n, dtype=int)

    # Single pass through all prices
    for i in range(1, n):
        previous_price: float = values[i - 1]
        current_price: float = values[i]

        # Determine movement direction
        if current_price > previous_price:
            new_direction: int = 1  # Upward movement
        elif current_price < previous_price:
            new_direction: int = -1  # Downward movement
        else:
            new_direction: int = 0  # Flat day

        # Record daily trend in mask for plotting
        mask[i] = new_direction

        # Update streaks efficiently
        if new_direction == current_direction and new_direction != 0:
            current_streak += 1
        else:
            # Direction change — update longest streaks before reset
            if current_direction == 1 and current_streak > longest_up_streak:
                longest_up_streak = current_streak
            elif current_direction == -1 and current_streak > longest_down_streak:
                longest_down_streak = current_streak

            # Reset for next streak
            current_direction = new_direction
            current_streak = 1 if new_direction != 0 else 0

    # Final streak check (include last run)
    if current_direction == 1 and current_streak > longest_up_streak:
        longest_up_streak = current_streak
    elif current_direction == -1 and current_streak > longest_down_streak:
        longest_down_streak = current_streak

    # Return results with mask as Series
    return (longest_up_streak, longest_down_streak, pd.Series(mask, index=close.index))


@timer
def compute_sdr(close: pd.Series) -> pd.Series:
    """
    Compute simple daily returns (fractional change from the previous close).

    Args:
        close (pd.Series): Series of closing prices.

    Returns:
        pd.Series: Fractional daily returns computed as close.pct_change()
            (r_t = close_t / close_{t-1} - 1); preserves the input index;
            the first value is NaN.
    """
    # RATIONALE (dev note):
    # - Normalizes moves for comparability; basis for volatility/risk metrics.

    # EFFICIENCY (dev note):
    # - O(n) time; O(n) output. Constant extra vars.

    # Convert pandas input to NumPy array for computation (manual algorithms)
    values: np.ndarray = close.values
    n: int = len(values)
    daily_returns: list[float | None] = [
        None
    ]  # First day has no return (no previous day)

    # Calculate daily returns using pure Python
    for i in range(1, n):
        previous_price: float = values[i - 1]
        current_price: float = values[i]

        if previous_price != 0:  # Avoid division by zero
            # Daily return = (current_price - previous_price) / previous_price
            daily_return: float = (current_price - previous_price) / previous_price
            daily_returns.append(daily_return)
        else:
            daily_returns.append(None)  # Handle zero price case

    # Convert Python result back to pandas Series for output
    return pd.Series(daily_returns, index=close.index)


@timer
def compute_max_profit(close: pd.Series) -> float:
    """
    Computes the maximum achievable profit by summing all positive day-to-day rises.

    Args:
        close (pd.Series): Closing price

    Returns:
        float: Total profit from the greedy algorithm. 
    """
    # RATIONALE (dev note):
    # - “Buy before every rise, sell after it” captures all local gains.
    # - Equivalent to summing max(0, P_{t+1} - P_t).

    # EFFICIENCY (dev note):
    # - O(n) time; O(1) extra space.
    # - Greedy is optimal here; no backtracking/lookahead required.

    # Convert pandas input to NumPy array for computation (manual algorithms)
    values: np.ndarray = close.values
    n: int = len(values)
    total_profit: float = 0.0

    # Calculate max profit by summing all positive price increases
    # Strategy: Buy before every price increase, sell after it
    for i in range(n - 1):
        current_price: float = values[i]
        next_price: float = values[i + 1]

        if next_price > current_price:
            # If next day's price is higher, we can profit by buying today and selling tomorrow
            profit_from_transaction: float = next_price - current_price
            total_profit += profit_from_transaction

    return float(total_profit)
