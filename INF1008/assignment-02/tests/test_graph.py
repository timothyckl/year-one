"""
tests/test_graph.py

pytest tests for the Graph class in data_structures/graph.py.
covers obstacle exclusion, corner adjacency, weighted edges,
and the edge-based wall model (east wall blocking).
"""

from data_structures.node import Node
from data_structures.graph import Graph


def _build_3x3_grid(obstacle: tuple = None) -> list:
    """
    creates a 3x3 grid of Node objects with an optional single obstacle.

    args:
        obstacle (tuple): (row, col) of the cell to mark as an obstacle, or None

    returns:
        2d list of Node objects
    """
    grid = [[Node(r, c) for c in range(3)] for r in range(3)]
    if obstacle:
        r, c = obstacle
        grid[r][c].is_obstacle = True
    return grid


def test_obstacle_node_not_in_adjacency():
    """an obstacle cell must not appear as a vertex in the adjacency list."""
    grid = _build_3x3_grid(obstacle=(1, 1))
    g = Graph(is_weighted=False)
    g.build_from_grid(grid)
    assert (1, 1) not in g.adjacency


def test_corner_node_has_correct_neighbours():
    """(0,0) in a 3x3 unobstructed grid must have exactly (0,1) and (1,0) as neighbours."""
    grid = _build_3x3_grid(obstacle=(1, 1))
    g = Graph(is_weighted=False)
    g.build_from_grid(grid)
    neighbour_keys = [key for key, _ in g.get_neighbours((0, 0))]
    assert (0, 1) in neighbour_keys
    assert (1, 0) in neighbour_keys


def test_obstacle_not_reachable_as_neighbour():
    """the obstacle cell must not appear as a neighbour of any other cell."""
    grid = _build_3x3_grid(obstacle=(1, 1))
    g = Graph(is_weighted=False)
    g.build_from_grid(grid)
    for key, neighbours in g.adjacency.items():
        for nbr, _ in neighbours:
            assert nbr != (1, 1), f"obstacle must not appear as neighbour of {key}"


def test_weighted_edge_uses_destination_weight():
    """in weighted mode, the edge weight must equal the destination node's weight."""
    # assign weight = position index + 1 so weight at (0,1) == 2
    grid = [[Node(r, c, weight=(r * 3 + c + 1)) for c in range(3)] for r in range(3)]
    gw = Graph(is_weighted=True)
    gw.build_from_grid(grid)
    for nbr_key, w in gw.get_neighbours((0, 0)):
        if nbr_key == (0, 1):
            assert w == 2, f"expected weight 2 for edge (0,0)->(0,1), got {w}"


def test_east_wall_removes_edge():
    """an east wall on (0,0) must remove the directed edge from (0,0) to (0,1)."""
    grid = [[Node(r, c) for c in range(3)] for r in range(3)]
    grid[0][0].walls['E'] = True
    g = Graph(is_weighted=False)
    g.build_from_grid(grid)
    neighbour_keys = [key for key, _ in g.get_neighbours((0, 0))]
    assert (0, 1) not in neighbour_keys


def test_east_wall_blocks_reverse_edge_via_mirror():
    """an east wall on (0,0) must also block the reverse edge from (0,1) to (0,0)."""
    grid = [[Node(r, c) for c in range(3)] for r in range(3)]
    grid[0][0].walls['E'] = True
    g = Graph(is_weighted=False)
    g.build_from_grid(grid)
    neighbour_keys = [key for key, _ in g.get_neighbours((0, 1))]
    assert (0, 0) not in neighbour_keys, (
        "east wall on (0,0) must also block the reverse edge from (0,1) via mirror check"
    )
