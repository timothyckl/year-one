"""
tests/test_algorithms.py

pytest tests for the BFS and Dijkstra algorithm implementations.
covers path validity, shortest-path length, obstacle navigation,
unreachable goals, start-equals-goal, and weight-aware routing.
"""

from data_structures.graph import Graph
from algorithms.bfs import BFS
from algorithms.dijkstra import Dijkstra
from algorithms.utils import compute_path_cost
from tests.conftest import build_test_grid, assert_valid_path


# ------------------------------------------------------------------
# test 1 — 5x5 open grid, no obstacles
# ------------------------------------------------------------------

def test_bfs_finds_shortest_path_open_grid():
    """bfs must find a valid path across a fully open 5x5 grid."""
    rows, cols = 5, 5
    start, goal = (0, 0), (4, 4)
    grid = build_test_grid(rows, cols)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    path, _, _ = BFS().run(graph, start, goal)
    assert_valid_path(path, start, goal, grid)


def test_dijkstra_finds_shortest_path_open_grid():
    """dijkstra must find a valid path across a fully open 5x5 grid."""
    rows, cols = 5, 5
    start, goal = (0, 0), (4, 4)
    grid = build_test_grid(rows, cols)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    path, _, _ = Dijkstra().run(graph, start, goal)
    assert_valid_path(path, start, goal, grid)


def test_bfs_shortest_path_length_is_9():
    """bfs path from (0,0) to (4,4) on an open 5x5 grid must be exactly 9 cells."""
    rows, cols = 5, 5
    start, goal = (0, 0), (4, 4)
    grid = build_test_grid(rows, cols)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    path, _, _ = BFS().run(graph, start, goal)
    assert len(path) == 9, f"BFS shortest path should be 9 cells, got {len(path)}"


def test_dijkstra_shortest_path_length_is_9():
    """dijkstra path from (0,0) to (4,4) on an open 5x5 grid must be exactly 9 cells."""
    rows, cols = 5, 5
    start, goal = (0, 0), (4, 4)
    grid = build_test_grid(rows, cols)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    path, _, _ = Dijkstra().run(graph, start, goal)
    assert len(path) == 9, f"Dijkstra shortest path should be 9 cells, got {len(path)}"


# ------------------------------------------------------------------
# test 2 — obstacle wall blocking the direct route
# ------------------------------------------------------------------

def test_bfs_navigates_obstacle_wall():
    """bfs must find an alternative route when column 2 rows 0-3 are obstacles."""
    rows, cols = 5, 5
    start, goal = (0, 0), (4, 4)
    # wall along column 2 except row 4
    obstacles = [(0, 2), (1, 2), (2, 2), (3, 2)]
    grid = build_test_grid(rows, cols, obstacles)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    path, _, _ = BFS().run(graph, start, goal)
    assert_valid_path(path, start, goal, grid)


def test_dijkstra_navigates_obstacle_wall():
    """dijkstra must find an alternative route when column 2 rows 0-3 are obstacles."""
    rows, cols = 5, 5
    start, goal = (0, 0), (4, 4)
    obstacles = [(0, 2), (1, 2), (2, 2), (3, 2)]
    grid = build_test_grid(rows, cols, obstacles)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    path, _, _ = Dijkstra().run(graph, start, goal)
    assert_valid_path(path, start, goal, grid)


# ------------------------------------------------------------------
# test 3 — no path exists
# ------------------------------------------------------------------

def test_bfs_returns_empty_when_no_path():
    """bfs must return [] when the goal is completely surrounded by obstacles."""
    rows, cols = 5, 5
    start, goal = (0, 0), (4, 4)
    obstacles = [(3, 4), (4, 3)]
    obstacles += [(0, 2), (1, 2), (2, 2), (3, 2), (3, 3), (4, 2)]
    grid = build_test_grid(rows, cols, obstacles)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    path, _, _ = BFS().run(graph, start, goal)
    assert path == [], f"BFS should return [] when no path, got {path}"


def test_dijkstra_returns_empty_when_no_path():
    """dijkstra must return [] when the goal is completely surrounded by obstacles."""
    rows, cols = 5, 5
    start, goal = (0, 0), (4, 4)
    obstacles = [(3, 4), (4, 3)]
    obstacles += [(0, 2), (1, 2), (2, 2), (3, 2), (3, 3), (4, 2)]
    grid = build_test_grid(rows, cols, obstacles)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    path, _, _ = Dijkstra().run(graph, start, goal)
    assert path == [], f"Dijkstra should return [] when no path, got {path}"


# ------------------------------------------------------------------
# test 4 — start equals goal
# ------------------------------------------------------------------

def test_bfs_start_equals_goal():
    """bfs must return [same] with 0 nodes visited and 0.0 time when start == goal."""
    rows, cols = 5, 5
    grid = build_test_grid(rows, cols)
    graph = Graph(is_weighted=False)
    graph.build_from_grid(grid)

    same = (2, 2)
    path, nodes_visited, exec_time = BFS().run(graph, same, same)
    assert path == [same], f"start==goal path should be [same], got {path}"
    assert nodes_visited == 0
    assert exec_time == 0.0


# ------------------------------------------------------------------
# test 5 — weighted dijkstra prefers the lower-cost route
# ------------------------------------------------------------------

def test_dijkstra_weighted_lower_cost_path():
    """
    on a 3x3 grid where the bottom-left cell has weight 9, dijkstra must
    find a total path cost no greater than the cost of the bfs path.
    """
    # top path:    (0,0) -> (0,1) -> (0,2) -> (1,2) -> (2,2)  all weight 1
    # bottom path: (0,0) -> (1,0) -> (2,0) -> (2,1) -> (2,2)  (2,0) weight 9
    rows, cols = 3, 3
    grid = build_test_grid(rows, cols)
    grid[2][0].weight = 9  # high-cost cell on the bottom path

    graph_bfs = Graph(is_weighted=False)
    graph_bfs.build_from_grid(grid)
    graph_dijk = Graph(is_weighted=True)
    graph_dijk.build_from_grid(grid)

    start, goal = (0, 0), (2, 2)
    path_bfs, _, _ = BFS().run(graph_bfs, start, goal)
    path_dijk, _, _ = Dijkstra().run(graph_dijk, start, goal)

    assert_valid_path(path_bfs, start, goal, grid)
    assert_valid_path(path_dijk, start, goal, grid)

    bfs_cost = compute_path_cost(path_bfs, grid)
    dijk_cost = compute_path_cost(path_dijk, grid)
    assert dijk_cost <= bfs_cost, (
        "Dijkstra should find a path with cost <= BFS path cost"
    )
