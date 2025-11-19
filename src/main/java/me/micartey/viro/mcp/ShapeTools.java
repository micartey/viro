package me.micartey.viro.mcp;

import lombok.RequiredArgsConstructor;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.mcp.objects.Color;
import me.micartey.viro.mcp.objects.PathPoint;
import me.micartey.viro.shapes.Path;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.Window;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShapeTools {

    private final ApplicationContext context;
    private final Window             window;

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

}
