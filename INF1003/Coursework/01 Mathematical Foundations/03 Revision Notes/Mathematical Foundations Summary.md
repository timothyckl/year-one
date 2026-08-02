# Mathematical Foundations Summary

Quick-reference revision covering Topics 1--3. For detailed notes, see the
individual lecture pages.

---

## Topic 1: Sequences and Summation

### Sequences
- **AP:** $a_n = a + (n-1)d$, $S_n = \frac{n}{2}(2a + (n-1)d)$
- **GP:** $a_n = ar^{n-1}$, $S_n = \frac{a(1-r^n)}{1-r}$ for $r \ne 1$
- **Infinite GP ($|r| < 1$):** $S_{\infty} = \frac{a}{1-r}$
- **Compound interest (annual):** $A = P(1+r)^n$

### Summation
- $\sum_{k=1}^{n} k = \frac{n(n+1)}{2}$
- $\sum_{k=1}^{n} k^2 = \frac{n(n+1)(2n+1)}{6}$
- $\sum_{k=0}^{n} ar^k = \frac{a(r^{n+1} - 1)}{r - 1}$

### Key skills
- Telescoping: write out first and last few terms to spot cancellation
- Number of terms: $n - m + 1$

---

## Topic 2: Number Theory

### Core definitions
- $a \mid b$ iff $b = ak$ for some integer $k$
- Division algorithm: $a = dq + r$, $0 \le r < d$
- $a \equiv b \pmod{m}$ iff $m \mid (a-b)$
- $p$ is prime iff its only positive divisors are $1$ and $p$ ($p > 1$)

### Euclidean Algorithm
```
gcd(a, b):
  while b != 0: a, b = b, a mod b
  return |a|
```
- $\gcd(a, b) \cdot \operatorname{lcm}(a, b) = |ab|$
- Bezout: exists $s$, $t$ such that $\gcd(a, b) = sa + tb$

### Modular arithmetic
- $(a+b) \bmod m = ((a \bmod m) + (b \bmod m)) \bmod m$
- $(ab) \bmod m = ((a \bmod m)(b \bmod m)) \bmod m$
- Exponentiation: $a^b \bmod m = ((a \bmod m)^b) \bmod m$
- From prime factorisations, GCD uses minimum exponents and LCM uses maximum
  exponents

---

## Topic 3: Combinatorics

### Core rules
- **Product rule:** independent sequential steps -> multiply counts
- **Sum rule:** mutually exclusive cases -> add counts
- **Subtraction rule:** count complement: $|A| = |U| - |A^c|$
- **Division rule:** divide by $k$ if each outcome counted $k$ times

### Formulas
- $P(n, r) = \frac{n!}{(n-r)!}$ (order matters)
- $C(n, r) = \frac{n!}{r!(n-r)!}$ (order does not matter)

### Pigeonhole Principle
- $n$ items into $m$ boxes, $n > m$: at least one box has $\ge 2$ items
- Generalised: at least one box has $\ge \lceil n/m \rceil$ items

### Problem-solving pattern
1. Identify if order matters (permutations vs combinations)
2. Check for independence (product) or mutual exclusion (sum)
3. Watch for overcounting and correct with division
4. "At least one" -> use complement
