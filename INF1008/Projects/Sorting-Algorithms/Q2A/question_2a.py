from numbers import Real

# ---------- CODE FOR LINEARITHMIC ALGORITHM O(n log n) DEMO : aggregate_sales_by_region ----------
# Function: aggregate_sales_by_region
# Purpose:
#   1) Validate input data
#   2) Sort orders by region using Heap Sort (O(n log n))
#   3) Aggregate total sales per region in one pass (O(n))


def aggregate_sales_by_region(order_list):
    """
    This function demonstrates a linearithmic time algorithm O(n log n).

    Step 1: Sort the orders by region using Heap Sort -> O(n log n)
    Step 2: Traverse the sorted list once to sum sales -> O(n)

    Input validation rules:
    - order_list must be a list
    - each element must be a dictionary
    - each dictionary must contain:
        - 'region': non-empty string
        - 'amount': real number (int or float, not bool)
    """

    # --------------------
    # Input Validation
    # --------------------
    if not isinstance(order_list, list):
        raise TypeError("order_list must be a list of orders (dicts).")

    for i, order in enumerate(order_list):
        # Each order must be a dictionary
        if not isinstance(order, dict):
            raise TypeError(f"Order at index {i} must be a dict.")

        # Required keys check
        if "region" not in order:
            raise KeyError(f"Order at index {i} is missing key: 'region'")
        if "amount" not in order:
            raise KeyError(f"Order at index {i} is missing key: 'amount'")

        region = order["region"]
        amount = order["amount"]

        # Region must be a non-empty string
        if not isinstance(region, str) or not region.strip():
            raise ValueError(f"Order at index {i} has invalid region: {region!r}")

        # Amount must be a real number (exclude bool)
        if isinstance(amount, bool) or not isinstance(amount, Real):
            raise ValueError(f"Order at index {i} has invalid amount: {amount!r}")

    # ==============================
    # 2.Heap Sort O(n log n)
    # ==============================

    # Make a copy so the ori list is not modified
    arr = order_list.copy()

    def region_key(idx):
        # Normalise region string for fair comparison
        return arr[idx]["region"].strip()

    def heapify(n, i):
        """
        Making sure the subtree rooted at index i satisfies
        the max-heap property based on region comparison.
        """
        largest = i
        left = 2 * i + 1
        right = 2 * i + 2

        # Compare left child with current largest
        if left < n and region_key(left) > region_key(largest):
            largest = left

        # Compare right child with current largest
        if right < n and region_key(right) > region_key(largest):
            largest = right

        # If root is not the largest, swap then continue heapifying
        if largest != i:
            arr[i], arr[largest] = arr[largest], arr[i]
            heapify(n, largest)

    n = len(arr)

    # Build a max heap from the list
    # Time complexity: O(n)
    for i in range(n // 2 - 1, -1, -1):
        heapify(n, i)

    # Extract elements from heap one by one
    # Each extraction takes O(log n), repeated n times
    for end in range(n - 1, 0, -1):
        arr[0], arr[end] = arr[end], arr[0]
        heapify(end, 0)

    # ======================================================
    # 3. aggregation scan – O(n)
    # # ======================================================
    totals_by_region = {}

    for order in arr:
        r = order["region"].strip()
        a = order["amount"]
        totals_by_region[r] = totals_by_region.get(r, 0) + a

    return totals_by_region


# ----- EXPONENTIAL ALGORITHM O(2^n) Access Control: Generate all permission combinations -----


def generate_permission_combo(num_permissions):
    """
    Exponential demo: O(2^n)

    num_permissions = total number of permissions
    Each permission can be either:
    - granted (True)
    - not granted (False)

    We generate every possible permission configuration.
    """

    # validation (so n = -1 won't crash)
    if not isinstance(num_permissions, int) or num_permissions < 0:
        raise ValueError("num_permissions must be a non-negative integer.")

    permission_combinations = []

    def backtrack(permission_index, current_configuration):
        if permission_index == num_permissions:
            permission_combinations.append(current_configuration.copy())
            return

        # Choice 1: deny
        current_configuration[permission_index] = False
        backtrack(permission_index + 1, current_configuration)

        # Choice 2: allow
        current_configuration[permission_index] = True
        backtrack(permission_index + 1, current_configuration)

    backtrack(0, [False] * num_permissions)
    return permission_combinations


def main():
    pass


if __name__ == "__main__":
    main()
