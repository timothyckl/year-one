# Tutorial 7: Proof Methods

## Skills Tested

- Choosing an appropriate proof strategy for a given statement.
- Writing clear direct proofs.
- Applying proof by contraposition.
- Constructing proofs by contradiction.
- Finding counterexamples to disprove universal statements.
- Using exhaustive proof on a small finite domain.
- Using proof by cases for statements with natural partitions.

## Suggested Approach

1. **Read the statement carefully:** Is it $p \to q$, a universal statement,
   or an "if and only if"?
2. **Strategy selection:**
   - If the conclusion is a conditional (if...then), try direct proof first.
   - If the premise or conclusion involves negation, consider contrapositive
     or contradiction.
   - For "iff", prove both directions separately.
   - For universal statements, start with "Let $x$ be an arbitrary..."
3. **Direct proof template:**
   - Assume $p$.
   - Expand definitions.
   - Derive $q$ through logical reasoning and algebraic manipulation.
   - Conclude $p \to q$.
4. **Contrapositive template:**
   - Assume $\neg q$.
   - Derive $\neg p$.
   - Conclude $p \to q$.
5. **Contradiction template:**
   - Assume $p \land \neg q$ (or the negation of the whole statement).
   - Derive any contradiction (e.g., $0 = 1$, a statement and its negation).
   - Conclude the original statement must be true.
6. **Exhaustive proof:** For a small finite domain, verify every possible
   element separately.
7. **Proof by cases:** List all cases, prove the statement for each case.
   Cases must be exhaustive and mutually exclusive.
8. **Counterexample:** Find one concrete value in the domain that violates
   the statement.

## Common Pitfalls

- Starting a direct proof by assuming the conclusion instead of the premise.
- Using circular reasoning (assuming what you are trying to prove).
- Proving only one direction of an "iff" statement.
- Using a specific example to try to prove a universal statement.
