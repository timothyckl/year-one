"""
maze/generator.py

generates a random imperfect maze on a 2d grid using a two-phase approach:

phase 1 — spanning tree:
  uses a randomised depth-first search (recursive backtracker) algorithm.
  the algorithm uses an edge-based wall model:
    1. all cells start open (no obstacles); every cell's four sides are
       initialised as closed walls.
    2. a recursive dfs carves the maze by removing the shared wall between
       the current cell and an unvisited neighbour one step away.
    3. because every cell is visited exactly once and all cells begin
       connected via wall removal, the result is a perfect (spanning-tree)
       maze with guaranteed full connectivity.

phase 2 — loop carving:
  a random subset of remaining closed internal walls are opened to introduce
  cycles into the spanning tree. this produces an imperfect maze where
  multiple routes between cells may exist, allowing weighted shortest-path
  algorithms (dijkstra) to find genuinely different routes
  from unweighted search (bfs).

no cell is ever marked as an obstacle; graph.py reads the walls dict on
each node to determine which edges are passable.

uses true python recursion (sys.setrecursionlimit adjusted per grid size).
the maximum dfs depth for a 30x30 grid is 900 frames; the limit is set
to rows * cols * 2 + 500 to give comfortable headroom.
uses random.shuffle and random.random from the standard library for randomness.
"""

import sys
import random


# maps (delta_row, delta_col) to the wall removed on the current cell
# and the wall removed on the neighbouring cell when moving in that direction
_WALL_PAIR = {
    (-1, 0): ('N', 'S'),   # moving north removes N wall here, S wall on neighbour
    ( 1, 0): ('S', 'N'),   # moving south removes S wall here, N wall on neighbour
    ( 0,-1): ('W', 'E'),   # moving west  removes W wall here, E wall on neighbour
    ( 0, 1): ('E', 'W'),   # moving east  removes E wall here, W wall on neighbour
}

# fraction of remaining closed internal walls that are randomly opened after
# the spanning tree is carved. 0.0 = perfect maze; 0.15 gives roughly 2–5
# extra connections on a 10×10 grid, enough to create genuine route choices.
LOOP_DENSITY = 0.15


