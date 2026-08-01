import pandas as pd
import pytest

from src.constants.sectors import SECTORS


@pytest.fixture
def sectors():
    return SECTORS


@pytest.fixture
def sample_close():
    sample_data: list[float] = [(i) for i in range(1, 101)]
    data: pd.Series = pd.Series(sample_data)
    return data
