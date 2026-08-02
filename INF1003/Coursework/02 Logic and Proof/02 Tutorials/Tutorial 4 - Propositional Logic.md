# Tutorial 4: Propositional Logic

## Skills Tested

- Identifying propositions and determining their truth values.
- Translating English sentences into propositional logic.
- Constructing and interpreting truth tables.
- Determining logical equivalence of compound propositions.
- Applying De Morgan's laws and other logical equivalences.
- Identifying tautologies, contradictions, and contingencies.
- Finding the converse, contrapositive, and inverse of a conditional.

## Suggested Approach

1. **Translation:** Identify atomic propositions and assign variables ($p$, $q$,
   $r$). Map English connectives to logical operators:
   - "and" -> $\land$
   - "or" -> $\lor$
   - "if...then" -> $\to$
   - "if and only if" -> $\leftrightarrow$
   - "not" -> $\neg$
   - "unless" -> $\lor$ (or $\neg q \to p$)
2. **Truth tables:** For $n$ variables, create $2^n$ rows. Compute intermediate
   columns before the final expression.
3. **Logical equivalence:** Build truth tables for both expressions and compare
   the final columns. Alternatively, transform one expression into the other
   using known equivalences.
4. **Simplifying:** Apply De Morgan's laws, double negation, distributive laws,
   and the implication equivalence ($p \to q \equiv \neg p \lor q$).
5. **Tautology check:** The truth table's final column should be all $T$.
   Contradiction: all $F$. Contingency: mixture.

## Common Pitfalls

- Confusing $p \to q$ (if $p$ then $q$) with $q \to p$ (converse).
- Forgetting that $p \to q$ is true when $p$ is false.
- Applying De Morgan's laws to a single variable: $\neg(p \land q) \equiv \neg p \lor \neg q$
  requires changing the operator.
- Missing parentheses when translating nested English conditionals.
