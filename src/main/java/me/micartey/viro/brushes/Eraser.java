package me.micartey.viro.brushes;

import javafx.scene.paint.Color;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.window.Canvas;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Order(1)
@Component
public class Eraser extends Brush {

    @Value("${viro.brush.eraser.highlighter.radius}")
    private Integer radius;

    public Eraser(@Value("${viro.brush.eraser.icon}") String icon, @Value("${viro.brush.eraser.name}") String name, JationObserver observer) {
        super(icon, name, observer);
    }

    @Observe
    public void onDrag(MouseDragEvent event, Canvas canvas) {
        this.drawHighlighter(
                event.getDestination(),
                canvas.getPreviewGraphics(),
                this.radius
        );

        List<Shape> shapes = canvas.getVisible().stream()
                .filter(shape -> shape.select(event.getDestination()))
                .collect(Collectors.toList());

        if (shapes.isEmpty())
            return;

        canvas.getVisible().removeAll(shapes);
        canvas.getInvisible().addAll(shapes);
        canvas.repaint();
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, Canvas canvas) {
        canvas.getPreviewGraphics().reset();
    }

    private void drawHighlighter(Position position, GraphicsWrapper graphics, int radius) {
        graphics.reset();

        int width = graphics.getLineWidth();
        Color color = graphics.getColor();

        graphics.setLineWidth(4);
        graphics.setColor(Color.BLACK);

        graphics.drawOval(
                position.getX() - radius,
                position.getY() - radius,
                radius * 2,
                radius * 2
        );

        graphics.setLineWidth(2);
        graphics.setColor(Color.WHITESMOKE);

        graphics.drawOval(
                position.getX() - radius,
                position.getY() - radius,
                radius * 2,
                radius * 2
        );

        graphics.setLineWidth(width);
        graphics.setColor(color);
    }
}
