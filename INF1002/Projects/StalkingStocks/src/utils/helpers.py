"""
helpers.py

This module contains general-purpose utility functions and helpers used across
the application.

The module is intended to provide reusable tools that simplify data ingestion,
processing, and analysis workflows. Additional helper functions may be added
as the application evolves.
"""

import logging
from datetime import date
from functools import wraps
from time import perf_counter
from typing import Callable, ParamSpec, TypeVar

import pandas as pd

# Configure logging
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(message)s", datefmt="%Y-%m-%d %H:%M:%S"
)


def format_date(date: date) -> str:
    return date.strftime("%b %d, %Y")  # e.g. Oct 08, 2025


def format_name(name: str) -> str:
    """Formats name of sector/industry to be usable by API"""
    # replace dashes
    name = name.replace("-", " ")
    # replace underscores
    name = name.replace("_", " ")
    return name.title()


def format_large_number(n: int | float) -> str:
    """
    Convert a large numeric value into a human-readable string with suffixes.

    Parameters:
        n (int | float ): The numeric value to format.

    Returns:
        A string representing the number in a shortened, human-readable format:
        - Thousands (K) for numbers >= 1,000
        - Millions (M) for numbers >= 1,000,000
        - Billions (B) for numbers >= 1,000,000,000
        - Trillions (T) for numbers >= 1,000,000,000,000
        - Otherwise, returns the number as a string
    """
    if n >= 1_000_000_000_000:
        return f"{n/1_000_000_000_000:.2f}T"
    elif n >= 1_000_000_000:
        return f"{n/1_000_000_000:.2f}B"
    elif n >= 1_000_000:
        return f"{n/1_000_000:.2f}M"
    elif n >= 1_000:
        return f"{n/1_000:.2f}K"
    return str(n)


def rolling_window(
    n: int, unit: str = "years", timezone: str = "Asia/Singapore"
) -> tuple[date, date]:
    """
    Compute a rolling (start, end) window ending at the current date.

    Args:
        n (int): Number of units to look back from.
        unit (str): Time unit for the window. One of {"days", "weeks", "months", "years"}.
        timezone (str): Time zone for the Timestamp.

    Returns:
        A tuple containing start and end dates.
    """
    end_ts: pd.Timestamp = pd.Timestamp.now(tz=timezone).normalize()

    if unit == "days":
        start_ts = end_ts - pd.Timedelta(days=n)
    elif unit == "weeks":
        start_ts = end_ts - pd.Timedelta(weeks=n)
    elif unit == "months":
        start_ts = end_ts - pd.DateOffset(months=n)
    elif unit == "years":
        start_ts = end_ts - pd.DateOffset(years=n)
    else:
        raise ValueError("unit must be one of {'days', 'weeks', 'months', 'years'}")

    return start_ts.date(), end_ts.date()


P = ParamSpec("P")
R = TypeVar("R")


def timer(func: Callable[P, R]) -> Callable[P, R]:
    """
    Measures wall clock-time of two points.

    Args:
        func (Callable): Function to be timed.

    Returns:
        A wrapped function that behaves like `func` but prints the time
        it took to execute.
    """

    @wraps(func)
    def wrapper(*args: P.args, **kwargs: P.kwargs) -> R:
        """Wrapper function that times the execution of `func`."""
        start = perf_counter()
        output = func(*args, **kwargs)
        end = perf_counter()
        logging.info(f"{func.__name__} executed in {(end - start):.6f} seconds")
        return output

    return wrapper
