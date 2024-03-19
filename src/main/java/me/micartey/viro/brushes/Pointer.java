package me.micartey.viro.brushes;

import javafx.scene.paint.Color;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.window.Window;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Order(4)
@Component
public class Pointer extends Brush {

    private final Map<Position, Long> positions;

    @Value("${viro.brush.pointer.duration}")
    private Double duration;

    public Pointer(@Value("${viro.brush.pointer.icon}") String icon, @Value("${viro.brush.pointer.name}") String name, JationObserver observer) {
        super(icon, name, observer);

        this.positions = new LinkedHashMap<>();
    }

    @Observe
    public void onDrag(MouseDragEvent event, Window window) {
        this.positions.put(event.getSource(), System.currentTimeMillis());
        this.draw(window.getPreviewGraphics());
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, Window window) {
        window.getPreviewGraphics().reset();
    }

    private void draw(GraphicsWrapper graphics) {
        graphics.reset();

        int width = graphics.getLineWidth();
        graphics.setLineWidth(Math.max(3, width));

        AtomicReference<Position> reference = new AtomicReference<>();
        new LinkedHashMap<>(this.positions).forEach((position, time) -> {
            if (System.currentTimeMillis() - time > duration) {
                this.positions.remove(position);
                return;
            }

            Position last = reference.getAndSet(position);

            if (last == null)
                return;

            graphics.setColor(Color.color(
                    graphics.getColor().getRed(),
                    graphics.getColor().getGreen(),
                    graphics.getColor().getBlue()
            ));

            graphics.drawLine(
                    last.getX(),
                    last.getY(),
                    position.getX(),
                    position.getY()
            );
        });

        graphics.setLineWidth(width);
    }
}
