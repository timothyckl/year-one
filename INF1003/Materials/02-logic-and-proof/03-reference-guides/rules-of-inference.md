# Rules of Inference Reference

## Propositional Rules

| Rule | Premises | Conclusion |
|---|---|---|
| Modus ponens | $p \to q$, $p$ | $q$ |
| Modus tollens | $p \to q$, $\neg q$ | $\neg p$ |
| Hypothetical syllogism | $p \to q$, $q \to r$ | $p \to r$ |
| Disjunctive syllogism | $p \lor q$, $\neg p$ | $q$ |
| Simplification | $p \land q$ | $p$ |
| Conjunction | $p$, $q$ | $p \land q$ |
| Addition | $p$ | $p \lor q$ |
| Resolution | $p \lor q$, $\neg p \lor r$ | $q \lor r$ |

## Invalid Fallacies

| Fallacy | Premises | Erroneous conclusion |
|---|---|---|
| Affirming consequent | $p \to q$, $q$ | $p$ |
| Denying antecedent | $p \to q$, $\neg p$ | $\neg q$ |

## Predicate Rules

| Rule | Action |
|---|---|
| Universal instantiation (UI) | $\forall x \, P(x) \therefore P(c)$ |
| Universal generalisation (UG) | $P(c)$ for arbitrary $c$ therefore $\forall x \, P(x)$ |
| Existential instantiation (EI) | $\exists x \, P(x) \therefore P(c)$, $c$ is new |
| Existential generalisation (EG) | $P(c) \therefore \exists x \, P(x)$ |

## Combined Reasoning Pattern

1. Strip quantifiers using UI/EI.
2. Apply propositional rules to the instantiated statements.
3. Add quantifiers back using UG/EG as needed.

Key constraint: when using UG, the element $c$ must be arbitrary (not appearing
in any premise or derived from EI).
