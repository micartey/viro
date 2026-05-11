package me.micartey.viro.mcp;

import lombok.RequiredArgsConstructor;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.mcp.objects.Color;
import me.micartey.viro.mcp.objects.PathPoint;
import me.micartey.viro.mcp.objects.McpShape;
import me.micartey.viro.shapes.Path;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.Canvas;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShapeTools {

    private final ApplicationContext context;
    private final Canvas             canvas;

    @McpTool(name = "drawShape", description = "Draw a shape for the user to see based on polygon points. Get the shape id in return")
    public int drawShape(
            @McpToolParam(description = "List of path points") List<PathPoint> points,
            @McpToolParam(description = "Path color") Color color,
            @McpToolParam(description = "Connect first and last point") boolean connectFirstAndLastPoint) {
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
                0
        );

        context.publishEvent(new ShapeSubmitEvent(path));

        return path.hashCode();
    }

    @McpTool(name = "deleteShapeById", description = "Delete a shape by id. An id is returned when calling 'drawShape'")
    public void deleteShape(@McpToolParam(description = "Shape id to delete") int shapeId) {
        this.canvas.getVisible().stream().filter(shape -> shape.hashCode() == shapeId).findFirst().ifPresent(shape -> {
            this.canvas.getVisible().remove(shape);
            this.canvas.getInvisible().add(shape);
        });

        this.canvas.repaint();
    }

    @McpTool(name = "getShapePositionById", description = "Get the shape position by id a shapes position can be transformed")
    public Set<PathPoint> getPositionById(@McpToolParam(description = "Shape id") int shapeId) {
        Optional<Shape> match = this.canvas.getVisible().stream().filter(shape -> shape.hashCode() == shapeId)
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

    @McpTool(name = "getShapes", description = "Get all shapes and their path points")
    public Set<McpShape> getShapes() {
        return this.canvas.getVisible().stream()
                .map(shape -> new McpShape(shape.getPoints().stream().map(point -> new PathPoint(point.getX(), point.getY(), shape.getWidth())).collect(Collectors.toList())))
                .collect(Collectors.toSet());
    }
}