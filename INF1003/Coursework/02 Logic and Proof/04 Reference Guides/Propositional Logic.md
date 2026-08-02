# Propositional Logic Reference

## Truth Table Size

$n$ variables $\to 2^n$ rows.

## Connectives

| Symbol | Name | True when |
|---|---|---|
| $\neg p$ | Negation | $p = F$ |
| $p \land q$ | Conjunction | $p = T$, $q = T$ |
| $p \lor q$ | Disjunction | At least one $T$ |
| $p \oplus q$ | Exclusive OR | Exactly one $T$ |
| $p \to q$ | Implication | $p = F$ or $q = T$ |
| $p \leftrightarrow q$ | Biconditional | $p = q$ |

## Logical Equivalences

| Name | Form |
|---|---|
| Implication | $p \to q \equiv \neg p \lor q$ |
| Contrapositive | $p \to q \equiv \neg q \to \neg p$ |
| Double negation | $\neg(\neg p) \equiv p$ |
| De Morgan 1 | $\neg(p \land q) \equiv \neg p \lor \neg q$ |
| De Morgan 2 | $\neg(p \lor q) \equiv \neg p \land \neg q$ |
| Commutative | $p \land q \equiv q \land p$ |
| Associative | $(p \land q) \land r \equiv p \land (q \land r)$ |
| Distributive AND | $p \land (q \lor r) \equiv (p \land q) \lor (p \land r)$ |
| Distributive OR | $p \lor (q \land r) \equiv (p \lor q) \land (p \lor r)$ |
| Absorption | $p \land (p \lor q) \equiv p$ |
| Identity | $p \land T \equiv p$, $p \lor F \equiv p$ |
| Domination | $p \land F \equiv F$, $p \lor T \equiv T$ |
| Negation | $p \land \neg p \equiv F$, $p \lor \neg p \equiv T$ |

## Precedence (highest to lowest)

$\neg > \land > \lor > \to > \leftrightarrow$

## English to Logic

| English | Logic |
|---|---|
| $p$ only if $q$ | $p \to q$ |
| $p$ if $q$ | $q \to p$ |
| $p$ unless $q$ | $\neg q \to p$ (or $p \lor q$) |
| $p$ is sufficient for $q$ | $p \to q$ |
| $p$ is necessary for $q$ | $q \to p$ |
| Neither $p$ nor $q$ | $\neg p \land \neg q$ |
| $p$ or $q$ but not both | $p \oplus q$ |
