
import pytest

# Example dummy functions for demonstration
def compute_sma(data, window):
    if not data or window <= 0:
        raise ValueError("Invalid input for SMA calculation.")
    return sum(data[-window:]) / window

def validate_ticker(ticker):
    if not isinstance(ticker, str):
        raise TypeError("Ticker must be a string.")
    if len(ticker) < 1:
        return False
    return True

# Expected failure: window larger than data length
@pytest.mark.xfail(reason="Window larger than data length not yet handled gracefully")
def test_sma_large_window():
    compute_sma([10, 20], 5)

# Expected failure: invalid ticker type
@pytest.mark.xfail(raises=TypeError, reason="Invalid ticker type should raise TypeError")
def test_invalid_ticker_type():
    validate_ticker(123)

# Expected pass: valid ticker string
@pytest.mark.xpass
def test_valid_ticker():
    assert validate_ticker("AAPL") is True
