"""
comprehensive test suite for radix sort implementation

this module provides exhaustive testing of the radix sort algorithm,
covering edge cases, typical usage scenarios, and stress conditions.
all tests verify correctness by comparing against python's built-in
sorted() function.

test categories:
    - edge cases: empty arrays, single elements, all identical
    - ordering tests: sorted, reverse sorted, random
    - value range tests: small values, large values, mixed
    - scale tests: performance with large input sizes
    - stability tests: verify stable sorting behaviour

run tests with: python -m pytest tests/test_radix_sort.py -v
"""

import random
import sys
import unittest

# add src directory to path for imports
sys.path.insert(0, "../src")
from src.radix_sort import (count_digits, counting_sort_by_digit, get_digit,
                            radix_sort)


class TestGetDigit(unittest.TestCase):
    """tests for the get_digit helper function."""

    def test_units_digit(self):
        """extract the least significant digit."""
        self.assertEqual(get_digit(12345, 0), 5)
        self.assertEqual(get_digit(100, 0), 0)
        self.assertEqual(get_digit(7, 0), 7)

    def test_higher_digits(self):
        """extract digits at various positions."""
        self.assertEqual(get_digit(12345, 1), 4)  # tens
        self.assertEqual(get_digit(12345, 2), 3)  # hundreds
        self.assertEqual(get_digit(12345, 3), 2)  # thousands
        self.assertEqual(get_digit(12345, 4), 1)  # ten-thousands

    def test_beyond_number_length(self):
        """positions beyond the number should return 0."""
        self.assertEqual(get_digit(12345, 5), 0)
        self.assertEqual(get_digit(12345, 10), 0)
        self.assertEqual(get_digit(1, 5), 0)

    def test_zero(self):
        """zero should return 0 for any position."""
        self.assertEqual(get_digit(0, 0), 0)
        self.assertEqual(get_digit(0, 5), 0)

    def test_different_bases(self):
        """verify digit extraction in different bases."""
        # binary (base 2)
        self.assertEqual(get_digit(10, 0, base=2), 0)  # 10 = 1010 in binary
        self.assertEqual(get_digit(10, 1, base=2), 1)
        self.assertEqual(get_digit(10, 2, base=2), 0)
        self.assertEqual(get_digit(10, 3, base=2), 1)

        # hexadecimal (base 16)
        self.assertEqual(get_digit(255, 0, base=16), 15)  # 255 = FF in hex
        self.assertEqual(get_digit(255, 1, base=16), 15)


class TestCountDigits(unittest.TestCase):
    """tests for the count_digits helper function."""

    def test_single_digit(self):
        """single digit numbers have 1 digit."""
        for i in range(10):
            self.assertEqual(count_digits(i), 1)

    def test_multiple_digits(self):
        """verify digit counts for various numbers."""
        self.assertEqual(count_digits(10), 2)
        self.assertEqual(count_digits(99), 2)
        self.assertEqual(count_digits(100), 3)
        self.assertEqual(count_digits(12345), 5)
        self.assertEqual(count_digits(1000000000), 10)

    def test_powers_of_ten(self):
        """powers of ten should have expected digit counts."""
        self.assertEqual(count_digits(1), 1)
        self.assertEqual(count_digits(10), 2)
        self.assertEqual(count_digits(100), 3)
        self.assertEqual(count_digits(1000), 4)

    def test_different_bases(self):
        """verify digit counts in different bases."""
        # in binary, 8 = 1000 (4 digits)
        self.assertEqual(count_digits(8, base=2), 4)
        # in binary, 255 = 11111111 (8 digits)
        self.assertEqual(count_digits(255, base=2), 8)


class TestCountingSortByDigit(unittest.TestCase):
    """tests for the counting sort subroutine."""

    def test_single_digit_numbers(self):
        """sort single-digit numbers by their only digit."""
        arr = [5, 3, 8, 1, 9, 2, 7, 4, 6, 0]
        result = counting_sort_by_digit(arr, 0)
        self.assertEqual(result, sorted(arr))

    def test_stability(self):
        """verify that equal digits preserve relative order."""
        # all numbers have the same units digit (0)
        arr = [30, 10, 20, 50, 40]
        result = counting_sort_by_digit(arr, 0)
        # should maintain original order since all have least significant digit of 0
        self.assertEqual(result, [30, 10, 20, 50, 40])

    def test_empty_array(self):
        """empty array should return empty array."""
        self.assertEqual(counting_sort_by_digit([], 0), [])

    def test_single_element(self):
        """single element array should return copy."""
        self.assertEqual(counting_sort_by_digit([42], 0), [42])


