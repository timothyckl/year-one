# Predicate Logic Reference

## Quantifiers

| Symbol | Meaning | True when |
|---|---|---|
| $\forall x \, P(x)$ | For all $x$, $P(x)$ | $P(x)$ holds for every $x$ in domain |
| $\exists x \, P(x)$ | There exists $x$ such that $P(x)$ | $P(x)$ holds for at least one $x$ |
| $\exists! x \, P(x)$ | There exists a unique $x$ | Exactly one $x$ satisfies $P(x)$ |

## Negation Rules

| Original | Negation |
|---|---|
| $\forall x \, P(x)$ | $\exists x \, \neg P(x)$ |
| $\exists x \, P(x)$ | $\forall x \, \neg P(x)$ |
| $\forall x \forall y \, P(x,y)$ | $\exists x \exists y \, \neg P(x,y)$ |
| $\forall x \exists y \, P(x,y)$ | $\exists x \forall y \, \neg P(x,y)$ |
| $\exists x \forall y \, P(x,y)$ | $\forall x \exists y \, \neg P(x,y)$ |

## English to Predicate Logic

| English | Logic |
|---|---|
| All P are Q | $\forall x \, (P(x) \to Q(x))$ |
| Some P are Q | $\exists x \, (P(x) \land Q(x))$ |
| No P are Q | $\forall x \, (P(x) \to \neg Q(x))$ |
| Some P are not Q | $\exists x \, (P(x) \land \neg Q(x))$ |
| Not all P are Q | $\exists x \, (P(x) \land \neg Q(x))$ |
| Only P are Q | $\forall x \, (Q(x) \to P(x))$ |
| There is exactly one P | $\exists! x \, P(x)$ |

## Quantifier Order

- $\forall x \forall y \equiv \forall y \forall x$
- $\exists x \exists y \equiv \exists y \exists x$
- $\forall x \exists y \not\equiv \exists y \forall x$ (order matters)

## Binding

- A variable is bound if within the scope of a quantifier.
- A variable is free if not bound by any quantifier.
- A statement with no free variables is a proposition.

## Inference Rules

| Rule | From | To |
|---|---|---|
| Universal instantiation | $\forall x \, P(x)$ | $P(c)$ for any $c$ |
| Universal generalisation | $P(c)$ for arbitrary $c$ | $\forall x \, P(x)$ |
| Existential instantiation | $\exists x \, P(x)$ | $P(c)$ for fresh $c$ |
| Existential generalisation | $P(c)$ for some $c$ | $\exists x \, P(x)$ |
