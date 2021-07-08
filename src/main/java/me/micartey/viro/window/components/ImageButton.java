package me.micartey.viro.window.components;

import javafx.scene.image.Image;
import lombok.Getter;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;

import java.util.function.Consumer;

public class ImageButton {

    private final GraphicsWrapper graphics;
    private final Image           image;

    private final Consumer<Position> action;

    @Getter private final Position position, size;

    public ImageButton(GraphicsWrapper graphics, Image image, Position position, Position size, Consumer<Position> action) {
        this.graphics = graphics;
        this.position = position;
        this.action = action;
        this.image = image;
        this.size = size;

        this.setup();
    }

    private void setup() {
        this.graphics.drawImage(this.image,
                position.getX() + 2.5,
                position.getY() + 2.5,
                size.getX() - 5,
                size.getY() - 5
        );
    }

    public void trigger(Position hit) {
        if (hit.getX() >= this.position.getX()
                && hit.getY() >= this.position.getY()
                && hit.getX() <= this.position.getX() + this.size.getX()
                && hit.getY() <= this.position.getY() + this.size.getY())
            this.action.accept(hit);
    }
}
