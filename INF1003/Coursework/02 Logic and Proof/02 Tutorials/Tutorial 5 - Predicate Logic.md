# Tutorial 5: Predicate Logic

## Skills Tested

- Evaluating predicate expressions over given domains.
- Determining the truth value of quantified statements.
- Negating quantified expressions (flipping quantifiers).
- Translating English sentences into predicate logic with quantifiers.
- Working with nested quantifiers and understanding scope.
- Determining whether arguments in predicate logic are valid.

## Suggested Approach

1. **Domain check:** Always identify the domain (universe of discourse) first.
   The truth of quantified statements depends on the domain.
2. **Existential:** "There exists" means find at least one element that works.
   To prove true, exhibit an example. To prove false, show no element works.
3. **Universal:** "For all" means check every element. To prove true, use a
   general argument. To prove false, provide one counterexample.
4. **Negation:**
   - $\neg(\forall x \, P(x)) \equiv \exists x \, \neg P(x)$
   - $\neg(\exists x \, P(x)) \equiv \forall x \, \neg P(x)$
   - Push negation all the way in through nested quantifiers.
5. **Translation patterns:**
   - "All P are Q": $\forall x \, (P(x) \to Q(x))$
   - "Some P are Q": $\exists x \, (P(x) \land Q(x))$
   - "No P are Q": $\forall x \, (P(x) \to \neg Q(x))$
   - "Not all P are Q": $\exists x \, (P(x) \land \neg Q(x))$
6. **Nested quantifiers:** Read left to right. $\forall x \exists y \, P(x, y)$ means
   "for each $x$ there is some $y$" — $y$ can depend on $x$. $\exists y \forall x \, P(x, y)$
   means "there is a single $y$ that works for all $x$".

## Common Pitfalls

- Using $\land$ with $\forall$: "All cats are black" is $\forall x \, (\mathrm{Cat}(x) \to \mathrm{Black}(x))$,
  NOT $\forall x \, (\mathrm{Cat}(x) \land \mathrm{Black}(x))$.
- Using $\to$ with $\exists$: "Some cat is black" is $\exists x \, (\mathrm{Cat}(x) \land \mathrm{Black}(x))$,
  NOT $\exists x \, (\mathrm{Cat}(x) \to \mathrm{Black}(x))$.
- Negating nested quantifiers incompletely — every quantifier must flip.
- Reversing the order of mixed quantifiers changes meaning.
