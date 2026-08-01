"""
tests/test_priority_queue.py

pytest tests for the PriorityQueue class in data_structures/priority_queue.py.
covers emptiness, ascending extraction order, equal priorities, error handling,
and single-element behaviour.
"""

import pytest

from data_structures.priority_queue import PriorityQueue


def test_new_priority_queue_is_empty():
    """a freshly constructed PriorityQueue must report itself as empty."""
    pq = PriorityQueue()
    assert pq.is_empty()


def test_extract_min_returns_items_in_priority_order():
    """items inserted out of order must be extracted in ascending priority order."""
    pq = PriorityQueue()
    pq.insert(5, 'e')
    pq.insert(1, 'a')
    pq.insert(3, 'c')
    pq.insert(2, 'b')
    pq.insert(4, 'd')

    extracted = []
    while not pq.is_empty():
        extracted.append(pq.extract_min())

    priorities = [p for p, _ in extracted]
    assert priorities == sorted(priorities), (
        "extraction must be in ascending priority order"
    )


def test_equal_priority_items_all_returned():
    """two items with the same priority must both be extractable."""
    pq = PriorityQueue()
    pq.insert(2, 'x')
    pq.insert(2, 'y')
    results = {pq.extract_min(), pq.extract_min()}
    assert (2, 'x') in results and (2, 'y') in results


def test_extract_from_empty_raises_index_error():
    """extract_min on an empty priority queue must raise IndexError."""
    pq = PriorityQueue()
    with pytest.raises(IndexError):
        pq.extract_min()


def test_single_element_insert_and_extract():
    """a single inserted element must be returned correctly and leave the queue empty."""
    pq = PriorityQueue()
    pq.insert(10, 'solo')
    assert pq.extract_min() == (10, 'solo')
    assert pq.is_empty()
