# Topic 2: Number Theory

## Overview

Number theory studies the properties of integers. Key topics include
divisibility, modular arithmetic, prime numbers, greatest common divisors,
and the Euclidean algorithm. These concepts motivate applications such as
RSA encryption.

---

## Division

For integers $a$ and $b$ with $a \ne 0$: **$a$ divides $b$** (written
$a \mid b$) if there exists an integer $c$ such that $b = ac$.

**Division Algorithm:** For any integer $a$ and positive integer $d$, there
exist unique integers $q$ (quotient) and $r$ (remainder) such that:
$a = dq + r$, where $0 \le r < d$.

- $a \mathbin{\mathrm{div}} d = q$ (integer division)
- $a \mathbin{\mathrm{mod}} d = r$ (remainder)

---

## Modular Arithmetic

For integers $a$, $b$ and positive integer $m$: $a$ is **congruent** to $b$
modulo $m$, written $a \equiv b \pmod{m}$, if $m$ divides $(a - b)$.

**Properties:**
- Addition: if $a \equiv b \pmod{m}$ and $c \equiv d \pmod{m}$ then
  $a + c \equiv b + d \pmod{m}$
- Multiplication: if $a \equiv b \pmod{m}$ and $c \equiv d \pmod{m}$ then
  $ac \equiv bd \pmod{m}$

**Exponentiation rule:** Reduce the base modulo $m$ before exponentiation:
$a^b \bmod m = ((a \bmod m)^b) \bmod m$.

---

## Prime Numbers

A positive integer $p > 1$ is **prime** if its only positive divisors are $1$
and $p$. Otherwise it is **composite**. $1$ is neither prime nor composite.

**Fundamental Theorem of Arithmetic:** Every integer $n > 1$ can be expressed
uniquely (up to ordering) as a product of primes.

**Infinitude of primes:** There are infinitely many primes.

---

## Greatest Common Divisor (GCD)

The GCD of integers $a$ and $b$ (not both zero) is the largest integer $d$
such that $d \mid a$ and $d \mid b$. Denoted $\gcd(a, b)$.

**Coprime:** $a$ and $b$ are coprime (relatively prime) if
$\gcd(a, b) = 1$.

**Least Common Multiple (LCM):** the smallest positive integer that is a
multiple of both $a$ and $b$.

**Relationship:** $ab = \gcd(a, b) \cdot \operatorname{lcm}(a, b)$.

From prime factorisations, the GCD takes the minimum exponent of each prime,
while the LCM takes the maximum exponent of each prime.

---

## Euclidean Algorithm

Computes $\gcd(a, b)$ efficiently using repeated application of the division
algorithm.

```
while b != 0:
    r = a mod b
    a = b
    b = r
return |a|
```

**Bezout's Identity:** There exist integers $s$, $t$ such that
$\gcd(a, b) = sa + tb$.

The **Extended Euclidean Algorithm** finds $s$ and $t$.

---

## Common Mistakes

- Confusing $a \mid b$ with $\frac{b}{a}$ — "a divides b" means
  $\frac{b}{a}$ is an integer.
- Forgetting that $1$ is not prime.
- Using the Euclidean algorithm with negative inputs incorrectly; $\gcd$ is
  always reported as positive.
- Mixing up the remainder condition: for positive $d$, $0 \le r < d$.
