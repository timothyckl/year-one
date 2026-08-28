# Big O Notation - Study Notes

## Contents
1. What Big O describes
2. Formal definitions (big O)
3. Common complexity classes
4. Rules of thumb (sums, log bases)
5. The five Landau notations
6. Performance vs complexity
7. Basic operations and problem size
8. Worst-case analysis
9. Formal definition used in CS
10. Analysing code (sequences, if-else, loops, nested loops, calls)

---

## 1. What Big O describes

- Big O notation (capital letter O, not zero), also called Landau's symbol,
  is a symbolism used in complexity theory, computer science, and mathematics
  to describe the asymptotic behavior of functions - basically how fast a
  function grows or declines.
- Named after German number theorist Edmund Landau. The letter O is used
  because the rate of growth of a function is also called its "order".
- Example: if an algorithm on a problem of size n takes
  `T(n) = 4n^2 - 2n + 2` steps, we ignore constants (they depend on the
  particular hardware) and slower-growing terms, and say
  `T(n) = O(n^2)` - "T(n) grows at the order of n squared".
- In mathematics it is also used for error terms, e.g.
  `e^x = 1 + x + x^2/2 + O(x^3)` for x near 0, meaning the error is smaller
  than some constant times x^3 when x is close enough to 0.

## 2. Formal definitions

- For functions f(x), g(x) defined on a subset of the reals:
  `f(x) = O(g(x))` (as x -> infinity) iff there exist constants N and C such
  that `|f(x)| <= C * |g(x)|` for all x > N.
  Intuitively: f does not grow faster than g.
- For x -> a (a a real number): `f(x) = O(g(x))` iff there exist constants
  d > 0 and C such that `|f(x)| <= C * |g(x)|` for all x with |x - a| < d.
- The first (infinity) definition is the only one used in computer science,
  where typically only positive functions with a natural number n as argument
  are considered, so absolute values can be ignored.

## 3. Common complexity classes

Ordered from slower-growing to faster-growing (c is an arbitrary constant):

| Notation      | Name           |
|---------------|----------------|
| O(1)          | constant       |
| O(log n)      | logarithmic    |
| O((log n)^c)  | polylogarithmic|
| O(n)          | linear         |
| O(n^2)        | quadratic      |
| O(n^c)        | polynomial     |
| O(c^n)        | exponential    |

Key points:
- O(n^c) and O(c^n) are very different. O(c^n) grows much, much faster no
  matter how large the constant c is.
- Superpolynomial: grows faster than any power of n.
- Subexponential: grows slower than an exponential function c^n.
- An algorithm can be both superpolynomial and subexponential (e.g. the
  fastest known algorithms for integer factorization).

## 4. Rules of thumb

- `O(log n)` is exactly the same as `O(log(n^c))`: logarithms differ only by
  a constant factor, which Big O ignores. Logs with different constant bases
  are equivalent.
- If f(n) is a sum of functions and one grows faster than the others, that
  fastest one determines the order.
  Example: `f(n) = 10 log(n) + 5(log n)^3 + 7n + 3n^2 + 6n^3`, so
  `f(n) = O(n^3)`.
- Caveat: the number of summands must be constant and may not depend on n.
- The notation also works with multiple variables, e.g.
  `f(n,m) = n^2 + m^3 + O(n + m)`.

Notes on sloppiness:
- Using "=" is technically abusing the equality symbol; some authors define
  O(g(x)) as a set of functions and use set membership. The "=" form is more
  common at present.
- Sometimes the parameter whose behavior is examined is not clear
  (e.g. f(x,y) = O(g(x,y))), though this is rare in practice.

## 5. The five Landau notations

- Little o: informally `f(x) = o(g(x))` means f grows much slower than g and
  is insignificant in comparison. Formally: for every C > 0 there exists N
  such that for all x > N, `|f(x)| < C * |g(x)|`; if g(x) != 0 this is
  equivalent to `lim(x->inf) f(x)/g(x) = 0`.
- The five comparison notations:

| Notation         | Definition                    | Analogy |
|------------------|-------------------------------|---------|
| f(n) = O(g(n))   | grows no faster than          | <=      |
| f(n) = o(g(n))   | grows much slower than        | <       |
| f(n) = Omega(g(n))| g(n) = O(f(n))                | >=      |
| f(n) = omega(g(n))| g(n) = o(f(n))                | >       |
| f(n) = Theta(g(n))| f(n)=O(g(n)) and g(n)=O(f(n)) | =       |

