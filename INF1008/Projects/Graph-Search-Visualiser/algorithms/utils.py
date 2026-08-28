"""
algorithms/utils.py

shared utility functions used across all path-finding algorithm modules.
"""


def compute_path_cost(path: list, grid: list) -> int:
    """
    sums the traversal weight of every cell on the path, excluding the start node.

    args:
        path (list): list of (row, col) tuples from start to goal
        grid (list): 2d list of Node objects; grid[row][col].weight gives cell cost

    returns:
        int: total cost of the path (sum of weights for all cells except start)
    """
    # start node is excluded — cost is incurred on entering each subsequent cell
    return sum(grid[r][c].weight for r, c in path[1:])


def reconstruct_path(parent: dict, start: tuple, goal: tuple) -> list:
    """
    traces the parent map backward from goal to start to build the path.

    args:
        parent (dict): maps (row, col) -> (row, col) predecessor
        start  (tuple): (row, col) of the start node
        goal   (tuple): (row, col) of the goal node

    returns:
        list of (row, col) tuples from start to goal inclusive,
        or empty list if goal is not reachable (not in parent)
    """
    if goal not in parent:
        return []

    path = []
    current = goal
    while current is not None:
        path.append(current)
        current = parent.get(current)

    path.reverse()
    return path
