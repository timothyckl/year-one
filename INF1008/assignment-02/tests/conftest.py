"""
tests/conftest.py

shared helper functions used across all test modules.
plain functions rather than pytest fixtures because they require parameters.
"""

from data_structures.node import Node
from data_structures.graph import Graph


def build_test_grid(rows: int, cols: int, obstacles: list = None) -> list:
    """
    creates a rows x cols grid of Node objects with optional obstacles.

    args:
        rows (int): number of rows
        cols (int): number of columns
        obstacles (list): list of (row, col) tuples to mark as obstacles

    returns:
        2d list of Node objects
    """
    grid = [[Node(r, c) for c in range(cols)] for r in range(rows)]
    if obstacles:
        for r, c in obstacles:
            grid[r][c].is_obstacle = True
            grid[r][c].state = 'obstacle'
    return grid


def assert_valid_path(path: list, start: tuple, goal: tuple, grid: list) -> None:
    """
    asserts that a path is non-empty, starts at start, ends at goal,
    all consecutive steps are adjacent (manhattan distance 1), and no
    cell on the path is an obstacle.

    args:
        path (list): list of (row, col) tuples
        start (tuple): expected first cell
        goal (tuple): expected last cell
        grid (list): 2d list of Node objects used for the obstacle check
    """
    assert path, "path should not be empty for a reachable goal"
    assert path[0] == start, f"path must start at {start}, got {path[0]}"
    assert path[-1] == goal, f"path must end at {goal}, got {path[-1]}"

    for i in range(len(path) - 1):
        r1, c1 = path[i]
        r2, c2 = path[i + 1]
        assert abs(r1 - r2) + abs(c1 - c2) == 1, (
            f"non-adjacent step in path: {path[i]} -> {path[i + 1]}"
        )

    for r, c in path:
        assert not grid[r][c].is_obstacle, (
            f"path passes through obstacle at ({r},{c})"
        )
