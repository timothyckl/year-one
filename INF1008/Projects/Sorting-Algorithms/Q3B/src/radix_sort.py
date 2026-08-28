"""
radix sort implementation

this module provides a least-significant-digit (lsd) radix sort implementation
for sorting non-negative integers in linear time. the algorithm bypasses the
omega(n log n) lower bound for comparison-based sorting by exploiting the
digit structure of integer keys.

time complexity: o(d * (n + k)) where:
    - n = number of elements
    - d = number of digits in the maximum value
    - k = base (radix) used for digit extraction

for fixed-length integers with constant base, this simplifies to o(n).

references:
    - cormen et al., introduction to algorithms, 3rd ed., section 8.3
    - knuth, the art of computer programming, vol. 3, section 5.2.5
"""

from typing import List, Optional


def get_digit(number: int, position: int, base: int = 10) -> int:
    """
    extract the digit at a given position from a number.

    the position is counted from the right (least significant digit = position 0).

    args:
        number: the non-negative integer to extract from
        position: the digit position (0 = rightmost digit)
        base: the radix/base to use (default 10 for decimal)

    returns:
        the digit value at the specified position (0 to base-1)

    examples:
        >>> get_digit(12345, 0)  # units digit
        5
        >>> get_digit(12345, 2)  # hundreds digit
        3
        >>> get_digit(12345, 10)  # position beyond number
        0
    """
    # integer division shifts the number right, modulo extracts the digit
    return (number // (base**position)) % base


def count_digits(number: int, base: int = 10) -> int:
    """
    count the number of digits in a non-negative integer.

    args:
        number: the non-negative integer
        base: the radix/base to use (default 10 for decimal)

    returns:
        the number of digits (minimum 1 for zero)

    examples:
        >>> count_digits(0)
        1
        >>> count_digits(12345)
        5
        >>> count_digits(1000000000)
        10
    """
    if number == 0:
        return 1

    digit_count = 0
    while number > 0:
        digit_count += 1
        number //= base

    return digit_count


def counting_sort_by_digit(
    array: List[int], digit_position: int, base: int = 10
) -> List[int]:
    """
    perform a stable counting sort on a specific digit position.

    this is the key subroutine for radix sort. stability is crucial:
    elements with the same digit value must maintain their relative
    order from the input. this ensures that previously sorted digit
    positions remain correctly ordered.

    args:
        array: list of non-negative integers to sort
        digit_position: which digit to sort by (0 = least significant)
        base: the radix/base used for digit extraction

    returns:
        a new list sorted by the specified digit position

    time complexity: o(n + k) where n = len(array), k = base
    space complexity: o(n + k) for count array and output array
    """
    n = len(array)

    # handle edge cases
    if n <= 1:
        return array.copy()

    # step 1: count occurrences of each digit value
    # count[d] = number of elements with digit value d at this position
    count = [0] * base
    for element in array:
        digit = get_digit(element, digit_position, base)
        count[digit] += 1

    # step 2: compute cumulative counts (prefix sums)
    # after this, count[d] = number of elements with digit value <= d
    # this gives us the ending position for each digit group
    for i in range(1, base):
        count[i] += count[i - 1]

    # step 3: build output array by placing elements in correct positions
    # traverse input in reverse order to maintain stability:
    # if two elements have the same digit, the one appearing later
    # in the input should appear later in the output
    output = [0] * n
    for i in range(n - 1, -1, -1):
        element = array[i]
        digit = get_digit(element, digit_position, base)

        # decrement count to get the correct position (0-indexed)
        count[digit] -= 1
        output_position = count[digit]

        output[output_position] = element

    return output


def radix_sort(array: List[int], base: int = 10, trace: bool = False) -> List[int]:
    """
    sort non-negative integers using least-significant-digit radix sort.

    the algorithm processes digits from least significant to most significant,
    using counting sort as a stable subroutine for each digit position.

    args:
        array: list of non-negative integers to sort
        base: the radix/base to use (default 10 for decimal)
               higher bases reduce passes but increase counting array size
        trace: if true, print intermediate states for visualisation

    returns:
        a new list containing the sorted elements

    raises:
        valueerror: if array contains negative integers

    time complexity: o(d * (n + k)) where:
        - n = len(array)
        - d = number of digits in max(array)
        - k = base

    for 32-bit integers with base 10: d <= 10, so o(10 * (n + 10)) = o(n)

    space complexity: o(n + k) for the counting sort subroutine

    examples:
        >>> radix_sort([170, 45, 75, 90, 802, 24, 2, 66])
        [2, 24, 45, 66, 75, 90, 170, 802]

        >>> radix_sort([])
        []

        >>> radix_sort([42])
        [42]
    """
    # handle edge cases
    if len(array) <= 1:
        return array.copy()

    # validate input: radix sort only works for non-negative integers
    for element in array:
        if element < 0:
            raise ValueError(
                f"radix sort requires non-negative integers, got {element}"
            )

    # determine the number of digit positions to process
    max_value = max(array)
    num_digits = count_digits(max_value, base)

    if trace:
        print(f"radix sort: {len(array)} elements, max value = {max_value}")
        print(f"base = {base}, digits to process = {num_digits}")
        print(f"initial: {array}")

    # process each digit position from least significant to most significant
    result = array.copy()
    for digit_position in range(num_digits):
        result = counting_sort_by_digit(result, digit_position, base)

        if trace:
            print(f"after sorting by digit {digit_position}: {result}")

    return result


if __name__ == "__main__":
    test_array = [329, 457, 657, 839, 436, 720, 355]

    print("=" * 60)
    print("RADIX SORT DEMONSTRATION")
    print("=" * 60)
    print()

    print("input array:", test_array)
    print()

    # sort with tracing enabled
    sorted_array = radix_sort(test_array, trace=True)

    print()
    print("final sorted array:", sorted_array)
    print()

    # verify correctness against python's built-in sort
    expected = sorted(test_array)
    assert sorted_array == expected, "sort verification failed!"
    print("verification: passed (matches python's sorted())")
