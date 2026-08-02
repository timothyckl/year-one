# Relations Reference

## Definition

$R \subseteq A \times A$ (binary relation on $A$). $(a, b) \in R$: "$a$ is related to $b$".

## Representations

| Form | Description |
|---|---|
| Set of ordered pairs | $R = \{(a,b), (c,d), \ldots\}$ |
| Digraph | Nodes are $A$, with directed edge $a \to b$ for each $(a,b) \in R$ |
| Zero-one matrix | $M_{ij} = 1$ if $(i,j) \in R$, $0$ otherwise (for $A = \{1,\ldots,n\}$) |

## Properties

| Property | Definition | Matrix check | Digraph check |
|---|---|---|---|
| Reflexive | $(a,a) \in R$ for all $a \in A$ | Diagonal all 1s | Self-loop at every node |
| Symmetric | $(a,b) \in R \Rightarrow (b,a) \in R$ | $M = M^T$ | Every edge has reverse |
| Antisymmetric | $(a,b) \in R$ and $(b,a) \in R \Rightarrow a = b$ | $M_{ij}=1$, $i\ne j \Rightarrow M_{ji}=0$ | No pair of reverse edges |
| Transitive | $(a,b) \in R$ and $(b,c) \in R \Rightarrow (a,c) \in R$ | $M^2 \le M$ (Boolean) | Path of length 2 $\Rightarrow$ direct edge |

## Equivalence Relations

A relation $R$ on $A$ is an equivalence relation iff it is:
1. **Reflexive** — every element related to itself
2. **Symmetric** — if $a\,R\,b$ then $b\,R\,a$
3. **Transitive** — if $a\,R\,b$ and $b\,R\,c$ then $a\,R\,c$

### Equivalence Class

$[a]_R = \{b \in A \mid (a, b) \in R\}$

### Properties

- $[a] = [b]$ iff $(a, b) \in R$
- $[a] \ne [b] \Rightarrow [a]$ and $[b]$ are disjoint
- The set of all distinct equivalence classes partitions $A$

### Example

- Equality: $[a]=\{a\}$.
