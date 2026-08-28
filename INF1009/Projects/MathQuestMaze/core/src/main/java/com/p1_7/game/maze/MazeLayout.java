package com.p1_7.game.maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.p1_7.game.Settings;
import com.p1_7.game.hud.HudStrip;

/**
 * immutable value object that describes the fixed spatial layout of the math maze.
 *
 * the layout reserves the four corners for answer rooms and the playfield centre
 * for the player spawn room. corridors are generated between those spaces, then
 * the remaining interior area is converted into wall rectangles so collision and
 * rendering both use the same geometry.
 */
public class MazeLayout {

    /** total width of the play area in pixels */
    private static final float SCREEN_WIDTH = Settings.getWindowWidth();

    /** total height of the play area in pixels */
    private static final float SCREEN_HEIGHT = HudStrip.PLAYFIELD_HEIGHT;

    /** thickness of the perimeter wall in pixels */
    private static final float WALL_THICKNESS = 20f;

    /** gap between the perimeter wall and each answer room */
    private static final float ROOM_MARGIN = 10f;

    /** width of each answer room rectangle */
    private static final float ROOM_WIDTH = 220f;

    /** height of each answer room rectangle */
    private static final float ROOM_HEIGHT = 160f;

    /** width shared by all corridor rectangles */
    private static final float CORRIDOR_WIDTH = 96f;

    /** width of the centre spawn room; matches the corridor width so the centre junction is flush */
    private static final float SPAWN_ROOM_WIDTH = CORRIDOR_WIDTH;

    /** height of the centre spawn room; matches the corridor width so the centre junction is flush */
    private static final float SPAWN_ROOM_HEIGHT = CORRIDOR_WIDTH;

    /** expected number of answer rooms */
    private static final int ROOM_COUNT = 4;

    /** uniform scale applied to the whole maze footprint */
    private static final float LAYOUT_SCALE = 0.88f;

    /** vertical offset applied to the whole maze after scaling; positive moves the maze upward */
    private static final float LAYOUT_OFFSET_Y = 45f;

    /** downward shift applied to corridors after scaling, without affecting room positions */
    private static final float CORRIDOR_OFFSET_Y = -30f;

    /** tolerance used to collapse float-noise seams between adjacent walkable edges */
    private static final float EDGE_MERGE_EPSILON = 0.01f;

    /** fixed y centre of the upper cross-corridor */
    private static final float TOP_HALL_CENTER_Y = 586f;

    /** fixed y centre of the lower cross-corridor */
    private static final float BOTTOM_HALL_CENTER_Y = 150f;

    /** fixed x centre of the left vertical corridor */
    private static final float LEFT_HALL_CENTER_X = 150f;

    /** fixed x centre of the right vertical corridor */
    private static final float RIGHT_HALL_CENTER_X = 1130f;

    /** fixed x centre of the north and south doors from the spawn room */
    private static final float HUB_VERTICAL_CENTER_X = SCREEN_WIDTH / 2f;

    /** fixed y centre of the east and west doors from the spawn room */
    private static final float HUB_HORIZONTAL_CENTER_Y = SCREEN_HEIGHT / 2f;

    /** spawn position as [x, y]; stored as a defensive copy */
    private final float[] spawnPoint;

    /** centre-room bounds as [x, y, w, h]; stored as a defensive copy */
    private final float[] spawnRoomBounds;

    /** four answer-room bounds, each [x, y, w, h]; stored as an unmodifiable deep copy */
    private final List<float[]> roomBounds;

    /** all maze wall bounds, including perimeter and interior blocks */
    private final List<float[]> wallBounds;

    /** corridor and pathway rectangles where free-roaming pickups may spawn */
    private final List<float[]> pathwayBounds;

