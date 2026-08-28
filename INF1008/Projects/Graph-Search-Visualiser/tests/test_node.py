"""
tests/test_node.py

pytest tests for the Node class in data_structures/node.py.
covers equality, hashing, reset behaviour, and wall initialisation.
"""

from data_structures.node import Node


def test_nodes_at_same_position_are_equal():
    """nodes constructed at identical (row, col) must compare equal regardless of weight."""
    n1 = Node(0, 0)
    n2 = Node(0, 0, weight=5)
    assert n1 == n2


def test_nodes_at_different_positions_are_not_equal():
    """nodes at different grid positions must not compare equal."""
    n1 = Node(0, 0)
    n3 = Node(1, 2)
    assert n1 != n3


def test_equal_nodes_share_hash():
    """nodes at the same position must produce the same hash value."""
    n1 = Node(0, 0)
    n2 = Node(0, 0, weight=5)
    assert hash(n1) == hash(n2)


def test_reset_restores_unvisited_state():
    """reset() must return a non-special state back to 'unvisited'."""
    n = Node(0, 0)
    n.state = 'frontier'
    n.reset()
    assert n.state == 'unvisited'


def test_reset_preserves_start_state():
    """reset() must not clear the 'start' state — it is a persistent marker."""
    n = Node(0, 0)
    n.state = 'start'
    n.reset()
    assert n.state == 'start'


def test_reset_preserves_wall_state():
    """reset() must not clear wall flags — maze layout persists across re-runs."""
    n = Node(0, 0)
    n.walls['N'] = True
    n.state = 'visited'
    n.reset()
    assert n.walls['N'] is True


def test_full_reset_clears_all():
    """full_reset() must restore weight, obstacle flag, and state to defaults."""
    n = Node(1, 2)
    n.weight = 7
    n.is_obstacle = True
    n.state = 'visited'
    n.full_reset()
    assert n.weight == 1
    assert not n.is_obstacle
    assert n.state == 'unvisited'


def test_full_reset_clears_walls():
    """full_reset() must set every wall back to False."""
    n = Node(1, 2)
    n.walls['S'] = True
    n.full_reset()
    assert n.walls == {'N': False, 'S': False, 'E': False, 'W': False}


def test_new_node_has_all_walls_open():
    """a freshly constructed Node must have all four walls set to False."""
    n = Node(2, 3)
    assert n.walls == {'N': False, 'S': False, 'E': False, 'W': False}
