# Final Examination Revision

**Weight:** 50%
**Coverage:** All topics
**Timing:** Week 14
**Format:** Written examination

## Topics Covered

1. Sequences and Summation
2. Number Theory
3. Combinatorics
4. Propositional Logic
5. Predicate Logic
6. Rules of Inference
7. Proof Methods
8. Set Theory
9. Functions
10. Relations
11. Guest lecture / special topics

The archived assessment table states that the examination covers "all topics."
The weekly plan includes Topic 11 but does not provide more detailed examination
scope for the guest lecture, so its status should be confirmed against the
current examination instructions.

## Revision Strategy

### Phase 1: Topic Review (by conceptual block)

1. **Mathematical Foundations** (Topics 1--3):
   - AP/GP formulas, summation notation, compound interest
   - Divisibility, modular arithmetic, Euclidean algorithm, primes
   - Counting rules, $P(n,r)$, $C(n,r)$, pigeonhole principle
   - Review: lecture notes + [Tutorials 1--3](../../../Notes/README.md) + revision summary

2. **Logic and Proof** (Topics 4--7):
   - Propositional logic: truth tables, equivalences, De Morgan
   - Predicate logic: quantifiers, negation, translation, nesting
   - Rules of inference: all 8 propositional rules, predicate rules, fallacies
   - Proof methods: direct, contrapositive, contradiction, cases, existence, uniqueness
   - Review: lecture notes + [Tutorials 4--7](../../../Notes/README.md) + revision summary

3. **Discrete Structures** (Topics 8--10):
   - Set theory: operations, identities, power sets, Cartesian products
   - Functions: injectivity, surjectivity, bijectivity, sum/product,
     composition, inverses
   - Relations: properties (reflexive, symmetric, antisymmetric, transitive), equivalence relations and classes
   - Review: lecture notes + [Tutorials 8--10](../../../Notes/README.md) + revision summary

### Phase 2: Gap Analysis

- Compare your performance across all ten [tutorials](../../../Notes/README.md).
- Identify topics with lower scores or less confidence.
- Revisit those lectures and tutorials, and work additional problems.
- Use the reference guides for formula memorisation.

### Phase 3: Timed Practice

- Work through the full [tutorial set](../../../Notes/README.md) under timed conditions.
- Practice writing proofs with clear structure.
- Memorise all key formulas and identities.

## Key Formulas and Identities to Memorise

### Sequences and Sums
- $S_n(\text{AP}) = \frac{n}{2}(2a + (n-1)d)$
- $S_n(\text{GP}) = \frac{a(1-r^n)}{1-r}$, $S_\infty = \frac{a}{1-r}$ for $|r| < 1$
- $\sum_{k=1}^{n}k = \frac{n(n+1)}{2}$,
  $\sum_{k=1}^{n}k^2 = \frac{n(n+1)(2n+1)}{6}$

### Combinatorics
- $P(n, r) = \frac{n!}{(n-r)!}$, $C(n, r) = \frac{n!}{r!(n-r)!}$
- $C(n, r) = C(n, n-r)$

### Logic
- $p \to q \equiv \neg p \lor q$, $p \to q \equiv \neg q \to \neg p$
- De Morgan: $\neg(p \land q) \equiv \neg p \lor \neg q$, $\neg(p \lor q) \equiv \neg p \land \neg q$

### Inference
- 8 propositional rules, 4 predicate rules
- Common fallacies: affirming consequent, denying antecedent

### Set Theory
- $|\mathcal{P}(A)| = 2^{|A|}$, $|A \times B| = |A| \cdot |B|$
- All 10 types of set identity laws (De Morgan, distributive, absorption, etc.)

### Functions
- Injective: $f(x) = f(y) \Rightarrow x = y$
- Surjective: For all $y$, exists $x$, $f(x) = y$
- Composition: $(g \circ f)(x) = g(f(x))$

### Relations
- $\text{equivalence relation} = \text{reflexive} + \text{symmetric} + \text{transitive}$
- Classes partition the set

## Exam Technique

- Read all questions first; start with the ones you are most confident about.
- For proofs: state the method, write the assumption clearly, show each step.
- For calculations: show working; partial credit is available.
- For truth tables: be systematic, $2^n$ rows for $n$ variables.
- For set identity proofs: use element arguments or known identities.
- Check your answers: does the result make sense? Are there counterexamples?