    /**
     * constructs a maze layout from explicit spawn, room, and wall data.
     *
     * @param spawnPoint      two-element array [x, y] for the player spawn position
     * @param spawnRoomBounds four-element array [x, y, w, h] for the centre room
     * @param roomBounds      list of exactly four answer-room rectangles
     * @param wallBounds      list of wall rectangles for collision and rendering
     * @param pathwayBounds   list of corridor/pathway rectangles for random pickup placement
     */
    MazeLayout(float[] spawnPoint, float[] spawnRoomBounds,
               List<float[]> roomBounds, List<float[]> wallBounds, List<float[]> pathwayBounds) {
        validateSpawnPoint(spawnPoint);
        validateRect(spawnRoomBounds, "spawnRoomBounds");

        if (roomBounds == null) {
            throw new IllegalArgumentException("roomBounds must not be null");
        }
        if (roomBounds.size() != ROOM_COUNT) {
            throw new IllegalArgumentException(
                "roomBounds must contain exactly " + ROOM_COUNT + " entries, got: " + roomBounds.size());
        }
        if (wallBounds == null) {
            throw new IllegalArgumentException("wallBounds must not be null");
        }
        if (pathwayBounds == null) {
            throw new IllegalArgumentException("pathwayBounds must not be null");
        }

        List<float[]> roomCopy = new ArrayList<>(ROOM_COUNT);
        for (int i = 0; i < roomBounds.size(); i++) {
            float[] rect = roomBounds.get(i);
            validateRect(rect, "roomBounds[" + i + "]");
            roomCopy.add(rect.clone());
        }

        List<float[]> wallCopy = new ArrayList<>(wallBounds.size());
        for (int i = 0; i < wallBounds.size(); i++) {
            float[] rect = wallBounds.get(i);
            validateRect(rect, "wallBounds[" + i + "]");
            wallCopy.add(rect.clone());
        }

        List<float[]> pathwayCopy = new ArrayList<>(pathwayBounds.size());
        for (int i = 0; i < pathwayBounds.size(); i++) {
            float[] rect = pathwayBounds.get(i);
            validateRect(rect, "pathwayBounds[" + i + "]");
            pathwayCopy.add(rect.clone());
        }

        this.spawnPoint = spawnPoint.clone();
        this.spawnRoomBounds = spawnRoomBounds.clone();
        this.roomBounds = Collections.unmodifiableList(roomCopy);
        this.wallBounds = Collections.unmodifiableList(wallCopy);
        this.pathwayBounds = Collections.unmodifiableList(pathwayCopy);
    }

