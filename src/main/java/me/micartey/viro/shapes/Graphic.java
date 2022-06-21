package me.micartey.viro.shapes;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Graphic extends Shape {

    private Position middle;

    private final Image  image;
    private final double size;

    public Graphic(Image image, Position middle, int size, Color color, int width) {
        super(color, width);

        this.middle = middle;
        this.image = image;
        this.size = size;
    }

    @Override
    public void paint(GraphicsWrapper context) {
        context.drawImage(
                this.image,
                this.middle.getX() - (this.size / 2),
                this.middle.getY() - (this.size / 2),
                size,
                size
        );
    }

    @Override
    public Set<Position> getPoints() {
        return new HashSet<>(Collections.singletonList(this.middle));
    }

    @Override
    public void translate(Position vector) {
        this.middle = middle.translate(vector);
    }

    @Override
    public boolean select(Position position) {
        return isInside(this.middle, size, position);
    }

    private boolean isInside(Position middle, double size, Position position) {
        return middle.getX() - (size / 2) < position.getX() && middle.getY() - (size / 2) < position.getY()
                && middle.getX() + (size / 2) > position.getX() && middle.getY() + (size / 2) > position.getY();
    }
}
