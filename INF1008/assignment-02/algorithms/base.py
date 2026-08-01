"""
algorithms/base.py

abstract base class for all path-finding algorithms.
defines the interface that BFS, Dijkstra, and any future algorithms must implement.
"""

import abc


class Algorithm(abc.ABC):
    """
    abstract base class for grid search algorithms.

    all concrete algorithm classes must implement the run() method with
    the signature defined below. this enables the Application class to
    depend on the Algorithm abstraction rather than on specific algorithm
    functions (dependency inversion principle).
    """

    @abc.abstractmethod
    def run(self, graph, start: tuple, goal: tuple, step_callback=None) -> tuple:
        """
        runs the search algorithm from start to goal on graph.

        args:
            graph (Graph):          adjacency-list graph instance built from the grid
            start (tuple):          (row, col) of the start cell
            goal  (tuple):          (row, col) of the goal cell
            step_callback:          callable(node_key, state) or None; called at each
                                    algorithm step to record (node_key, state) tuples
                                    for animation replay

        returns:
            tuple: (path, nodes_visited, exec_time)
                   path is a list of (row, col) tuples from start to goal,
                   or an empty list if the goal is unreachable;
                   nodes_visited is the count of nodes explored;
                   exec_time is the wall-clock duration in seconds
        """
