# Topic 6: Rules of Inference

## Overview

Rules of inference are valid argument forms that allow deriving conclusions
from premises. An argument is valid if the conclusion must be true whenever all
premises are true. Rules exist for both propositional and predicate logic.

---

## Propositional Rules of Inference

| Rule | Form | Name |
|---|---|---|
| Modus ponens | $p \to q$, $p \therefore q$ | Affirming the antecedent |
| Modus tollens | $p \to q$, $\neg q \therefore \neg p$ | Denying the consequent |
| Hypothetical syllogism | $p \to q$, $q \to r \therefore p \to r$ | Transitivity of implication |
| Disjunctive syllogism | $p \lor q$, $\neg p \therefore q$ | Eliminating a disjunct |
| Addition | $p \therefore p \lor q$ | Introducing a disjunct |
| Simplification | $p \land q \therefore p$ | Eliminating a conjunct |
| Conjunction | $p$, $q \therefore p \land q$ | Combining assertions |
| Resolution | $p \lor q$, $\neg p \lor r \therefore q \lor r$ | Clause resolution |

---

## Common Fallacies

| Fallacy | Form | Why invalid |
|---|---|---|
| Affirming the consequent | $p \to q$, $q \therefore p$ | $q$ could be true for other reasons |
| Denying the antecedent | $p \to q$, $\neg p \therefore \neg q$ | $q$ could be true without $p$ |

---

## Predicate Logic Rules of Inference

**Universal instantiation:** From $\forall x \, P(x)$, infer $P(c)$ for any element $c$.

**Universal generalisation:** From $P(c)$ for an arbitrary element $c$, infer
$\forall x \, P(x)$. ($c$ must be arbitrary, not a specific element used elsewhere.)

**Existential instantiation:** From $\exists x \, P(x)$, infer $P(c)$ for some
element $c$. ($c$ must be a new constant not appearing elsewhere.)

**Existential generalisation:** From $P(c)$ for some element $c$, infer
$\exists x \, P(x)$.

---

## Combining Propositional and Predicate Rules

A typical proof sequence:
1. Express premises in predicate logic.
2. Instantiate universal or existential quantifiers.
3. Apply propositional rules (modus ponens, etc.) to the instantiated
   statements.
4. Generalise if needed to return to quantified form.

---

## Universal Modus Ponens

$\forall x \, (P(x) \to Q(x))$
$P(a)$ for a particular $a$
$\therefore Q(a)$

---

## Universal Modus Tollens

$\forall x \, (P(x) \to Q(x))$
$\neg Q(a)$ for a particular $a$
$\therefore \neg P(a)$

---

## Common Mistakes

- Confusing modus ponens with affirming the consequent.
- Instantiating an existential quantifier with a constant already in use.
- Trying to generalise from a specific element that was not arbitrary.
- Forgetting to check that a rule's premises are actually in hand before
  applying it.
