package me.micartey.viro.shapes;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Graphic extends Shape {

    private Position middle;

    private final Image  image;
    private final double width, height;

    public Graphic(Image image, Position middle, double width, double height, Color color, int weight) {
        super(color, weight);

        this.middle = middle;
        this.image = image;
        this.height = height;
        this.width = width;
    }

    @Override
    public void paint(GraphicsWrapper context) {
        context.drawImage(
                this.image,
                this.middle.getX() - (this.width / 2),
                this.middle.getY() - (this.height / 2),
                width,
                height
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
        return isInside(this.middle, width, height, position);
    }

    private boolean isInside(Position middle, double width, double height, Position position) {
        return middle.getX() - (width / 2) < position.getX() && middle.getY() - (height / 2) < position.getY()
                && middle.getX() + (width / 2) > position.getX() && middle.getY() + (height / 2) > position.getY();
    }
}
