package me.micartey.viro.brushes;

import javafx.scene.paint.Color;
import lombok.Getter;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.Canvas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Move extends Brush {

    @Getter private final List<Shape> shapes;

    public Move(@Value("${viro.brush.move.name}") String name, @Value("${viro.brush.move.icon}") String icon, JationObserver observer) {
        super(icon, name, observer);

        this.shapes = new LinkedList<>();
    }

    @Observe
    public void onPress(MousePressEvent event, Canvas canvas) {
        List<Shape> collected = canvas.getVisible().stream().filter(shape -> shape.select(event.getPosition()))
                .distinct()
                .collect(Collectors.toList());

//        collected.addAll(selection.getShapes()); // TODO:

        canvas.getVisible().removeAll(collected);
        canvas.repaint();

        this.shapes.clear();
//        selection.getShapes().clear();

        this.shapes.addAll(collected);
    }

    @Observe
    public void onMove(MouseDragEvent event, Canvas canvas) {
        Position translation = event.getSource().direction(event.getDestination());

        Color color = canvas.getPreviewGraphics().getColor();
        canvas.getPreviewGraphics().reset();

        this.shapes.forEach(shape -> {
            shape.translate(translation);
            shape.draw(canvas.getPreviewGraphics());
        });

        canvas.getPreviewGraphics().setColor(color);
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, Canvas canvas) {
        canvas.getVisible().addAll(this.shapes);
        canvas.repaint();
    }
}
