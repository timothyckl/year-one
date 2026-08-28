"""
algorithms/dijkstra.py

dijkstra's shortest-path algorithm using a min-heap PriorityQueue.
finds the minimum-cost path from start to goal in a non-negative weighted graph.

uses lazy deletion: when a shorter path to a node is found, the updated
(distance, node) pair is re-inserted. stale entries (where the node has
already been settled at a lower cost) are skipped on extraction.
"""

import time

from algorithms.base import Algorithm
from algorithms.utils import reconstruct_path
from data_structures.priority_queue import PriorityQueue


class Dijkstra(Algorithm):
    """
    dijkstra's shortest-path algorithm with lazy deletion.

    settles each node exactly once at its minimum distance from the start.
    stale priority queue entries for already-settled nodes are discarded
    on extraction rather than being removed eagerly (lazy deletion).
    """

    def run(self, graph, start: tuple, goal: tuple, step_callback=None) -> tuple:
        """
        runs dijkstra's algorithm from start to goal on a weighted graph.

        each node is settled exactly once (when extracted from the priority queue
        with the minimum tentative distance). if a node is extracted but already
        in 'settled', the entry is stale and is discarded (lazy deletion).

        args:
            graph (Graph):          adjacency-list graph built from the grid
            start (tuple):          (row, col) of the start cell
            goal  (tuple):          (row, col) of the goal cell
            step_callback:          callable(node_key, state_str) or None; called with
                                    'frontier' on first discovery/relaxation and
                                    'visited' on settle

        returns:
            tuple: (path, nodes_visited, exec_time)
                   path (list): (row, col) sequence from start to goal, or []
                   nodes_visited (int): number of nodes settled (extracted and processed)
                   exec_time (float): wall-clock seconds taken
        """
        # handle trivial case
        if start == goal:
            return ([start], 0, 0.0)

        start_time = time.perf_counter()

        pq = PriorityQueue()
        # dist maps node_key -> best known cost from start
        dist: dict = {start: 0}
        # parent maps node_key -> predecessor on the best known path
        parent: dict = {start: None}
        # settled nodes have been extracted with their minimum distance
        settled: set = set()
        nodes_visited = 0

        pq.insert(0, start)
        if step_callback:
            step_callback(start, "frontier")

        while not pq.is_empty():
            current_dist, current = pq.extract_min()

            # skip stale entries (lazy deletion)
            if current in settled:
                continue

            settled.add(current)
            nodes_visited += 1

            if step_callback:
                step_callback(current, "visited")

            # early exit once goal is settled
            if current == goal:
                path = reconstruct_path(parent, start, goal)
                exec_time = time.perf_counter() - start_time
                return (path, nodes_visited, exec_time)

            # relax all outgoing edges
            for neighbour_key, edge_weight in graph.get_neighbours(current):
                if neighbour_key in settled:
                    continue

                new_dist = current_dist + edge_weight

                # only update if this is a shorter path
                if neighbour_key not in dist or new_dist < dist[neighbour_key]:
                    dist[neighbour_key] = new_dist
                    parent[neighbour_key] = current
                    pq.insert(new_dist, neighbour_key)
                    if step_callback:
                        step_callback(neighbour_key, "frontier")

        # goal was never reached
        exec_time = time.perf_counter() - start_time
        return ([], nodes_visited, exec_time)
