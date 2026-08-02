# Tutorial 3: Combinatorics

## Skills Tested

- Applying the product and sum rules to count outcomes.
- Distinguishing between permutations (order matters) and combinations
  (order does not matter).
- Computing $P(n, r)$ and $C(n, r)$.
- Using the subtraction rule (counting complements).
- Applying the pigeonhole principle to prove existence.

## Suggested Approach

1. **Counting strategy:** First determine if the task involves sequential
   independent steps (product rule) or mutually exclusive cases (sum rule).
2. **Permutation vs combination:** Ask "does order matter?" If rearranging
   the selected items produces a different outcome, use permutations.
   If the selection is the same regardless of order, use combinations.
3. **At least one / none:** Use complement: count the total and subtract the
   "forbidden" cases.
4. **Pigeonhole:** Identify the "pigeons" (items placed), "holes" (categories),
   and the inequality $n > m$. Conclude at least one hole gets
   $\lceil n/m \rceil$ items.

## Common Pitfalls

- Using $C(n, r)$ when order matters (should use $P(n, r)$).
- Double-counting cases in inclusion-exclusion problems.
- Forgetting to consider whether selections are with or without replacement.
- Misapplying the pigeonhole principle: ensure the mapping from items to
  categories is well-defined.
