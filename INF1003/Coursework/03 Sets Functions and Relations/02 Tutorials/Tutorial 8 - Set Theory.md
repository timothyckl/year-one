# Tutorial 8: Set Theory

## Skills Tested

- Defining sets using roster and set-builder notation.
- Determining set membership and subset relations.
- Computing set operations: union, intersection, difference, complement.
- Proving set identities using element arguments and membership tables.
- Computing power sets and Cartesian products.
- Determining set cardinalities.
- Drawing Venn diagrams for compound set expressions.
- Applying inclusion-exclusion to three sets.

## Suggested Approach

1. **Set membership:** $x \in A$ means $x$ is listed (roster) or satisfies the
   property (set-builder). Check each element individually.
2. **Subset proof ($A \subseteq B$):** Take an arbitrary $x \in A$. Show $x$ must be in $B$
   using the definition of $A$.
3. **Set equality ($A = B$):** Prove $A \subseteq B$ and $B \subseteq A$ separately.
4. **Set operations from definitions:**
   - $A \cup B$: take all elements in $A$, then add those in $B$ not already in $A$.
   - $A \cap B$: take only elements that appear in both.
   - $A - B$: take elements in $A$ that are not in $B$.
5. **Identity proofs:** Use mutual subset containment, translate membership
   into logically equivalent predicates, or build a membership table.
6. **Power set:** List all subsets including $\varnothing$ and the set itself.
   For $|A| = n$, there are $2^n$ subsets.
7. **Cartesian product:** List all ordered pairs $(a, b)$ where $a$ is from the
   first set and $b$ from the second. $|A \times B| = |A| \cdot |B|$.
8. **Venn diagrams:** Shade one operation at a time, respecting complements
   relative to the universal set.
9. **Three-set inclusion-exclusion:** Add the three individual cardinalities,
   subtract the three pairwise intersections, then add the triple intersection.

## Common Pitfalls

- Confusing $a \in A$ (element) with $\{a\} \subseteq A$ (subset). The set containing $a$
  is a subset, not $a$ itself.
- Missing the empty set when listing subsets of a set.
- Listing ordered pairs incorrectly: $(a, b)$ is not the same as $(b, a)$.
- Forgetting that $A \times B \ne B \times A$ in general, though $|A \times B| = |B \times A|$.
