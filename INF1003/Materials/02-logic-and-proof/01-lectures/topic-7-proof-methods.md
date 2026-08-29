# Topic 7: Proof Methods

## Overview

A mathematical proof is a valid argument that establishes the truth of a
statement. Different types of statements call for different proof strategies.
Mastering the standard methods is essential for all subsequent topics.

---

## Direct Proof

To prove $p \to q$: assume $p$ is true, then use definitions, axioms, and previously
established results to deduce $q$.

**Example:** If $n$ is odd, then $n^2$ is odd.
- Assume $n$ is odd: $n = 2k + 1$.
- $n^2 = (2k+1)^2 = 4k^2 + 4k + 1 = 2(2k^2 + 2k) + 1$ (odd).

---

## Proof by Contraposition

To prove $p \to q$: prove its contrapositive $\neg q \to \neg p$.

**Example:** If $n^2$ is even, then $n$ is even.
- Contrapositive: If $n$ is NOT even (odd), then $n^2$ is NOT even (odd).
- $n = 2k + 1 \Rightarrow n^2 = 2(2k^2 + 2k) + 1$ (odd). Done.

---

## Proof by Contradiction

To prove $p$: assume $\neg p$ and derive a contradiction ($q \land \neg q$). Therefore
$p$ must be true.

**Example:** $\sqrt{2}$ is irrational.
- Assume $\sqrt{2} = a/b$ in lowest terms, $a$, $b$ integers with $\gcd(a, b) = 1$.
- Square: $2 = a^2/b^2 \Rightarrow a^2 = 2b^2 \Rightarrow a^2$ is even $\Rightarrow a$ is even $\Rightarrow a = 2k$.
- $(2k)^2 = 2b^2 \Rightarrow 4k^2 = 2b^2 \Rightarrow b^2 = 2k^2 \Rightarrow b^2$ is even $\Rightarrow b$ is even.
- Both $a$ and $b$ are even, contradicting $\gcd(a, b) = 1$.

---

## Trivial and Vacuous Proofs

- **Trivial proof:** If $q$ is always true, then $p \to q$ is true regardless of $p$.
- **Vacuous proof:** If $p$ is always false, then $p \to q$ is true regardless of $q$.

These are rarely used in practice but are valid argument forms.

---

## Counterexamples

To disprove $\forall x \, P(x)$: find a single $x$ for which $P(x)$ is false.

**Example:** Disprove "all primes are odd."
- Counterexample: $2$ is prime and even.

---

## Exhaustive Proof

For a finite domain, verify the statement separately for every possible
element. This is practical only when the domain is small.

**Example:** To prove a statement for every integer $n$ with $1 \le n \le 4$, check
$n = 1$, $n = 2$, $n = 3$, and $n = 4$ individually.

---

## Proof by Cases

Partition the domain into exhaustive groups and prove the statement for each
group. A group may contain finitely or infinitely many elements.

**Example:** Prove $|xy| = |x||y|$ for all real numbers.
- Case 1: $x \ge 0$, $y \ge 0$
- Case 2: $x \ge 0$, $y < 0$
- Case 3: $x < 0$, $y \ge 0$
- Case 4: $x < 0$, $y < 0$
- Prove each case using the definition of absolute value.

---

## Existence Proofs

**Constructive:** Exhibit a specific example that satisfies the property.

**Non-constructive:** Prove existence without providing an explicit example
(e.g. using pigeonhole principle or counting argument).

**Example (constructive):** Prove there exists an integer $n$ such that
$n^2 - 4 = 0$. Answer: $n = 2$ or $n = -2$.

---

## Uniqueness Proofs

To prove $\exists! x \, P(x)$:
1. **Existence:** Show that at least one $x$ satisfies $P(x)$.
2. **Uniqueness:** If $P(a)$ and $P(b)$, then $a = b$.

**Example:** Prove there is a unique additive identity in real numbers.
- Existence: $a + 0 = a$ for all $a$, so $0$ works.
- Uniqueness: Suppose $e$ and $f$ are both additive identities. Then
  $e = e + f = f$. So $e = f$.

---

## Choosing a Proof Strategy

1. Try direct proof first.
2. If the conclusion involves "if and only if", prove both directions.
3. If the premise gives a negative condition or the conclusion is negative,
   try contrapositive or contradiction.
4. For universal statements, consider an arbitrary element.
5. If the statement has natural cases, use proof by cases.
6. For "at least one" or existence, construct an example or use a counting
   argument.

---

## Common Mistakes

- Proving the converse instead of the original statement.
- Using a specific example to "prove" a universal statement.
- Circular reasoning: assuming what needs to be proved.
- Not handling all cases in a proof by cases.
