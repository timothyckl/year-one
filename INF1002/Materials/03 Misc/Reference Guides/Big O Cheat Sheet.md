# Big O - Quick Reference (Cheat Sheet)

See the full study notes in
[`Big O Notation Study Notes.md`](../../../Notes/Misc/Algorithms%20and%20Complexity/Big%20O%20Notation%20Study%20Notes.md) for details.

## Common complexity classes (slowest to fastest)

| Class            | Notation      | Typical pattern                       |
|------------------|---------------|---------------------------------------|
| constant         | O(1)          | single statement, fixed work          |
| logarithmic      | O(log n)      | halving each step (binary search)     |
| polylogarithmic  | O((log n)^c)  |                                    |
| linear           | O(n)          | one pass over n items                 |
| quadratic        | O(n^2)        | nested loops of n                     |
| polynomial       | O(n^c)        | c nested loops / exponent             |
| exponential      | O(c^n)        | grows much faster than any n^c        |

## The five notations

| Notation         | Meaning                | Analogy |
|------------------|------------------------|---------|
| O(g(n))          | grows no faster than   | <=      |
| o(g(n))          | grows much slower than | <       |
| Omega(g(n))      | grows at least as fast | >=      |
| omega(g(n))      | grows faster than      | >       |
| Theta(g(n))      | grows at the same rate | =       |

- Big O is the one used in most INF1002 work. Theta is the stronger claim.
- Theta(g) means f = O(g) AND g = O(f).

## Key rules

- Constants and low-order terms are dropped: T(n) = 4n^2 - 2n + 2 is O(n^2).
- `O(log n)` is the same regardless of log base, and O(log n) = O(log(n^c)).
- In a sum of terms, the fastest-growing term dominates:
  `f(n) = 10 log n + 5(log n)^3 + 7n + 3n^2 + 6n^3` is O(n^3).
  (The number of summands must be constant.)
- We usually analyse the worst case.

## Formal definition

T(N) is O(F(N)) if there exist constants c and n0 such that for all N > n0:
`T(N) <= c * F(N)`.
- Example: T(N) = 3N^2 + 5 is O(N^2) using c = 4, n0 = 2.
- T(N) = 3N^2 + 5 is NOT O(N) (grows too fast).

## Analysing code

| Construct          | Cost                                             |
|--------------------|--------------------------------------------------|
| sequence of k statements | sum of the times; O(1) if each is simple   |
| if (cond) A else B | max(time(A), time(B))                            |
| for i in 1..N      | N x body cost; O(N) if body is O(1)              |
| nested loops       | product of loop counts; O(N*M), or O(N^2) when equal |
| call f(k) (constant) | O(1)                                          |
| call g(k) (linear) | O(k)                                             |
| loop calling g(J) for J in 1..N | O(N^2)                            |

## Basic operations (each counts as one step)

- one arithmetic operation (+, *, ...)
- one assignment
- one test (comparison)
- one read or write of a primitive type (integer, float, character, boolean)

## Performance vs complexity

- Performance = actual time/memory used when run (depends on machine/compiler).
- Complexity = how resource use scales with problem size (machine-independent).
- Complexity affects performance; the reverse does not hold.
