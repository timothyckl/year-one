# Tutorial 9: Functions

## Skills Tested

- Determining whether a rule defines a valid function.
- Computing images and preimages of elements.
- Proving a function is injective, surjective, or bijective.
- Disproving injectivity or surjectivity with counterexamples.
- Computing the sum and product of functions.
- Computing function composition.
- Working with floor and ceiling functions.

## Suggested Approach

1. **Function check:** Every domain element must map to exactly one codomain
   element. Check for ambiguities or undefined outputs.
2. **Injectivity proof:** Assume $f(x_1) = f(x_2)$. Manipulate algebraically
   to show $x_1 = x_2$.
3. **Surjectivity proof:** Take arbitrary $y$ in the codomain. Solve $f(x) = y$ for $x$.
   Show the solution exists and belongs to the domain.
4. **Disproving injectivity:** Find two distinct domain elements that produce
   the same output. For even functions like $f(x) = x^2$, try $x$ and $-x$.
5. **Disproving surjectivity:** Find a codomain element that no domain element
   can map to. For $f: \mathbb{R} \to \mathbb{R}$, $f(x) = x^2$, try $-1$ (no real $x$ maps to $-1$).
6. **Function operations:** Evaluate $(f+g)(x)$ by adding outputs and
   $(fg)(x)$ by multiplying outputs.
7. **Composition:** $(g \circ f)(x) = g(f(x))$. Apply the inner function first, then
   the outer function. Domain of $g \circ f$ is $\{x \in \mathrm{Dom}(f) \mid f(x) \in \mathrm{Dom}(g)\}$.
8. **Floor/ceiling:** $\lfloor x \rfloor$ is the greatest integer $\le x$. $\lceil x \rceil$ is the
   smallest integer $\ge x$. Use these for rounding and counting problems.

## Common Pitfalls

- Proving injectivity by assuming $x_1 = x_2$ (that is what you need to show,
  not what you assume).
- For surjectivity, finding an expression for $x$ but not verifying it is in
  the domain.
- Reversing composition: $(g \circ f)$ applies $f$ first, then $g$.
