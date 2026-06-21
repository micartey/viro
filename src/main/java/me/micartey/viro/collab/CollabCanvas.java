package me.micartey.viro.collab;

import javafx.application.Platform;
import me.micartey.viro.shapes.Graphic;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.window.Canvas;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
final class CollabCanvas {

    private final Canvas canvas;

    CollabCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    List<Shape> visibleShapes() {
        synchronized (canvas.getVisible()) {
            return canvas.getVisible().stream()
                    .filter(shape -> !(shape instanceof Graphic))
                    .toList();
        }
    }

    boolean isVisible(Shape shape) {
        synchronized (canvas.getVisible()) {
            return canvas.getVisible().contains(shape);
        }
    }

    void add(Shape shape) {
        synchronized (canvas.getVisible()) {
            if (!canvas.getVisible().contains(shape)) {
                canvas.getVisible().add(shape);
            }
        }
        repaint();
    }

    void replace(Shape previous, Shape current) {
        synchronized (canvas.getVisible()) {
            canvas.getVisible().remove(previous);
            if (!canvas.getVisible().contains(current)) {
                canvas.getVisible().add(current);
            }
        }
        repaint();
    }

    void remove(CollabShapeEntry entry) {
        synchronized (canvas.getVisible()) {
            canvas.getVisible().remove(entry.shape());
            if (!canvas.getInvisible().contains(entry.shape())) {
                canvas.getInvisible().add(entry.shape());
            }
        }
        repaint();
    }

    private void repaint() {
        if (Platform.isFxApplicationThread()) {
            canvas.repaint();
        } else {
            Platform.runLater(canvas::repaint);
        }
    }
}
