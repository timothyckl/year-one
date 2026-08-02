# Sets, Functions, and Relations Summary

Quick-reference revision covering Topics 8--10. For detailed notes, see the
individual lecture pages.

---

## Topic 8: Set Theory

### Notation
- Roster: $A = \{1, 2, 3\}$
- Set-builder: $A = \{x \mid P(x)\}$
- Empty set: $\varnothing$ (subset of every set, $|\varnothing| = 0$)

### Operations
- $A \cup B = \{x \mid x \in A \text{ OR } x \in B\}$
- $A \cap B = \{x \mid x \in A \text{ AND } x \in B\}$
- $A \setminus B = \{x \mid x \in A \text{ AND } x \notin B\}$
- $A^c = \{x \in U \mid x \notin A\}$
- $\mathcal{P}(A)$: the set of all subsets of $A$,
  $|\mathcal{P}(A)| = 2^{|A|}$
- $A \times B = \{(a,b) \mid a \in A, b \in B\}$, $|A \times B| = |A| \cdot |B|$

### Core identities
| Identity | Form |
|---|---|
| De Morgan | $(A \cup B)^c = A^c \cap B^c$ |
| De Morgan | $(A \cap B)^c = A^c \cup B^c$ |
| Distributive | $A \cap (B \cup C) = (A \cap B) \cup (A \cap C)$ |
| Distributive | $A \cup (B \cap C) = (A \cup B) \cap (A \cup C)$ |
| Absorption | $A \cup (A \cap B) = A$ |
| Complement | $A \cup A^c = U$, $A \cap A^c = \varnothing$ |

### Proving set identities
- **Element method:** $x \in \mathrm{LHS}$ iff ... iff $x \in \mathrm{RHS}$
- **Predicate method:** Prove the membership predicates are logically equivalent
- **Membership table:** Truth table for set membership

---

## Topic 9: Functions

### Definitions
- $f: A \to B$: each $a \in A$ maps to exactly one $f(a) \in B$
- Domain: $A$, Codomain: $B$, Range: $f(A) \subseteq B$

### Properties
- **Injective (1-1):** $f(a_1) = f(a_2) \Rightarrow a_1 = a_2$
- **Surjective (onto):** $\forall b \in B$, $\exists a \in A$ with $f(a) = b$
- **Bijective:** Both injective and surjective (invertible)

### Composition
- $(g \circ f)(x) = g(f(x))$
- Associative: $(h \circ g) \circ f = h \circ (g \circ f)$
- NOT commutative in general

### Sum and Product
- $(f+g)(x) = f(x) + g(x)$
- $(fg)(x) = f(x)g(x)$

### Inverse
- $f^{-1}$ exists iff $f$ is bijective
- $f^{-1}(f(a)) = a$, $f(f^{-1}(b)) = b$
- $(g \circ f)^{-1} = f^{-1} \circ g^{-1}$

### Proof patterns
- **Injectivity:** Assume $f(x) = f(y)$, show $x = y$
- **Surjectivity:** Take $y$ in the codomain, solve $f(x) = y$ for $x$ in the domain
- **Non-injective:** Exhibit $x \ne y$ with $f(x) = f(y)$
- **Non-surjective:** Find $y$ with no preimage

---

## Topic 10: Relations

### Definition
$R \subseteq A \times A$. $(a, b) \in R$ means $a$ is related to $b$.

### Properties (on set $A$)
| Property | Test |
|---|---|
| Reflexive | $(a, a) \in R$ for all $a \in A$ |
| Symmetric | $(a, b) \in R \Rightarrow (b, a) \in R$ |
| Antisymmetric | $(a, b) \in R$ and $(b, a) \in R \Rightarrow a = b$ |
| Transitive | $(a, b) \in R$ and $(b, c) \in R \Rightarrow (a, c) \in R$ |

### Equivalence Relations
Must be: **Reflexive + Symmetric + Transitive**.

- Equivalence class: $[a] = \{b \in A \mid (a, b) \in R\}$
- Classes form a partition of $A$ (disjoint, cover $A$)
- $[a] = [b]$ iff $(a, b) \in R$

### Matrix representation
For a relation on $\{1, \ldots, n\}$, $M$ is $n \times n$:
- $M_{ij} = 1$ if $(i, j) \in R$, else $0$
- Reflexive: diagonal all 1
- Symmetric: $M = M^T$
- Antisymmetric: $M_{ij} = 1$, $i \ne j \Rightarrow M_{ji} = 0$
- Transitive: a $1$ in the Boolean product $M^2$ at $(i,j)$ requires
  $M_{ij}=1$
