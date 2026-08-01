"""
base.py

This module defines Pydantic data models representing financial entities
such as industries, sectors, and individual stock tickers.
These models can be used for data validation, serialization, and
interfacing with APIs or other data sources.

Notes:
    - Certain information fields from yfinance classes are not available,
    to handle this, we allow NoneTypes as value replacements.
"""

from typing import Any

from pydantic import BaseModel


class Ticker(BaseModel):
    """Represents an individual stock with financial attributes."""

    symbol: str
    display_name: str | None
    long_name: str | None
    short_name: str | None
    market_cap: float | None
    price: float | None
    sector: str | None
    industry: str | None
    description: str | None
    dividend_rate: float | None
    dividend_yield: float | None
    volume: int | None


class Industry(BaseModel):
    """Represents an industry and its top-performing companies."""

    description: str | None
    employee_count: int | None
    market_cap: int | None
    market_weight: float | None
    pct_change: float | None


class Sector(BaseModel):
    """Represents a market sector containing multiple industries."""

    key: str
    name: str
    overview: dict[str, Any]
    top_companies: list[str]
    top_etfs: list[str]
    top_mutual_funds: list[str]
    industries: list[str]
