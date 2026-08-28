"""
radix sort demonstration

this is the main entry point for demonstrating the radix sort
implementation. it provides:
    1. step-by-step visualisation of the algorithm
    2. verification against python's built-in sort
    3. example with various input types

run with: python main.py
"""

from src.radix_sort import radix_sort


def demonstrate_radix_sort():
    """demonstrate radix sort with step-by-step tracing."""

    print()
    print("=" * 70)
    print("  RADIX SORT DEMONSTRATION")
    print("  a linear-time sorting algorithm for bounded integers")
    print("=" * 70)
    print()

    # example from clrs textbook (introduction to algorithms)
    clrs_example = [329, 457, 657, 839, 436, 720, 355]

    print("example from clrs textbook:")
    print(f"input: {clrs_example}")
    print()
    print("step-by-step execution (lsd radix sort):")
    print("-" * 50)

    sorted_result = radix_sort(clrs_example, trace=True)

    print("-" * 50)
    print(f"final result: {sorted_result}")
    print()

    # verify correctness
    expected = sorted(clrs_example)
    if sorted_result == expected:
        print("verification: PASSED (matches python's sorted())")
    else:
        print("verification: FAILED")
        print(f"expected: {expected}")

    print()


def demonstrate_various_inputs():
    """demonstrate sorting with various input types."""

    print("=" * 70)
    print("  ADDITIONAL TEST CASES")
    print("=" * 70)
    print()

    test_cases = [
        ("empty array", []),
        ("single element", [42]),
        ("already sorted", [1, 2, 3, 4, 5]),
        ("reverse sorted", [5, 4, 3, 2, 1]),
        ("with duplicates", [3, 1, 4, 1, 5, 9, 2, 6, 5, 3]),
        ("mixed digit lengths", [1, 10, 100, 1000, 5, 55, 555]),
        ("large values", [1000000, 999999, 123456, 987654]),
    ]

    for name, arr in test_cases:
        result = radix_sort(arr)
        expected = sorted(arr)
        status = "pass" if result == expected else "FAIL"

        print(f"{name}:")
        print(f"  input:  {arr}")
        print(f"  output: {result}")
        print(f"  status: {status}")
        print()


def print_algorithm_summary():
    """print a summary of the radix sort algorithm."""

    print("=" * 70)
    print("  ALGORITHM SUMMARY")
    print("=" * 70)
    print()
    print("radix sort (lsd variant)")
    print("-" * 30)
    print()
    print("how it works:")
    print("  1. find the maximum value to determine the number of digits")
    print("  2. for each digit position (least to most significant):")
    print("     - perform a stable counting sort based on that digit")
    print("  3. after processing all digits, the array is sorted")
    print()
    print("time complexity:")
    print("  o(d * (n + k)) where:")
    print("    n = number of elements")
    print("    d = number of digits in maximum value")
    print("    k = base (radix), default 10")
    print()
    print("  for 32-bit integers with base 10:")
    print("    d <= 10 (max value ~4 billion)")
    print("    time = o(10 * (n + 10)) = o(n)")
    print()
    print("why it bypasses the omega(n log n) bound:")
    print("  - uses array indexing, not comparisons")
    print("  - each indexing operation reveals log2(k) bits")
    print("  - this exceeds the 1 bit from a comparison")
    print()


def main():
    """main entry point."""
    demonstrate_radix_sort()
    demonstrate_various_inputs()
    print_algorithm_summary()

    print("=" * 70)
    print("  demonstration complete")
    print("=" * 70)
    print()


if __name__ == "__main__":
    main()
