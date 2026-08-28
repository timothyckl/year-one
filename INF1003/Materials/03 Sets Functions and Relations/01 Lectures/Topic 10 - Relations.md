# Topic 10: Relations

## Overview

A relation on a set $A$ is a subset of $A \times A$ (or more generally, a subset of
$A \times B$ for relations between different sets). Relations model connections
between elements and can be classified by properties such as reflexivity,
symmetry, antisymmetry, and transitivity.

---

## Relation Definition

A binary relation $R$ from set $A$ to set $B$ is a subset of $A \times B$.

- $(a, b) \in R$ means "$a$ is related to $b$", also written $a\,R\,b$.
- If $A = B$, $R$ is a relation **on** $A$.

**Representations:**
- Set of ordered pairs: $R = \{(1,2), (2,3), \ldots\}$
- Digraph (directed graph): nodes = elements, edges = pairs in $R$.
- Zero-one matrix: $M_{ij} = 1$ if $(i, j) \in R$, $0$ otherwise.

---

## Properties of Relations (on a set $A$)

| Property | Definition | Matrix condition | Digraph condition |
|---|---|---|---|
| **Reflexive** | $\forall a \in A$, $(a, a) \in R$ | Main diagonal all 1s | Every node has a self-loop |
| **Symmetric** | If $(a, b) \in R$ then $(b, a) \in R$ | $M = M^T$ | Every edge has a reverse edge |
| **Antisymmetric** | If $(a, b) \in R$ and $(b, a) \in R$, then $a = b$ | $M_{ij}=1$ and $i\ne j$ implies $M_{ji}=0$ | No pair of reverse edges between distinct nodes |
| **Transitive** | If $(a, b) \in R$ and $(b, c) \in R$, then $(a, c) \in R$ | A $1$ in the Boolean product $M^2$ at $(i,j)$ requires $M_{ij}=1$ | If there is a path of length 2, there is a direct edge |

---

## Equivalence Relations

A relation $R$ on $A$ is an **equivalence relation** if it is:
1. Reflexive
2. Symmetric
3. Transitive

**Equivalence class** of $a$: $[a]_R = \{b \in A \mid (a, b) \in R\}$.

**Partition:** The equivalence classes of an equivalence relation form a
partition of $A$ (they are pairwise disjoint and their union is $A$).

---

## Common Mistakes

- Confusing antisymmetric with "not symmetric" — these are different.
  A relation can be both symmetric and antisymmetric (only if it contains
  only pairs of the form $(a, a)$).
- Forgetting to check ALL pairs when testing transitivity.
- Confusing the definition of equivalence class with the equivalence
  relation itself.
