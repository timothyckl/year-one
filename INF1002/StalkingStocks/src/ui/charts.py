"""
charts.py

Plotly chart builders used by the UI layer.
"""

import pandas as pd
import plotly.express as px
import plotly.graph_objs as go
from plotly.subplots import make_subplots


def create_figure() -> go.Figure:
    """
    Creates a plotly figure for visualisation of various charts.

    Returns:
        go.Figure: A plot-ready figure.
    """
    fig: go.Figure = make_subplots(
        rows=2,
        cols=1,
        shared_xaxes=True,
        vertical_spacing=0.03,
        row_heights=[0.7, 0.3],
    )
    fig.update_xaxes(showticklabels=True, row=1, col=1)
    return fig


def set_treemap(summary_df: pd.DataFrame) -> go.Figure:
    """
    Builds an industry treemap colored by sign of percent change.

    Note:
        This figure is separate from the one used in visualising ticker-specific data.

    Args:
        summary_df (pd.DataFrame): summary_df

    Returns:
        go.Figure: Treemap figure.
    """
    fig: go.Figure = px.treemap(
        summary_df,
        path=["industry"],  # unique leaves
        values="weight",
        color=summary_df["pct_change"] >= 0,  # boolean → True/False
        color_discrete_map={True: "#2ca02c", False: "#d62728"},
        custom_data=["pct_change"],
    )
    fig.update_traces(
        texttemplate="%{label}<br>Weight: %{value:.2%}<br>Change: %{customdata[0]:.2f}%",
        textfont=dict(size=18),
    )

    fig.update_layout(
        margin=dict(l=0, r=0, t=0, b=0),
    )

    return fig


def set_linechart(fig: go.Figure, close: pd.Series) -> go.Figure:
    """
    Sets a simple line trace of closing prices onto a figure.

    Args:
        fig (go.Figure): Target figure to receive trace
        close (pd.Series): Closing price

    Returns:
        go.Figure: Figure with line chart plotted.
    """
    fig.add_trace(
        go.Scatter(
            x=close.index,
            y=close.values,
            mode="lines",
            line=dict(color="blue"),
            name="Close Price",
        )
    )
    return fig


def set_line_trend_chart(
    fig: go.Figure, close: pd.Series, up: pd.Series, down: pd.Series, mask: pd.Series
) -> go.Figure:
    """
    Sets line+marker trace and annotate longest up/down runs onto a figure.

    Args:
        fig (go.Figure): Target figure (assumes subplot at row=1, col=1 exists)
        close (pd.Series): Closing price
        up (pd.Series): Longest upward run length
        down (pd.Series): Longwar downward run length
        mask (pd.Series): Direction per day: 1 (up), 0 (flat), -1 (down).

    Returns:
        go.Figure: Figure with line+marker chart and annotations plotted.
    """
    # DEV NOTES:
    # - Marker color encodes direction (green for up/flat, red for down).
    # - Annotation uses paper coordinates (xref/yref="paper") so it stays
    #   pinned relative to the figure area regardless of data zoom/pan.
    marker_colors = ["green" if m == 1 or m == 0 else "red" for m in mask]
    mode = "lines+markers"
    line_color = "gray"

    fig.add_trace(
        go.Scatter(
            x=close.index,
            y=close.values,
            mode=mode,
            line=dict(color=line_color),  # fallback, ignored by marker coloring
            marker=dict(color=marker_colors, size=8),  # color points
            name="Close Price",
        ),
        row=1,
        col=1,
    )

    # Add static annotation (paper coordinates)
    fig.add_annotation(
        x=0.02,
        y=0.98,  # 2% from left, 2% from top
        xref="paper",
        yref="paper",  # relative to figure, not data
        text=f"📈 Longest Up Run: {up} days<br>📉 Longest Down Run: {down} days",
        showarrow=False,
        font=dict(size=13, color="black"),
        align="left",
        bgcolor="rgba(255,255,255,0.6)",  # semi-transparent background for visibility
        bordercolor="black",
        borderwidth=1,
        borderpad=4,
    )
    return fig


def set_candlechart(fig: go.Figure, ticker_data: pd.DataFrame) -> go.Figure:
    """
    Sets a candlestick trace onto a figure.

    Args:
        fig (go.Figure): Target figure
        ticker_data (pd.DataFrame): Ticker dataframe containing OHLC data

    Returns:
        go.Figure: Figure with candlestick chart plotted.
    """
    # DEV NOTES:
    # - Adds to row=1, col=1 to align with other price overlays/indicators.
    fig.add_trace(
        go.Candlestick(
            x=ticker_data.index,
            open=ticker_data["Open"],
            high=ticker_data["High"],
            low=ticker_data["Low"],
            close=ticker_data["Close"],
            name="Price",
        ),
        row=1,
        col=1,
    )
    return fig


def set_indicators(fig: go.Figure, close: pd.Series, n: int) -> go.Figure:
    """
    Sets a technical indicator trace onto a figure.

    Note:
        - We assume SMA to be the only technical incators as of now.

    Args:
        fig (go.Figure): Target figure
        close (pd.Series): Closing price
        n (int): SMA period (e.g. 5, 20, 50)

    Returns:
        go.Figure: Figure with computed technical indicators plotted.
    """
    # DEV NOTES:
    # - Keep line thin so it layers well over candles.
    # - Name uses f"SMA {n}" for concise legends.
    fig.add_trace(
        go.Scatter(
            x=close.index, y=close, mode="lines", line=dict(width=1.5), name=f"SMA {n}"
        )
    )

    return fig
