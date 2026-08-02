# Topic 4: Propositional Logic

## Overview

Propositional logic studies declarative statements (propositions) that are
either true ($T$) or false ($F$). Logical connectives combine propositions into
compound expressions, and logical equivalences allow simplification and
transformation.

---

## Propositions

A **proposition** is a declarative sentence that is unambiguously true or false.

- "$2 + 2 = 4$" is a true proposition.
- "Singapore is south of Malaysia" is a true proposition.
- "$x > 5$" is NOT a proposition (truth depends on $x$).
- Questions and commands are NOT propositions.

---

## Logical Connectives

| Name | Symbol | Meaning | Truth condition |
|---|---|---|---|
| Negation | $\neg p$ | "not $p$" | Opposite truth value of $p$ |
| Conjunction | $p \land q$ | "$p$ and $q$" | True iff both $p$ and $q$ are true |
| Disjunction | $p \lor q$ | "$p$ or $q$" (inclusive) | True iff at least one is true |
| Exclusive OR | $p \oplus q$ | "$p$ or $q$, not both" | True iff exactly one is true |
| Implication | $p \to q$ | "if $p$ then $q$" | False only when $p$ is $T$ and $q$ is $F$ |
| Biconditional | $p \leftrightarrow q$ | "$p$ if and only if $q$" | True iff $p$ and $q$ have same value |

---

## Implication Details

For $p \to q$:
- **Converse:** $q \to p$
- **Contrapositive:** $\neg q \to \neg p$ (logically equivalent to original)
- **Inverse:** $\neg p \to \neg q$

$p \to q$ is true when $p$ is false (vacuously true), regardless of $q$.

---

## Precedence of Connectives (highest to lowest)

1. $\neg$
2. $\land$
3. $\lor$
4. $\to$ (implication)
5. $\leftrightarrow$ (biconditional)

Use parentheses to override precedence.

---

## Logical Equivalence

Two compound propositions $p$ and $q$ are **logically equivalent** ($p \equiv q$) if
$p \leftrightarrow q$ is a tautology (always true).

**De Morgan's Laws:**
- $\neg(p \land q) \equiv \neg p \lor \neg q$
- $\neg(p \lor q) \equiv \neg p \land \neg q$

**Other key equivalences:**

| Law | Form |
|---|---|
| Double negation | $\neg(\neg p) \equiv p$ |
| Idempotent | $p \land p \equiv p$, $p \lor p \equiv p$ |
| Commutative | $p \land q \equiv q \land p$, $p \lor q \equiv q \lor p$ |
| Associative | $(p \land q) \land r \equiv p \land (q \land r)$ |
| Distributive | $p \land (q \lor r) \equiv (p \land q) \lor (p \land r)$ |
| Absorption | $p \land (p \lor q) \equiv p$ |
| Identity | $p \land T \equiv p$, $p \lor F \equiv p$ |
| Domination | $p \lor T \equiv T$, $p \land F \equiv F$ |
| Negation | $p \land \neg p \equiv F$, $p \lor \neg p \equiv T$ |
| Implication | $p \to q \equiv \neg p \lor q$ |

---

## Tautologies, Contradictions, Contingencies, and Satisfiability

- **Tautology:** always true (e.g. $p \lor \neg p$).
- **Contradiction:** always false (e.g. $p \land \neg p$).
- **Contingency:** neither a tautology nor a contradiction.
- **Satisfiable:** true for at least one assignment of truth values.
- **Unsatisfiable:** false for every assignment of truth values.

---

## Translating English to Logic

- "$p$ only if $q$" means $p \to q$
- "$p$ is sufficient for $q$" means $p \to q$
- "$p$ is necessary for $q$" means $q \to p$ (or $\neg p \to \neg q$)
- "$p$ unless $q$" means $\neg q \to p$ (or $p \lor q$)
- "Neither $p$ nor $q$" means $\neg p \land \neg q$

---

## Common Mistakes

- Confusing exclusive OR with inclusive OR.
- Mixing up converse, inverse, and contrapositive.
- Forgetting that $p \to q$ is true when $p$ is false.
- Applying De Morgan's laws incorrectly: forgetting to change $\land$/$\lor$.
