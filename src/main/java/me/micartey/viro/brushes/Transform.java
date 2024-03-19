package me.micartey.viro.brushes;

import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.window.Window;
import me.micartey.viro.shapes.utilities.Position;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class Transform extends Brush {

    public Transform(@Value("${viro.brush.transform.icon}") String icon, @Value("${viro.brush.transform.name}") String name, JationObserver observer) {
        super(icon, name, observer);
    }

    @Observe
    public void onDrag(MouseDragEvent event, Window window) {
        Position translation = event.getSource().direction(event.getDestination());

        Stream.concat(window.getVisible().stream(), window.getInvisible().stream()).forEach(shape -> {
            shape.translate(translation);
        });

        window.repaint();
    }
}