class TestRadixSortEdgeCases(unittest.TestCase):
    """tests for edge cases and boundary conditions."""

    def test_empty_array(self):
        """sorting an empty array should return empty array."""
        result = radix_sort([])
        self.assertEqual(result, [])

    def test_single_element(self):
        """sorting a single element should return that element."""
        result = radix_sort([42])
        self.assertEqual(result, [42])

    def test_two_elements_sorted(self):
        """two elements already sorted."""
        result = radix_sort([1, 2])
        self.assertEqual(result, [1, 2])

    def test_two_elements_unsorted(self):
        """two elements in reverse order."""
        result = radix_sort([2, 1])
        self.assertEqual(result, [1, 2])

    def test_all_same_elements(self):
        """array with all identical elements."""
        arr = [7, 7, 7, 7, 7]
        result = radix_sort(arr)
        self.assertEqual(result, [7, 7, 7, 7, 7])

    def test_all_zeros(self):
        """array of all zeros."""
        arr = [0, 0, 0, 0]
        result = radix_sort(arr)
        self.assertEqual(result, [0, 0, 0, 0])

    def test_contains_zero(self):
        """array containing zero among other elements."""
        arr = [5, 0, 3, 0, 1]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))


class TestRadixSortOrdering(unittest.TestCase):
    """tests for various input orderings."""

    def test_already_sorted(self):
        """array that is already sorted."""
        arr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        result = radix_sort(arr)
        self.assertEqual(result, arr)

    def test_reverse_sorted(self):
        """array in reverse order."""
        arr = [10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_random_order(self):
        """randomly ordered array."""
        arr = [3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_alternating_high_low(self):
        """alternating high and low values."""
        arr = [1, 100, 2, 99, 3, 98, 4, 97]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))


class TestRadixSortValueRanges(unittest.TestCase):
    """tests for various value ranges."""

    def test_single_digit_values(self):
        """all values are single digits."""
        arr = [5, 3, 8, 1, 9, 2, 7, 4, 6, 0]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_two_digit_values(self):
        """values up to two digits."""
        arr = [42, 17, 93, 55, 12, 88, 31, 64]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_three_digit_values(self):
        """values up to three digits."""
        arr = [329, 457, 657, 839, 436, 720, 355]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_mixed_digit_lengths(self):
        """mix of different digit lengths."""
        arr = [1, 10, 100, 1000, 5, 55, 555, 5555]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_large_values(self):
        """large multi-digit values."""
        arr = [1000000000, 999999999, 123456789, 987654321, 111111111]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_powers_of_ten(self):
        """powers of ten."""
        arr = [1000, 10, 100000, 1, 10000, 100]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))


class TestRadixSortDuplicates(unittest.TestCase):
    """tests for handling duplicate values."""

    def test_some_duplicates(self):
        """array with some duplicate values."""
        arr = [3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_many_duplicates(self):
        """array with many duplicates."""
        arr = [5, 5, 3, 3, 5, 3, 5, 3, 5, 3]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_all_but_one_same(self):
        """all elements same except one."""
        arr = [7, 7, 7, 1, 7, 7]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))


class TestRadixSortScale(unittest.TestCase):
    """tests for larger input sizes."""

    def test_hundred_elements(self):
        """sort 100 random elements."""
        random.seed(42)
        arr = [random.randint(0, 10000) for _ in range(100)]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_thousand_elements(self):
        """sort 1000 random elements."""
        random.seed(42)
        arr = [random.randint(0, 100000) for _ in range(1000)]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_ten_thousand_elements(self):
        """sort 10000 random elements."""
        random.seed(42)
        arr = [random.randint(0, 1000000) for _ in range(10000)]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))


