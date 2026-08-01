import pandas as pd
import pytest

from src.models.base import Industry, Sector, Ticker
from src.services.finance import (get_industry_info, get_sector_info,
                                  get_ticker_data, get_ticker_info)


def test_get_ticker_info():
    ticker = get_ticker_info(ticker_symbol="NVDA")
    assert isinstance(ticker, Ticker)

    # test for valid ticker names
    # test for ticker not found


@pytest.mark.parametrize(
    "symbol",
    [
        "AAPL",
        ["NVDA", "MSFT", "AAPL"],
    ],
)
def test_get_ticker_data(symbol):
    data = get_ticker_data(ticker_symbols=symbol, progress=False, auto_adjust=True)
    assert isinstance(data, pd.DataFrame)  # our custom aliases don't exist at runtime


def test_get_sector_info(sectors):
    data = get_sector_info(sector_key=sectors[0])
    assert isinstance(data, Sector)


@pytest.mark.parametrize(
    "industry_key",
    [
        "semiconductors",
        "software-application",
    ],
)
def test_get_industry_info(industry_key):
    industry = get_industry_info(industry_key=industry_key)
    assert isinstance(industry, Industry)
