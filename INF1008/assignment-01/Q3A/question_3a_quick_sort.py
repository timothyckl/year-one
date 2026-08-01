def quick_sort(arr, low, high, is_tuple=None):

    is_tuple = isinstance(arr[0], tuple) if len(arr) > 0 else False

    if low < high:
        p = partition(arr, low, high, is_tuple)  # Partitioning index
        quick_sort(arr, low, p, is_tuple)  # Sort the left side
        quick_sort(arr, p + 1, high, is_tuple)  # Sort the right side



def partition(arr, low, high, is_tuple):
    if is_tuple:
        pivot = arr[(low + high) // 2][0]  # pivot is the first element of the tuple
    else:
        pivot = arr[(low + high) // 2]  # pivot is the element itself
    
    i = low - 1
    j = high + 1

    while True:
        # Move right until an element >= pivot is found
        i += 1  
        if is_tuple:
            while arr[i][0] < pivot:
                i += 1
        else:
            while arr[i] < pivot:
                i += 1

        # Move left until an element <= pivot is found
        j -= 1  
        if is_tuple:
            while arr[j][0] > pivot:
                j -= 1
        else:
            while arr[j] > pivot:
                j -= 1

        # If pointers have crossed, return the partition index
        if i >= j: 
            return j

        # Swap elements at i and j
        arr[i], arr[j] = arr[j], arr[i]


# Test case 
data = [8, 4, 6, 2, 7, 1, 5, 3, 9, 0]  # Can be replaced with different data
quick_sort(data, 0, len(data) - 1)
print(data)