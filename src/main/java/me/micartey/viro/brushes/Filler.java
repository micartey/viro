package me.micartey.viro.brushes;

import lombok.SneakyThrows;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.shapes.Polygon;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import me.micartey.viro.shapes.utilities.Position;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Order(3)
@Component
public class Filler extends Brush {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    @Value("${viro.brush.filler.slices}")
    private Integer slices;

    @Value("${viro.brush.filler.maxRadius}")
    private Integer radius;

    public Filler(@Value("${viro.brush.filler.icon}") String icon, @Value("${viro.brush.filler.name}") String name, JationObserver observer) {
        super(icon, name, observer);
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, RadialMenu radialMenu, Window window) {
        /*
         * Special case: Everyting is empty.
         *
         * User is attempting to color the entire canvas
         */
        if (window.getVisible().isEmpty()) {
            window.setBackground(radialMenu.getColor());
            return;
        }

        // TODO: Refactor
        EXECUTOR_SERVICE.submit(() -> {
            List<Position> positions = this.computeEdges(event.getPosition(), window);

            this.filterPositions(positions);

            this.submit(new Polygon(
                    radialMenu.getColor(),
                    window.getPreviewGraphics().getLineWidth(),
                    positions.toArray(new Position[] {})
            ));
        });
    }

    @SneakyThrows
    private List<Position> computeEdges(Position position, Window window) {
        Map<Integer, Position> positions = new HashMap<>();

        /*
         * Number of tasks -> threads
         */
        int tasks = this.slices / 50;
        CountDownLatch latch = new CountDownLatch(tasks);

        for (int task = 0; task < tasks; task++) {
            final int taskOffset = 50 * task;

            EXECUTOR_SERVICE.submit(() -> {
                IntStream.range(taskOffset, taskOffset + 50).forEach(index -> {
                    double slice = 2 * Math.PI / this.slices;
                    double part = slice * index;

                    for (int i = 0; i < this.radius; i++) {
                        Position ray = new Position(
                                i * Math.cos(part) + position.getX(),
                                i * Math.sin(part) + position.getY()
                        );

                        if (window.getVisible().stream().anyMatch(shape -> shape.select(ray))) {
                            positions.put(index, ray);
                            break;
                        }
                    }
                });

                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);

        return positions.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Remove outliers
     *
     * @param positions sorted list of positions in sequencial order
     */
    private void filterPositions(List<Position> positions) {
        AtomicReference<Position> lastPosition = new AtomicReference<>(positions.get(0));
        new LinkedList<>(positions).stream().skip(1).forEach(position -> {
            Position last = lastPosition.getAndSet(position);
            if (last.distance(position) > 10)
                positions.remove(position);
        });
    }
}
