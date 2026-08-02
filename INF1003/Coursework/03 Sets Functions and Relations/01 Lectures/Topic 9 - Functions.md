# Topic 9: Functions

## Overview

A function (mapping) assigns each element of a domain set to exactly one
element of a codomain set. Functions can be classified by their mapping
properties (injective, surjective, bijective) and combined through composition.

---

## Function Definition

A function $f$ from set $A$ to set $B$, written $f: A \to B$, assigns to each element
$a \in A$ exactly one element $f(a) \in B$.

- **Domain:** $A$ (the set of inputs)
- **Codomain:** $B$ (the target set)
- **Range (image):** $\{f(a) \mid a \in A\} \subseteq B$

$f(a) = b$ means $b$ is the **image** of $a$, and $a$ is a **preimage** of $b$.

---

## Injective (One-to-One) Functions

$f: A \to B$ is **injective** if distinct elements of $A$ map to distinct elements
of $B$:
$\forall a_1, a_2 \in A$, if $a_1 \ne a_2$ then $f(a_1) \ne f(a_2)$.

Equivalent contrapositive: if $f(a_1) = f(a_2)$ then $a_1 = a_2$.

**Graph test:** A horizontal line intersects the graph at most once.

---

## Surjective (Onto) Functions

$f: A \to B$ is **surjective** if every element of $B$ is the image of at least
one element of $A$:
$\forall b \in B$, $\exists a \in A$ such that $f(a) = b$.

The range equals the codomain.

---

## Bijective Functions

$f: A \to B$ is **bijective** if it is both injective and surjective (a one-to-one
correspondence).

Bijective functions have inverse functions.

If $|A| = |B| = n$ (finite), then $f: A \to B$ is:
- Injective iff it is surjective iff it is bijective.

---

## Composition

Given $f: A \to B$ and $g: B \to C$, the **composition** $(g \circ f): A \to C$ is:
$(g \circ f)(a) = g(f(a))$.

**Properties:**
- Composition is associative: $(h \circ g) \circ f = h \circ (g \circ f)$.
- Composition is NOT commutative: $f \circ g \ne g \circ f$ in general.
- If $f$ and $g$ are injective, then $g \circ f$ is injective.
- If $f$ and $g$ are surjective, then $g \circ f$ is surjective.

---

## Function Sum and Product

For functions $f$ and $g$ with a common domain:
- $(f+g)(x) = f(x) + g(x)$
- $(fg)(x) = f(x)g(x)$

The resulting function is defined where both component functions are defined.

---

## Inverse Functions

If $f: A \to B$ is bijective, its **inverse function** $f^{-1}: B \to A$ satisfies:
- $f^{-1}(f(a)) = a$ for all $a \in A$.
- $f(f^{-1}(b)) = b$ for all $b \in B$.

$f$ is invertible iff $f$ is bijective.

If $f$ and $g$ are invertible: $(g \circ f)^{-1} = f^{-1} \circ g^{-1}$.

---

## Proving Function Properties

**Proving injectivity:**
Assume $f(x_1) = f(x_2)$ and show $x_1 = x_2$.

**Proving surjectivity:**
Take arbitrary $y \in B$, then solve $f(x) = y$ for $x$ to find an explicit preimage.
Show the preimage is in $A$.

**Proving bijectivity:**
Prove injectivity and surjectivity separately, or construct the inverse.

**Disproving injectivity:**
Find $x_1 \ne x_2$ with $f(x_1) = f(x_2)$.

**Disproving surjectivity:**
Find $y \in B$ with no preimage in $A$.

---

## Special Functions

- **Identity function:** $i_A: A \to A$, $i_A(a) = a$.
- **Constant function:** $f(x) = c$ for all $x$.
- **Floor:** $\lfloor x \rfloor$ is the greatest integer $\le x$.
- **Ceiling:** $\lceil x \rceil$ is the smallest integer $\ge x$.
- **Factorial:** $n! = n(n-1)\cdots 2\cdot 1$ for a positive integer $n$,
  with $0! = 1$.

---

## Common Mistakes

- Confusing codomain with range: surjectivity depends on the declared
  codomain, not just the range.
- Forgetting to verify the domain of the preimage when proving surjectivity.
- Assuming $f^{-1}$ exists without proving bijectivity first.
- Reversing composition order: $(g \circ f)(x) = g(f(x))$, apply $f$ first, then $g$.
