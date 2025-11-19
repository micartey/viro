package me.micartey.viro.mcp;

import lombok.RequiredArgsConstructor;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.mcp.objects.Color;
import me.micartey.viro.mcp.objects.PathPoint;
import me.micartey.viro.shapes.Path;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.Window;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShapeTools {

    private final ApplicationContext context;
    private final Window             window;

    /**
     * Draw a shape (list of connected points)
     *
     * @param points                   list of points
     * @param color                    path color
     * @param connectFirstAndLastPoint connect first and last point
     * @return shape id
     */
    @Tool(
            name = "drawShape",
            description = "Draw a shape for the user to see based on polygon points. Get the shape id in return"
    )
    public int drawShape(List<PathPoint> points, Color color, boolean connectFirstAndLastPoint) {
        Map<Position, Integer> positions = points.stream().collect(Collectors.toMap(
                point -> new Position(point.x(), point.y()),
                PathPoint::width,
                (v1, v2) -> v1,
                LinkedHashMap::new
        ));

        if (connectFirstAndLastPoint) {
            positions.put(
                    new Position(points.get(0).x(), points.get(0).y()),
                    points.get(0).width()
            );
        }

        Path path = new Path(
                positions,
                color.toFxColor(),
                0 // Width will be overwritten by points
        );

        context.publishEvent(new ShapeSubmitEvent(path));

        return path.hashCode();
    }

    /**
     * Delete a shape based on its id which is the {@link Object#hashCode()}
     *
     * @param shapeId shape id
     */
    @Tool(
            name = "deleteShapeById",
            description = "Delete a shape by id. An id is returned when calling 'drawShape'"
    )
    public void deleteShape(int shapeId) {
        this.window.getVisible().stream().filter(shape -> shape.hashCode() == shapeId).findFirst().ifPresent(shape -> {
            this.window.getVisible().remove(shape);
            this.window.getInvisible().add(shape);
        });

        this.window.repaint();
    }

    /**
     * Get the current position of a shape by its id
     *
     * @param shapeId shape id
     * @return Set of pathPoint
     */
    @Tool(
            name = "getShapePositionById",
            description = "Get the shape position by id a shapes position can be transformed"
    )
    public Set<PathPoint> getPositionById(int shapeId) {
        Optional<Shape> match = this.window.getVisible().stream().filter(shape -> shape.hashCode() == shapeId)
                .findFirst();

        if (match.isEmpty()) {
            return null;
        }

        if (match.get() instanceof Path path) {
            int width = path.getWidth();

            return path.getPoints().stream()
                    .map(point -> new PathPoint(point.getX(), point.getY(), width))
                    .collect(Collectors.toSet());
        }

        return null;
    }

}
