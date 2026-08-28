# Topic 3: Combinatorics

## Overview

Combinatorics studies arrangements and selections of objects. The fundamental
counting principles (product rule, sum rule) lead to formulas for permutations
and combinations. Subtraction, division, and the pigeonhole principle handle
more complex counting problems.

---

## Product Rule

If a task can be broken into $k$ independent steps, with $n_1$ ways to
complete step 1, $n_2$ ways for step 2, ..., $n_k$ ways for step $k$, then the
total number of ways to complete the task is:
$n_1 \cdot n_2 \cdot \ldots \cdot n_k$.

Applies when steps are **independent** and **sequential**.

---

## Sum Rule

If a task can be done in one of $k$ mutually exclusive ways, with $n_1$ ways
for method 1, $n_2$ for method 2, ..., then the total is:
$n_1 + n_2 + \ldots + n_k$.

Applies when cases are **mutually exclusive** (disjoint).

**Inclusion-Exclusion (two sets):** $|A \cup B| = |A| + |B| - |A \cap B|$.

---

## Permutations

An arrangement of distinct objects where **order matters**.

- **$r$-permutations of $n$ distinct objects:**
  $P(n, r) = \frac{n!}{(n-r)!}$

---

## Combinations

A selection of objects where **order does not matter**.

- **r-combinations of n distinct objects:** $C(n, r) = \frac{n!}{r!(n-r)!}$
- Symmetry: $C(n, r) = C(n, n-r)$

---

## Subtraction Rule

If it is easier to count the complement: $|A| = |U| - |A^c|$.

Use when counting "at least one" or "not all" type problems.

---

## Division Rule

If each desired outcome is counted exactly $k$ times in an overcount, divide
by $k$ to get the correct count. Used when order is irrelevant but was
initially counted as if it mattered.

---

## Pigeonhole Principle

If $n$ items are placed into $m$ boxes and $n > m$, then at least one box
contains at least $2$ items.

**Generalised:** If $n$ items are placed into $m$ boxes, at least one box
contains at least $\lceil n/m \rceil$ items.

Applications: proving existence of collisions, repeated remainders, and
unavoidable patterns.

---

## Common Mistakes

- Using permutation formula when order does not matter (should use
  combination).
- Forgetting to correct an overcount using the division rule.
- Applying the product rule to dependent choices without adjusting.
