# Functions Reference

## Basic Definitions

- $f: A \to B$: function from domain $A$ to codomain $B$
- $f(a) = b$: $b$ is the image of $a$, $a$ is a preimage of $b$
- Range (image): $f(A) = \{f(a) \mid a \in A\} \subseteq B$

## Function Properties

| Property | Definition | Proof strategy |
|---|---|---|
| Injective (1-1) | $f(x) = f(y) \Rightarrow x = y$ | Assume $f(x) = f(y)$, deduce $x = y$ |
| Surjective (onto) | $\forall b \in B$, $\exists a \in A$: $f(a) = b$ | Take $b \in B$, solve $f(a) = b$ for $a$ |
| Bijective | Injective AND surjective | Prove both properties |

## Disproving Properties

| Property | Counterexample needed |
|---|---|
| Not injective | $x \ne y$ with $f(x) = f(y)$ |
| Not surjective | $b \in B$ with no preimage in $A$ |
| Not bijective | Fail either injectivity or surjectivity |

## Composition

- $(g \circ f)(x) = g(f(x))$
- $f: A \to B$, $g: B \to C \Rightarrow g \circ f: A \to C$
- Associative: $(h \circ g) \circ f = h \circ (g \circ f)$
- NOT commutative in general

### Composition and Properties

| If $f$ and $g$ are... | Then $g \circ f$ is... |
|---|---|
| Both injective | Injective |
| Both surjective | Surjective |
| Both bijective | Bijective |

## Function Sum and Product

- $(f+g)(x) = f(x) + g(x)$
- $(fg)(x) = f(x)g(x)$
- Both operations require $x$ to lie in the domains of both functions.

## Inverse Functions

- $f^{-1}: B \to A$ exists iff $f$ is bijective
- $f^{-1}(f(a)) = a$ for all $a \in A$
- $f(f^{-1}(b)) = b$ for all $b \in B$
- $(g \circ f)^{-1} = f^{-1} \circ g^{-1}$
- $(f^{-1})^{-1} = f$

## Special Functions

| Function | Definition |
|---|---|
| Identity $i_A$ | $i_A(a) = a$ for all $a \in A$ |
| Constant | $f(x) = c$ for all $x$ |
| Floor | $\lfloor x \rfloor$ is the greatest integer $\le x$ |
| Ceiling | $\lceil x \rceil$ is the smallest integer $\ge x$ |
| Factorial | $n! = n(n-1)\cdots 2\cdot 1$ and $0! = 1$ |

## Proving Bijectivity

- Prove injectivity and surjectivity separately, OR
- Construct the inverse function explicitly (if $f$ is defined by a formula)
