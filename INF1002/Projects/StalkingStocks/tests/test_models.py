import pytest
from pydantic import ValidationError

from src.models.base import Ticker


@pytest.mark.parametrize(
    "ticker_data",
    [
        {
            "symbol": "NVDA",
            "display_name": "NVIDIA",
            "long_name": "NVIDIA Corporation",
            "short_name": "NVIDIA Corporation",
            "market_cap": 4338392236032,
            "price": 178.19,
            "sector": "Technology",
            "industry": "Semiconductors",
            "description": "NVIDIA Corporation, a computing infrastructure company, provides graphics and compute and networking solutions in the United States, Singapore, Taiwan, China, Hong Kong, and internationally. The Compute & Networking segment includes its Data Centre accelerated computing platforms and artificial intelligence solutions and software; networking; automotive platforms and autonomous and electric vehicle solutions; Jetson for robotics and other embedded platforms; and DGX Cloud computing services. The Graphics segment offers GeForce GPUs for gaming and PCs, the GeForce NOW game streaming service and related infrastructure, and solutions for gaming platforms; Quadro/NVIDIA RTX GPUs for enterprise workstation graphics; virtual GPU or vGPU software for cloud-based visual and virtual computing; automotive platforms for infotainment systems; and Omniverse software for building and operating industrial AI and digital twin applications. It also customized agentic solutions designed in collaboration with NVIDIA to accelerate enterprise AI adoption. The company's products are used in gaming, professional visualization, data center, and automotive markets. It sells its products to original equipment manufacturers, original device manufacturers, system integrators and distributors, independent software vendors, cloud service providers, consumer internet companies, add-in board manufacturers, distributors, automotive manufacturers and tier-1 automotive suppliers, and other ecosystem participants. NVIDIA Corporation was incorporated in 1993 and is headquartered in Santa Clara, California.",
            "dividend_rate": 0.04,
            "dividend_yield": 0.02,
            "volume": 148071154,
        }
    ],
)
def test_ticker_serialisation(ticker_data):
    data = Ticker(**ticker_data)
    assert data.symbol == "NVDA"
    # check the rest of the attributes (im lazy D:)


def test_valid_ticker():
    with pytest.raises(ValidationError):
        Ticker(
            **{
                "symbol": 123123,
                "display_name": "NVIDIA",
                "long_name": "NVIDIA Corporation",
                "short_name": "NVIDIA Corporation",
                "market_cap": 4338392236032,
                "price": 178.19,
                "sector": "Technology",
                "industry": "Semiconductors",
                "description": None,
                "dividend_rate": 0.04,
                "dividend_yield": 0.02,
                "volume": 148071154,
            }
        )
