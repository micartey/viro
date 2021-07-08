package me.micartey.viro.brushes;

import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.shapes.Polygon;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import me.micartey.viro.window.utilities.Position;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Order(3)
@Component
public class Filler extends Brush {

    @Value("${viro.brush.filler.slices}")
    private Integer slices;

    @Value("${viro.brush.filler.maxRadius}")
    private Integer radius;

    public Filler(@Value("${viro.brush.filler.icon}") String icon, @Value("${viro.brush.filler.name}") String name, JationObserver observer) {
        super(icon, name, observer);
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, RadialMenu radialMenu, Window window) {
        this.computeEdges(event.getPosition(), window).thenAccept(list -> {
            this.filterPositions(list);

            this.submit(new Polygon(
                    radialMenu.getColor(),
                    window.getPreviewGraphics().getLineWidth(),
                    list.toArray(new Position[]{})
            ));
        });
    }

    private CompletableFuture<List<Position>> computeEdges(Position position, Window window) {
        return CompletableFuture.supplyAsync(() -> {
            List<Position> positions = new LinkedList<>();

            IntStream.range(0, slices).forEach(index -> {
                double slice = 2 * Math.PI / slices;
                double part = slice * index;

                for (int i = 0; i < radius; i++) {
                    Position ray = new Position(
                            i * Math.cos(part) + position.getX(),
                            i * Math.sin(part) + position.getY()
                    );

                    if (window.getVisible().stream().anyMatch(shape -> shape.select(ray))) {
                        positions.add(ray);
                        break;
                    }
                }
            });

            return positions;
        });
    }

    private void filterPositions(List<Position> positions) {
        AtomicReference<Position> lastPosition = new AtomicReference<>(positions.get(0));
        new LinkedList<>(positions).stream().skip(1).forEach(position -> {
            Position last = lastPosition.getAndSet(position);
            if (last.distance(position) > 10)
                positions.remove(position);
        });
    }
}
