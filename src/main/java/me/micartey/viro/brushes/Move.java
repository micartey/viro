package me.micartey.viro.brushes;

import javafx.scene.paint.Color;
import lombok.Getter;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.window.Window;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Move extends Brush {

    @Getter private final List<Shape> shapes;

    @Autowired
    private Selection selection;

    public Move(@Value("${viro.brush.move.name}") String name, @Value("${viro.brush.move.icon}") String icon, JationObserver observer) {
        super(icon, name, observer);

        this.shapes = new LinkedList<>();
    }

    @Observe
    public void onPress(MousePressEvent event, Window window) {
        List<Shape> collected = window.getVisible().stream().filter(shape -> shape.select(event.getPosition()))
                .distinct()
                .collect(Collectors.toList());

        collected.addAll(this.selection.getShapes());

        window.getVisible().removeAll(collected);
        window.repaint();

        this.shapes.clear();
        this.selection.getShapes().clear();

        collected.stream().distinct().forEach(this.shapes::add);
    }

    @Observe
    public void onMove(MouseDragEvent event, Window window) {
        Position translation = event.getSource().direction(event.getDestination());

        Color color = window.getPreviewGraphics().getColor();
        window.getPreviewGraphics().reset();

        this.shapes.forEach(shape -> {
            shape.translate(translation);
            shape.draw(window.getPreviewGraphics());
        });

        window.getPreviewGraphics().setColor(color);
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, Window window) {
        window.getVisible().addAll(this.shapes);

        this.shapes.forEach(shape -> {
            shape.draw(new GraphicsWrapper(
                    window.getGraphicsContext2D()
            ));
        });

        window.getPreviewGraphics().reset();
    }
}
