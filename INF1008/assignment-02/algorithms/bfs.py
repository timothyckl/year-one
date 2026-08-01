"""
algorithms/bfs.py

breadth-first search on an unweighted (or ignored-weight) grid graph.
guarantees the shortest path by number of hops from start to goal.
uses the custom Queue from data_structures to process nodes in FIFO order.
"""

import time

from algorithms.base import Algorithm
from algorithms.utils import reconstruct_path
from data_structures.queue import Queue


class BFS(Algorithm):
    """
    breadth-first search algorithm.

    guarantees the shortest path in terms of hop count by exploring all
    nodes at distance d before any node at distance d+1. nodes are marked
    visited on enqueue to avoid redundant exploration.
    """

    def run(self, graph, start: tuple, goal: tuple, step_callback=None) -> tuple:
        """
        runs breadth-first search from start to goal on the given graph.

        nodes are marked visited when enqueued (not when dequeued) to avoid
        re-processing the same node via different paths — this is correct for
        BFS because the first path to any node is guaranteed shortest.

        args:
            graph (Graph):          adjacency-list graph built from the grid
            start (tuple):          (row, col) of the start cell
            goal  (tuple):          (row, col) of the goal cell
            step_callback:          callable(node_key, state_str) or None; called with
                                    'frontier' on enqueue and 'visited' on dequeue

        returns:
            tuple: (path, nodes_visited, exec_time)
                   path (list): (row, col) sequence from start to goal, or []
                   nodes_visited (int): number of nodes popped from the queue
                   exec_time (float): wall-clock seconds taken
        """
        # handle trivial case: start equals goal
        if start == goal:
            return ([start], 0, 0.0)

        start_time = time.perf_counter()

        frontier = Queue()
        # parent maps each visited node to the node it was reached from
        parent: dict = {start: None}
        nodes_visited = 0

        frontier.enqueue(start)
        if step_callback:
            step_callback(start, "frontier")

        while not frontier.is_empty():
            current = frontier.dequeue()
            nodes_visited += 1

            if step_callback:
                step_callback(current, "visited")

            # goal check on dequeue
            if current == goal:
                path = reconstruct_path(parent, start, goal)
                exec_time = time.perf_counter() - start_time
                return (path, nodes_visited, exec_time)

            # enqueue unvisited neighbours
            for neighbour_key, _ in graph.get_neighbours(current):
                if neighbour_key not in parent:
                    parent[neighbour_key] = current
                    frontier.enqueue(neighbour_key)
                    if step_callback:
                        step_callback(neighbour_key, "frontier")

        # goal was never reached
        exec_time = time.perf_counter() - start_time
        return ([], nodes_visited, exec_time)
