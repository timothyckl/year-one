"""
visualization/grid_renderer.py

handles all turtle-based drawing for the grid visualiser.
draws the cell grid, colours individual cells by state, renders edge
weights, and manages panels arranged in a side-by-side layout:

    ┌──────────────┬───────────────────┬─────────────────────┐  ← canvas top
    │  LEFT PANEL  │                   │    RIGHT PANEL       │
    │              │                   │                      │
    │ Algorithm:   │      GRID         │  Run History:        │
    │ BFS          │                   │  #1  BFS             │
    │              │  (rows × cols     │    P:5 N:7 0.1ms     │
    │ S — Start    │   cells)          │  #2  Dijkstra        │
    │ G — Goal     │                   │    P:5 N:9 0.3ms     │
    │ M — Maze     │                   │  ...                 │
    │ 1 — BFS      │                   │                      │
    │ ...          │                   │                      │
    ├──────────────┴───────────────────┴──────────────────────┤
    │  Status message...                                      │
    └─────────────────────────────────────────────────────────┘

the grid is shifted by x_offset horizontally and y_offset vertically so
the side panels and bottom panels fit within the canvas, sized to:
  width  = cols*cell_size + LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH + 2*H_MARGIN
  height = rows*cell_size + ABOVE_SPACE + BELOW_SPACE

turtle.tracer(0) is set globally so the caller controls when the screen
redraws via screen.update() — this avoids flickering during bulk draws.
"""

import turtle


# -- colour palette mapped to node state strings --
# monochrome base (unvisited/obstacle) + vivid high-contrast accents for algorithm states
STATE_COLOURS = {
    'unvisited': '#ffffff',   # pure white — open corridor, background
    'obstacle':  '#1a1a1a',   # near-black — used as wall indicator fill
    'frontier':  '#22c5e8',   # vivid cyan — bright active-exploration colour
    'visited':   '#0b4f6c',   # deep navy — same blue family as frontier, clearly darker/settled
    'start':     '#00c853',   # vivid green — prominent, unambiguous origin marker
    'goal':      '#ff3d00',   # vivid orange-red — unmistakable destination
    'path':      '#ffd600',   # vivid amber-yellow — pops over deep-navy visited cells
}

# fallback colour for unknown states
DEFAULT_COLOUR = '#ffffff'

# colour used for all wall line indicators and the outer grid border
WALL_COLOUR = '#1a1a1a'

# side panel dimensions in pixels
LEFT_PANEL_WIDTH  = 300
RIGHT_PANEL_WIDTH = 320
H_MARGIN          = 40   # gap between each side panel and the grid

# bottom panel heights in pixels
BELOW_GRID_BUFFER = 80   # empty space between grid bottom and the status bar
STATUS_HEIGHT     = 50

# total space above and below the grid
ABOVE_SPACE = 20
BELOW_SPACE = BELOW_GRID_BUFFER + STATUS_HEIGHT + 20   # 150 px

# shift the grid upward so bottom panels fit — positive y_offset moves grid up
Y_OFFSET = (BELOW_SPACE - ABOVE_SPACE) / 2   # 65.0 px

# shift the grid leftward to balance left/right panels — negative x_offset moves grid left
X_OFFSET = (LEFT_PANEL_WIDTH - RIGHT_PANEL_WIDTH) / 2  # -10.0 px


