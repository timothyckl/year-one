"""
performance comparison benchmark

this module provides instrumented versions of radix sort, merge sort,
and quick sort that count their dominant operations. operation counts
are hardware-independent and directly correspond to big-o analysis,
making them more suitable than execution time for proving asymptotic
complexity differences.

operations counted:
    - radix sort: digit extractions + array writes (o(n) per digit pass)
    - merge sort: comparisons (bounded by omega(n log n))
    - quick sort: comparisons (bounded by omega(n log n))

usage:
    python benchmarks/performance_comparison.py
"""

import math
import os
import random
import sys
import time
from typing import Dict, List, Tuple

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt


# ---------------------------------------------------------------------------
# instrumented radix sort
# ---------------------------------------------------------------------------

def radix_sort_counted(array: List[int], base: int = 10) -> Tuple[List[int], int]:
    """
    sort non-negative integers using lsd radix sort whilst counting operations.

    counts every digit extraction and every array write performed during the
    counting sort subroutine. these are the dominant operations whose total
    determines radix sort's o(d * (n + k)) complexity.

    args:
        array: list of non-negative integers to sort
        base: the radix/base to use (default 10)

    returns:
        a tuple of (sorted list, total operation count)
    """
    operations = 0

    if len(array) <= 1:
        return array.copy(), operations

    # find the maximum value to determine the number of digit passes
    max_value = max(array)

    # count digits in the maximum value
    if max_value == 0:
        num_digits = 1
    else:
        num_digits = 0
        temp = max_value
        while temp > 0:
            num_digits += 1
            temp //= base

    result = array.copy()
    operations += len(array)  # initial copy writes

    # process each digit position from least significant to most significant
    for digit_position in range(num_digits):
        n = len(result)

        # step 1: count occurrences of each digit value
        count = [0] * base
        for element in result:
            # digit extraction operation
            digit = (element // (base ** digit_position)) % base
            operations += 1  # count the digit extraction
            count[digit] += 1

        # step 2: compute cumulative counts (prefix sums)
        for i in range(1, base):
            count[i] += count[i - 1]

        # step 3: build output array (reverse traversal for stability)
        output = [0] * n
        for i in range(n - 1, -1, -1):
            element = result[i]
            digit = (element // (base ** digit_position)) % base
            operations += 1  # count the digit extraction
            count[digit] -= 1
            output[count[digit]] = element
            operations += 1  # count the array write

        result = output

    return result, operations


# ---------------------------------------------------------------------------
# instrumented merge sort
# ---------------------------------------------------------------------------

def merge_sort_counted(array: List[int]) -> Tuple[List[int], int]:
    """
    sort an array using merge sort whilst counting comparisons.

    the omega(n log n) lower bound for comparison-based sorting is
    specifically about the number of comparisons. counting comparisons
    therefore provides a direct empirical measure of this bound.

    args:
        array: list of integers to sort

    returns:
        a tuple of (sorted list, comparison count)
    """
    comparisons = [0]  # mutable container for nested function access

    def _merge_sort_inner(arr: List[int]) -> List[int]:
        """recursively divide and sort the array."""
        if len(arr) <= 1:
            return arr.copy()

        mid = len(arr) // 2
        sorted_left = _merge_sort_inner(arr[:mid])
        sorted_right = _merge_sort_inner(arr[mid:])

        return _merge_inner(sorted_left, sorted_right)

    def _merge_inner(left: List[int], right: List[int]) -> List[int]:
        """merge two sorted arrays, counting each comparison."""
        merged = []
        left_index = 0
        right_index = 0

        while left_index < len(left) and right_index < len(right):
            comparisons[0] += 1  # count this comparison
            if left[left_index] <= right[right_index]:
                merged.append(left[left_index])
                left_index += 1
            else:
                merged.append(right[right_index])
                right_index += 1

        merged.extend(left[left_index:])
        merged.extend(right[right_index:])

        return merged

    sorted_array = _merge_sort_inner(array)
    return sorted_array, comparisons[0]


# ---------------------------------------------------------------------------
# instrumented quick sort
# ---------------------------------------------------------------------------

def quick_sort_counted(array: List[int]) -> Tuple[List[int], int]:
    """
    sort an array using quick sort whilst counting comparisons.

    uses the same random pivot selection as the main implementation.
    counts every element-to-pivot comparison in the partition step.

    args:
        array: list of integers to sort

    returns:
        a tuple of (sorted list, comparison count)
    """
    comparisons = [0]  # mutable container for nested function access

    def _quick_sort_inner(arr: List[int], low: int, high: int) -> None:
        """recursively partition and sort the array."""
        if low < high:
            pivot_index = _partition_inner(arr, low, high)
            _quick_sort_inner(arr, low, pivot_index - 1)
            _quick_sort_inner(arr, pivot_index + 1, high)

    def _partition_inner(arr: List[int], low: int, high: int) -> int:
        """partition around a random pivot, counting comparisons."""
        # random pivot selection to avoid worst-case on sorted inputs
        pivot_pos = random.randint(low, high)
        arr[pivot_pos], arr[high] = arr[high], arr[pivot_pos]

        pivot = arr[high]
        i = low - 1

        for j in range(low, high):
            comparisons[0] += 1  # count this comparison
            if arr[j] <= pivot:
                i += 1
                arr[i], arr[j] = arr[j], arr[i]

        arr[i + 1], arr[high] = arr[high], arr[i + 1]
        return i + 1

    result = array.copy()
    _quick_sort_inner(result, 0, len(result) - 1)
    return result, comparisons[0]


# ---------------------------------------------------------------------------
# benchmark runner
# ---------------------------------------------------------------------------

def run_benchmarks() -> Dict:
    """
    run operation count benchmarks across multiple input sizes.

    generates random integer arrays for each input size and measures
    both operation counts and execution time for each algorithm.

    returns:
        a dictionary containing all benchmark results keyed by metric.
    """
    input_sizes = [
        1_000, 2_000, 5_000, 10_000, 20_000,
        50_000, 100_000, 200_000, 500_000, 1_000_000,
    ]
    max_value = 999_999  # 6-digit bounded integers

    results = {
        "sizes": input_sizes,
        "radix_ops": [],
        "merge_ops": [],
        "quick_ops": [],
        "radix_time": [],
        "merge_time": [],
        "quick_time": [],
    }

    # fixed seed for reproducibility
    random.seed(42)

    # pre-generate all test arrays so each algorithm sorts the same data
    test_arrays = {}
    for size in input_sizes:
        test_arrays[size] = [random.randint(0, max_value) for _ in range(size)]

    for size in input_sizes:
        array = test_arrays[size]
        print(f"benchmarking n = {size:>10,} ... ", end="", flush=True)

        # radix sort (reset seed not needed — no randomness in radix sort)
        start = time.perf_counter()
        _, radix_ops = radix_sort_counted(array)
        radix_time = time.perf_counter() - start

        # merge sort
        start = time.perf_counter()
        _, merge_ops = merge_sort_counted(array)
        merge_time = time.perf_counter() - start

        # quick sort (reset seed so pivot selection is reproducible)
        random.seed(size)
        start = time.perf_counter()
        _, quick_ops = quick_sort_counted(array)
        quick_time = time.perf_counter() - start

        results["radix_ops"].append(radix_ops)
        results["merge_ops"].append(merge_ops)
        results["quick_ops"].append(quick_ops)
        results["radix_time"].append(radix_time)
        results["merge_time"].append(merge_time)
        results["quick_time"].append(quick_time)

        print(
            f"radix={radix_ops:>14,}  "
            f"merge={merge_ops:>14,}  "
            f"quick={quick_ops:>14,}"
        )

    return results


# ---------------------------------------------------------------------------
# output formatting
# ---------------------------------------------------------------------------

def print_results_tables(results: Dict) -> None:
    """
    print formatted benchmark result tables to the console.

    produces three tables: operation counts, growth ratios, and
    speedup factors. these tables are designed to be copied directly
    into the report.

    args:
        results: dictionary of benchmark results from run_benchmarks.
    """
    sizes = results["sizes"]
    radix_ops = results["radix_ops"]
    merge_ops = results["merge_ops"]
    quick_ops = results["quick_ops"]

    # table 1: operation counts
    print("\n" + "=" * 80)
    print("TABLE: Operation Counts by Input Size")
    print("=" * 80)
    print(f"{'Size':>10} | {'Radix Sort':>14} | {'Merge Sort':>14} | {'Quick Sort':>14}")
    print("-" * 60)
    for i, size in enumerate(sizes):
        print(
            f"{size:>10,} | {radix_ops[i]:>14,} | "
            f"{merge_ops[i]:>14,} | {quick_ops[i]:>14,}"
        )

    # table 2: growth ratios (operation count ratio when doubling input)
    print("\n" + "=" * 80)
    print("TABLE: Operation Count Growth Ratios (ratio when input size doubles)")
    print("=" * 80)

    # find pairs where the second size is double the first
    double_pairs = []
    for i, size_a in enumerate(sizes):
        for j, size_b in enumerate(sizes):
            if size_b == 2 * size_a:
                double_pairs.append((i, j, size_a, size_b))

    print(f"{'Transition':>18} | {'Radix Sort':>12} | {'Merge Sort':>12} | {'Quick Sort':>12}")
    print("-" * 62)
    for idx_a, idx_b, size_a, size_b in double_pairs:
        radix_ratio = radix_ops[idx_b] / radix_ops[idx_a] if radix_ops[idx_a] else 0
        merge_ratio = merge_ops[idx_b] / merge_ops[idx_a] if merge_ops[idx_a] else 0
        quick_ratio = quick_ops[idx_b] / quick_ops[idx_a] if quick_ops[idx_a] else 0
        label = f"n={size_a:,}->{size_b:,}"
        print(
            f"{label:>18} | {radix_ratio:>12.2f} | "
            f"{merge_ratio:>12.2f} | {quick_ratio:>12.2f}"
        )

    # table 3: speedup factor (merge sort ops / radix sort ops)
    print("\n" + "=" * 80)
    print("TABLE: Operation Count Speedup Factor (Merge Sort ops / Radix Sort ops)")
    print("=" * 80)
    print(f"{'Input Size':>12} | {'Speedup Factor':>16}")
    print("-" * 34)
    for i, size in enumerate(sizes):
        if radix_ops[i] > 0:
            speedup = merge_ops[i] / radix_ops[i]
            print(f"{size:>12,} | {speedup:>15.2f}x")


# ---------------------------------------------------------------------------
# graph generation
# ---------------------------------------------------------------------------

def generate_graphs(results: Dict, output_directory: str) -> None:
    """
    generate benchmark visualisation graphs and save them as png files.

    produces two graphs:
        1. operation count vs input size (log-log scale)
        2. operation count growth ratios (bar chart)

    args:
        results: dictionary of benchmark results from run_benchmarks.
        output_directory: path to the directory where png files are saved.
    """
    sizes = results["sizes"]
    radix_ops = results["radix_ops"]
    merge_ops = results["merge_ops"]
    quick_ops = results["quick_ops"]

    # -----------------------------------------------------------------------
    # figure 1: operation count vs input size (log-log scale)
    # -----------------------------------------------------------------------
    fig, ax = plt.subplots(figsize=(10, 6))

    ax.loglog(sizes, radix_ops, "o-", label="Radix Sort (operations)", linewidth=2)
    ax.loglog(sizes, merge_ops, "s-", label="Merge Sort (comparisons)", linewidth=2)
    ax.loglog(sizes, quick_ops, "^-", label="Quick Sort (comparisons)", linewidth=2)

    # theoretical reference lines
    # scale to match the data at the midpoint for visual comparison
    mid = len(sizes) // 2
    scale_linear = radix_ops[mid] / sizes[mid]
    scale_nlogn = merge_ops[mid] / (sizes[mid] * math.log2(sizes[mid]))

    theoretical_linear = [scale_linear * n for n in sizes]
    theoretical_nlogn = [scale_nlogn * n * math.log2(n) for n in sizes]

    ax.loglog(
        sizes, theoretical_linear, "--", color="grey", alpha=0.5,
        label="Theoretical O(n)", linewidth=1,
    )
    ax.loglog(
        sizes, theoretical_nlogn, "--", color="grey", alpha=0.5,
        label="Theoretical O(n log n)", linewidth=1,
    )

    ax.set_xlabel("Input Size (n)", fontsize=12)
    ax.set_ylabel("Operation Count", fontsize=12)
    ax.set_title("Operation Count vs Input Size", fontsize=14)
    ax.legend(fontsize=10)
    ax.grid(True, which="both", linestyle="--", alpha=0.3)

    fig.tight_layout()
    path_ops = os.path.join(output_directory, "benchmark_operations.png")
    fig.savefig(path_ops, dpi=150)
    plt.close(fig)
    print(f"\nsaved: {path_ops}")

    # -----------------------------------------------------------------------
    # figure 2: operation count growth ratios (bar chart)
    # -----------------------------------------------------------------------
    # find pairs where the second size is double the first
    double_pairs = []
    for i, size_a in enumerate(sizes):
        for j, size_b in enumerate(sizes):
            if size_b == 2 * size_a:
                double_pairs.append((i, j, size_a, size_b))

    if not double_pairs:
        return

    labels = [f"{a:,}\u2192{b:,}" for _, _, a, b in double_pairs]
    radix_ratios = [
        radix_ops[j] / radix_ops[i] if radix_ops[i] else 0
        for i, j, _, _ in double_pairs
    ]
    merge_ratios = [
        merge_ops[j] / merge_ops[i] if merge_ops[i] else 0
        for i, j, _, _ in double_pairs
    ]
    quick_ratios = [
        quick_ops[j] / quick_ops[i] if quick_ops[i] else 0
        for i, j, _, _ in double_pairs
    ]

    x = range(len(labels))
    bar_width = 0.25

    fig, ax = plt.subplots(figsize=(12, 6))

    bars_radix = ax.bar(
        [xi - bar_width for xi in x], radix_ratios,
        bar_width, label="Radix Sort", color="#2196F3",
    )
    bars_merge = ax.bar(
        x, merge_ratios,
        bar_width, label="Merge Sort", color="#FF9800",
    )
    bars_quick = ax.bar(
        [xi + bar_width for xi in x], quick_ratios,
        bar_width, label="Quick Sort", color="#4CAF50",
    )

    # reference line at ratio = 2.0 (ideal linear growth)
    ax.axhline(y=2.0, color="red", linestyle="--", alpha=0.7, label="Ideal O(n) ratio = 2.0")

    ax.set_xlabel("Input Size Transition (n doubles)", fontsize=12)
    ax.set_ylabel("Operation Count Growth Ratio", fontsize=12)
    ax.set_title("Operation Count Growth Ratios When Input Size Doubles", fontsize=14)
    ax.set_xticks(list(x))
    ax.set_xticklabels(labels, rotation=30, ha="right", fontsize=9)
    ax.legend(fontsize=10)
    ax.grid(True, axis="y", linestyle="--", alpha=0.3)

    # add value labels on bars
    for bars in [bars_radix, bars_merge, bars_quick]:
        for bar in bars:
            height = bar.get_height()
            ax.annotate(
                f"{height:.2f}",
                xy=(bar.get_x() + bar.get_width() / 2, height),
                xytext=(0, 3),
                textcoords="offset points",
                ha="center", va="bottom", fontsize=8,
            )

    fig.tight_layout()
    path_ratio = os.path.join(output_directory, "benchmark_op_growth_ratio.png")
    fig.savefig(path_ratio, dpi=150)
    plt.close(fig)
    print(f"saved: {path_ratio}")


# ---------------------------------------------------------------------------
# main entry point
# ---------------------------------------------------------------------------

def main():
    """
    run all benchmarks, print result tables, and generate graphs.

    output files are saved to the docs/ directory alongside the report.
    """
    print("=" * 80)
    print("PERFORMANCE COMPARISON BENCHMARK")
    print("operation counts: hardware-independent algorithm analysis")
    print("=" * 80)
    print()

    results = run_benchmarks()

    print_results_tables(results)

    # determine output directory (docs/ relative to project root)
    script_directory = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_directory)
    output_directory = os.path.join(project_root, "docs")

    generate_graphs(results, output_directory)

    print("\nbenchmark complete.")


if __name__ == "__main__":
    main()
