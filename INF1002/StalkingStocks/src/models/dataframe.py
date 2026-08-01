"""
dataframe.py

This module defines dataframe models/schema used to validate OHLCV
(open, high, low, close, volume) data retrieved from yfinance.

Notes:
    - `DataFrameModel` is used instead of `SchemaModel`, which will be deprecated.
    - In this context, the terms 'model' and 'schema' are used interchangeably to
    describe data validation structures.
"""

from typing import TypeAlias

from pandera.dtypes import DateTime
from pandera.pandas import DataFrameModel, Field
from pandera.typing import DataFrame, Series


class SingleStockData(DataFrameModel):
    """Schema for validating historical market price and volume data."""

    close: Series[float] = Field(alias="Close")
    high: Series[float] = Field(alias="High")
    low: Series[float] = Field(alias="Low")
    open: Series[float] = Field(alias="Open")
    volume: Series[int] = Field(alias="Volume")

    class Config:
        coerce = True
        strict = False  # allow extra columns if yfinance adds fields


class MultipleStockData(DataFrameModel):
    """Schema for validating multiple historical market prices and volume data."""

    date: Series[DateTime] = Field(alias="Date", nullable=False)
    ticker: Series[str] = Field(alias="Ticker", nullable=False)
    close: Series[float] = Field(alias="Close")
    high: Series[float] = Field(alias="High")
    low: Series[float] = Field(alias="Low")
    open: Series[float] = Field(alias="Open")
    volume: Series[int] = Field(alias="Volume")

    class Config:
        coerce = True
        strict = False  # allow extra columns if yfinance adds fields


StockDataFrame: TypeAlias = DataFrame[SingleStockData] | DataFrame[MultipleStockData]
