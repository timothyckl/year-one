# Number Theory Reference

## Divisibility

- $a \mid b$ iff exists $c$ such that $b = ac$
- Division algorithm: $a = dq + r$, $0 \le r < |d|$
- $q = a \mathbin{\mathrm{div}} d$, $r = a \bmod d$

## Modular Arithmetic

- $a \equiv b \pmod{m}$ iff $m \mid (a-b)$
- $(a + b) \bmod m = ((a \bmod m) + (b \bmod m)) \bmod m$
- $(a \cdot b) \bmod m = ((a \bmod m) \cdot (b \bmod m)) \bmod m$
- $a^b \bmod m = ((a \bmod m)^b) \bmod m$

## Primes

- $p > 1$ is prime iff only divisors are $1$ and $p$
- Fundamental Theorem of Arithmetic: every $n > 1$ has unique prime
  factorisation
- Infinitely many primes

## GCD and LCM

- $\gcd(a, b)$: largest $d$ such that $d \mid a$ and $d \mid b$
- $\operatorname{lcm}(a, b)$: smallest positive $m$ such that $a \mid m$ and
  $b \mid m$
- $ab = \gcd(a, b) \cdot \operatorname{lcm}(a, b)$

From prime factorisations:
- GCD uses the minimum exponent of each prime.
- LCM uses the maximum exponent of each prime.

### Euclidean Algorithm

```
while b != 0:
    r = a mod b
    a = b
    b = r
return |a|
```

### Bezout's Identity

There exist integers $s$, $t$ such that $\gcd(a, b) = sa + tb$.

## Remainders

- $a \bmod m = a - m \cdot \lfloor a/m \rfloor$
