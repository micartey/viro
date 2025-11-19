package me.micartey.viro.mcp;

import javafx.scene.paint.Color;
import lombok.RequiredArgsConstructor;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Path;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class ShapeTool {

    private final ApplicationContext context;
    private final RadialMenu         radialMenu;
    private final Window             window;

    public record PathPoint(double x, double y, int width) {
    }

    @Tool(
            name = "drawShape",
            description = "Draw a shape for the user to see based on polygon points"
    )
    public void drawShape(List<PathPoint> points) {
        Map<Position, Integer> positions = points.stream().collect(Collectors.toMap(
                point -> new Position(point.x(), point.y()),
                PathPoint::width,
                (v1, v2) -> v1,
                LinkedHashMap::new
        ));

        context.publishEvent(new ShapeSubmitEvent(new Path(
                positions,
                radialMenu.getColor(),
                0 // Width will be overwritten by points
        )));
    }

    public record Resolution(double width, double height) {
    }

    @Tool(
            name = "getScreenResolution",
            description = "Get the screen resolution in order to find a visual fitting size for shapes"
    )
    public Resolution getScreenResolution() {
        return new Resolution(
                window.getWidth(),
                window.getHeight()
        );
    }

    public record Color(int red, int green, int blue, int alpha) {
    }

    @Tool(
            name = "setBackgroundColor",
            description = "Set the background color of the default plane for viro"
    )
    public void setBackgroundColor(Color color) {
        window.setBackground(new javafx.scene.paint.Color(
                color.red / 255D,
                color.green / 255D,
                color.blue / 255D,
                color.alpha / 255D
        ));
    }
}
