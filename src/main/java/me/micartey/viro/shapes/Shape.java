package me.micartey.viro.shapes;

import javafx.scene.paint.Color;
import me.micartey.viro.shapes.properties.Drawable;
import me.micartey.viro.shapes.properties.Movable;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;

import java.io.Serializable;
import java.util.Set;

public abstract class Shape implements Drawable, Movable, Serializable {

    protected final Color color;
    protected final int   width;

    public Shape(Color color, int width) {
        this.color = color;
        this.width = width;
    }

    @Override
    public void draw(GraphicsWrapper context) {
        context.setLineWidth(this.width);
        context.setColor(this.color);

        this.paint(context);
    }

    public abstract void paint(GraphicsWrapper context);

    public abstract Set<Position> getPoints();
}
