# Tutorial 1: Sequences and Summation

## Skills Tested

- Identifying and generating terms of a sequence from an explicit formula or
  recurrence relation.
- Recognising arithmetic and geometric progressions.
- Computing AP and GP sums using the standard formulas.
- Manipulating summation notation by splitting sums and identifying telescoping sums.
- Evaluating compound interest problems.

## Suggested Approach

1. **Sequence identification:** Check whether consecutive differences are
   constant (AP) or consecutive ratios are constant (GP). If neither, look
   for a recurrence pattern or explicit formula.
2. **AP/GP sums:** Identify the first term $a$, common difference $d$ or ratio
   $r$, and the number of terms $n$. Substitute directly into:
   - AP: $S_n = \frac{n}{2}(2a + (n-1)d)$
   - GP: $S_n = \frac{a(1-r^n)}{1-r}$
3. **Summation manipulation:** Split the sum into simpler parts and apply
   standard sum formulas where appropriate.
4. **Telescoping:** Write out the first few and last few terms to spot the
   cancellation pattern.
5. **Compound interest:** Identify the principal $P$, annual interest rate $r$,
   and number of years $n$. Apply $A = P(1 + r)^n$.

## Common Pitfalls

- Counting terms incorrectly in a sequence: for terms from index $m$ to $n$,
  there are $n - m + 1$ terms.
- Using the GP infinite sum formula when $|r| \ge 1$.
- Applying a standard sum formula without checking its starting index.
- Confusing $a_n$ ($n$-th term) with $S_n$ (sum of first $n$ terms).
