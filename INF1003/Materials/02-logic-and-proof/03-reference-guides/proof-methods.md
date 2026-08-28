# Proof Methods Reference

## Strategy Selection Guide

| Statement form | Try first | Alternative |
|---|---|---|
| $p \to q$ | Direct proof | Contrapositive, contradiction |
| $p \leftrightarrow q$ | Prove both directions | |
| $\forall x \, P(x)$ | Arbitrary element + direct | Contradiction |
| $\exists x \, P(x)$ | Construct example | Non-constructive argument |
| $\neg\forall x \, P(x)$ | Find counterexample | |
| $\neg\exists x \, P(x)$ | Contradiction | |

## Method Templates

### Direct Proof

> **Theorem:** If $p$, then $q$.
> **Proof:** Assume $p$. Apply a logical deduction, then conclude $q$.

### Proof by Contraposition

> **Theorem:** If $p$, then $q$.
> **Proof:** Prove the contrapositive. Assume $\neg q$, apply a logical
> deduction, then conclude $\neg p$.

### Proof by Contradiction

> **Theorem:** $p$.
> **Proof:** Assume, for contradiction, that $\neg p$. Derive a contradiction
> with a known fact, then conclude $p$.

### Exhaustive Proof

> **Theorem:** $P(x)$ for every $x$ in the finite set $A$.
> **Proof:** Verify $P(x)$ separately for each element of $A$.

### Proof by Cases

> **Theorem:** For all $x$, $P(x)$.
> **Proof:** Consider exhaustive cases. Prove $P(x)$ under condition $C_1$,
> condition $C_2$, and every other required case. Therefore $P(x)$ holds in
> all cases.

### Existence Proof (Constructive)

> **Theorem:** $\exists x$ such that $P(x)$.
> **Proof:** Choose an explicit value of $x$, verify $P(x)$, then conclude that
> such an $x$ exists.

### Uniqueness Proof

> **Theorem:** $\exists! x$ such that $P(x)$.
> **Proof:** First show that at least one $x$ satisfies $P(x)$. Then assume
> $P(a)$ and $P(b)$ and prove $a=b$.

## Counterexample

To disprove $\forall x \, P(x)$: exhibit one $x_0$ such that $\neg P(x_0)$.

## IFF Proof Structure

Prove both:
1. ($\to$) If $p$ then $q$.
2. ($\leftarrow$) If $q$ then $p$.