    /**
     * creates a maze with four fixed corner rooms, a centre spawn room, and
     * a fixed corridor topology matching the intended scene composition.
     *
     * each corner room has two routes toward the centre: one through its row and
     * one through its column. the coordinates are deterministic so the level does
     * not reshuffle between runs.
     *
     * @return a new immutable MazeLayout
     */
    public static MazeLayout createDefault() {
        float leftX = WALL_THICKNESS + ROOM_MARGIN;
        float rightX = SCREEN_WIDTH - WALL_THICKNESS - ROOM_MARGIN - ROOM_WIDTH;
        float bottomY = WALL_THICKNESS + ROOM_MARGIN;
        float topY = SCREEN_HEIGHT - WALL_THICKNESS - ROOM_MARGIN - ROOM_HEIGHT;

        List<float[]> rooms = new ArrayList<>(ROOM_COUNT);
        rooms.add(new float[]{ leftX, topY, ROOM_WIDTH, ROOM_HEIGHT });
        rooms.add(new float[]{ rightX, topY, ROOM_WIDTH, ROOM_HEIGHT });
        rooms.add(new float[]{ leftX, bottomY, ROOM_WIDTH, ROOM_HEIGHT });
        rooms.add(new float[]{ rightX, bottomY, ROOM_WIDTH, ROOM_HEIGHT });

        float spawnRoomX = (SCREEN_WIDTH - SPAWN_ROOM_WIDTH) / 2f;
        float spawnRoomY = (SCREEN_HEIGHT - SPAWN_ROOM_HEIGHT) / 2f;
        float[] spawnRoom = new float[]{ spawnRoomX, spawnRoomY, SPAWN_ROOM_WIDTH, SPAWN_ROOM_HEIGHT };
        float[] spawnPoint = new float[]{ SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f };

        float halfCorridor = CORRIDOR_WIDTH / 2f;
        float topHallBottomY = TOP_HALL_CENTER_Y - halfCorridor;
        float bottomHallTopY = BOTTOM_HALL_CENTER_Y + halfCorridor;
        float middleHallBottomY = HUB_HORIZONTAL_CENTER_Y - halfCorridor;
        float middleHallTopY = HUB_HORIZONTAL_CENTER_Y + halfCorridor;
        float leftInnerCutCenterX = (leftX + ROOM_WIDTH + spawnRoomX) / 2f;
        float rightInnerCutCenterX = (spawnRoomX + SPAWN_ROOM_WIDTH + rightX) / 2f;
        List<float[]> walkable = new ArrayList<>();
        walkable.addAll(rooms);
        walkable.add(spawnRoom);

        // top and bottom row corridors
        walkable.add(new float[]{
            leftX + ROOM_WIDTH,
            TOP_HALL_CENTER_Y - halfCorridor,
            rightX - (leftX + ROOM_WIDTH),
            CORRIDOR_WIDTH
        });
        walkable.add(new float[]{
            leftX + ROOM_WIDTH,
            BOTTOM_HALL_CENTER_Y - halfCorridor,
            rightX - (leftX + ROOM_WIDTH),
            CORRIDOR_WIDTH
        });

        // left and right column corridors
        walkable.add(new float[]{
            LEFT_HALL_CENTER_X - halfCorridor,
            bottomY + ROOM_HEIGHT,
            CORRIDOR_WIDTH,
            topY - (bottomY + ROOM_HEIGHT)
        });
        walkable.add(new float[]{
            RIGHT_HALL_CENTER_X - halfCorridor,
            bottomY + ROOM_HEIGHT,
            CORRIDOR_WIDTH,
            topY - (bottomY + ROOM_HEIGHT)
        });

        // spokes from the centre room to each corridor band
        walkable.add(new float[]{
            HUB_VERTICAL_CENTER_X - halfCorridor,
            spawnRoomY + SPAWN_ROOM_HEIGHT,
            CORRIDOR_WIDTH,
            (TOP_HALL_CENTER_Y - halfCorridor) - (spawnRoomY + SPAWN_ROOM_HEIGHT)
        });
        walkable.add(new float[]{
            HUB_VERTICAL_CENTER_X - halfCorridor,
            BOTTOM_HALL_CENTER_Y + halfCorridor,
            CORRIDOR_WIDTH,
            spawnRoomY - (BOTTOM_HALL_CENTER_Y + halfCorridor)
        });
        walkable.add(new float[]{
            LEFT_HALL_CENTER_X + halfCorridor,
            HUB_HORIZONTAL_CENTER_Y - halfCorridor,
            spawnRoomX - (LEFT_HALL_CENTER_X + halfCorridor),
            CORRIDOR_WIDTH
        });
        walkable.add(new float[]{
            spawnRoomX + SPAWN_ROOM_WIDTH,
            HUB_HORIZONTAL_CENTER_Y - halfCorridor,
            RIGHT_HALL_CENTER_X - halfCorridor - (spawnRoomX + SPAWN_ROOM_WIDTH),
            CORRIDOR_WIDTH
        });

        // additional cuts through the four inner wall blocks around the spawn room
        walkable.add(new float[]{
            leftInnerCutCenterX - halfCorridor,
            middleHallTopY,
            CORRIDOR_WIDTH,
            topHallBottomY - middleHallTopY
        });
        walkable.add(new float[]{
            rightInnerCutCenterX - halfCorridor,
            middleHallTopY,
            CORRIDOR_WIDTH,
            topHallBottomY - middleHallTopY
        });
        walkable.add(new float[]{
            leftInnerCutCenterX - halfCorridor,
            bottomHallTopY,
            CORRIDOR_WIDTH,
            middleHallBottomY - bottomHallTopY
        });
        walkable.add(new float[]{
            rightInnerCutCenterX - halfCorridor,
            bottomHallTopY,
            CORRIDOR_WIDTH,
            middleHallBottomY - bottomHallTopY
        });

        List<float[]> scaledRooms = transformRects(rooms);
        float[] scaledSpawnRoom = transformRect(spawnRoom);
        float[] scaledSpawnPoint = transformPoint(spawnPoint);
        List<float[]> scaledWalkable =
            new ArrayList<>(scaledRooms.size() + 1 + (walkable.size() - rooms.size() - 1));
        List<float[]> scaledPathways = new ArrayList<>(walkable.size() - rooms.size() - 1);
        scaledWalkable.addAll(scaledRooms);
        scaledWalkable.add(scaledSpawnRoom);
        for (int i = rooms.size() + 1; i < walkable.size(); i++) {
            float[] rect = transformRect(walkable.get(i));
            int ci = i - rooms.size() - 1;

            // shift all corridors downward
            rect[1] += CORRIDOR_OFFSET_Y;

            // outer vertical corridors: extend height upward to bridge the gap with the top rooms
            if (ci == 2 || ci == 3) {
                rect[3] -= CORRIDOR_OFFSET_Y;
            }

            // middle horizontal corridors: cancel the downward shift to realign with the spawn room
            if (ci == 6 || ci == 7) {
                rect[1] -= CORRIDOR_OFFSET_Y;
            }

            // hub-to-bottom spoke: shifted down so its top no longer reaches the spawn room —
            // extend height upward to close the gap
            if (ci == 5) {
                rect[3] -= CORRIDOR_OFFSET_Y;
            }

            // top inner cuts (ci 8, 9): bottom should meet the unshifted middle horizontal —
            // raise the bottom edge up while keeping the top aligned with the top row corridor
            if (ci == 8 || ci == 9) {
                rect[1] -= CORRIDOR_OFFSET_Y;
                rect[3] += CORRIDOR_OFFSET_Y;
            }

            // bottom inner cuts (ci 10, 11): top should meet the unshifted middle horizontal —
            // extend height upward to close the gap
            if (ci == 10 || ci == 11) {
                rect[3] -= CORRIDOR_OFFSET_Y;
            }

            scaledWalkable.add(rect);
            scaledPathways.add(rect.clone());
        }

        List<float[]> walls = buildWallBounds(scaledWalkable);
        return new MazeLayout(scaledSpawnPoint, scaledSpawnRoom, scaledRooms, walls, scaledPathways);
    }