class TestRadixSortValidation(unittest.TestCase):
    """tests for input validation."""

    def test_negative_number_raises_error(self):
        """negative numbers should raise ValueError."""
        with self.assertRaises(ValueError):
            radix_sort([-1, 2, 3])

    def test_mixed_negative_positive_raises_error(self):
        """mixed negative and positive should raise ValueError."""
        with self.assertRaises(ValueError):
            radix_sort([1, -2, 3, -4])


class TestRadixSortDifferentBases(unittest.TestCase):
    """tests for different base (radix) values."""

    def test_base_2(self):
        """binary radix sort."""
        arr = [5, 3, 8, 1, 9, 2, 7, 4, 6, 0]
        result = radix_sort(arr, base=2)
        self.assertEqual(result, sorted(arr))

    def test_base_16(self):
        """hexadecimal radix sort."""
        arr = [255, 128, 64, 32, 16, 8, 4, 2, 1, 0]
        result = radix_sort(arr, base=16)
        self.assertEqual(result, sorted(arr))

    def test_base_256(self):
        """byte-based radix sort."""
        arr = [1000000, 500000, 750000, 250000, 999999]
        result = radix_sort(arr, base=256)
        self.assertEqual(result, sorted(arr))


class TestRadixSortStability(unittest.TestCase):
    """tests to verify stability of the sort."""

    def test_stability_preserved(self):
        """
        verify that radix sort produces the correct final output
        for an array where stability matters across multiple passes.
        """
        arr = [123, 124, 125, 113, 114, 115, 103, 104, 105]
        result = radix_sort(arr)
        self.assertEqual(result, sorted(arr))

    def test_stability_per_digit_pass(self):
        """
        verify that counting sort preserves relative order of elements
        with equal digit values. this is the core stability property
        that radix sort depends on.

        when all elements share the same digit at a given position,
        counting sort must return them in their original input order.
        """
        # all elements have units digit 0, so sorting by position 0
        # should preserve the original order exactly
        arr = [210, 110, 310, 410, 510]
        result = counting_sort_by_digit(arr, 0)
        self.assertEqual(result, [210, 110, 310, 410, 510])

        # elements grouped by tens digit: 1-group (210, 110, 310, 410, 510)
        # all share tens digit 1, so order should be preserved
        arr = [210, 110, 310, 410, 510]
        result = counting_sort_by_digit(arr, 1)
        self.assertEqual(result, [210, 110, 310, 410, 510])

        # elements with different units digits but some sharing values
        # 0-group: 720 | 5-group: 355 | 6-group: 436 | 7-group: 457, 657 | 9-group: 329, 839
        arr = [329, 457, 657, 839, 436, 720, 355]
        result = counting_sort_by_digit(arr, 0)
        # within the 7-group, 457 should appear before 657 (original order)
        # within the 9-group, 329 should appear before 839 (original order)
        self.assertEqual(result, [720, 355, 436, 457, 657, 329, 839])


def run_verification_demo():
    """
    demonstrate the algorithm with visual output.
    run with: python -m tests.test_radix_sort
    """
    print("=" * 60)
    print("RADIX SORT VERIFICATION DEMO")
    print("=" * 60)
    print()

    # test case from clrs textbook
    test_cases = [
        ("CLRS Example", [329, 457, 657, 839, 436, 720, 355]),
        ("Single Digits", [5, 3, 8, 1, 9, 2, 7, 4, 6, 0]),
        ("With Duplicates", [3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5]),
        ("Mixed Lengths", [1, 10, 100, 1000, 5, 55, 555]),
        ("Already Sorted", [1, 2, 3, 4, 5]),
        ("Reverse Sorted", [5, 4, 3, 2, 1]),
    ]

    for name, arr in test_cases:
        print(f"Test: {name}")
        print(f"  Input:    {arr}")
        result = radix_sort(arr)
        expected = sorted(arr)
        status = "PASS" if result == expected else "FAIL"
        print(f"  Output:   {result}")
        print(f"  Expected: {expected}")
        print(f"  Status:   {status}")
        print()


if __name__ == "__main__":
    # run demo first
    run_verification_demo()

    # then run unit tests
    print("=" * 60)
    print("RUNNING UNIT TESTS")
    print("=" * 60)
    unittest.main(verbosity=2)
