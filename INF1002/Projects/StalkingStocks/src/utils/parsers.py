"""
parsers.py

This module contains parsers that convert yfinance domain objects into our own typed models
(`Ticker`, `Industry`, `Sector`). Missing fields are tolerated via dict `.get()`.
"""


from typing import Any

import yfinance as yf

from src.models.base import Industry, Sector, Ticker


def yf_ticker_to_model(ticker_obj: yf.Ticker) -> Ticker:
    """
    Converts a yfinance Ticker into a structured Ticker model.

    Note:
        Certain ticker information is unavailable. To handle this we use
        the dictionary get() method which returns None when a key does not exist.

    Args:
        symbol (str): Ticker symbol

    Return:
        A Ticker data model.
    """
    info: dict[str, Any] = ticker_obj.info

    return Ticker(
        symbol=info.get("symbol"),
        display_name=info.get("displayName"),
        long_name=info.get("longName"),
        short_name=info.get("shortName"),
        market_cap=info.get("marketCap"),
        price=info.get("currentPrice"),
        sector=info.get("sector"),
        industry=info.get("industry"),
        description=info.get("longBusinessSummary"),
        dividend_rate=info.get("dividendRate"),
        dividend_yield=info.get("dividendYield"),
        volume=info.get("volume"),
    )


def yf_industry_to_model(industry_obj: yf.Industry) -> Industry:
    """Converts a yfinance Industry into a structured Industry model."""

    overview = industry_obj.overview
    extra_info = industry_obj.ticker.get_info()

    return Industry(
        description=overview.get("description"),
        employee_count=overview.get("employee_count"),
        market_cap=overview.get("market_cap"),
        market_weight=overview.get("market_weight"),
        pct_change=extra_info.get("regularMarketChangePercent"),
    )


def yf_sector_to_model(sector_obj: yf.Sector) -> Sector:
    """Converts a yfinance Sector into a structured Sector model."""

    return Sector(
        key=sector_obj.key,
        name=sector_obj.name,
        overview=sector_obj.overview,
        top_companies=sector_obj.top_companies.index,
        top_etfs=sector_obj.top_etfs.keys(),
        top_mutual_funds=sector_obj.top_mutual_funds.keys(),
        industries=sector_obj.industries.index,
    )
