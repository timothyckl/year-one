"""
comparison-based sorting algorithms for benchmarking

this module provides implementations of o(n log n) comparison-based
sorting algorithms for performance comparison against radix sort.

algorithms implemented:
    - merge sort: o(n log n) worst case, stable
    - quick sort: o(n log n) average case, unstable

these serve as baselines to demonstrate that radix sort achieves
better-than-o(n log n) performance for bounded integer keys.
"""

import random
from typing import List


def merge_sort(array: List[int]) -> List[int]:
    """
    sort an array using the merge sort algorithm.

    merge sort is a divide-and-conquer algorithm that:
    1. divides the array into two halves
    2. recursively sorts each half
    3. merges the sorted halves

    this implementation is comparison-based and achieves o(n log n)
    time complexity in all cases (best, average, worst).

    args:
        array: list of integers to sort

    returns:
        a new sorted list

    time complexity: o(n log n)
    space complexity: o(n) auxiliary space for merging
    """
    # base case: arrays of 0 or 1 element are already sorted
    if len(array) <= 1:
        return array.copy()

    # divide: split the array into two halves
    mid = len(array) // 2
    left_half = array[:mid]
    right_half = array[mid:]

    # conquer: recursively sort each half
    sorted_left = merge_sort(left_half)
    sorted_right = merge_sort(right_half)

    # combine: merge the sorted halves
    return _merge(sorted_left, sorted_right)


def _merge(left: List[int], right: List[int]) -> List[int]:
    """
    merge two sorted arrays into a single sorted array.

    this is the core operation of merge sort, requiring o(n)
    comparisons to merge two arrays of total size n.

    args:
        left: first sorted array
        right: second sorted array

    returns:
        merged sorted array
    """
    result = []
    left_index = 0
    right_index = 0

    # compare elements from both arrays and add the smaller one
    while left_index < len(left) and right_index < len(right):
        if left[left_index] <= right[right_index]:
            result.append(left[left_index])
            left_index += 1
        else:
            result.append(right[right_index])
            right_index += 1

    # add remaining elements (only one of these will have elements)
    result.extend(left[left_index:])
    result.extend(right[right_index:])

    return result


def quick_sort(array: List[int]) -> List[int]:
    """
    sort an array using the quick sort algorithm.

    quick sort is a divide-and-conquer algorithm that:
    1. selects a pivot element
    2. partitions the array around the pivot
    3. recursively sorts the partitions

    this implementation uses random pivot selection to achieve
    o(n log n) expected time complexity.

    args:
        array: list of integers to sort

    returns:
        a new sorted list

    time complexity: o(n log n) average, o(n^2) worst case
    space complexity: o(log n) average for recursion stack
    """
    # work on a copy to avoid modifying the original
    result = array.copy()
    _quick_sort_helper(result, 0, len(result) - 1)
    return result


def _quick_sort_helper(array: List[int], low: int, high: int) -> None:
    """
    recursive helper for quick sort.

    args:
        array: the array being sorted (modified in place)
        low: starting index of the partition
        high: ending index of the partition
    """
    if low < high:
        # partition and get the pivot's final position
        pivot_index = _partition(array, low, high)

        # recursively sort elements before and after partition
        _quick_sort_helper(array, low, pivot_index - 1)
        _quick_sort_helper(array, pivot_index + 1, high)


def _partition(array: List[int], low: int, high: int) -> int:
    """
    partition the array around a randomly selected pivot.

    elements less than the pivot go to the left,
    elements greater than the pivot go to the right.

    args:
        array: the array being partitioned
        low: starting index
        high: ending index

    returns:
        the final position of the pivot element
    """
    # random pivot selection to avoid worst-case on sorted inputs
    pivot_index = random.randint(low, high)
    array[pivot_index], array[high] = array[high], array[pivot_index]

    pivot = array[high]
    i = low - 1

    for j in range(low, high):
        if array[j] <= pivot:
            i += 1
            array[i], array[j] = array[j], array[i]

    array[i + 1], array[high] = array[high], array[i + 1]
    return i + 1


def python_builtin_sort(array: List[int]) -> List[int]:
    """
    wrapper for python's built-in sort (timsort).

    timsort is a hybrid sorting algorithm derived from merge sort
    and insertion sort. it achieves o(n log n) worst-case time
    complexity and is highly optimised for real-world data.

    args:
        array: list of integers to sort

    returns:
        a new sorted list
    """
    return sorted(array)


# verification when run directly
if __name__ == "__main__":
    test_array = [64, 34, 25, 12, 22, 11, 90]

    print("comparison sorts verification")
    print("=" * 40)
    print(f"input: {test_array}")
    print()

    print(f"merge sort:  {merge_sort(test_array)}")
    print(f"quick sort:  {quick_sort(test_array)}")
    print(f"python sort: {python_builtin_sort(test_array)}")
    print()

    # verify all produce the same result
    expected = sorted(test_array)
    assert merge_sort(test_array) == expected, "merge sort failed!"
    assert quick_sort(test_array) == expected, "quick sort failed!"
    print("all algorithms produce correct results.")