class GridRenderer:
    """
    draws and updates the grid using python's turtle graphics library.

    coordinate system (turtle):
      - origin (0, 0) is the screen centre
      - x increases rightward, y increases upward
      - the grid is shifted by (x_offset, y_offset) so asymmetric panels fit
      - cell (row, col) bottom-left corner:
            x = x_offset - cols * cell_size / 2 + col * cell_size
            y = y_offset + rows * cell_size / 2 - (row + 1) * cell_size

    args:
        screen    (turtle.Screen): the turtle screen object
        rows      (int): number of grid rows
        cols      (int): number of grid columns
        cell_size (int): pixel width/height of each square cell (default 40)
    """

    def __init__(self, screen, rows: int, cols: int,
                 cell_size: int = 40, is_weighted: bool = False) -> None:
        self.screen      = screen
        self.rows        = rows
        self.cols        = cols
        self.cell_size   = cell_size
        self.is_weighted = is_weighted
        self.y_offset    = Y_OFFSET
        self.x_offset    = X_OFFSET

        # cached state so draw_full_grid can redraw panels automatically
        self._current_algo_name = ''
        self._current_runs: list = []

        # reference to the live grid — set by draw_full_grid so that
        # colour_cell can redraw wall segments after recolouring a cell
        self._grid = None

        # disable auto-redraw — caller calls screen.update() explicitly
        turtle.tracer(0)

        # main pen for drawing cells and background rectangles
        self._pen = turtle.Turtle()
        self._pen.hideturtle()
        self._pen.speed(0)
        self._pen.penup()

        # dedicated pen for text labels (weights, metrics, status, panels)
        self._label_pen = turtle.Turtle()
        self._label_pen.hideturtle()
        self._label_pen.speed(0)
        self._label_pen.penup()

        # wall thickness scales with cell size so lines stay proportional
        self._wall_thickness = max(2, cell_size // 10)

        # dedicated pen for wall segments and the outer grid border.
        # kept separate from _pen so its drawings persist during animation —
        # it is only cleared at the start of draw_full_grid, not per cell update.
        self._wall_pen = turtle.Turtle()
        self._wall_pen.hideturtle()
        self._wall_pen.speed(0)
        self._wall_pen.penup()

        # dedicated pen for the status bar text only.
        # kept separate from _label_pen so that clear() removes only the status
        # text without wiping panel or metrics text.
        self._status_pen = turtle.Turtle()
        self._status_pen.hideturtle()
        self._status_pen.speed(0)
        self._status_pen.penup()

    # ------------------------------------------------------------------
    # public drawing methods
    # ------------------------------------------------------------------

    def set_weighted(self, is_weighted: bool) -> None:
        """
        updates weighted display mode for this renderer.
        affects whether weight labels are shown inside cells when drawing.
        takes effect on the next draw_full_grid() or colour_cell() call.

        args:
            is_weighted (bool): True to render weight labels; False to hide them
        """
        self.is_weighted = is_weighted

    def draw_full_grid(self, grid: list) -> None:
        """
        draws every cell in the grid, redraws all panels, then calls
        screen.update() once. use this for initial draw or full redraws
        (e.g. after maze generation).

        args:
            grid (list): 2d list of Node objects
        """
        # store the grid reference so colour_cell can redraw walls later
        self._grid = grid

        self._pen.clear()
        self._label_pen.clear()
        self._wall_pen.clear()
        self._status_pen.clear()

        # fill the grid area with white before drawing cells so that the
        # screen background colour does not show through corridor spaces
        gx = self.x_offset - self.cols * self.cell_size / 2
        gy = self.y_offset  - self.rows * self.cell_size / 2
        self._draw_rectangle(gx, gy,
                             self.cols * self.cell_size,
                             self.rows * self.cell_size,
                             '#ffffff')

        for row in range(self.rows):
            for col in range(self.cols):
                node = grid[row][col]
                self._draw_cell_internal(row, col, node.state, node.weight)

        # draw wall segments for every cell after all fills are done
        for row in range(self.rows):
            for col in range(self.cols):
                self._draw_cell_walls_all(row, col)

        # draw outer grid border on top of all cells and walls
        self._draw_grid_border()

        # redraw panels so they are never lost after a full grid clear
        self._draw_left_panel_internal(self._current_algo_name)
        self._draw_right_panel_internal(self._current_runs)

        self.screen.update()

    def draw_cell(self, row: int, col: int, state: str, weight: int = 1) -> None:
        """
        draws a single cell with its fill colour and optional weight label.
        redraws wall segments on the cell and its neighbours after filling
        so that the fill does not erase wall lines.
        does NOT call screen.update() — batch with other draws for performance.

        args:
            row    (int): row index of the cell
            col    (int): column index of the cell
            state  (str): node state string (determines fill colour)
            weight (int): cell traversal weight; shown as text if > 1
        """
        self._draw_cell_internal(row, col, state, weight)

        # redraw walls so the fill does not erase wall lines
        if self._grid is not None:
            self._draw_cell_walls_all(row, col)
            for dr, dc in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                nr, nc = row + dr, col + dc
                if 0 <= nr < self.rows and 0 <= nc < self.cols:
                    self._draw_cell_walls_all(nr, nc)

    def colour_cell(self, row: int, col: int, state: str, weight: int = 1) -> None:
        """
        draws a single cell and immediately calls screen.update().
        used by the animator for step-by-step animation.

        redraws wall segments on the recoloured cell and its four neighbours
        so that wall lines are not erased by the cell fill.
        also restores the outer grid border when a perimeter cell is recoloured.

        args:
            row    (int): row index of the cell
            col    (int): column index of the cell
            state  (str): node state string
            weight (int): cell traversal weight
        """
        self._draw_cell_internal(row, col, state, weight)

        # redraw walls on this cell and adjacent cells so fill does not erase them
        if self._grid is not None:
            self._draw_cell_walls_all(row, col)
            # redraw walls on the four neighbours that share a border with this cell
            for dr, dc in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                nr, nc = row + dr, col + dc
                if 0 <= nr < self.rows and 0 <= nc < self.cols:
                    self._draw_cell_walls_all(nr, nc)

        # restore outer border if this cell sits on the grid perimeter
        if (row == 0 or row == self.rows - 1
                or col == 0 or col == self.cols - 1):
            self._draw_grid_border()
        self.screen.update()

    def draw_header(self, algo_name: str = '') -> None:
        """
        clears the left panel and redraws it with the algorithm name and
        the full key-binding legend. stores algo_name so draw_full_grid
        can redraw it after a clear.

        args:
            algo_name (str): label shown at the top of the left panel
        """
        self._current_algo_name = algo_name
        self._draw_left_panel_internal(algo_name)
        self.screen.update()

    def display_past_runs(self, runs: list) -> None:
        """
        clears the right panel and writes a table of the most recent
        algorithm executions. stores runs so draw_full_grid can redraw
        after a clear.

        args:
            runs (list): list of run dicts from Application.run_history;
                         each dict has keys: run_num, algo, path_length,
                         path_cost, nodes_visited, exec_time_ms, result
        """
        self._current_runs = runs
        self._draw_right_panel_internal(runs)
        self.screen.update()

    def display_message(self, message: str) -> None:
        """
        displays a status message in the bar at the very bottom of the screen.
        uses a dedicated pen so clear() removes only status text, leaving all
        other panel text intact.

        args:
            message (str): the status text to show
        """
        self._draw_status_background()
        grid_bottom = self._grid_bottom_y()
        # place text in the vertical centre of the status bar
        status_y = grid_bottom - BELOW_GRID_BUFFER - STATUS_HEIGHT // 2

        # clear any previous status text before writing the new one
        self._status_pen.clear()
        self._status_pen.color('#222222')
        self._status_pen.goto(self.x_offset - self.cols * self.cell_size / 2, status_y)
        self._status_pen.write(message, font=('Courier', 20, 'normal'))
        self.screen.update()

    def clear_message(self) -> None:
        """clears the status bar message."""
        self._status_pen.clear()
        self._draw_status_background()
        self.screen.update()

    def pixel_to_cell(self, px: float, py: float) -> tuple:
        """
        converts turtle pixel coordinates (px, py) to a (row, col) grid index.
        accounts for x_offset and y_offset so the grid is correctly positioned
        despite panel asymmetry.

        args:
            px (float): x pixel coordinate (turtle canvas origin)
            py (float): y pixel coordinate (turtle canvas origin)

        returns:
            (row, col) tuple if the click is within the grid, (-1, -1) otherwise
        """
        grid_left = self.x_offset - self.cols * self.cell_size / 2
        grid_top  = self.y_offset + self.rows * self.cell_size / 2

        col = int((px - grid_left) / self.cell_size)
        row = int((grid_top - py) / self.cell_size)

        if 0 <= row < self.rows and 0 <= col < self.cols:
            return (row, col)
        return (-1, -1)

    def cell_centre(self, row: int, col: int) -> tuple:
        """
        returns the (x, y) pixel coordinate of the centre of a cell.

        args:
            row (int): row index
            col (int): column index

        returns:
            (x, y) float tuple in turtle screen coordinates
        """
        x, y = self._cell_origin(row, col)
        half = self.cell_size / 2
        return (x + half, y + half)

    # ------------------------------------------------------------------
    # internal drawing helpers
    # ------------------------------------------------------------------

    def _cell_origin(self, row: int, col: int) -> tuple:
        """
        returns the (x, y) pixel coordinate of the bottom-left corner
        of the cell at (row, col). x_offset and y_offset position the
        grid correctly relative to the side panels.

        args:
            row (int): row index
            col (int): column index

        returns:
            (x, y) float tuple in turtle screen coordinates
        """
        x = self.x_offset - self.cols * self.cell_size / 2 + col * self.cell_size
        y = self.y_offset + self.rows * self.cell_size / 2 - (row + 1) * self.cell_size
        return (x, y)

    def _grid_bottom_y(self) -> float:
        """returns the y-coordinate of the bottom edge of the grid."""
        return self.y_offset - self.rows * self.cell_size / 2

    def _grid_top_y(self) -> float:
        """returns the y-coordinate of the top edge of the grid."""
        return self.y_offset + self.rows * self.cell_size / 2

    def _draw_rectangle(self, x: float, y: float,
                        width: float, height: float, colour: str) -> None:
        """
        draws a filled rectangle with the given colour starting at (x, y).

        args:
            x      (float): x coordinate of the bottom-left corner
            y      (float): y coordinate of the bottom-left corner
            width  (float): rectangle width in pixels
            height (float): rectangle height in pixels
            colour (str):   hex fill colour string
        """
        pen = self._pen
        pen.goto(x, y)
        pen.setheading(0)
        pen.fillcolor(colour)
        pen.pencolor(colour)
        pen.pendown()
        pen.begin_fill()
        pen.goto(x + width, y)
        pen.goto(x + width, y + height)
        pen.goto(x, y + height)
        pen.goto(x, y)
        pen.end_fill()
        pen.penup()

    def _draw_cell_internal(self, row: int, col: int, state: str,
                            weight: int = 1) -> None:
        """
        draws a single cell with a solid fill. pen colour matches fill colour
        so no border line appears between adjacent cells.
        all states (including 'obstacle') are rendered via STATE_COLOURS.

        args:
            row    (int): row index
            col    (int): column index
            state  (str): node state used to look up the fill colour
            weight (int): cell weight; displayed inside the cell if > 1
        """
        # all states use a solid fill — the edge-based wall model draws walls
        # as line segments on top of filled cells, not as obstacle cell fills
        colour = STATE_COLOURS.get(state, DEFAULT_COLOUR)
        x, y   = self._cell_origin(row, col)
        size   = self.cell_size

        pen = self._pen
        pen.goto(x, y)
        pen.setheading(0)
        pen.fillcolor(colour)
        pen.pencolor(colour)
        pen.pendown()
        pen.begin_fill()
        for _ in range(4):
            pen.forward(size)
            pen.left(90)
        pen.end_fill()
        pen.penup()

        # write weight label centred in the cell for weighted grids.
        # show all weights (including 1) so every cell's cost is explicit.
        # use white text on dark-background states so the label remains
        # legible; all other states have light fills that suit dark text.
        if self.is_weighted:
            label_x = x + size / 2
            label_y = y + size / 4
            label_colour = '#ffffff' if state in ('visited', 'obstacle') else '#333333'
            self._label_pen.goto(label_x, label_y)
            self._label_pen.color(label_colour)
            self._label_pen.write(
                str(weight),
                align='center',
                font=('Courier', max(16, size // 2), 'bold')
            )

    def _draw_wall_segment(self, x1: float, y1: float,
                           x2: float, y2: float) -> None:
        """
        draws a single wall line segment between two pixel coordinates
        using the dedicated wall pen.

        args:
            x1 (float): start x coordinate
            y1 (float): start y coordinate
            x2 (float): end x coordinate
            y2 (float): end y coordinate
        """
        pen = self._wall_pen
        pen.pensize(self._wall_thickness)
        pen.pencolor(WALL_COLOUR)
        pen.goto(x1, y1)
        pen.pendown()
        pen.goto(x2, y2)
        pen.penup()
        # reset pen size so subsequent border draws are not affected
        pen.pensize(1)

    def _draw_cell_walls_all(self, row: int, col: int) -> None:
        """
        draws all four wall segments for the cell at (row, col) based on
        its walls dict. outer boundary walls are skipped because they are
        covered by _draw_grid_border; only interior wall segments are drawn.

        interior walls are drawn twice in draw_full_grid (once from each
        adjacent cell) which is harmless and keeps the per-cell logic simple.

        args:
            row (int): row index of the cell
            col (int): column index of the cell
        """
        if self._grid is None:
            return

        node = self._grid[row][col]
        x, y = self._cell_origin(row, col)
        s    = self.cell_size

        # north wall — top edge of cell; skip if on the top row (outer border)
        if node.walls.get('N') and row > 0:
            self._draw_wall_segment(x, y + s, x + s, y + s)

        # south wall — bottom edge of cell; skip if on the bottom row (outer border)
        if node.walls.get('S') and row < self.rows - 1:
            self._draw_wall_segment(x, y, x + s, y)

        # west wall — left edge of cell; skip if on the leftmost column (outer border)
        if node.walls.get('W') and col > 0:
            self._draw_wall_segment(x, y, x, y + s)

        # east wall — right edge of cell; skip if on the rightmost column (outer border)
        if node.walls.get('E') and col < self.cols - 1:
            self._draw_wall_segment(x + s, y, x + s, y + s)

    def _draw_grid_border(self) -> None:
        """
        draws a rectangle outline around the entire grid to form the outer
        maze boundary wall. uses _wall_pen so the border persists during
        animation without being cleared by per-cell _pen draws.
        """
        x = self.x_offset - self.cols * self.cell_size / 2
        y = self.y_offset  - self.rows * self.cell_size / 2
        w = self.cols * self.cell_size
        h = self.rows * self.cell_size
        t = self._wall_thickness

        pen = self._wall_pen
        pen.pensize(t)
        pen.pencolor(WALL_COLOUR)
        pen.goto(x, y)
        pen.pendown()
        pen.goto(x + w, y)
        pen.goto(x + w, y + h)
        pen.goto(x,     y + h)
        pen.goto(x,     y)
        pen.penup()
        pen.pensize(1)   # reset to default so other wall_pen draws are unaffected

    def _draw_left_panel_internal(self, algo_name: str) -> None:
        """
        clears and redraws the left panel without calling screen.update().
        draws the algorithm name at the top and keybinding lines below,
        with adaptive vertical spacing to fill the available height.

        args:
            algo_name (str): algorithm name shown at the top of the panel
        """
        grid_top    = self._grid_top_y()
        grid_bottom = self._grid_bottom_y()
        grid_height = grid_top - grid_bottom

        # left panel occupies the area directly to the left of the grid
        panel_x = self.x_offset - self.cols * self.cell_size / 2 - H_MARGIN - LEFT_PANEL_WIDTH
        panel_w = LEFT_PANEL_WIDTH
        # panel spans full canvas height from above_space top to below_space bottom
        panel_y = grid_bottom - BELOW_SPACE
        panel_h = grid_height + ABOVE_SPACE + BELOW_SPACE

        # clear left panel background
        self._draw_rectangle(panel_x, panel_y, panel_w, panel_h, '#ffffff')

        # -- title block: left-aligned so long names never clip off the canvas edge --
        title_left_x = panel_x + 8
        title_y      = grid_top - 40

        self._label_pen.color('#111111')
        self._label_pen.goto(title_left_x, title_y)
        self._label_pen.write(
            "Algorithm:",
            align='left',
            font=('Courier', 24, 'bold')
        )

        # only draw the name line when an algorithm has been selected
        if algo_name:
            # 20pt for algo name display
            self._label_pen.goto(title_left_x, title_y - 34)
            self._label_pen.write(
                algo_name,
                align='left',
                font=('Courier', 20, 'bold')
            )
            title_block_bottom = title_y - 80
        else:
            # no name line — mode label sits closer to the "Algorithm:" label
            title_block_bottom = title_y - 48

        # -- weighted/unweighted mode indicator --
        mode_label = 'WEIGHTED' if self.is_weighted else 'UNWEIGHTED'
        mode_colour = '#007a33' if self.is_weighted else '#555555'
        self._label_pen.color(mode_colour)
        self._label_pen.goto(title_left_x, title_block_bottom - 2)
        self._label_pen.write(
            f'Mode: {mode_label}',
            align='left',
            font=('Courier', 16, 'bold')
        )
        title_block_bottom -= 30

        # -- keybinding lines distributed below the title block --
        key_lines = [
            'S \u2014 Set Start',
            'G \u2014 Set Goal',
            'W \u2014 Set Weight',
            'T \u2014 Toggle Weights',
            'M \u2014 Generate Maze',
            'C \u2014 Clear',
            'R \u2014 Re-run',
            '1 \u2014 BFS',
            '2 \u2014 Dijkstra',
            '+ \u2014 Speed Up',
            '- \u2014 Slow Down',
        ]
        available_height   = title_block_bottom - (grid_bottom - BELOW_SPACE + 10)
        spacing            = min(28, available_height / max(len(key_lines), 1))

        self._label_pen.color('#333333')
        for idx, line in enumerate(key_lines):
            ky = title_block_bottom - idx * spacing
            self._label_pen.goto(panel_x + 10, ky)
            self._label_pen.write(line, font=('Courier', 18, 'normal'))

    def _draw_right_panel_internal(self, runs: list) -> None:
        """
        clears and redraws the right panel without calling screen.update().
        draws a run history heading and 2-line entries for every run,
        stopping when the entries would overflow the panel bottom.

        args:
            runs (list): list of run result dicts from Application.run_history
        """
        grid_top    = self._grid_top_y()
        grid_bottom = self._grid_bottom_y()
        grid_height = grid_top - grid_bottom

        # right panel occupies the area directly to the right of the grid
        panel_x = self.x_offset + self.cols * self.cell_size / 2 + H_MARGIN
        panel_w = RIGHT_PANEL_WIDTH
        panel_y = grid_bottom - BELOW_SPACE
        panel_h = grid_height + ABOVE_SPACE + BELOW_SPACE

        # clear right panel background
        self._draw_rectangle(panel_x, panel_y, panel_w, panel_h, '#F8F8F8')

        # -- heading near grid top --
        heading_y = grid_top - 36
        self._label_pen.color('#333333')
        self._label_pen.goto(panel_x + 8, heading_y)
        self._label_pen.write("Run History:", font=('Courier', 20, 'bold'))

        # -- 3-line run entries at ~76 px per entry --
        entry_start_y = heading_y - 48
        for idx, run in enumerate(runs):
            entry_y = entry_start_y - idx * 76
            # stop drawing if this entry would overflow the panel bottom
            if entry_y < panel_y + 10:
                break
            # line 1: run number and algorithm name
            self._label_pen.goto(panel_x + 8, entry_y)
            self._label_pen.write(
                f"#{run['run_num']}  {run['algo']}",
                font=('Courier', 18, 'normal')
            )
            # line 2: path hops and nodes visited
            self._label_pen.goto(panel_x + 20, entry_y - 26)
            self._label_pen.write(
                f"Hops: {run['path_length']}   Nodes: {run['nodes_visited']}",
                font=('Courier', 18, 'normal')
            )
            # line 3: total traversal cost and execution time
            self._label_pen.goto(panel_x + 20, entry_y - 50)
            self._label_pen.write(
                f"Cost: {run['path_cost']}   Time: {run['exec_time_ms']:.2f}ms",
                font=('Courier', 18, 'normal')
            )

    def _draw_status_background(self) -> None:
        """draws a rectangle spanning the full canvas width over the status bar area."""
        grid_bottom = self._grid_bottom_y()
        status_top  = grid_bottom - BELOW_GRID_BUFFER
        # span from the left panel edge to the right panel edge
        full_left  = self.x_offset - self.cols * self.cell_size / 2 - H_MARGIN - LEFT_PANEL_WIDTH
        full_width = LEFT_PANEL_WIDTH + H_MARGIN + self.cols * self.cell_size + H_MARGIN + RIGHT_PANEL_WIDTH
        self._draw_rectangle(full_left, status_top - STATUS_HEIGHT,
                             full_width, STATUS_HEIGHT, '#E8E8E8')
