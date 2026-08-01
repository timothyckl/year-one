"""
data_structures/graph.py

defines the Graph class that represents the grid as an adjacency list.
edges are built from a 2d list of Node objects, connecting each
non-obstacle cell to its four cardinal neighbours (up, down, left, right).
edge weight equals the destination node's weight in weighted mode, or 1
in unweighted mode.

two wall models are supported and can coexist:
  - obstacle-based: a node with is_obstacle=True is excluded entirely
  - edge-based: a node's walls dict marks which sides are closed;
    a closed wall between two cells removes that directed edge
"""

from data_structures.node import Node

# maps (delta_row, delta_col) to the wall key on the source cell that
# would block movement in that direction
_DIR_TO_WALL = {(-1, 0): "N", (1, 0): "S", (0, -1): "W", (0, 1): "E"}

# maps each wall key to the opposite wall on the neighbouring cell;
# used to enforce symmetric wall blocking even when only one side is set
_OPPOSITE_WALL = {"N": "S", "S": "N", "E": "W", "W": "E"}


class Graph:
    """
    an adjacency-list graph built from a 2d grid of Node objects.

    the graph maps each passable cell key (row, col) to a list of
    ((neighbour_row, neighbour_col), edge_weight) tuples.

    args:
        is_weighted (bool): when True, edge weights come from neighbour.weight;
                            when False, all edges have weight 1.
    """

    # four cardinal directions: (delta_row, delta_col)
    DIRECTIONS = [(-1, 0), (1, 0), (0, -1), (0, 1)]

    def __init__(self, is_weighted: bool = False) -> None:
        self.is_weighted = is_weighted
        # maps (row, col) -> list of ((nr, nc), weight)
        self.adjacency: dict = {}

    def build_from_grid(self, grid: list) -> None:
        """
        constructs the adjacency list from a 2d list of Node objects.
        obstacle nodes are excluded as vertices and as neighbours.
        additionally, edges blocked by a node's walls dict are skipped,
        supporting the edge-based wall model used by the maze generator.

        args:
            grid (list): 2d list where grid[row][col] is a Node

        time complexity: O(rows * cols)
        """
        self.adjacency = {}
        rows = len(grid)
        cols = len(grid[0]) if rows > 0 else 0

        for row in range(rows):
            for col in range(cols):
                node = grid[row][col]
                if node.is_obstacle:
                    continue

                key = (row, col)
                self.adjacency[key] = []

                for delta_row, delta_col in self.DIRECTIONS:
                    neighbour_row = row + delta_row
                    neighbour_col = col + delta_col

                    # bounds check
                    if not (0 <= neighbour_row < rows and 0 <= neighbour_col < cols):
                        continue

                    neighbour = grid[neighbour_row][neighbour_col]
                    if neighbour.is_obstacle:
                        continue

                    # edge-based wall check: skip if either the source cell has
                    # a wall on the side facing the neighbour, or the neighbour
                    # has the mirror wall facing back. checking both sides means
                    # a single-sided wall set (e.g. from a future paint-wall
                    # feature) is sufficient to block the passage in both
                    # directions without requiring the caller to set both sides.
                    wall_key = _DIR_TO_WALL[(delta_row, delta_col)]
                    mirror_key = _OPPOSITE_WALL[wall_key]
                    if node.walls.get(wall_key, False) or neighbour.walls.get(
                        mirror_key, False
                    ):
                        continue

                    # weight comes from destination node in weighted mode
                    edge_weight = neighbour.weight if self.is_weighted else 1
                    neighbour_key = (neighbour_row, neighbour_col)
                    self.adjacency[key].append((neighbour_key, edge_weight))

    def set_weighted(self, is_weighted: bool) -> None:
        """
        updates the edge-cost mode for this graph.
        the graph must be rebuilt via build_from_grid() after calling this
        for the new mode to take effect in subsequent traversals.

        args:
            is_weighted (bool): True to use node weights as edge costs;
                                False to treat all edges as cost 1
        """
        self.is_weighted = is_weighted

    def get_neighbours(self, node_key: tuple) -> list:
        """
        returns the neighbours of a given node as a list of
        ((row, col), weight) tuples.

        args:
            node_key (tuple): (row, col) of the query node

        returns:
            list of ((row, col), weight); empty list if node not in graph
        """
        return self.adjacency.get(node_key, [])

    def __repr__(self) -> str:
        vertex_count = len(self.adjacency)
        edge_count = sum(len(nbrs) for nbrs in self.adjacency.values())
        return f"Graph(vertices={vertex_count}, edges={edge_count}, weighted={self.is_weighted})"

