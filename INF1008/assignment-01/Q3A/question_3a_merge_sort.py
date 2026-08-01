def merge_sort(arr):
    if len(arr) <= 1:
        return arr

    # Divide the array into halves & sort each half
    mid = len(arr) // 2
    left = merge_sort(arr[:mid])
    right = merge_sort(arr[mid:])

    return merge(left, right)

# Merging two sorted arrays
def merge(left, right):
    result = []
    i = j = 0

    while i < len(left) and j < len(right):
        left_val = left[i][0] if isinstance(left[i], tuple) else left[i]
        right_val = right[j][0] if isinstance(right[j], tuple) else right[j]

        if left_val <= right_val:  # <= ensures stability
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1

    # Add any remaining elements
    result.extend(left[i:])
    result.extend(right[j:])
    return result


# Test case
data = [(5, 'A'), (3, 'B'), (5, 'C'), (1, 'D'), (4, 'E'), (3, 'F')]  ## Can be replaced with different data
sorted_data = merge_sort(data)
print(sorted_data)
