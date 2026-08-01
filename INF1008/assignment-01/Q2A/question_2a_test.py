from question_2a import aggregate_sales_by_region, generate_permission_combo


 # ---------- Test Cases for LINEARITHMIC ALGORITHM ----------

def run_aggregate_tests():
    print("=== Tests for aggregate_sales_by_region ===")

    # 1. Empty list
    orders_empty = []
    print("Test 1 (empty):", aggregate_sales_by_region(orders_empty))
    # Expected: {}

    # 2. One order
    orders_one = [{"order_id": 1, "region": "SG", "amount": 120.0}]
    print("Test 2 (one):", aggregate_sales_by_region(orders_one))
    # Expected: {'SG': 120.0}

    # 3. All orders same region
    orders_same_region = [
        {"order_id": 1, "region": "SG", "amount": 50.0},
        {"order_id": 2, "region": "SG", "amount": 30.0},
        {"order_id": 3, "region": "SG", "amount": 20.0},
    ]
    print("Test 3 (same region):", aggregate_sales_by_region(orders_same_region))
    # Expected: {'SG': 100.0}

    # 4. Already sorted by region
    orders_sorted_region = [
        {"order_id": 1, "region": "JP", "amount": 40.0},
        {"order_id": 2, "region": "JP", "amount": 10.0},
        {"order_id": 3, "region": "MY", "amount": 25.0},
        {"order_id": 4, "region": "SG", "amount": 60.0},
        {"order_id": 5, "region": "SG", "amount": 15.0},
    ]
    print("Test 4 (already sorted):", aggregate_sales_by_region(orders_sorted_region))
    # Expected: {'JP': 50.0, 'MY': 25.0, 'SG': 75.0}

    # 5. Random order, multiple regions
    orders_random = [
        {"order_id": 1, "region": "SG", "amount": 20.0},
        {"order_id": 2, "region": "MY", "amount": 15.0},
        {"order_id": 3, "region": "JP", "amount": 5.0},
        {"order_id": 4, "region": "SG", "amount": 35.0},
        {"order_id": 5, "region": "MY", "amount": 10.0},
        {"order_id": 6, "region": "US", "amount": 50.0},
    ]
    print("Test 5 (random):", aggregate_sales_by_region(orders_random))
    # Expected: {'SG': 55.0, 'MY': 25.0, 'JP': 5.0, 'US': 50.0}

    # 6. Larger list (20)
    orders_large = [
        {"order_id": 1, "region": "SG", "amount": 10},
        {"order_id": 2, "region": "MY", "amount": 5},
        {"order_id": 3, "region": "SG", "amount": 7},
        {"order_id": 4, "region": "JP", "amount": 12},
        {"order_id": 5, "region": "US", "amount": 20},
        {"order_id": 6, "region": "SG", "amount": 30},
        {"order_id": 7, "region": "MY", "amount": 8},
        {"order_id": 8, "region": "JP", "amount": 3},
        {"order_id": 9, "region": "US", "amount": 15},
        {"order_id": 10, "region": "SG", "amount": 25},
        {"order_id": 11, "region": "MY", "amount": 6},
        {"order_id": 12, "region": "JP", "amount": 9},
        {"order_id": 13, "region": "US", "amount": 18},
        {"order_id": 14, "region": "SG", "amount": 40},
        {"order_id": 15, "region": "MY", "amount": 11},
        {"order_id": 16, "region": "JP", "amount": 4},
        {"order_id": 17, "region": "US", "amount": 22},
        {"order_id": 18, "region": "SG", "amount": 16},
        {"order_id": 19, "region": "MY", "amount": 9},
        {"order_id": 20, "region": "JP", "amount": 7},
    ]
    print("Test 6 (large-ish):", aggregate_sales_by_region(orders_large))

    # Invalid 1: amount is not numeric
    try:
        aggregate_sales_by_region([{"region": "SG", "amount": "abc"}])
    except ValueError as e:
        print("Test 7 (invalid amount) PASSED:", e)

    # Invalid 2: missing key
    try:
        aggregate_sales_by_region([{"region": "SG"}])
    except KeyError as e:
        print("Test 8 (missing key) PASSED:", e)


# ---------- Test Cases for EXPONENTIAL ALGORITHM ----------

def run_permission_tests():
    print("\n=== Tests for generate_permission_combinations ===")

    # Edge case: 0 permissions -> 1 config (empty config)
    combos = generate_permission_combo(0)
    print("Test 1 (n=0): count =", len(combos), "configs =", combos)
    # Expected count: 1  (i.e. [[]] or [ []-like ], depending on representation)

    # Normal: n=1 -> 2 configs
    combos = generate_permission_combo(1)
    print("Test 2 (n=1): count =", len(combos))
    # Expected count: 2

    # Normal: n=3 -> 8 configs
    combos = generate_permission_combo(3)
    print("Test 3 (n=3): count =", len(combos))
    # Expected count: 8

    # invalid case: n must not be negative
    try:
        generate_permission_combo(-1)
    except Exception as e:
        print("Test 4 (n=-1 invalid):", e)

def main():
    run_aggregate_tests()
    run_permission_tests()

if __name__ == "__main__":
    main()
