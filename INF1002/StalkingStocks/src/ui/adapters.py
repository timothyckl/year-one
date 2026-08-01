"""
adapter.py

Streamlit view layer for finance features: transforms domain/service data into
display-ready inputs, metrics, and data frames (sectors/industries, ticker info),
cleans OHLC data, computes indicators, and assembles chart/insight payloads with caching.
"""

from datetime import date
from typing import Any, Iterable, Sequence

import pandas as pd
import streamlit as st
from streamlit.delta_generator import DeltaGenerator

from src.models.base import Sector, Ticker
from src.services.core import (compute_max_profit, compute_sdr, compute_sma,
                               compute_streak)
from src.services.data import clean_data
from src.services.finance import (IndustryOverview, get_industry_overview,
                                  get_sector_info, get_sectors,
                                  get_ticker_data, get_ticker_info)
from src.utils.helpers import format_date, format_name


def make_sector_inputs(column: DeltaGenerator) -> Sector:
    """
    Prepares sector ready inputs for visualisation.

    Args:
        column (DeltaGenerator): Column for streamlit elements to be stationed.

    Returns:
        Sector: Sector information for visualisation or passed to other Streamlit components.
    """
    sectors: Sequence[str] = get_sectors()

    selected_sector: str = column.selectbox(
        "Choose a sector", options=sectors, index=9, format_func=format_name
    )

    sector_data: Sector = get_sector_info(selected_sector)
    return sector_data


def make_price_metrics(
    ticker_info: Ticker, ticker_data: pd.DataFrame
) -> dict[str, float | str | pd.Series] | None:
    """
    Prepares basic price metrics for visualisation.

    Args:
        ticker_info (Ticker): Ticker information
        ticker_data (pd.DataFrame): Ticker data

    Returns:
        dict[str, float | str | pd.Series] | None: If no errors from yfinance,
        will return price metrics, else None.
    """

    # data here is assumed to be cleaned before this
    close: pd.Series = ticker_data["Close"]
    open: pd.Series = ticker_data["Close"]
    high: pd.Series = ticker_data["Close"]
    low: pd.Series = ticker_data["Open"]

    # handles NoneType error when interval and horizon match
    if close.isna().any() and len(close) == 1:
        st.error("Whoops, could not fetch data!")
        return None
    else:
        sdr: pd.Series = compute_sdr(close)
        latest_price: float = ticker_info.price
        latest_return: float = sdr.iloc[-1]
        previous_close: float = close.iloc[-2]
        absolute_change: float = latest_price - previous_close
        latest_open: float = open.iloc[-1]
        days_range: str = f"{low.iloc[-1]:.2f} - {high.iloc[-1]:.2f}"

        metrics: dict[str, float | str | pd.Series] = {
            "sdr": sdr,
            "latest_price": latest_price,
            "latest_return": latest_return,
            "previous_close": previous_close,
            "absolute_change": absolute_change,
            "latest_open": latest_open,
            "days_range": days_range,
        }

        return metrics


@st.cache_data
def make_industry_summary_df(industries: list[str]) -> pd.DataFrame:
    """
    Prepares summary data of all industries.

    Args:
        industries (list[str]): Industries to be compiled.

    Returns:
        pd.DataFrame: Industry summary containing weights, percentage changes,
        and colours (for visualisation).
    """
    overview: IndustryOverview = get_industry_overview(industries)
    df = pd.DataFrame(list(overview))  # realize iterable once
    df.rename(columns={"industry": "raw_industry"}, inplace=True)

    df.insert(0, "industry", df["raw_industry"].map(format_name))
    df["color"] = df["pct_change"].apply(lambda x: "green" if float(x) >= 0 else "red")

    subset: pd.DataFrame = df[["industry", "weight", "pct_change", "color"]]

    return subset


def make_indicator_inputs(
    close: pd.Series, indicators: Iterable[int]
) -> dict[int, pd.Series]:
    """
    Prepares technical indicator inputs for visualisation.

    Args:
        close (pd.Series): Closing price
        indicators (Iterable[int]): Technical indicator to compute

    Returns:
        dict[int, pd.Series]: Mapping of technical indicators to their computed values.
    """
    out: dict[int, pd.Series] = {}

    for n in indicators:
        out[n] = compute_sma(close, n)
    return out


def clean_ohlc(df: pd.DataFrame) -> pd.DataFrame:
    """
    Cleans Open, High, Low, Close (OHLC) data within a dataframe.

    Args:
        df (pd.DataFrame): Financial stock data to be cleaned. 

    Returns:
        pd.DataFrame: Cleaned dataframe.
    """
    cols = ["Open", "High", "Low", "Close"]
    # guard against missing columns
    missing = [c for c in cols if c not in df.columns]
    if missing:
        raise KeyError(f"Missing columns: {missing}")
    return df[cols].apply(clean_data, axis=0)


@st.cache_data
def make_chart_inputs(filters: dict[str, Any]) -> dict[str, Any] | None:
    """
    Prepares chart inputs for ticker data visualisation. 

    Args:
        filters (dict[str, Any]): Filter information from UI. 

    Returns:
        dict[str, Any]: Plotting-ready inputs for visualisation. 
    """
    ticker_data = get_ticker_data(
        filters["selected_ticker"],
        interval=filters["selected_interval"],
        **filters["selected_horizon"],
        progress=False,
        auto_adjust=True,
    )

    if ticker_data is None:
        return None
    else:
        cleaned = clean_ohlc(ticker_data)

        ticker_info = get_ticker_info(filters["selected_ticker"])
        up, down, mask = compute_streak(cleaned["Close"])

        chart_inputs: dict[str, Any] = {
            "ticker_info": ticker_info,
            "ticker_data": cleaned,
            "up_streaks": up,
            "down_streaks": down,
            "streak_mask": mask,
        }

        return chart_inputs


def make_insight_input(
    close: pd.Series, horizon: dict[str, date]
) -> tuple[float, str, str]:
    """
    Prepares inputs for textual insight visualisation.

    Args:
        close (pd.Series): Closing price 
        horizon (dict[str, date]): User selected horizon from UI. 

    Returns:
        tuple[float, str, str]: Maximum achieveable profit, start date, end data
    """
    max_profit: float = compute_max_profit(close)
    start_date: str = format_date(horizon["start"])
    end_date: str = format_date(horizon["end"])

    return max_profit, start_date, end_date
