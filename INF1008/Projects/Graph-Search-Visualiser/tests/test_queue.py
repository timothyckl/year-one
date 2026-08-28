"""
tests/test_queue.py

pytest tests for the Queue class in data_structures/queue.py.
covers emptiness checks, FIFO ordering, and empty-dequeue error handling.
"""

import pytest

from data_structures.queue import Queue


def test_new_queue_is_empty():
    """a freshly constructed Queue must report itself as empty."""
    q = Queue()
    assert q.is_empty()


def test_enqueue_dequeue_fifo_order():
    """items must be returned in first-in first-out order."""
    q = Queue()
    q.enqueue(1)
    q.enqueue(2)
    q.enqueue(3)

    assert q.dequeue() == 1
    assert q.dequeue() == 2
    assert not q.is_empty()
    assert q.dequeue() == 3
    assert q.is_empty()


def test_dequeue_from_empty_raises_index_error():
    """dequeuing from an empty queue must raise IndexError."""
    q = Queue()
    with pytest.raises(IndexError):
        q.dequeue()


def test_fifo_ordering_with_strings():
    """enqueue/dequeue of string characters must preserve insertion order."""
    q = Queue()
    for ch in 'abcde':
        q.enqueue(ch)
    result = [q.dequeue() for _ in range(5)]
    assert result == list('abcde')
