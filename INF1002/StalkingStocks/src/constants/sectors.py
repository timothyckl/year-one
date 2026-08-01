"""
sectors.py

This module provides a list of constant sector names used for interfacing with
the yfinance API. The `SECTORS` list can be used when filtering or querying
stock data by sector.
"""

from typing import Sequence

SECTORS: Sequence[str] = (
    "basic-materials",
    "communication-services",
    "consumer-cyclical",
    "consumer-defensive",
    "energy",
    "financial-services",
    "healthcare",
    "industrials",
    "real-estate",
    "technology",
    "utilities",
)
