# Tutorial 10: Relations

## Skills Tested

- Determining whether ordered pairs belong to a relation.
- Representing relations as sets of ordered pairs, digraphs, and matrices.
- Testing relations for reflexivity, symmetry, antisymmetry, and transitivity.
- Determining whether a relation is an equivalence relation.
- Finding equivalence classes.
- Determining the number of distinct equivalence classes.
- Understanding the connection between equivalence relations and partitions.

## Suggested Approach

1. **Listing a relation:** Generate all ordered pairs $(a, b)$ from the defining
   condition. For finite sets, systematically check each combination.
2. **Property checking on a finite set:**
   - **Reflexive:** Check if $(a, a) \in R$ for every $a \in A$. If any is missing,
     it is not reflexive.
   - **Symmetric:** For each $(a, b) \in R$, check that $(b, a)$ is also in $R$.
     If any reverse pair is missing, it is not symmetric.
   - **Antisymmetric:** For each $(a, b) \in R$ with $a \ne b$, check that $(b, a)$
     is NOT in $R$. If both directions exist for distinct elements, it is not
     antisymmetric.
   - **Transitive:** For each pair $(a, b)$ and $(b, c)$ in $R$, check that $(a, c)$
     is in $R$. This is the most tedious; check all combinations systematically.
3. **Matrix method:** For the zero-one matrix $M$:
   - Reflexive: main diagonal all 1s.
   - Symmetric: $M = M^T$.
   - Antisymmetric: $M_{ij} = 1$ and $i \ne j$ implies $M_{ji} = 0$.
   - Transitive: whenever $M_{ij} = 1$ and $M_{jk} = 1$, we must have $M_{ik} = 1$.
   - Equivalently, $M^2$ (Boolean) has 1s only where $M$ already has 1s.
4. **Equivalence relation:** Verify all three properties. If all hold, the
   relation partitions the set.
5. **Equivalence class:** $[a]$ is the set of all elements related to $a$. List
   all elements $b$ such that $(a, b) \in R$. The distinct classes should form a
   partition.
6. **Proving properties:** Apply each definition directly. For the relation
   defined by "$m-n$ is odd," test reflexivity, symmetry, and transitivity,
   then identify its equivalence classes if it is an equivalence relation.

## Common Pitfalls

- Testing transitivity incompletely — every pair $(a,b)$ and $(b,c)$ must
  have the direct pair $(a,c)$. Missing one pair invalidates the property.
- Confusing antisymmetric with asymmetric. A relation can be both symmetric
  and antisymmetric (only self-pairs).
- Forgetting that the empty set is an equivalence relation on the empty set,
  but not on a non-empty set (lacks reflexivity).
- Mixing up equivalence classes: $[a] = [b]$ iff $(a, b)$ is in the relation.
  Distinct equivalence classes are disjoint.
