package com.p1_7.game.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.input.ActionId;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * UI row displaying an action label and two editable key-binding cells (primary and alternate).
 * the owning scene drives hover/active state via setHoveredColumn and setActiveColumn,
 * then reads the updated key codes after a remap.
 */
public final class RemapSlot extends Entity implements IRenderable {

    public static final float TABLE_WIDTH = 560f;
    public static final float ACTION_COLUMN_WIDTH = 190f;
    public static final float KEY_COLUMN_WIDTH = 170f;
    public static final float CELL_HEIGHT = 30f;
    public static final float CELL_GAP = 15f;

    private static final Color CELL_FILL = new Color(0.54f, 0.58f, 0.86f, 0.94f);
    private static final Color CELL_HOVER = new Color(0.66f, 0.71f, 0.95f, 0.98f);
    private static final Color CELL_ACTIVE = new Color(0.93f, 0.77f, 0.36f, 1f);
    private static final Color CELL_BORDER = new Color(0.90f, 0.94f, 1.0f, 1f);
    private static final Color TEXT_COLOR = new Color(0.12f, 0.16f, 0.28f, 1f);

    /** identifies which of the two key-binding columns a remap operation targets */
    public enum BindingColumn {
        PRIMARY("Primary"),
        ALTERNATE("Alternate");

        private final String label;

        BindingColumn(String label) {
            this.label = label;
        }

        /** returns the column header shown in the remap table */
        public String getLabel() {
            return label;
        }
    }

    private final String label;
    private final ActionId actionId;
    private final BitmapFont font;
    private final float actionColumnLeft;
    private final float actionColumnWidth;
    private final float cellBaselineY;
    private final float primaryCellX;
    private final float alternateCellX;
    private final float cellY;

    private int primaryKeyCode;
    private int alternateKeyCode;
    private BindingColumn hoveredColumn;
    private BindingColumn activeColumn;

    /**
     * constructs a remap slot row centred at (centreX, centreY).
     *
     * @param label            display name of the game action
     * @param actionId         the action whose bindings this slot controls
     * @param primaryKeyCode   initial primary key (libGDX Input.Keys constant)
     * @param alternateKeyCode initial alternate key (libGDX Input.Keys constant)
     * @param centreX          horizontal centre of the table in world coordinates
     * @param centreY          vertical centre of this row in world coordinates
     * @param font             BitmapFont owned by the scene
     */
    public RemapSlot(String label,
              ActionId actionId,
              int primaryKeyCode,
              int alternateKeyCode,
              float centreX,
              float centreY,
              BitmapFont font) {
        float tableLeft = centreX - TABLE_WIDTH / 2f;
        this.label = label;
        this.actionId = actionId;
        this.primaryKeyCode = primaryKeyCode;
        this.alternateKeyCode = alternateKeyCode;
        this.font = font;
        this.actionColumnLeft = tableLeft;
        this.actionColumnWidth = ACTION_COLUMN_WIDTH;
        this.primaryCellX = tableLeft + ACTION_COLUMN_WIDTH + CELL_GAP;
        this.alternateCellX = primaryCellX + KEY_COLUMN_WIDTH + CELL_GAP;
        this.cellY = centreY - CELL_HEIGHT / 2f;
        this.cellBaselineY = centreY + 8f;
    }

    /** returns the display name of the game action. */
    public String getLabel() {
        return label;
    }

    /** returns the action identifier for this slot. */
    public ActionId getActionId() {
        return actionId;
    }

    /** returns the current primary key code (libGDX Input.Keys constant). */
    public int getPrimaryKeyCode() {
        return primaryKeyCode;
    }

    /** returns the current alternate key code (libGDX Input.Keys constant). */
    public int getAlternateKeyCode() {
        return alternateKeyCode;
    }

    /**
     * returns the column whose cell contains (x, y), or null if neither cell is hit.
     *
     * @param x world-space x coordinate
     * @param y world-space y coordinate
     * @return the hit column, or null
     */
    public BindingColumn hitTest(float x, float y) {
        if (contains(primaryCellX, y, x)) {
            return BindingColumn.PRIMARY;
        }
        if (contains(alternateCellX, y, x)) {
            return BindingColumn.ALTERNATE;
        }
        return null;
    }

