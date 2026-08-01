"""
data_structures/priority_queue.py

defines a min-heap PriorityQueue used by Dijkstra's algorithm.
stores (priority, item) tuples and always extracts the minimum priority first.
implements lazy deletion to support re-insertion without a decrease-key
operation (the caller simply re-inserts with the updated priority and skips
stale entries when extracting).
"""


class PriorityQueue:
    """
    a min-heap priority queue storing (priority, item) pairs.

    the heap invariant is: heap[parent] <= heap[child] for all nodes.
    duplicate items may exist (lazy deletion pattern) — callers should
    check whether an extracted item has already been settled.

    methods:
        insert(priority, item)  -- O(log n)
        extract_min()           -- O(log n)
        is_empty()              -- O(1)
    """

    def __init__(self) -> None:
        # backing list of (priority, item) tuples
        self._heap: list = []

    # ------------------------------------------------------------------
    # public interface
    # ------------------------------------------------------------------

    def insert(self, priority: float, item: object) -> None:
        """
        inserts a new (priority, item) pair into the heap.

        args:
            priority (float): numeric priority; lower values extracted first
            item: any hashable python object

        time complexity: O(log n)
        """
        self._heap.append((priority, item))
        self._sift_up(len(self._heap) - 1)

    def extract_min(self) -> tuple:
        """
        removes and returns the (priority, item) pair with the lowest priority.

        returns:
            tuple: (priority, item)

        raises:
            IndexError: if the heap is empty

        time complexity: O(log n)
        """
        if self.is_empty():
            raise IndexError("extract_min from an empty priority queue")

        # swap root (minimum) with last element, then pop last
        self._swap(0, len(self._heap) - 1)
        minimum = self._heap.pop()
        # restore heap property from root downward
        if self._heap:
            self._sift_down(0)
        return minimum

    def is_empty(self) -> bool:
        """returns True if the heap contains no items."""
        return len(self._heap) == 0

    # ------------------------------------------------------------------
    # internal heap helpers
    # ------------------------------------------------------------------

    def _parent(self, idx: int) -> int:
        """returns the index of the parent of node at idx."""
        return (idx - 1) // 2

    def _left(self, idx: int) -> int:
        """returns the index of the left child of node at idx."""
        return 2 * idx + 1

    def _right(self, idx: int) -> int:
        """returns the index of the right child of node at idx."""
        return 2 * idx + 2

    def _swap(self, i: int, j: int) -> None:
        """swaps elements at indices i and j in the heap."""
        self._heap[i], self._heap[j] = self._heap[j], self._heap[i]

    def _sift_up(self, idx: int) -> None:
        """
        moves the element at idx upward until the heap invariant is restored.
        called after inserting at the tail.

        args:
            idx (int): index of the newly inserted element

        time complexity: O(log n)
        """
        while idx > 0:
            parent_idx = self._parent(idx)
            # compare tuples: (priority, item) — priority is index 0
            if self._heap[parent_idx] > self._heap[idx]:
                self._swap(parent_idx, idx)
                idx = parent_idx
            else:
                break

    def _sift_down(self, idx: int) -> None:
        """
        moves the element at idx downward until the heap invariant is restored.
        called after extracting the root (min element).

        args:
            idx (int): index of the element to sift down (usually 0)

        time complexity: O(log n)
        """
        size = len(self._heap)
        while True:
            smallest = idx
            left = self._left(idx)
            right = self._right(idx)

            # find the smallest among node, left child, and right child
            if left < size and self._heap[left] < self._heap[smallest]:
                smallest = left
            if right < size and self._heap[right] < self._heap[smallest]:
                smallest = right

            if smallest == idx:
                # heap invariant restored
                break

            self._swap(idx, smallest)
            idx = smallest

