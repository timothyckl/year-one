# Tutorial 6: Rules of Inference

## Skills Tested

- Identifying valid and invalid argument forms.
- Applying propositional rules of inference (modus ponens, modus tollens,
  hypothetical syllogism, disjunctive syllogism, resolution, etc.).
- Applying predicate logic rules (universal instantiation/generalisation,
  existential instantiation/generalisation).
- Constructing step-by-step proofs using rules of inference.
- Detecting common fallacies (affirming the consequent, denying the
  antecedent).

## Suggested Approach

1. **Identify premises and conclusion:** Write them clearly in logical notation.
2. **Number each premise:** As you derive new statements, assign numbers and
   cite the rule and line numbers used.
3. **Build a chain:** Look for implications you can apply modus ponens to, or
   negations you can use with modus tollens or disjunctive syllogism.
4. **Propositional rules quick reference:**
   - Modus ponens: $p \to q$, $p \therefore q$
   - Modus tollens: $p \to q$, $\neg q \therefore \neg p$
   - Hypothetical syllogism: $p \to q$, $q \to r \therefore p \to r$
   - Disjunctive syllogism: $p \lor q$, $\neg p \therefore q$
   - Simplification: $p \land q \therefore p$
   - Conjunction: $p$, $q \therefore p \land q$
   - Addition: $p \therefore p \lor q$
   - Resolution: $p \lor q$, $\neg p \lor r \therefore q \lor r$
5. **Predicate rules:**
   - Universal instantiation: strip $\forall$, replace variable with a specific
     constant.
   - Existential instantiation: strip $\exists$, introduce a new constant.
   - Universal generalisation: from a proof for an arbitrary element, conclude
     $\forall$.
   - Existential generalisation: from a specific example, conclude $\exists$.
6. **Watch for fallacies:** If the argument looks like modus ponens reversed
   ($p \to q$, $q \therefore p$), it is invalid.

## Common Pitfalls

- Applying a rule to a sub-expression instead of the whole statement.
- Using existential instantiation without introducing a fresh constant.
- Generalising from a constant that appeared in a premise (the element must be
  arbitrary).
- Skipping steps: each inference must cite exactly one rule and the exact
  line numbers it applies to.