    /**
     * returns the player spawn position.
     *
     * @return a new two-element array [x, y]
     */
    public float[] getSpawnPoint() {
        return spawnPoint.clone();
    }

    /**
     * returns the centre spawn-room bounds.
     *
     * @return a new four-element array [x, y, w, h]
     */
    public float[] getSpawnRoomBounds() {
        return spawnRoomBounds.clone();
    }

    /**
     * returns the bounding rectangle for the answer room at the given index.
     *
     * @param roomIndex index in the range 0..3
     * @return a new four-element array [x, y, w, h]
     */
    public float[] getRoomBounds(int roomIndex) {
        if (roomIndex < 0 || roomIndex >= ROOM_COUNT) {
            throw new IllegalArgumentException(
                "roomIndex must be in [0, " + (ROOM_COUNT - 1) + "], got: " + roomIndex);
        }
        return roomBounds.get(roomIndex).clone();
    }

    /**
     * returns deep copies of all answer-room rectangles.
     *
     * @return unmodifiable list of four [x, y, w, h] arrays
     */
    public List<float[]> getAllRoomBounds() {
        return copyRects(roomBounds);
    }

    /**
     * returns deep copies of all wall rectangles.
     *
     * @return unmodifiable list of [x, y, w, h] arrays
     */
    public List<float[]> getWallBounds() {
        return copyRects(wallBounds);
    }

