package me.micartey.viro.shapes;

import javafx.scene.paint.Color;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Rectangle extends Shape {

    private Position position, size;

    public Rectangle(Color color, int width, Position position, Position size) {
        super(color, width);

        this.position = position;
        this.size = size;
    }

    @Override
    public void paint(GraphicsWrapper context) {
        context.drawRect(
                this.position.getX(),
                this.position.getY(),
                this.size.getX(),
                this.size.getY()
        );
    }

    @Override
    public Set<Position> getPoints() {
        return new HashSet<>(Arrays.asList(position, size));
    }

    @Override
    public void translate(Position vector) {
        this.position = this.position.translate(vector);
    }

    @Override
    public boolean select(Position position) {
        int width = Math.max(2, this.width >> 1);

        return isInside(
                this.position.translate(-width),
                this.position.translate(this.size).translate(width),
                position
        ) && !isInside(
                this.position.translate(width),
                this.position.translate(this.size).translate(-width),
                position
        );
    }

    private boolean isInside(Position point1, Position point2, Position position) {
        return point1.getX() < position.getX() && point1.getY() < position.getY()
                && point2.getX() > position.getX() && point2.getY() > position.getY();
    }
}
