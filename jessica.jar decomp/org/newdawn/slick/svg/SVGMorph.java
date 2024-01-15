/*
 * Decompiled with CFR 0.152.
 */
package org.newdawn.slick.svg;

import java.util.ArrayList;
import org.newdawn.slick.geom.MorphShape;
import org.newdawn.slick.svg.Diagram;
import org.newdawn.slick.svg.Figure;

public class SVGMorph
extends Diagram {
    private ArrayList figures = new ArrayList();

    public SVGMorph(Diagram diagram) {
        super(diagram.getWidth(), diagram.getHeight());
        int i = 0;
        while (i < diagram.getFigureCount()) {
            Figure figure = diagram.getFigure(i);
            Figure copy = new Figure(figure.getType(), new MorphShape(figure.getShape()), figure.getData(), figure.getTransform());
            this.figures.add(copy);
            ++i;
        }
    }

    public void addStep(Diagram diagram) {
        if (diagram.getFigureCount() != this.figures.size()) {
            throw new RuntimeException("Mismatched diagrams, missing ids");
        }
        int i = 0;
        while (i < diagram.getFigureCount()) {
            Figure figure = diagram.getFigure(i);
            String id = figure.getData().getMetaData();
            int j = 0;
            while (j < this.figures.size()) {
                Figure existing = (Figure)this.figures.get(j);
                if (existing.getData().getMetaData().equals(id)) {
                    MorphShape morph = (MorphShape)existing.getShape();
                    morph.addShape(figure.getShape());
                    break;
                }
                ++j;
            }
            ++i;
        }
    }

    public void setExternalDiagram(Diagram diagram) {
        int i = 0;
        while (i < this.figures.size()) {
            Figure figure = (Figure)this.figures.get(i);
            int j = 0;
            while (j < diagram.getFigureCount()) {
                Figure newBase = diagram.getFigure(j);
                if (newBase.getData().getMetaData().equals(figure.getData().getMetaData())) {
                    MorphShape shape = (MorphShape)figure.getShape();
                    shape.setExternalFrame(newBase.getShape());
                    break;
                }
                ++j;
            }
            ++i;
        }
    }

    public void updateMorphTime(float delta) {
        int i = 0;
        while (i < this.figures.size()) {
            Figure figure = (Figure)this.figures.get(i);
            MorphShape shape = (MorphShape)figure.getShape();
            shape.updateMorphTime(delta);
            ++i;
        }
    }

    public void setMorphTime(float time) {
        int i = 0;
        while (i < this.figures.size()) {
            Figure figure = (Figure)this.figures.get(i);
            MorphShape shape = (MorphShape)figure.getShape();
            shape.setMorphTime(time);
            ++i;
        }
    }

    @Override
    public int getFigureCount() {
        return this.figures.size();
    }

    @Override
    public Figure getFigure(int index) {
        return (Figure)this.figures.get(index);
    }
}