    /**
     * returns deep copies of all corridor/pathway rectangles.
     *
     * these exclude the answer rooms and centre spawn room, making them suitable
     * for random pickup placement within the maze pathways.
     *
     * @return unmodifiable list of [x, y, w, h] pathway rectangles
     */
    public List<float[]> getPathwayBounds() {
        return copyRects(pathwayBounds);
    }

    private static void validateSpawnPoint(float[] spawnPoint) {
        if (spawnPoint == null) {
            throw new IllegalArgumentException("spawnPoint must not be null");
        }
        if (spawnPoint.length != 2) {
            throw new IllegalArgumentException(
                "spawnPoint must have exactly 2 elements, got: " + spawnPoint.length);
        }
    }

    private static void validateRect(float[] rect, String label) {
        if (rect == null) {
            throw new IllegalArgumentException(label + " must not be null");
        }
        if (rect.length != 4) {
            throw new IllegalArgumentException(label + " must have exactly 4 elements, got: " + rect.length);
        }
    }

    private static List<float[]> copyRects(List<float[]> rects) {
        List<float[]> copy = new ArrayList<>(rects.size());
        for (float[] rect : rects) {
            copy.add(rect.clone());
        }
        return Collections.unmodifiableList(copy);
    }

    private static List<float[]> transformRects(List<float[]> rects) {
        List<float[]> transformed = new ArrayList<>(rects.size());
        for (float[] rect : rects) {
            transformed.add(transformRect(rect));
        }
        return transformed;
    }

    private static float[] transformRect(float[] rect) {
        float centreX = rect[0] + rect[2] / 2f;
        float centreY = rect[1] + rect[3] / 2f;
        float[] transformedCentre = transformPoint(new float[]{ centreX, centreY });
        float scaledWidth = rect[2] * LAYOUT_SCALE;
        float scaledHeight = rect[3] * LAYOUT_SCALE;
        return new float[]{
            transformedCentre[0] - scaledWidth / 2f,
            transformedCentre[1] - scaledHeight / 2f,
            scaledWidth,
            scaledHeight
        };
    }

    private static float[] transformPoint(float[] point) {
        float transformedX = SCREEN_WIDTH / 2f + (point[0] - SCREEN_WIDTH / 2f) * LAYOUT_SCALE;
        float transformedY = SCREEN_HEIGHT / 2f + (point[1] - SCREEN_HEIGHT / 2f) * LAYOUT_SCALE + LAYOUT_OFFSET_Y;
        return new float[]{ transformedX, transformedY };
    }

