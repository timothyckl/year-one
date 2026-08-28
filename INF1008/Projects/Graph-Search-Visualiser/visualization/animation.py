"""
visualization/animation.py

controls step-by-step animation of graph search algorithms on the grid.
the animator replays a pre-collected list of (node_key, state) steps
produced by the algorithm's step_callback, colouring each cell in turn
with a configurable delay between steps.

turtle.tracer(0) is assumed to be set globally by grid_renderer.
the animator calls screen.update() (via colour_cell) after each step.
time.sleep() is used for inter-step delay, which briefly blocks the
turtle event loop — acceptable for grid sizes up to 30x30.
"""

import time
import turtle


# turtle heading degrees for each cardinal direction
# turtle convention: 0=east, 90=north, 180=west, 270=south
_HEADING = {
    'up':    90,
    'down':  270,
    'left':  180,
    'right': 0,
}


def _compute_direction(from_row: int, from_col: int,
                       to_row: int, to_col: int) -> str:
    """
    returns the cardinal direction string from one grid cell to an adjacent cell.

    args:
        from_row (int): row of the origin cell
        from_col (int): column of the origin cell
        to_row   (int): row of the destination cell
        to_col   (int): column of the destination cell

    returns:
        one of 'up', 'down', 'left', 'right'
    """
    if to_row < from_row:
        return 'up'
    if to_row > from_row:
        return 'down'
    if to_col < from_col:
        return 'left'
    return 'right'


class Animator:
    """
    replays a pre-collected sequence of algorithm steps on the grid.

    steps are (node_key, state_str) tuples collected by the algorithm's
    step_callback during a dry run. the animator colours each cell
    according to its state and waits `delay` seconds between steps.

    args:
        renderer (GridRenderer): the grid renderer used to colour cells
        screen: the turtle screen object (for screen.update())
        delay (float): seconds to wait between animation frames (default 0.05)
    """

    def __init__(self, renderer, screen, delay: float = 0.05) -> None:
        self.renderer = renderer
        self.screen = screen
        self.delay = delay

        # arrow sprite used during path animation; hidden until animate_path runs.
        # created once here and reused across runs to avoid leaking turtle objects.
        self._sprite = turtle.Turtle()
        self._sprite.shape('arrow')
        # white fill with dark border — readable against gold, green, and red cells
        self._sprite.color('#333333', '#ffffff')
        self._sprite.penup()
        self._sprite.speed(0)
        self._sprite.hideturtle()

    def animate_steps(self, steps: list, grid: list) -> None:
        """
        replays algorithm exploration steps one at a time with a delay.

        each step is a (node_key, state_str) tuple. the corresponding node's
        state is updated in the grid and the cell is recoloured.

        skips colouring for start and goal nodes to preserve their identity
        during exploration.

        args:
            steps (list): list of ((row, col), state_str) tuples
            grid (list): 2d list of Node objects (states are read from here)
        """
        for node_key, state in steps:
            row, col = node_key
            node = grid[row][col]

            # preserve start and goal visual identity during traversal
            if node.state in ('start', 'goal'):
                if self.delay > 0:
                    time.sleep(self.delay)
                continue

            # update state in grid model
            node.state = state

            # colour the cell and flush to screen
            self.renderer.colour_cell(row, col, state, node.weight)

            if self.delay > 0:
                time.sleep(self.delay)

    def animate_path(self, path: list, grid: list) -> None:
        """
        reveals the found path by moving an arrow sprite cell by cell from
        start to goal, rotating it to face the direction of travel at each step
        and colouring each visited cell gold behind it.

        start and goal cells keep their original colours. the sprite is hidden
        once it reaches the goal.

        args:
            path (list): ordered list of (row, col) tuples from start to goal
            grid (list): 2d list of Node objects
        """
        # slightly longer delay for path reveal
        path_delay = max(self.delay * 2, 0.08)

        sprite = self._sprite
        # scale sprite to fit the current cell size (turtle default ≈ 20 px)
        sprite.shapesize(self.renderer.cell_size / 20)
        sprite.showturtle()

        for idx, (row, col) in enumerate(path):
            node = grid[row][col]

            # move the sprite to the centre of this cell
            cx, cy = self.renderer.cell_centre(row, col)
            sprite.goto(cx, cy)

            # rotate to face the next cell; keep last heading when at goal
            if idx + 1 < len(path):
                next_row, next_col = path[idx + 1]
                direction = _compute_direction(row, col, next_row, next_col)
                sprite.setheading(_HEADING[direction])

            # paint the cell gold, preserving start/goal colours
            if node.state not in ('start', 'goal'):
                node.state = 'path'
                self.renderer.draw_cell(row, col, 'path', node.weight)

            self.screen.update()

            if path_delay > 0:
                time.sleep(path_delay)

        # hide the sprite once it has reached the goal
        sprite.hideturtle()
        self.screen.update()

    def set_delay(self, delay: float) -> None:
        """
        updates the inter-step delay.

        args:
            delay (float): new delay in seconds; clamped to [0.0, 1.0]
        """
        self.delay = max(0.0, min(1.0, delay))
