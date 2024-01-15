/*
 * Decompiled with CFR 0.152.
 */
package org.darkstorm.minecraft.gui.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import org.darkstorm.minecraft.gui.layout.Constraint;
import org.darkstorm.minecraft.gui.layout.LayoutManager;

public class GridLayoutManager
implements LayoutManager {
    private int columns;
    private int rows;

    public GridLayoutManager(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
    }

    @Override
    public void reposition(Rectangle area, Rectangle[] componentAreas, Constraint[][] constraints) {
        int componentsPerRow;
        int componentsPerColumn;
        if (componentAreas.length == 0) {
            return;
        }
        if (this.columns == 0) {
            if (this.rows == 0) {
                double square = Math.sqrt(componentAreas.length);
                componentsPerColumn = (int)square;
                componentsPerRow = (int)square;
                if (square - (double)((int)square) > 0.0) {
                    ++componentsPerColumn;
                }
            } else {
                componentsPerRow = componentAreas.length / this.rows;
                if (componentAreas.length % this.rows > 0) {
                    ++componentsPerRow;
                }
                componentsPerColumn = this.rows;
            }
        } else if (this.rows == 0) {
            componentsPerColumn = componentAreas.length / this.columns;
            if (componentAreas.length % this.columns > 0) {
                ++componentsPerColumn;
            }
            componentsPerRow = this.columns;
        } else {
            componentsPerRow = this.columns;
            componentsPerColumn = this.rows;
        }
        double elementWidth = (double)area.width / (double)componentsPerRow;
        double elementHeight = (double)area.height / (double)componentsPerColumn;
        int row = 0;
        block12: while (row < componentsPerColumn) {
            int element = 0;
            while (element < componentsPerRow) {
                int index = row * componentsPerRow + element;
                if (index >= componentAreas.length) break block12;
                Rectangle componentArea = componentAreas[index];
                Constraint[] componentConstraints = constraints[index];
                HorizontalGridConstraint horizontalAlign = HorizontalGridConstraint.LEFT;
                VerticalGridConstraint verticalAlign = VerticalGridConstraint.CENTER;
                Constraint[] constraintArray = componentConstraints;
                int n = componentConstraints.length;
                int n2 = 0;
                while (n2 < n) {
                    Constraint constraint = constraintArray[n2];
                    if (constraint instanceof HorizontalGridConstraint) {
                        horizontalAlign = (HorizontalGridConstraint)constraint;
                    } else if (constraint instanceof VerticalGridConstraint) {
                        verticalAlign = (VerticalGridConstraint)constraint;
                    }
                    ++n2;
                }
                switch (horizontalAlign) {
                    case FILL: {
                        componentArea.width = (int)elementWidth;
                    }
                    case LEFT: {
                        componentArea.x = (int)((double)area.x + (double)element * elementWidth);
                        break;
                    }
                    case RIGHT: {
                        componentArea.x = (int)((double)area.x + (double)(element + 1) * elementWidth - (double)componentArea.width);
                        break;
                    }
                    case CENTER: {
                        componentArea.x = (int)((double)area.x + (double)element * elementWidth + elementWidth / 2.0 - (double)(componentArea.width / 2));
                    }
                }
                switch (verticalAlign) {
                    case FILL: {
                        componentArea.height = (int)elementHeight;
                    }
                    case TOP: {
                        componentArea.y = (int)((double)area.y + (double)row * elementHeight);
                        break;
                    }
                    case BOTTOM: {
                        componentArea.y = (int)((double)area.y + (double)(row + 1) * elementHeight - (double)componentArea.height);
                        break;
                    }
                    case CENTER: {
                        componentArea.y = (int)((double)area.y + (double)row * elementHeight + elementHeight / 2.0 - (double)(componentArea.height / 2));
                    }
                }
                ++element;
            }
            ++row;
        }
    }

    @Override
    public Dimension getOptimalPositionedSize(Rectangle[] componentAreas, Constraint[][] constraints) {
        int componentsPerRow;
        int componentsPerColumn;
        if (componentAreas.length == 0) {
            return new Dimension(0, 0);
        }
        if (this.columns == 0) {
            if (this.rows == 0) {
                double square = Math.sqrt(componentAreas.length);
                componentsPerColumn = (int)square;
                componentsPerRow = (int)square;
                if (square - (double)((int)square) > 0.0) {
                    ++componentsPerColumn;
                }
            } else {
                componentsPerRow = componentAreas.length / this.rows;
                if (componentAreas.length % this.rows > 0) {
                    ++componentsPerRow;
                }
                componentsPerColumn = this.rows;
            }
        } else if (this.rows == 0) {
            componentsPerColumn = componentAreas.length / this.columns;
            if (componentAreas.length % this.columns > 0) {
                ++componentsPerColumn;
            }
            componentsPerRow = this.columns;
        } else {
            componentsPerRow = this.columns;
            componentsPerColumn = this.rows;
        }
        int maxElementWidth = 0;
        int maxElementHeight = 0;
        Rectangle[] rectangleArray = componentAreas;
        int n = componentAreas.length;
        int n2 = 0;
        while (n2 < n) {
            Rectangle component = rectangleArray[n2];
            maxElementWidth = Math.max(maxElementWidth, component.width);
            maxElementHeight = Math.max(maxElementHeight, component.height);
            ++n2;
        }
        return new Dimension(maxElementWidth * componentsPerRow, maxElementHeight * componentsPerColumn);
    }

    public int getColumns() {
        return this.columns;
    }

    public int getRows() {
        return this.rows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public static enum HorizontalGridConstraint implements Constraint
    {
        CENTER,
        LEFT,
        RIGHT,
        FILL;

    }

    public static enum VerticalGridConstraint implements Constraint
    {
        CENTER,
        TOP,
        BOTTOM,
        FILL;

    }
}

