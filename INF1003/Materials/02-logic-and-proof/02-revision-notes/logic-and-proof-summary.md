# Logic and Proof Summary

Quick-reference revision covering Topics 4--7. For detailed notes, see the
individual lecture pages.

---

## Topic 4: Propositional Logic

### Connectives (precedence: $\neg > \land > \lor > \to > \leftrightarrow$)
| Symbol | Name | True when... |
|---|---|---|
| $\neg p$ | Negation | $p$ is false |
| $p \land q$ | Conjunction | Both true |
| $p \lor q$ | Disjunction | At least one true |
| $p \oplus q$ | Exclusive or | Exactly one true |
| $p \to q$ | Implication | $p$ false or $q$ true |
| $p \leftrightarrow q$ | Biconditional | $p$ and $q$ same value |

### Key equivalences
- $p \to q \equiv \neg p \lor q$
- De Morgan: $\neg(p \land q) \equiv \neg p \lor \neg q$
- De Morgan: $\neg(p \lor q) \equiv \neg p \land \neg q$
- Double negation: $\neg(\neg p) \equiv p$
- Distributive: $p \land (q \lor r) \equiv (p \land q) \lor (p \land r)$
- $p \to q$ contrapositive: $\neg q \to \neg p$ (equivalent to original)

### Translation patterns
- "$p$ only if $q$" = $p \to q$
- "$p$ is sufficient for $q$" = $p \to q$
- "$p$ is necessary for $q$" = $q \to p$
- "$p$ unless $q$" = $p \lor q$
- "Neither $p$ nor $q$" = $\neg p \land \neg q$

### Tautology/Contradiction/Contingency
- Tautology: truth table all $T$
- Contradiction: truth table all $F$
- Contingency: mixture

---

## Topic 5: Predicate Logic

### Quantifiers
- $\forall x \, P(x)$: $P(x)$ must hold for every $x$ in the domain
- $\exists x \, P(x)$: $P(x)$ must hold for at least one $x$
- $\exists! x \, P(x)$: exactly one $x$ satisfies $P(x)$

### Negation rules
- $\neg(\forall x \, P(x)) \equiv \exists x \, \neg P(x)$
- $\neg(\exists x \, P(x)) \equiv \forall x \, \neg P(x)$
- Push $\neg$ inward through nested quantifiers, flipping each one

### Nested quantifiers
- $\forall x \forall y \equiv \forall y \forall x$ (same type commutes)
- $\exists x \exists y \equiv \exists y \exists x$
- $\forall x \exists y$ vs $\exists y \forall x$: **order matters**

### Translation
- "All P are Q": $\forall x \, (P(x) \to Q(x))$
- "Some P are Q": $\exists x \, (P(x) \land Q(x))$
- "No P are Q": $\forall x \, (P(x) \to \neg Q(x))$
- "Some P are not Q": $\exists x \, (P(x) \land \neg Q(x))$

---

## Topic 6: Rules of Inference

### Propositional rules
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

### Common fallacies
- Affirming consequent: $p \to q$, $q \therefore p$ (INVALID)
- Denying antecedent: $p \to q$, $\neg p \therefore \neg q$ (INVALID)

### Predicate rules
- Universal instantiation: $\forall x \, P(x) \Rightarrow P(c)$
- Universal generalisation: $P(c)$ for arbitrary $c$ $\Rightarrow \forall x \, P(x)$
- Existential instantiation: $\exists x \, P(x) \Rightarrow P(c)$ for fresh $c$
- Existential generalisation: $P(c) \Rightarrow \exists x \, P(x)$

---

## Topic 7: Proof Methods

### Strategy guide
- **Direct proof:** Assume $p$, derive $q$. Best first choice.
- **Contrapositive:** Prove $\neg q \to \neg p$. Use when conclusion is negated.
- **Contradiction:** Assume $\neg(p \to q)$, derive inconsistency. Use when
  dealing with irrationality or non-existence.
- **Exhaustive proof:** Check every element of a small finite domain.
- **Proof by cases:** Partition the domain into exhaustive groups and prove
  the statement for each group.
- **Counterexample:** One concrete example disproves a universal claim.
- **Existence:** Show an explicit example (constructive) or prove one must
  exist (non-constructive).
- **Uniqueness:** (1) Prove existence. (2) Assume two elements both work,
  prove they are equal.

### IFF proofs
Prove both $p \to q$ and $q \to p$ (two separate proofs).

### Common proof vocabulary
- "Let $x$ be an arbitrary element of $A$..." (universal proof)
- "Assume, for contradiction, that..." (contradiction)
- "Without loss of generality..." (symmetry argument)
- "QED" or a box symbol marks the end of a proof.
