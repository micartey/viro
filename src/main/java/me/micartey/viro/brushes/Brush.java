package me.micartey.viro.brushes;

import javafx.scene.image.Image;
import lombok.Getter;
import me.micartey.jation.JationObserver;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Shape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public abstract class Brush {

    @Getter private final String name;
    @Getter private final Image  icon;

    @Autowired
    private ApplicationContext context;

    public Brush(String icon, String name, JationObserver observer) {
        observer.subscribe(this);
        this.name = name;

        this.icon = new Image(
                Objects.requireNonNull(Brush.class.getResourceAsStream(icon))
        );
    }

    protected void submit(Shape shape) {
        context.publishEvent(new ShapeSubmitEvent(shape));
    }
}