    private static List<float[]> buildWallBounds(List<float[]> walkableRects) {
        List<float[]> walls = new ArrayList<>();

        // perimeter walls
        walls.add(new float[]{ 0f, 0f, SCREEN_WIDTH, WALL_THICKNESS });
        walls.add(new float[]{ 0f, SCREEN_HEIGHT - WALL_THICKNESS, SCREEN_WIDTH, WALL_THICKNESS });
        walls.add(new float[]{ 0f, 0f, WALL_THICKNESS, SCREEN_HEIGHT });
        walls.add(new float[]{ SCREEN_WIDTH - WALL_THICKNESS, 0f, WALL_THICKNESS, SCREEN_HEIGHT });

        List<Float> rawXBreaks = new ArrayList<>();
        List<Float> rawYBreaks = new ArrayList<>();
        rawXBreaks.add(WALL_THICKNESS);
        rawXBreaks.add(SCREEN_WIDTH - WALL_THICKNESS);
        rawYBreaks.add(WALL_THICKNESS);
        rawYBreaks.add(SCREEN_HEIGHT - WALL_THICKNESS);

        for (float[] rect : walkableRects) {
            rawXBreaks.add(rect[0]);
            rawXBreaks.add(rect[0] + rect[2]);
            rawYBreaks.add(rect[1]);
            rawYBreaks.add(rect[1] + rect[3]);
        }

        List<Float> xs = collapseBreaks(rawXBreaks);
        List<Float> ys = collapseBreaks(rawYBreaks);
        List<float[]> normalizedWalkableRects = normalizeRects(walkableRects, xs, ys);
        boolean[][] wallCells = new boolean[ys.size() - 1][xs.size() - 1];

        for (int row = 0; row < ys.size() - 1; row++) {
            float y = ys.get(row);
            float h = ys.get(row + 1) - y;
            for (int col = 0; col < xs.size() - 1; col++) {
                float x = xs.get(col);
                float w = xs.get(col + 1) - x;
                float centreX = x + w / 2f;
                float centreY = y + h / 2f;
                wallCells[row][col] = !isInsideAnyRect(centreX, centreY, normalizedWalkableRects);
            }
        }

        boolean[][] used = new boolean[wallCells.length][wallCells[0].length];
        for (int row = 0; row < wallCells.length; row++) {
            for (int col = 0; col < wallCells[row].length; col++) {
                if (!wallCells[row][col] || used[row][col]) {
                    continue;
                }

                int spanWidth = 1;
                while (col + spanWidth < wallCells[row].length
                        && wallCells[row][col + spanWidth]
                        && !used[row][col + spanWidth]) {
                    spanWidth++;
                }

                int spanHeight = 1;
                boolean canGrow = true;
                while (row + spanHeight < wallCells.length && canGrow) {
                    for (int scan = col; scan < col + spanWidth; scan++) {
                        if (!wallCells[row + spanHeight][scan] || used[row + spanHeight][scan]) {
                            canGrow = false;
                            break;
                        }
                    }
                    if (canGrow) {
                        spanHeight++;
                    }
                }

                for (int r = row; r < row + spanHeight; r++) {
                    for (int c = col; c < col + spanWidth; c++) {
                        used[r][c] = true;
                    }
                }

                walls.add(new float[]{
                    xs.get(col),
                    ys.get(row),
                    xs.get(col + spanWidth) - xs.get(col),
                    ys.get(row + spanHeight) - ys.get(row)
                });
            }
        }

        return walls;
    }

    private static List<Float> collapseBreaks(List<Float> rawBreaks) {
        TreeSet<Float> sortedBreaks = new TreeSet<>(rawBreaks);
        List<Float> collapsed = new ArrayList<>(sortedBreaks.size());
        for (float value : sortedBreaks) {
            if (!collapsed.isEmpty()
                    && Math.abs(value - collapsed.get(collapsed.size() - 1)) <= EDGE_MERGE_EPSILON) {
                continue;
            }
            collapsed.add(value);
        }
        return collapsed;
    }

    private static List<float[]> normalizeRects(List<float[]> rects, List<Float> xs, List<Float> ys) {
        List<float[]> normalized = new ArrayList<>(rects.size());
        for (float[] rect : rects) {
            float startX = snapToBreak(rect[0], xs);
            float endX = snapToBreak(rect[0] + rect[2], xs);
            float startY = snapToBreak(rect[1], ys);
            float endY = snapToBreak(rect[1] + rect[3], ys);
            normalized.add(new float[]{ startX, startY, endX - startX, endY - startY });
        }
        return normalized;
    }

    private static float snapToBreak(float value, List<Float> breaks) {
        float snapped = value;
        float bestDistance = Float.MAX_VALUE;
        for (float candidate : breaks) {
            float distance = Math.abs(candidate - value);
            if (distance < bestDistance) {
                bestDistance = distance;
                snapped = candidate;
            }
        }
        return snapped;
    }

    private static boolean isInsideAnyRect(float x, float y, List<float[]> rects) {
        for (float[] rect : rects) {
            if (x >= rect[0] && x < rect[0] + rect[2]
                    && y >= rect[1] && y < rect[1] + rect[3]) {
                return true;
            }
        }
        return false;
    }
}
