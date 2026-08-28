# Combinatorics Reference

## Counting Rules

| Rule | When to apply |
|---|---|
| Product rule | Sequential independent steps — multiply counts |
| Sum rule | Mutually exclusive cases — add counts |
| Subtraction rule | Count complement: $|A| = |U| - |A^c|$ |
| Division rule | Overcounted $k$ times each — divide by $k$ |
| Inclusion-exclusion (2 sets) | $|A \cup B| = |A| + |B| - |A \cap B|$ |

## Permutations (order matters)

| Scenario | Formula |
|---|---|
| r from n distinct | $P(n, r) = \frac{n!}{(n-r)!}$ |
| n from n distinct | $n!$ |

## Combinations (order does not matter)

| Scenario | Formula |
|---|---|
| r from n distinct | $C(n, r) = \frac{n!}{r!(n-r)!}$ |
| Symmetry | $C(n, r) = C(n, n-r)$ |

## Pigeonhole Principle

- $n$ items, $m$ boxes, $n > m$: at least one box has $\ge \lceil n/m \rceil$
  items
- $n$ items, $m$ boxes, $n > km$: at least one box has $\ge k+1$ items
  (generalised)

## Common Counting Strategies

1. "At least one" -> complement (count total minus none)
2. Arrangements with conditions -> count without conditions, subtract bad cases
3. If every outcome is counted the same number of times, apply the division
   rule to correct the overcount.
