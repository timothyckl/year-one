# Set Theory Reference

## Set Operations

| Operation | Definition | Cardinality |
|---|---|---|
| $A \cup B$ | $\{x \mid x \in A \text{ OR } x \in B\}$ | $\lvert A \rvert + \lvert B \rvert - \lvert A \cap B \rvert$ |
| $A \cap B$ | $\{x \mid x \in A \text{ AND } x \in B\}$ | Depends on overlap |
| $A - B$ | $\{x \mid x \in A \text{ AND } x \notin B\}$ | $\lvert A \rvert - \lvert A \cap B \rvert$ |
| $A^c$ | $\{x \in U \mid x \notin A\}$ | $\lvert U \rvert - \lvert A \rvert$ |
| $\mathcal{P}(A)$ | Set of all subsets of $A$ | $2^{\lvert A \rvert}$ |
| $A \times B$ | $\{(a,b) \mid a \in A, b \in B\}$ | $\lvert A \rvert \cdot \lvert B \rvert$ |

## Set Identities

| Name | Identity |
|---|---|
| Identity | $A \cup \varnothing = A$, $A \cap U = A$ |
| Domination | $A \cup U = U$, $A \cap \varnothing = \varnothing$ |
| Idempotent | $A \cup A = A$, $A \cap A = A$ |
| Complementation | $(A^c)^c = A$ |
| Commutative | $A \cup B = B \cup A$, $A \cap B = B \cap A$ |
| Associative | $(A \cup B) \cup C = A \cup (B \cup C)$ |
| Distributive | $A \cup (B \cap C) = (A \cup B) \cap (A \cup C)$ |
| Distributive | $A \cap (B \cup C) = (A \cap B) \cup (A \cap C)$ |
| De Morgan | $(A \cup B)^c = A^c \cap B^c$ |
| De Morgan | $(A \cap B)^c = A^c \cup B^c$ |
| Absorption | $A \cup (A \cap B) = A$, $A \cap (A \cup B) = A$ |
| Complement | $A \cup A^c = U$, $A \cap A^c = \varnothing$ |

## Subset and Equality Proofs

- $A \subseteq B$: take arbitrary $x \in A$, show $x \in B$
- $A = B$: prove $A \subseteq B$ AND $B \subseteq A$
- $A \subset B$: prove $A \subseteq B$ AND $A \ne B$

## Key Facts

- $\varnothing \subseteq A$ for all $A$
- $A \subseteq U$ for all $A$
- $A \subseteq A$ (reflexive)
- If $A \subseteq B$ and $B \subseteq C$, then $A \subseteq C$ (transitive)
- If $A \subseteq B$, then $|A| \le |B|$ (for finite sets)
- $\mathcal{P}(\varnothing) = \{\varnothing\}$, $|\mathcal{P}(\varnothing)| = 1$
- $A \times B \ne B \times A$ in general
