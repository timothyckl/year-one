"""
overview.py

Streamlit UI renderers for sector & industry summaries and ticker charts.

Notes
- Cached helpers (`@st.cache_data`) are used to reduce repeated API calls during interaction.
- Expects ticker/industry dataframes with standard OHLC columns and datetime index.
"""

from typing import Any

import pandas as pd
import streamlit as st
from streamlit.delta_generator import DeltaGenerator

from src.models.base import Sector, Ticker
from src.ui.adapters import (make_chart_inputs, make_indicator_inputs,
                             make_industry_summary_df, make_insight_input,
                             make_price_metrics, make_sector_inputs)
from src.ui.charts import (create_figure, set_candlechart, set_indicators,
                           set_line_trend_chart, set_linechart, set_treemap)
from src.utils.helpers import format_large_number, timer


@timer
def display_sector_overview(column: DeltaGenerator) -> Sector:
    """
    Displays the sector overview panel.

    Args:
        column (DeltaGenerator): Column to be displayed in.

    Returns:
        Sector: Sector metadata to be passed to other components.
    """
    sector_data: Sector = make_sector_inputs(column)
    overview: dict[str, Any] = sector_data.overview

    column.subheader(sector_data.name)
    column.text(overview["description"])

    left, middle, right = column.columns(3, border=True)

    left.metric("Companies", overview["companies_count"])
    middle.metric("Industries", overview["industries_count"])
    right.metric("Employees", format_large_number(overview["employee_count"]))

    left, right = column.columns(2, border=True)

    left.metric("Market Cap", f"{format_large_number(overview["market_cap"])} USD")
    right.metric("Market Weight", f"{overview['market_weight']*100:.2f}%")

    return sector_data


@timer
def display_industry_overview(column: DeltaGenerator, industries: list[str]) -> None:
    """
    Render the sector’s industry breakdown (treemap).

    Args:
        column (DeltaGenerator): Column to be displayed in.
        industries (list[str]): List of industries
    """
    # DEV NOTE:
    # - Creates a DataFrame for Plotly treemap; color column derived from pct_change sign.
    column.subheader(
        "Sector Breakdown", help="A sector is comprised of multiplie industries."
    )
    column.info(
        "This shows a sector's industry weights and how they performed today.",
        icon=":material/info:",
    )

    summary_df = make_industry_summary_df(industries)
    fig = set_treemap(summary_df)

    column.plotly_chart(fig, use_container_width=True)


@timer
@st.cache_data
def display_basic_price_info(ticker_info: Ticker, ticker_data: pd.DataFrame):
    """
    Render top-level price metrics for the current ticker.

    Args:
        ticker_info (Ticker): Object providing `price`, `long_name`, `symbol`, and `description`.
        ticker_data (pd.DataFrame): OHLCV data with columns: Open, High, Low, Close (indexed by datetime).
    """
    metrics = make_price_metrics(ticker_info, ticker_data)
    st.metric(
        "Price",
        f"{metrics['latest_price']:.2f} USD",
        f"{metrics['latest_return']:.2%}",
        border=False,
    )
    st.metric("Abs. Change", f"{metrics['absolute_change']:.2f} USD", border=False)
    st.metric("Today's Open", f"{metrics['latest_open']:.2f} USD", border=False)
    st.metric("Day's Range", metrics["days_range"], border=False)


@timer
def display_charts(column: DeltaGenerator, filters: dict[str, Any]) -> None:
    """
    Render price chart(s), indicators, and summary stats for a ticker.

    Args:
        column (DeltaGenerator): Column to be displayed in.
        filters (dict[str, Any]): Filter selection to be used for plotting. 
    """
    inputs = make_chart_inputs(filters)

    if inputs is None:
        column.error(
            f"Whoops, seems like data for {filters['selected_ticker']} could not be retrieved."
        )
        st.stop()

    ticker_info = inputs["ticker_info"]
    ticker_data = inputs["ticker_data"]
    up_streaks = inputs["up_streaks"]
    down_streaks = inputs["down_streaks"]
    streak_mask = inputs["streak_mask"]
    close = ticker_data["Close"]

    display_name = f"{ticker_info.long_name} ({ticker_info.symbol})"
    column.subheader(display_name)

    row = column.container(horizontal=True)

    with row:
        display_basic_price_info(ticker_info, ticker_data)

    # DEV NOTE:
    # - Keeps branching for chart types; indicators are added afterward.
    if ticker_data is None:
        column.error("No price data was found. Try again.")
    else:
        fig = create_figure()

        if filters["selected_chart_type"] == "Line Chart":
            fig = set_linechart(fig, close)
        elif filters["selected_chart_type"] == "Line Chart and Trend Markers":
            fig = set_line_trend_chart(
                fig, close, up_streaks, down_streaks, streak_mask
            )
        else:
            fig = set_candlechart(fig, ticker_data)

        # this adds technical indicator overlaid onto existing charts
        indicator_inputs = make_indicator_inputs(close, filters["selected_indicators"])

        for key, value in indicator_inputs.items():
            fig = set_indicators(fig, value, key)

        fig.update_layout(
            xaxis=dict(type="date", tickformat="%b %d, %Y"),
            yaxis=dict(title="Price"),
            margin=dict(l=0, r=0, t=0, b=0),
        )

        column.plotly_chart(fig, use_container_width=True)

        # SUMMARY TEXT (dev note):
        # - Uses greedy max-profit metric as a quick “best sequence” indicator.
        max_profit, start_date, end_date = make_insight_input(
            close, filters["selected_horizon"]
        )
        column.markdown(
            f"**Your best trading sequence from :red[{start_date}] to :red[{end_date}], you could have earned :green[${max_profit:.2f}] in total profit.**"
        )

        with column.expander(f"{display_name} Overview"):
            st.write(ticker_info.description)
