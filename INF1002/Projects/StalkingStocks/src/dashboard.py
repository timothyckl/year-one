import streamlit as st

from src.ui.filters import display_filters
from src.ui.overview import (display_charts, display_industry_overview,
                             display_sector_overview)
from src.utils.helpers import timer


def configure_page() -> None:
    """Sets Streamlit page configurations."""
    st.set_page_config(page_title="Stalking Stocks", page_icon="📈", layout="wide")


def display_header() -> None:
    """Displays the header section of the page."""
    st.title("📈 Stalking Stocks")
    st.markdown(
        "<small style='color: gray;'>Made by Aamir, Dalton, Gin, Hasif, and Tim.</small>",
        unsafe_allow_html=True,
    )


def display_sector_industry_overview() -> dict[str, str | float]:
    """
    Displays sector and industry overview sections.

    Returns:
        dict[str, str | float]: sector data to be passed to filters
    """
    sector_col, industry_col = st.columns(2, border=True)
    sector_data = display_sector_overview(sector_col)
    display_industry_overview(industry_col, sector_data.industries)
    return sector_data.model_dump()  # this will be passed to filters


def display_filter_and_charts(sector_data: dict[str, str | float]) -> None:
    """
    Displays filter and chart sections.

    Args:
        sector_data (dict[str, str | float]): data to be passed to filters 
    """
    filter_col, chart_col = st.columns([1, 3], border=True)
    filters = display_filters(filter_col, sector_data)
    display_charts(chart_col, filters)


def display_body() -> None:
    """Displays the main content of the page."""
    sector_data = display_sector_industry_overview()
    display_filter_and_charts(sector_data)


@timer
def run_dashboard() -> None:
    configure_page()
    display_header()
    display_body()