- O and Omega are often used in computer science; little o is common in
  mathematics but rare in CS; lowercase omega is rarely used.
- Common error: using O when Theta is meant. E.g. saying "heapsort is
  O(n log n)" when the intent was "heapsort is Theta(n log n)". Both are
  true, but the Theta claim is stronger (Theta means it is bounded both
  above and below).

## 6. Performance vs complexity

- Efficiency covers lots of resources: CPU (time), memory, disk, network.
  All are important, but the focus here is mostly on time complexity
  (CPU usage).
- Performance: how much time/memory/disk/... is actually used when a program
  is run. Depends on the machine, compiler, etc., as well as the code.
- Complexity: how the resource requirements of a program or algorithm scale
  as the size of the problem being solved gets larger.
- Complexity affects performance, but not the other way around.

## 7. Basic operations and problem size

- The time required by a function/procedure is proportional to the number of
  "basic operations" it performs. Examples of basic operations:
  - one arithmetic operation (e.g. +, *)
  - one assignment (e.g. x := 0)
  - one test (e.g. x = 0)
  - one read of a primitive type (integer, float, character, boolean)
  - one write of a primitive type (integer, float, character, boolean)
- Some functions always perform the same number of operations (e.g.
  StackSize always returns the current count or states it is empty) - that is
  constant time.
- Other functions perform a different number of operations depending on a
  parameter; the parameter that drives the count is the problem size / input
  size. Example: in BubbleSort, the number of elements in the array.
- We are not interested in the exact number of operations, but in the
  relation of the number of operations to the problem size.

## 8. Worst-case analysis

- Typically we are interested in the worst case: the maximum number of
  operations that might be performed for a given problem size.
- Example: inserting an element into an array requires moving the current
  element and everything after it. In the worst case (inserting at the
  beginning) all elements must be moved, so the worst-case time for insertion
  is linear in the number of elements in the array.
- For a linear-time algorithm, if the problem size doubles, the number of
  operations also doubles.

## 9. Formal definition used in CS

- We express complexity using Big-O. For a problem of size N:
  - constant-time algorithm is "order 1": O(1)
  - linear-time algorithm is "order N": O(N)
  - quadratic-time algorithm is "order N squared": O(N^2)
- Big-O expressions do not have constants or low-order terms, because when N
  gets large enough they do not matter (constant-time beats linear beats
  quadratic).
- Formal: a function T(N) is O(F(N)) if for some constant c and for values of
  N greater than some value n0: `T(N) <= c * F(N)`.
  T(N) is the exact complexity as a function of problem size N; F(N) is an
  upper bound on that complexity (actual time/space for size N will be no
  worse than F(N)).
- In practice we want the smallest F(N) - the least upper bound.
- Worked example: `T(N) = 3*N^2 + 5`. It is O(N^2): choose c = 4 and n0 = 2,
  because for all N > 2, `3*N^2 + 5 <= 4*N^2`. It is not O(N): whatever c and
  n0 you choose, there is always a value of N > n0 such that
  `(3*N^2 + 5) > (c * N)`.

## 10. Analysing code

General approach: the running time depends on what kinds of statements are
used.

1. Sequence of statements - total time is the sum of the times of each
   statement. If each statement is simple (only basic operations), each is
   constant time and the total is O(1).

2. If-then-else - only one of the two blocks executes, so the worst-case time
   is the slower of the two possibilities:
   `max(time(block1), time(block2))`.
   Example: if block1 is O(1) and block2 is O(N), the if-then-else is O(N).

3. For loop `for I in 1..N` - executes N times. If the body is O(1), the
   total is N * O(1) = O(N).

4. Nested loops - outer executes N times, inner executes M times each time,
   so the inner statements execute N * M times total: O(N * M). In the common
   special case where the inner loop also runs N times (same bound), the
   total is O(N^2).

5. Statements with function/procedure calls - the complexity of the statement
   includes the complexity of the called function/procedure.
   - `f(k)` with f constant time: O(1).
   - `g(k)` with g linear in its parameter k: O(k).
   - Inside a loop: `for J in 1..N loop g(J)` has complexity O(N^2), because
     the loop runs N times and each call g(J) is O(N).