class MazeGenerator:
    """
    generates a random imperfect maze on a 2d grid using a two-phase approach:
    phase 1 carves a spanning tree via randomised dfs; phase 2 opens a random
    subset of remaining walls to introduce cycles.
    operates on all cells — no cell becomes an obstacle.
    guarantees full connectivity between all cells via wall carving.

    args:
        grid : 2d list of Node objects (modified in place)
        rows : grid row count
        cols : grid column count
    """

    # one-step cardinal directions: (delta_row, delta_col)
    DIRECTIONS = [(-1, 0), (1, 0), (0, -1), (0, 1)]

    def __init__(self, grid: list, rows: int, cols: int) -> None:
        self.grid = grid
        self.rows = rows
        self.cols = cols

    def generate(self) -> None:
        """
        runs the two-phase imperfect maze generation.

        phase 1 — spanning tree:
          modifies grid in place: closes all walls, then carves passages by
          opening shared walls between adjacent cells via a randomised dfs.
          adjusts sys.setrecursionlimit to safely handle grids up to 30x30.
          no cell is ever marked is_obstacle=True.

        phase 2 — loop carving:
          opens a random subset of remaining closed internal walls to introduce
          cycles, producing an imperfect maze with genuine route choices for
          weighted shortest-path algorithms.
        """
        # raise recursion limit: max dfs depth = rows * cols (every cell visited once)
        sys.setrecursionlimit(max(1000, self.rows * self.cols * 2 + 500))

        # step 1: open all cells and close all four walls on every cell
        for row in range(self.rows):
            for col in range(self.cols):
                node = self.grid[row][col]
                node.is_obstacle = False
                node.state = 'unvisited'
                node.walls = {'N': True, 'S': True, 'E': True, 'W': True}

        # step 2: carve passages via recursive dfs from (0, 0).
        # starting from a fixed corner guarantees the entire grid is carved
        # into one connected spanning tree.
        visited = set()
        self._carve_passages(0, 0, visited)

        # phase 2: carve additional passages to introduce cycles
        self._carve_loops()

    def _carve_loops(self) -> None:
        """
        opens a random subset of remaining closed internal walls to introduce
        cycles into the spanning-tree maze, producing an imperfect maze.

        iterates only the East and South wall of each cell to ensure each
        shared wall is evaluated exactly once. each qualifying wall is removed
        with probability LOOP_DENSITY.
        """
        for row in range(self.rows):
            for col in range(self.cols):
                # -- east wall: shared between (row, col) and (row, col+1) --
                if col + 1 < self.cols:
                    if self.grid[row][col].walls['E'] and random.random() < LOOP_DENSITY:
                        self._remove_wall(row, col, row, col + 1)

                # -- south wall: shared between (row, col) and (row+1, col) --
                if row + 1 < self.rows:
                    if self.grid[row][col].walls['S'] and random.random() < LOOP_DENSITY:
                        self._remove_wall(row, col, row + 1, col)

    def _remove_wall(self, row: int, col: int,
                     next_row: int, next_col: int) -> None:
        """
        removes the shared wall between two adjacent cells by opening
        the appropriate wall key on each cell.

        args:
            row      (int): row of the current cell
            col      (int): column of the current cell
            next_row (int): row of the neighbouring cell
            next_col (int): column of the neighbouring cell
        """
        delta = (next_row - row, next_col - col)
        wall_here, wall_there = _WALL_PAIR[delta]
        self.grid[row][col].walls[wall_here] = False
        self.grid[next_row][next_col].walls[wall_there] = False

    def _carve_passages(self, row: int, col: int, visited: set) -> None:
        """
        recursively carves passages using the recursive backtracker algorithm.
        marks the current cell visited, then visits each unvisited neighbour
        one step away in a random order, removing the shared wall.
        backtracks automatically when all neighbours are visited (via call stack).

        args:
            row     (int): row of the current cell being carved from
            col     (int): column of the current cell being carved from
            visited (set): set of (row, col) tuples already visited by the carver
        """
        visited.add((row, col))

        # shuffle directions so each run produces a different maze layout
        directions = list(self.DIRECTIONS)
        random.shuffle(directions)

        for delta_row, delta_col in directions:
            next_row = row + delta_row
            next_col = col + delta_col

            # bounds check: neighbour must be within the grid
            if not (0 <= next_row < self.rows and 0 <= next_col < self.cols):
                continue

            if (next_row, next_col) in visited:
                continue

            # remove the wall between current cell and unvisited neighbour
            self._remove_wall(row, col, next_row, next_col)

            # recurse into the neighbour; backtrack automatically on return
            self._carve_passages(next_row, next_col, visited)


if __name__ == '__main__':
    import sys
    import os
    # allow running from the maze/ directory or the project root
    sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

    from data_structures.node import Node

    rows, cols = 5, 5
    grid = [[Node(r, c) for c in range(cols)] for r in range(rows)]

    gen = MazeGenerator(grid, rows, cols)
    gen.generate()

    # no cell should be an obstacle
    for r in range(rows):
        for c in range(cols):
            assert not grid[r][c].is_obstacle, f"cell ({r},{c}) must not be obstacle"

    # every cell should have been given walls (all start True, some carved open)
    # verify at least some walls were removed (not all sides closed on every cell)
    all_closed = all(
        all(grid[r][c].walls[k] for k in 'NSEW')
        for r in range(rows) for c in range(cols)
    )
    assert not all_closed, "generator must carve at least one wall open"

    # verify full connectivity via bfs from (0,0) using walls as the edge filter
    from collections import deque as _deque  # allowed in self-test only
    visited_bfs = set()
    queue = _deque([(0, 0)])
    visited_bfs.add((0, 0))
    _DIR_MAP = {(-1, 0): 'N', (1, 0): 'S', (0, -1): 'W', (0, 1): 'E'}
    while queue:
        r, c = queue.popleft()
        for dr, dc in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            nr, nc = r + dr, c + dc
            if not (0 <= nr < rows and 0 <= nc < cols):
                continue
            if (nr, nc) in visited_bfs:
                continue
            if not grid[r][c].walls[_DIR_MAP[(dr, dc)]]:
                visited_bfs.add((nr, nc))
                queue.append((nr, nc))

    assert len(visited_bfs) == rows * cols, \
        f"maze must be fully connected; only reached {len(visited_bfs)} of {rows * cols} cells"

    print("generator.py: all tests passed")
