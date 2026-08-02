# Tutorial 2: Number Theory

## Skills Tested

- Determining whether one integer divides another.
- Applying the division algorithm to find quotient and remainder.
- Computing modular arithmetic expressions and simplifying congruences.
- Identifying primes and factorising integers.
- Computing GCD using the Euclidean algorithm.
- Computing LCM using the GCD-LCM relationship.
- Determining whether a collection of integers is pairwise relatively prime.
- Computing GCD and LCM from prime factorisations.

## Suggested Approach

1. **Divisibility:** $a \mid b$ means $b/a$ is an integer. Write $b = ak$ and
   solve for $k$.
2. **Division algorithm:** Given $a$ and $d > 0$, find $q = \lfloor a/d \rfloor$
   and $r = a - dq$. Check $0 \le r < d$.
3. **Modular arithmetic:** $a \bmod m$ is the remainder when $a$ is divided by
   $m$. In most programming contexts and this module, $n \bmod m$ yields a
   non-negative result. For negative $a$, add multiples of $m$ to get
   $0 \le r < m$.
4. **Primality testing:** Check divisors up to $\sqrt{n}$. If no divisor found,
   $n$ is prime.
5. **Euclidean algorithm:** Repeatedly replace $(a, b)$ with $(b, a \bmod b)$
   until $b = 0$. The last non-zero remainder is the GCD.
6. **Prime factorisation:** For the GCD, take the minimum exponent of each
   prime. For the LCM, take the maximum exponent of each prime.
7. **LCM relationship:** $\operatorname{lcm}(a, b) = \frac{ab}{\gcd(a, b)}$ for
   positive $a$ and $b$.

## Common Pitfalls

- Forgetting that the remainder must satisfy $0 \le r < d$ (non-negative).
- Computing $a \bmod m$ incorrectly for negative $a$ — add $m$ repeatedly until
  the result is in $[0, m-1]$.
- Using the Euclidean algorithm with $b > a$ on the first step — it still works,
  just takes one extra iteration.
- Confusing prime factorisation: $1$ has no prime factors, and every composite
  number has a unique prime factorisation.
