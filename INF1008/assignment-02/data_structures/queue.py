"""
data_structures/queue.py

defines a Queue (FIFO) data structure implemented without collections.deque.
uses a single list with a moving front pointer to achieve O(1) enqueue
and O(1) dequeue without shifting elements.
"""


class Queue:
    """
    a first-in first-out (FIFO) queue backed by a list with a front pointer.

    dequeue does not shift elements — the front pointer advances instead,
    giving amortised O(1) time for all operations.

    note: the backing list grows indefinitely. for very long-running sessions
    consider periodic compaction, but this is not needed for grid sizes
    up to 30x30 (max 900 elements).
    """

    def __init__(self) -> None:
        # backing store; front pointer avoids O(n) pops from index 0
        self._data: list = []
        self._front: int = 0

    def enqueue(self, item: object) -> None:
        """
        adds an item to the back of the queue.

        args:
            item: any python object to enqueue

        time complexity: O(1) amortised
        """
        self._data.append(item)

    def dequeue(self) -> object:
        """
        removes and returns the item at the front of the queue.

        returns:
            the oldest item in the queue

        raises:
            IndexError: if the queue is empty
        """
        if self.is_empty():
            raise IndexError("dequeue from an empty queue")
        item = self._data[self._front]
        self._front += 1
        return item

    def is_empty(self) -> bool:
        """returns True if the queue contains no items."""
        return self._front >= len(self._data)

