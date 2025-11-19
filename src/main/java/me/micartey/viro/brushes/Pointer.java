package me.micartey.viro.brushes;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import lombok.SneakyThrows;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Order(4)
@Component
public class Pointer extends Brush {

    private final Settings settings;

    private final Map<Position, Long> positions;

    @Value("${viro.brush.pointer.duration}")
    private Double duration;

    private ScheduledExecutorService executor;

    public Pointer(@Value("${viro.brush.pointer.icon}") String icon, @Value("${viro.brush.pointer.name}") String name, Settings settings, JationObserver observer) {
        super(icon, name, observer);

        this.positions = new LinkedHashMap<>();
        this.settings = settings;
    }

    @Observe
    public void onPress(MousePressEvent event, Window window, RadialMenu radialMenu) {
        Runnable task = () -> {
            try {
                draw(
                        new LinkedHashMap<>(positions),
                        window.getPreviewGraphics()
                );
            } catch(Exception e) {
                // ignore
            }
        };

        this.executor = Executors.newScheduledThreadPool(1);
        this.executor.scheduleAtFixedRate(task, 50, 50, TimeUnit.MILLISECONDS);
    }

    @Observe
    public void onDrag(MouseDragEvent event) {
        this.positions.put(event.getSource(), System.currentTimeMillis());
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, Window window) {
        this.executor.shutdownNow();
        window.getPreviewGraphics().reset();
    }

    private void draw(Map<Position, Long> positions, GraphicsWrapper graphics) {
        graphics.reset();

        int width = graphics.getLineWidth();
        graphics.setLineWidth(Math.max(3, width));

        AtomicReference<Position> reference = new AtomicReference<>();

        positions.forEach((position, time) -> {
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

            Position pos = new Position(
                    Math.max(0, last.getX() + ((position.getX() - last.getX()) / this.settings.getSmoothness().get())),
                    Math.max(0, last.getY() + ((position.getY() - last.getY()) / this.settings.getSmoothness().get()))
            );

            reference.set(pos);

            graphics.drawLine(
                    last.getX(),
                    last.getY(),
                    pos.getX(),
                    pos.getY()
            );
        });

        graphics.setLineWidth(width);
    }
}
