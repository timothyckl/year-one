"""
main.py

entry point for the graph search algorithm visualiser.
the Application class manages the turtle screen, all application state,
user input, key bindings, and coordinates between the grid, graph,
algorithms, maze generator, renderer, and animator.

supported algorithms:
  1 — BFS (breadth-first search)
  2 — Dijkstra's algorithm

interaction flow:
  1. startup: prompts for grid dimensions (defaults to unweighted mode)
  2. press T → toggle weighted/unweighted mode at any time
  3. press M → generate random maze
  4. press S then click a cell → set start node
  5. press G then click a cell → set goal node
  6. press 1 or 2 to run an algorithm
  7. press C to clear; R to re-run last algorithm
  8. press + / - to adjust animation speed
"""

import random
import turtle

from algorithms.base import Algorithm
from algorithms.bfs import BFS
from algorithms.dijkstra import Dijkstra
from algorithms.utils import compute_path_cost
from data_structures.graph import Graph
from data_structures.node import Node
from maze.generator import MazeGenerator
from visualization.animation import Animator
from visualization.grid_renderer import (ABOVE_SPACE, BELOW_SPACE, H_MARGIN,
                                         LEFT_PANEL_WIDTH, RIGHT_PANEL_WIDTH,
                                         GridRenderer)


class Application:
    """
    top-level application class for the graph search visualiser.

    consolidates all application state into instance attributes and
    delegates responsibilities to specialised components. algorithm
    execution depends on the Algorithm abstraction (not on specific
    functions), so new algorithms can be added without modifying this class.
    """

    # animation speed constants
    SPEED_STEP = 0.02
    MIN_DELAY = 0.0
    MAX_DELAY = 0.5

    def __init__(self) -> None:
        # all attributes initialised to safe defaults; populated by run()
        self.screen = None
        self.grid = None  # 2d list of Node objects
        self.graph = None  # Graph adjacency-list instance
        self.renderer = None  # GridRenderer instance
        self.animator = None  # Animator instance
        self.rows = 0
        self.cols = 0
        self.cell_size = 40
        self.is_weighted = False
        self.start = None  # (row, col) or None
        self.goal = None  # (row, col) or None
        self.mode = None  # 'set_start' | 'set_goal' | 'set_weight' | None
        self.running = False  # guard against re-entrant algorithm execution
        self.last_algo = None  # Algorithm instance — last algorithm run
        self.last_name = ""  # display name of the last algorithm
        self.run_history = []  # list of run result dicts; shown in past-runs panel

        # algorithm instances — created once in _setup_components()
        self._bfs = None
        self._dijkstra = None

    # ==========================================================================
    # public entry point
    # ==========================================================================

    def run(self) -> None:
        """
        initialises the turtle screen, prompts for grid settings, sets up
        all application components, and starts the turtle event loop.
        """
        screen = turtle.Screen()
        screen.tracer(0)
        self.screen = screen

        self._setup_display()
        self._setup_grid()
        self._setup_components()
        self._build_graph()

        # draw initial empty grid and panels
        self.renderer.draw_full_grid(self.grid)
        self.renderer.draw_header()
        self.renderer.display_past_runs([])
        mode_label = "WEIGHTED" if self.is_weighted else "UNWEIGHTED"
        self.renderer.display_message(
            f"{self.rows}×{self.cols} {mode_label} — Press M to generate a maze, or S/G to place nodes directly."
        )

        # register event handlers and start the event loop
        screen.onclick(self.handle_click)
        self._bind_keys()
        screen.listen()
        turtle.mainloop()

    # ==========================================================================
    # setup helpers
    # ==========================================================================

    def _setup_display(self) -> None:
        """
        prompts the user for grid dimensions via turtle dialogs.
        validates inputs and stores results as instance attributes.
        defaults to unweighted mode (press T at any time to toggle).
        auto-scales cell_size for large grids. configures the screen
        size using layout constants imported from grid_renderer.py.
        """
        screen = self.screen

        # prompt for number of rows
        rows = None
        while rows is None:
            raw = turtle.numinput(
                "Grid Setup", "Number of rows (3–30):", minval=3, maxval=30, default=11
            )
            # user cancelled dialog — use default
            if raw is None:
                rows = 11
            else:
                rows = int(raw)

        # prompt for number of columns
        cols = None
        while cols is None:
            raw = turtle.numinput(
                "Grid Setup",
                "Number of columns (3–30):",
                minval=3,
                maxval=30,
                default=11,
            )
            if raw is None:
                cols = 11
            else:
                cols = int(raw)

        # auto-scale cell size so the grid fits comfortably on screen
        max_dimension = max(rows, cols)
        if max_dimension <= 10:
            cell_size = 50
        elif max_dimension <= 15:
            cell_size = 40
        elif max_dimension <= 20:
            cell_size = 30
        else:
            cell_size = 22

        self.rows = rows
        self.cols = cols
        self.cell_size = cell_size
        self.is_weighted = False

        # derive window size from layout constants in grid_renderer so
        # the two places stay synchronised without manual duplication
        total_width = (
            cols * cell_size + LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH + 2 * H_MARGIN
        )
        total_height = rows * cell_size + ABOVE_SPACE + BELOW_SPACE

        screen.setup(width=total_width, height=total_height)
        screen.title("Graph Search Visualiser — INF1008")
        screen.bgcolor("#F8F8F8")

    def _setup_grid(self) -> None:
        """
        creates a fresh 2d list of Node objects, all passable.
        assigns random weights in weighted mode so labels appear immediately.
        """
        self.grid = [
            [Node(row, col) for col in range(self.cols)] for row in range(self.rows)
        ]
        if self.is_weighted:
            self._randomise_weights()

    def _randomise_weights(self) -> None:
        """
        assigns a random integer weight in [1, 9] to every non-obstacle cell.
        used at startup, after maze generation, and after toggling to weighted mode.
        """
        for row in range(self.rows):
            for col in range(self.cols):
                node = self.grid[row][col]
                # skip obstacle cells — their weight is irrelevant to path cost
                if not node.is_obstacle:
                    node.weight = random.randint(1, 9)

    def _build_graph(self) -> None:
        """rebuilds the adjacency-list graph from the current grid."""
        self.graph.build_from_grid(self.grid)

    def _setup_components(self) -> None:
        """
        creates all component instances: Graph, GridRenderer, Animator,
        and the two algorithm instances. called once during run().
        """
        self.graph = Graph(is_weighted=self.is_weighted)
        self.renderer = GridRenderer(
            self.screen,
            self.rows,
            self.cols,
            self.cell_size,
            is_weighted=self.is_weighted,
        )

        # auto-reduce animation delay for large grids
        initial_delay = 0.0 if (self.rows * self.cols) > 225 else 0.05
        self.animator = Animator(self.renderer, self.screen, delay=initial_delay)

        # create algorithm instances once; reused on every run
        self._bfs = BFS()
        self._dijkstra = Dijkstra()

    def _bind_keys(self) -> None:
        """registers all key handlers with the turtle screen."""
        self._bind_key(self.handle_key_s, "s", "S")
        self._bind_key(self.handle_key_g, "g", "G")
        self._bind_key(self.handle_key_w, "w", "W")
        self._bind_key(self.handle_key_t, "t", "T")
        self._bind_key(self.handle_key_m, "m", "M")
        self._bind_key(self.handle_key_c, "c", "C")
        self._bind_key(self.handle_key_r, "r", "R")
        self._bind_key(self.handle_key_1, "1")
        self._bind_key(self.handle_key_2, "2")
        self._bind_key(self.handle_key_plus, "=", "+")
        self._bind_key(self.handle_key_minus, "-")

    def _bind_key(self, handler, *keys) -> None:
        """
        registers a bound-method handler for one or more key strings.
        eliminates the repeated dual-registration pattern.

        args:
            handler:    bound method to call on key press
            *keys:      one or more key strings to bind (e.g. 's', 'S')
        """
        for key in keys:
            self.screen.onkeypress(handler, key)

    # ==========================================================================
    # algorithm execution
    # ==========================================================================

    def run_algorithm(self, algo: Algorithm, algo_name: str) -> None:
        """
        orchestrates a full algorithm run: validates preconditions, resets node
        states, rebuilds the graph, runs the algorithm, animates the steps,
        and records the result.

        args:
            algo (Algorithm):  concrete Algorithm instance to execute
            algo_name (str):   display name shown in the header and history panel
        """
        if self.running:
            return

        if not self._validate_run():
            return

        self.running = True
        self.last_algo = algo
        self.last_name = algo_name

        # prepare grid for a fresh run
        self._reset_node_states()
        self._build_graph()
        self.renderer.draw_full_grid(self.grid)
        self.renderer.clear_message()
        # show "running…" in the header while the algorithm executes
        self.renderer.draw_header(f"Running {algo_name}...")
        self.renderer.display_message(f"Running {algo_name}...")

        # collect (node_key, state) tuples; lambda packs the two callback args into a tuple
        steps: list = []
        path, nodes_visited, exec_time = algo.run(
            self.graph,
            self.start,
            self.goal,
            step_callback=lambda key, state: steps.append((key, state)),
        )

        # animate exploration steps then path reveal
        self.animator.animate_steps(steps, self.grid)

        if path:
            self.animator.animate_path(path, self.grid)
            path_length = len(path)
            path_cost = compute_path_cost(path, self.grid)
            self.renderer.display_message(
                f"{algo_name} complete — path found ({path_length} cells, cost {path_cost})."
            )
            result_str = "path"
        else:
            path_length = 0
            path_cost = 0
            self.renderer.display_message(f"{algo_name} complete — no path found.")
            result_str = "no_path"

        self.renderer.draw_header(algo_name)
        self._record_result(
            algo_name, path_length, path_cost, nodes_visited, exec_time, result_str
        )

        self.running = False

    def _validate_run(self) -> bool:
        """
        checks that start and goal are set and neither is an obstacle.

        returns:
            bool: True if all preconditions are met; False otherwise
                  (displays an error message in the status bar when False)
        """
        renderer = self.renderer

        if self.start is None or self.goal is None:
            renderer.display_message(
                "Set start (S) and goal (G) before running an algorithm."
            )
            return False

        start_node = self.grid[self.start[0]][self.start[1]]
        goal_node = self.grid[self.goal[0]][self.goal[1]]

        if start_node.is_obstacle:
            renderer.display_message(
                "Error: start cell is an obstacle. Clear it first (C)."
            )
            return False

        if goal_node.is_obstacle:
            renderer.display_message(
                "Error: goal cell is an obstacle. Clear it first (C)."
            )
            return False

        return True

    def _reset_node_states(self) -> None:
        """resets all nodes to 'unvisited' to clear the previous visualisation."""
        for row in range(self.rows):
            for col in range(self.cols):
                self.grid[row][col].reset()

    def _record_result(
        self,
        algo_name: str,
        path_length: int,
        path_cost: int,
        nodes_visited: int,
        exec_time: float,
        result_str: str,
    ) -> None:
        """
        appends a run result entry to run_history and updates the right panel.

        args:
            algo_name     (str):   display name of the algorithm
            path_length   (int):   number of cells in the found path (0 if none)
            path_cost     (int):   total traversal cost of the path
            nodes_visited (int):   number of nodes explored
            exec_time     (float): elapsed time in seconds
            result_str    (str):   one of 'path', 'no_path'
        """
        entry = {
            "run_num": len(self.run_history) + 1,
            "algo": algo_name,
            "path_length": path_length,
            "path_cost": path_cost,
            "nodes_visited": nodes_visited,
            "exec_time_ms": exec_time * 1000,
            "result": result_str,
        }
        self.run_history.append(entry)
        self.renderer.display_past_runs(self.run_history)

    # ==========================================================================
    # weighted mode
    # ==========================================================================

    def toggle_weighted(self) -> None:
        """
        single synchronisation point for switching between weighted and unweighted mode.
        updates self.is_weighted, graph, and renderer atomically so they never diverge.
        rebuilds the graph and redraws the grid immediately.
        """
        self.is_weighted = not self.is_weighted

        # propagate the new mode to both components via their encapsulated setters
        self.graph.set_weighted(self.is_weighted)
        self.renderer.set_weighted(self.is_weighted)

        if self.is_weighted:
            # randomise weights for all non-obstacle cells
            self._randomise_weights()
        else:
            # reset every cell to weight 1 (uniform cost = unweighted)
            for row in range(self.rows):
                for col in range(self.cols):
                    self.grid[row][col].weight = 1

        self._build_graph()
        self.renderer.draw_full_grid(self.grid)
        self.renderer.draw_header(self.last_name)
        mode_label = "WEIGHTED" if self.is_weighted else "UNWEIGHTED"
        self.renderer.display_message(
            f"Switched to {mode_label} mode. Press 1 or 2 to re-run an algorithm."
        )

    # ==========================================================================
    # click and mode handlers
    # ==========================================================================

    def handle_click(self, px: float, py: float) -> None:
        """
        dispatches a canvas click to the appropriate cell handler based on mode.

        args:
            px (float): turtle canvas x coordinate of the click
            py (float): turtle canvas y coordinate of the click
        """
        if self.running:
            return

        row, col = self.renderer.pixel_to_cell(px, py)

        # click was outside the grid
        if row == -1:
            return

        if self.mode == "set_start":
            self._set_start(row, col)
        elif self.mode == "set_goal":
            self._set_goal(row, col)
        elif self.mode == "set_weight":
            self._set_weight(row, col)

    def _set_start(self, row: int, col: int) -> None:
        """
        marks the clicked cell as the start node.

        args:
            row (int): row index of the clicked cell
            col (int): column index of the clicked cell
        """
        node = self.grid[row][col]

        if node.is_obstacle:
            self.renderer.display_message("Cannot set start on an obstacle cell.")
            return

        # clear the previous start cell if one exists
        if self.start is not None:
            pr, pc = self.start
            self.grid[pr][pc].state = "unvisited"
            self.renderer.colour_cell(pr, pc, "unvisited", self.grid[pr][pc].weight)

        # if the clicked cell was the goal, remove the goal assignment
        if self.goal == (row, col):
            self.goal = None

        node.state = "start"
        self.start = (row, col)
        self.mode = None
        self.renderer.colour_cell(row, col, "start", node.weight)
        self.renderer.display_message(
            f"Start set at ({row}, {col}). Press G to set goal."
        )

    def _set_goal(self, row: int, col: int) -> None:
        """
        marks the clicked cell as the goal node.

        args:
            row (int): row index of the clicked cell
            col (int): column index of the clicked cell
        """
        node = self.grid[row][col]

        if node.is_obstacle:
            self.renderer.display_message("Cannot set goal on an obstacle cell.")
            return

        # clear the previous goal cell if one exists
        if self.goal is not None:
            pr, pc = self.goal
            self.grid[pr][pc].state = "unvisited"
            self.renderer.colour_cell(pr, pc, "unvisited", self.grid[pr][pc].weight)

        if self.start == (row, col):
            self.renderer.display_message("Goal cannot be the same cell as start.")
            return

        node.state = "goal"
        self.goal = (row, col)
        self.mode = None
        self.renderer.colour_cell(row, col, "goal", node.weight)
        self.renderer.display_message(
            f"Goal set at ({row}, {col}). Press 1 or 2 to run an algorithm."
        )

    def _set_weight(self, row: int, col: int) -> None:
        """
        prompts the user for a weight value and assigns it to the clicked cell.
        only reachable when is_weighted is True.

        args:
            row (int): row index of the clicked cell
            col (int): column index of the clicked cell
        """
        node = self.grid[row][col]

        if node.is_obstacle:
            self.renderer.display_message("Cannot set weight on an obstacle cell.")
            return

        raw = turtle.numinput(
            "Set Weight",
            f"Weight for cell ({row}, {col}) [1–9]:",
            minval=1,
            maxval=9,
            default=node.weight,
        )
        if raw is not None:
            node.weight = int(raw)
            self.renderer.colour_cell(row, col, node.state, node.weight)
            self.renderer.display_message(
                f"Weight {node.weight} set at ({row}, {col})."
            )

    # ==========================================================================
    # key handlers — thin delegators
    # ==========================================================================

    def handle_key_s(self) -> None:
        """enters set_start mode; the next click sets the start node."""
        if self.running:
            return
        self.mode = "set_start"
        self.renderer.display_message("Click a cell to set the START node.")

    def handle_key_g(self) -> None:
        """enters set_goal mode; the next click sets the goal node."""
        if self.running:
            return
        self.mode = "set_goal"
        self.renderer.display_message("Click a cell to set the GOAL node.")

    def handle_key_w(self) -> None:
        """enters set_weight mode (weighted mode only); the next click sets cell weight."""
        if self.running:
            return
        if not self.is_weighted:
            self.renderer.display_message(
                "Weight mode is only available in weighted graphs."
            )
            return
        self.mode = "set_weight"
        self.renderer.display_message("Click a cell to set its traversal WEIGHT (1–9).")

    def handle_key_t(self) -> None:
        """toggles between weighted and unweighted mode."""
        if self.running:
            return
        self.toggle_weighted()

    def handle_key_m(self) -> None:
        """generates a random maze."""
        if self.running:
            return
        self._generate_maze()

    def handle_key_c(self) -> None:
        """resets the entire grid."""
        if self.running:
            return
        self._clear_grid()

    def handle_key_r(self) -> None:
        """re-runs the last algorithm on the current grid state."""
        if self.running:
            return
        if self.last_algo is None:
            self.renderer.display_message("No algorithm has been run yet.")
            return
        self.run_algorithm(self.last_algo, self.last_name)

    def handle_key_1(self) -> None:
        """runs BFS."""
        self.run_algorithm(self._bfs, "BFS")

    def handle_key_2(self) -> None:
        """runs Dijkstra's algorithm."""
        self.run_algorithm(self._dijkstra, "Dijkstra")

    def handle_key_plus(self) -> None:
        """increases animation speed (decreases delay)."""
        new_delay = max(self.MIN_DELAY, self.animator.delay - self.SPEED_STEP)
        self.animator.set_delay(new_delay)
        self.renderer.display_message(f"Animation delay: {new_delay:.2f}s")

    def handle_key_minus(self) -> None:
        """decreases animation speed (increases delay)."""
        new_delay = min(self.MAX_DELAY, self.animator.delay + self.SPEED_STEP)
        self.animator.set_delay(new_delay)
        self.renderer.display_message(f"Animation delay: {new_delay:.2f}s")

    # ==========================================================================
    # mutation logic — separated from key handlers
    # ==========================================================================

    def _generate_maze(self) -> None:
        """
        generates a random maze using the randomised DFS backtracker.
        clears start/goal before generating so they are not left pointing at
        cells that become walls. also resets run_history so the history panel
        does not show stale runs from the previous layout.
        """
        renderer = self.renderer

        # wipe start so its visual state is removed before generate() marks obstacles
        if self.start is not None:
            sr, sc = self.start
            self.grid[sr][sc].state = "unvisited"
            self.start = None

        # wipe goal for the same reason
        if self.goal is not None:
            gr, gc = self.goal
            self.grid[gr][gc].state = "unvisited"
            self.goal = None

        # reset run history — a new maze layout invalidates prior run results
        self.run_history = []

        renderer.display_message("Generating maze...")
        self.screen.update()

        generator = MazeGenerator(grid=self.grid, rows=self.rows, cols=self.cols)
        generator.generate()

        # re-randomise weights in weighted mode — generator only carves walls
        if self.is_weighted:
            self._randomise_weights()

        self._build_graph()
        renderer.draw_full_grid(self.grid)
        # draw_full_grid clears the label pen, so redraw header and history panel
        renderer.draw_header(self.last_name if self.last_name else "")
        renderer.display_past_runs(self.run_history)
        renderer.display_message("Maze generated. Press S to set start, G to set goal.")

    def _clear_grid(self) -> None:
        """
        resets the entire grid: clears all nodes, obstacles, start, goal,
        history, and rebuilds the graph on the fresh empty grid.
        """
        for row in range(self.rows):
            for col in range(self.cols):
                self.grid[row][col].full_reset()

        # full_reset sets every weight to 1; re-randomise in weighted mode
        if self.is_weighted:
            self._randomise_weights()

        self.start = None
        self.goal = None
        self.mode = None
        self.last_algo = None
        self.last_name = ""
        self.run_history = []

        self._build_graph()
        self.renderer.draw_full_grid(self.grid)
        self.renderer.draw_header()
        self.renderer.display_past_runs([])
        self.renderer.display_message(
            "Grid cleared. Press M to generate maze, or S/G to place nodes on open grid."
        )


# ==========================================================================
# entry point
# ==========================================================================

if __name__ == "__main__":
    app = Application()
    app.run()
