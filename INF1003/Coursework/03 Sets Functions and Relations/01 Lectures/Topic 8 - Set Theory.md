# Topic 8: Set Theory

## Overview

A set is an unordered collection of distinct objects (elements). Set theory
provides the fundamental language for all branches of mathematics and is
essential for defining functions, relations, and more complex structures.

---

## Set Definition

- **Roster (enumeration):** $A = \{1, 2, 3, 4\}$, $B = \{a, b, c, \ldots\}$
- **Set-builder notation:** $A = \{x \mid P(x)\}$ — the set of all $x$ such that $P(x)$
  holds. Example: $E = \{x \in \mathbb{Z} \mid x \text{ is even}\}$.
- **Empty set:** $\{\}$ or $\varnothing$, the set with no elements.

**Important sets:**
- $\mathbb{N} = \{0, 1, 2, \ldots\}$ (natural numbers)
- $\mathbb{Z} = \{\ldots, -2, -1, 0, 1, 2, \ldots\}$ (integers)
- $\mathbb{Z}^+ = \{1, 2, 3, \ldots\}$ (positive integers)
- $\mathbb{Q}$: the rational numbers
- $\mathbb{R}$: the real numbers
- $\mathbb{C}$: the complex numbers

---

## Element and Subset Relations

- **Element of:** $x \in A$ means $x$ is an element of set $A$.
- **Subset:** $A \subseteq B$ if every element of $A$ is also in $B$.
- **Proper subset:** $A \subset B$ if $A \subseteq B$ and $A \ne B$.
- **Equality:** $A = B$ iff $A \subseteq B$ and $B \subseteq A$.
- **Cardinality:** $|A|$ is the number of elements in $A$ (for finite sets).

---

## Set Operations

| Operation | Definition | Notation |
|---|---|---|
| Union | $\{x \mid x \in A \text{ OR } x \in B\}$ | $A \cup B$ |
| Intersection | $\{x \mid x \in A \text{ AND } x \in B\}$ | $A \cap B$ |
| Difference | $\{x \mid x \in A \text{ AND } x \notin B\}$ | $A \setminus B$ or $A - B$ |
| Complement | $\{x \mid x \notin A\}$ relative to universe $U$ | $A^c$ or $\bar{A}$ |

---

## Set Identities

| Identity | Form |
|---|---|
| Identity laws | $A \cup \varnothing = A$, $A \cap U = A$ |
| Domination laws | $A \cup U = U$, $A \cap \varnothing = \varnothing$ |
| Idempotent laws | $A \cup A = A$, $A \cap A = A$ |
| Complementation | $(A^c)^c = A$ |
| Commutative laws | $A \cup B = B \cup A$, $A \cap B = B \cap A$ |
| Associative laws | $A \cup (B \cup C) = (A \cup B) \cup C$, $A \cap (B \cap C) = (A \cap B) \cap C$ |
| Distributive laws | $A \cap (B \cup C) = (A \cap B) \cup (A \cap C)$, $A \cup (B \cap C) = (A \cup B) \cap (A \cup C)$ |
| De Morgan's laws | $(A \cup B)^c = A^c \cap B^c$, $(A \cap B)^c = A^c \cup B^c$ |
| Absorption laws | $A \cup (A \cap B) = A$, $A \cap (A \cup B) = A$ |
| Complement laws | $A \cup A^c = U$, $A \cap A^c = \varnothing$ |

---

## Power Set

The **power set** of $A$, denoted $\mathcal{P}(A)$, is the set of all subsets of $A$.

If $|A| = n$, then $|\mathcal{P}(A)| = 2^n$.

**Example:** $A = \{a, b\}$
$\mathcal{P}(A) = \{\varnothing, \{a\}, \{b\}, \{a, b\}\}$, $|\mathcal{P}(A)| = 4$.

---

## Cartesian Product

The **Cartesian product** of sets $A$ and $B$:
$A \times B = \{(a, b) \mid a \in A \text{ and } b \in B\}$.

If $|A| = m$ and $|B| = n$, then $|A \times B| = mn$.

**Generalisation:** $A_1 \times A_2 \times \cdots \times A_n = \{(a_1, \ldots, a_n) \mid a_i \in A_i\}$.

---

## Proving Set Identities

Three main approaches:
1. **Element argument:** Show $x \in \mathrm{LHS}$ iff $x \in \mathrm{RHS}$. Take an arbitrary element
   in the left set and prove it belongs to the right, and vice versa.
2. **Predicate method:** Write each set in set-builder form and prove that the
   corresponding membership predicates are logically equivalent.
3. **Membership table:** Build a truth table showing membership in each set
   for all possible cases.

---

## Common Mistakes

- Confusing element membership, $a \in A$, with subset containment,
  $\{a\} \subseteq A$.
- Forgetting the empty set is a subset of every set ($\varnothing \subseteq A$ for all $A$).
- Treating $A - B$ as $B - A$ (they are different).
- Miscounting cardinality of power set: $|\mathcal{P}(A)| = 2^{|A|}$, not $|A|^2$.