    private boolean contains(float cellX, float y, float x) {
        return x >= cellX && x <= cellX + KEY_COLUMN_WIDTH
            && y >= cellY && y <= cellY + CELL_HEIGHT;
    }

    /**
     * marks the given column as hovered; pass null to clear.
     *
     * @param column the column to highlight, or null
     */
    public void setHoveredColumn(BindingColumn column) {
        this.hoveredColumn = column;
    }

    /**
     * marks the given column as awaiting a key press; pass null to cancel.
     *
     * @param column the column to activate, or null
     */
    public void setActiveColumn(BindingColumn column) {
        this.activeColumn = column;
    }

    /**
     * returns the column whose current binding matches keyCode, or null if neither does.
     *
     * @param keyCode the libGDX Input.Keys constant to look up
     * @return the matching column, or null
     */
    public BindingColumn findColumnForKey(int keyCode) {
        if (primaryKeyCode == keyCode) {
            return BindingColumn.PRIMARY;
        }
        if (alternateKeyCode == keyCode) {
            return BindingColumn.ALTERNATE;
        }
        return null;
    }

    /**
     * returns the key code for the given column.
     *
     * @param column the column to query
     * @return the libGDX Input.Keys constant for that column
     */
    public int getKeyCode(BindingColumn column) {
        return column == BindingColumn.PRIMARY ? primaryKeyCode : alternateKeyCode;
    }

    /**
     * returns the key code for the column opposite to the given one.
     * column must not be null.
     *
     * @param column the reference column
     * @return the key code of the other column
     */
    public int getOtherKeyCode(BindingColumn column) {
        return column == BindingColumn.PRIMARY ? alternateKeyCode : primaryKeyCode;
    }

    /**
     * overwrites the key code for the given column.
     *
     * @param column  the column to update
     * @param keyCode the new libGDX Input.Keys constant
     */
    public void setKeyCode(BindingColumn column, int keyCode) {
        if (column == BindingColumn.PRIMARY) {
            primaryKeyCode = keyCode;
        } else {
            alternateKeyCode = keyCode;
        }
    }

    @Override
    public String getAssetPath() {
        return null;
    }

    @Override
    public ITransform getTransform() {
        return null;
    }

    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdxCtx = (GdxDrawContext) ctx;
        GlyphLayout actionLayout = new GlyphLayout(font, label);
        gdxCtx.drawFont(font, label,
            actionColumnLeft + (actionColumnWidth - actionLayout.width) / 2f,
            cellBaselineY);

        renderCell(gdxCtx, BindingColumn.PRIMARY, primaryCellX, displayText(BindingColumn.PRIMARY));
        renderCell(gdxCtx, BindingColumn.ALTERNATE, alternateCellX, displayText(BindingColumn.ALTERNATE));
    }

    private void renderCell(GdxDrawContext gdxCtx, BindingColumn column, float cellX, String text) {
        Color fill = CELL_FILL;
        if (activeColumn == column) {
            fill = CELL_ACTIVE;
        } else if (hoveredColumn == column) {
            fill = CELL_HOVER;
        }

        gdxCtx.rect(CELL_BORDER, cellX - 2f, cellY - 2f, KEY_COLUMN_WIDTH + 4f, CELL_HEIGHT + 4f, true);
        gdxCtx.rect(fill, cellX, cellY, KEY_COLUMN_WIDTH, CELL_HEIGHT, true);

        Color originalColor = font.getColor().cpy();
        font.setColor(TEXT_COLOR);
        GlyphLayout keyLayout = new GlyphLayout(font, text);
        gdxCtx.drawFont(font, text,
            cellX + (KEY_COLUMN_WIDTH - keyLayout.width) / 2f,
            cellBaselineY);
        font.setColor(originalColor);
    }

    private String displayText(BindingColumn column) {
        if (activeColumn == column) {
            return "PRESS KEY";
        }
        return formatKey(getKeyCode(column));
    }

    private String formatKey(int keyCode) {
        String keyText = Input.Keys.toString(keyCode);
        if (keyText == null || keyText.trim().isEmpty()) {
            return "UNKNOWN";
        }
        return keyText.toUpperCase();
    }
}
