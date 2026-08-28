"""
data_structures/node.py

defines the Node class representing a single cell in the 2d grid.
each node stores its position, traversal weight, obstacle flag,
current visualisation state used by the renderer for colour selection,
and a walls dict that records which of its four sides are closed.
"""


class Node:
    """
    represents a single cell in the 2d grid used by the visualiser.

    args:
        row (int): zero-based row index of the cell
        col (int): zero-based column index of the cell
        weight (int): traversal cost for weighted algorithms (default 1)

    attributes:
        is_obstacle (bool): whether the cell blocks traversal
        state (str): current visualisation state, one of:
            'unvisited' | 'frontier' | 'visited' | 'obstacle' |
            'start' | 'goal' | 'path'
        walls (dict): which of the four cardinal sides are closed walls;
            keys are 'N', 'S', 'E', 'W'; False = open, True = wall present
    """

    def __init__(self, row: int, col: int, weight: int = 1) -> None:
        self.row = row
        self.col = col
        self.weight = weight
        self.is_obstacle = False
        self.state = "unvisited"
        # all False = no walls = fully open cell
        self.walls: dict = {"N": False, "S": False, "E": False, "W": False}

    def reset(self) -> None:
        """
        resets the node to its default traversal state.
        preserves obstacle status, weight, and walls — only clears algorithm
        state. start and goal states are also preserved.
        walls are intentionally kept so the maze layout survives re-runs.
        """
        if self.state not in ("start", "goal", "obstacle"):
            self.state = "unvisited"

    def full_reset(self) -> None:
        """
        fully resets the node including obstacle, weight, state, and walls.
        used when clearing the entire grid.
        """
        self.weight = 1
        self.is_obstacle = False
        self.state = "unvisited"
        self.walls = {"N": False, "S": False, "E": False, "W": False}

    def __repr__(self) -> str:
        # summarise which sides have walls, e.g. "NW" when north and west are closed
        wall_summary = "".join(k for k, v in self.walls.items() if v) or "none"
        return (
            f"Node(row={self.row}, col={self.col}, weight={self.weight}, "
            f"state='{self.state}', obstacle={self.is_obstacle}, "
            f"walls='{wall_summary}')"
        )

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Node):
            return NotImplemented
        return self.row == other.row and self.col == other.col

    def __hash__(self) -> int:
        # hash based solely on grid position
        return hash((self.row, self.col))

