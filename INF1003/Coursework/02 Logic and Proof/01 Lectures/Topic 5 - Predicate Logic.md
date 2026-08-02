# Topic 5: Predicate Logic

## Overview

Predicate logic extends propositional logic by introducing variables,
predicates, and quantifiers. This allows reasoning about properties of objects
and statements like "all integers are even" or "there exists a prime greater
than 100".

---

## Predicates

A **predicate** (or propositional function) $P(x)$ is a statement involving a
variable $x$. It becomes a proposition when $x$ is assigned a specific value or
when a quantifier is applied.

- Domain (universe of discourse): the set of values $x$ can take.
- $P(x_1, \ldots, x_n)$ is an $n$-place ($n$-ary) predicate.

---

## Universal Quantifier

For all $x$, $P(x)$. Written: $\forall x \, P(x)$ or $(\forall x)P(x)$.

- True if $P(x)$ is true for **every** $x$ in the domain.
- False if there exists at least one counterexample.

---

## Existential Quantifier

There exists $x$ such that $P(x)$. Written: $\exists x \, P(x)$ or $(\exists x)P(x)$.

- True if $P(x)$ is true for **at least one** $x$ in the domain.
- False if $P(x)$ is false for every $x$.

---

## Uniqueness Quantifier

There exists a unique $x$ such that $P(x)$. Written: $\exists! x \, P(x)$.

Equivalent to: $\exists x \, (P(x) \land \forall y \, (P(y) \to x = y))$.

---

## Negating Quantified Expressions

| Original | Negation |
|---|---|
| $\forall x \, P(x)$ | $\exists x \, \neg P(x)$ |
| $\exists x \, P(x)$ | $\forall x \, \neg P(x)$ |
| $\forall x \forall y \, P(x, y)$ | $\exists x \exists y \, \neg P(x, y)$ |
| $\forall x \exists y \, P(x, y)$ | $\exists x \forall y \, \neg P(x, y)$ |

General rule: push negation inward, flipping $\forall \leftrightarrow \exists$ and negating the
predicate.

---

## Nested Quantifiers

Multiple quantifiers applied to multi-place predicates.

**Order matters:**
- $\forall x \forall y \, P(x, y) \equiv \forall y \forall x \, P(x, y)$
- $\exists x \exists y \, P(x, y) \equiv \exists y \exists x \, P(x, y)$
- $\forall x \exists y \, P(x, y)$ is NOT equivalent to $\exists y \forall x \, P(x, y)$

**Example (domain = integers):**
- $\forall x \exists y \, (x + y = 0)$: TRUE ($y = -x$)
- $\exists y \forall x \, (x + y = 0)$: FALSE (no single $y$ works for all $x$)

---

## Translating English to Predicate Logic

- "All S are P": $\forall x \, (S(x) \to P(x))$
- "Some S are P": $\exists x \, (S(x) \land P(x))$
- "No S are P": $\forall x \, (S(x) \to \neg P(x))$ or $\neg \exists x \, (S(x) \land P(x))$
- "All S are not P": $\forall x \, (S(x) \to \neg P(x))$
- "Some S are not P": $\exists x \, (S(x) \land \neg P(x))$
- "Only S are P": $\forall x \, (P(x) \to S(x))$

**"Any" vs "Every" vs "Each":** All usually translate to universal
quantification. Context determines the scope.

---

## Binding and Scope

- A variable is **bound** if it is within the scope of a quantifier.
- A variable is **free** if it is not bound by any quantifier.
- A proposition contains no free variables.
- Renaming bound variables is valid ($\alpha$-conversion).

---

## Common Mistakes

- Using $\land$ with universal quantifier instead of implication.
- Using implication with existential quantifier instead of $\land$.
- Forgetting that quantifier order matters for mixed types.
- Negating nested quantifiers incorrectly (missing a flip).
