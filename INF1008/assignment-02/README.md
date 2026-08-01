# Graph Search Algorithm Visualiser

---

## Table of Contents

1. [Project Description](#project-description)
2. [Tech Stack](#tech-stack)
3. [Libraries Used](#libraries-used)
4. [Project Structure](#project-structure)
5. [Data Structures Used](#data-structures-used)
6. [Algorithms Used](#algorithms-used)
7. [Node Colour Reference](#node-colour-reference)
8. [Maze Generation](#maze-generation)
9. [Usage Flow](#usage-flow)
10. [Performance Metrics Collected](#performance-metrics-collected)

---

## Project Description

This project implements an interactive grid-based application that visualises and benchmarks two fundamental graph search algorithms: **Breadth-First Search (BFS) and Dijkstra's Algorithm**.

BFS finds the shortest path by hop count on unweighted graphs. Dijkstra's algorithm is a generalisation of BFS to weighted graphs — it reduces to BFS when all edge weights are equal. This relationship makes the two algorithms a natural pair for direct comparison: same guarantee (optimal path), different cost model.

The system models a 2D grid as a graph where each cell represents a vertex and edges connect adjacent cells. Users can configure the grid size at runtime, define start and goal nodes, generate a random maze, and assign edge weights.

The application visualises node exploration in real time using the Turtle graphics library and records performance metrics including:

* Number of nodes visited
* Path length
* Path cost
* Execution time

This enables direct comparison of traversal behaviour and computational complexity across different search strategies.

---

## Tech Stack

* **Language:** Python 3.x
* **Visualisation:** `turtle` (standard library)
* **Timing & Metrics:** `time` module

---

## Libraries Used

Only Python standard libraries:

* `turtle` → grid visualisation and animation
* `time` → execution time measurement
* `random` → maze generation

No external libraries.

---

## Project Structure

```
./
├── main.py
│
├── algorithms/
│   ├── __init__.py
│   ├── base.py
│   ├── utils.py
│   ├── bfs.py
│   └── dijkstra.py
│
├── data_structures/
│   ├── __init__.py
│   ├── graph.py
│   ├── node.py
│   ├── queue.py
│   └── priority_queue.py
│
├── maze/
│   ├── __init__.py
│   └── generator.py
│
├── visualization/
│   ├── __init__.py
│   ├── grid_renderer.py
│   └── animation.py
│
└── tests/
    ├── conftest.py
    ├── test_algorithms.py
    ├── test_graph.py
    ├── test_node.py
    ├── test_priority_queue.py
    └── test_queue.py
```

---

## Data Structures Used

The two algorithms under evaluation are **BFS** (using the Queue) and **Dijkstra's Algorithm** (using the Priority Queue). Both rely on the Graph for traversal and on Distance & Visited Tracking for correctness.

### 1. Graph Representation

* **Adjacency List** (models the grid; shared by both algorithms)
* Implemented using `dict[(row, col)] → list[(neighbour, weight)]`
* Efficient for sparse grids
* Time complexity:
  * Space: O(V + E)
  * Traversal: O(V + E)

---

### 2. Queue (for BFS)

* **FIFO structure** (drives the BFS frontier expansion)
* Implemented manually using a list with a **front pointer**
* The front pointer advances on dequeue rather than shifting elements — this gives true O(1) dequeue without list reallocation
* Operations:
  * enqueue → O(1)
  * dequeue → O(1)

---

### 3. Priority Queue (for Dijkstra)

* **Min-heap** (orders nodes by tentative distance for Dijkstra's greedy selection)
* Uses a **lazy-deletion** pattern: stale entries for a node are left in the heap and skipped when extracted, so no decrease-key operation is required
* Operations:
  * insert → O(log n)
  * extract_min → O(log n)

---

### 4. Distance & Visited Tracking

* **Dictionary and Set** (maintain per-node state for both algorithms)
* Dictionary for distance mapping
* Set for visited / settled nodes

---

## Algorithms Used

### 1. Breadth-First Search (BFS)

* Used for shortest path in unweighted grids
* Data structure: Queue
* Time Complexity: **O(V + E)**
* Guarantees shortest path when all edges have equal weight

**Pseudocode**
```
mark start in parent; enqueue start
while frontier not empty:
    dequeue current; increment nodes_visited
    if current == goal: return reconstruct_path()
    for each neighbour not in parent:
        set parent[neighbour]; enqueue; fire 'frontier' callback
return no path
```

---

### 2. Dijkstra's Algorithm

* Used for shortest path in weighted grids
* Data structure: Min-Heap Priority Queue (lazy deletion)
* Time Complexity: **O((V + E) log V)**
* Greedy relaxation strategy; cannot handle negative weights

**Pseudocode**
```
dist[start] = 0; insert (0, start) into pq
while pq not empty:
    extract (d, current) with minimum d
    if current in settled: skip (lazy deletion)
    add current to settled; increment nodes_visited
    if current == goal: return reconstruct_path()
    for each unsettled neighbour:
        new_dist = d + edge_weight
        if new_dist < dist[neighbour]:
            update dist; set parent; insert into pq
return no path
```

Both algorithms implement the `Algorithm` abstract base class defined in `algorithms/base.py`, which enforces a common `run()` interface and enables the application to depend on the abstraction rather than concrete implementations.

---

## Node Colour Reference

| State    | Colour           | Hex       | Meaning                                           |
|----------|------------------|-----------|---------------------------------------------------|
| Unvisited | White           | `#ffffff` | Open corridor; not yet reached                    |
| Obstacle  | Near-black      | `#1a1a1a` | Wall; blocked cell                                |
| Frontier  | Vivid cyan      | `#22c5e8` | Queued / discovered but not yet explored          |
| Visited   | Deep navy       | `#0b4f6c` | Fully explored by the algorithm                   |
| Start     | Vivid green     | `#00c853` | Algorithm origin                                  |
| Goal      | Vivid orange-red| `#ff3d00` | Algorithm destination                             |
| Path      | Amber-yellow    | `#ffd600` | Final shortest / found path from start to goal    |

---

## Maze Generation

Maze generation runs in two phases when the user presses **M**.

### Phase 1 — Spanning Tree (Randomised DFS Backtracker)

Starting at cell (0, 0), all cell walls are initially closed. The generator uses a recursive DFS (via Python's call stack) to carve passages to unvisited neighbours in random order. Every cell is visited exactly once, producing a **perfect maze** — a spanning tree with no cycles and exactly one path between any two cells.

### Phase 2 — Loop Carving

Each remaining closed internal wall is removed with probability `LOOP_DENSITY = 0.15`. This introduces cycles and multiple routes, making the maze **imperfect**.

**Why imperfect?** With multiple routes available, Dijkstra can find a genuinely different optimal path from BFS's shortest-hop route, demonstrating the difference between weighted and unweighted shortest-path search.

> **Note:** No cell is ever marked `is_obstacle`. The maze is expressed entirely through the edge-based `walls` dictionary on each `Node`. Obstacles exist conceptually as closed walls, not as filled cells.

---

## Usage Flow

1. **Launch** the application:
   ```
   python main.py
   ```

2. Enter the number of **rows** (3–30) and **columns** (3–30) in the dialog boxes. Press **T** at any point to toggle between weighted and unweighted mode.

3. Press **M** to generate a random maze. Skip this step to work on an open grid.

4. Press **S**, then click a cell to set the **start node** (green).

5. Press **G**, then click a cell to set the **goal node** (orange-red).

6. *(Weighted mode only)* Press **W**, click a cell, then enter a weight value (1–9) in the dialog.

7. Press `1` or `2` to run an algorithm:

   | Key | Algorithm  |
   |-----|------------|
   | `1` | BFS        |
   | `2` | Dijkstra   |

8. Press **R** to re-run the last algorithm on the current grid state.

9. Press **C** to clear the entire grid and reset to the initial state.

10. Press **+** or **-** to increase or decrease the animation speed.

### Key Binding Summary

| Key      | Action                          |
|----------|---------------------------------|
| `S`      | Enter start-placement mode      |
| `G`      | Enter goal-placement mode       |
| `W`      | Enter weight-assignment mode    |
| `T`      | Toggle weighted/unweighted mode |
| `M`      | Generate a random maze          |
| `1`      | Run BFS                         |
| `2`      | Run Dijkstra                    |
| `R`      | Re-run last algorithm           |
| `C`      | Clear grid                      |
| `+` / `=`| Speed up animation             |
| `-`      | Slow down animation             |

---

## Performance Metrics Collected

For each algorithm run:

* Total nodes explored
* Final path length
* Path cost
* Total execution time (milliseconds)
* Whether a path was found

After each run, a history entry is added to the **Run History** panel on the right side of the screen. Each entry shows the algorithm name, path length, nodes visited, and execution time. The history persists across runs until the grid is cleared.
