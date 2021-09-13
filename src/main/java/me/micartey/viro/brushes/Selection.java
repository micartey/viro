package me.micartey.viro.brushes;

import javafx.scene.paint.Color;
import lombok.Getter;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Component
public class Selection extends Brush {

    @Getter private final List<Shape> shapes;

    @Autowired
    private Move move;

    public Selection(@Value("${viro.brush.selection.name}") String name, @Value("${viro.brush.selection.icon}") String icon, JationObserver observer) {
        super(icon, name, observer);

        this.shapes = new LinkedList<>();
    }

    @Observe
    public void onPress(MousePressEvent event) {
        this.shapes.clear();
    }

    @Observe
    public void onDrag(MouseDragEvent event, Window window) {
        this.draw(
                window.getPreviewGraphics(),
                event.getOrigin(),
                event.getDestination()
        );

        window.getVisible().stream().filter(shape -> this.inSelection(shape, event.getOrigin(), event.getDestination()))
                .filter(shape -> !this.shapes.contains(shape))
                .forEach(this.shapes::add);
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, Window window, RadialMenu radialMenu) {
        List<Shape> backup = new LinkedList<>(this.shapes);

        this.shapes.clear();

        backup.stream().distinct().sorted(Comparator.comparingInt(shape -> window.getVisible().search(shape)))
                .forEach(this.shapes::add);

        window.getPreviewGraphics().reset();

        if (backup.isEmpty())
            return;

        radialMenu.selectBrush(this.move);
    }

    private boolean inSelection(Shape shape, Position pos1, Position pos2) {
        return shape.getPoints().stream().anyMatch(position -> position.between(pos1, pos2));
    }

    private void draw(GraphicsWrapper graphics, Position origin, Position destination) {
        graphics.reset();

        Color color = graphics.getColor();
        double[] currentPattern = graphics.getLineDashes();
        int lineWidth = graphics.getLineWidth();

        graphics.setColor(Color.LIGHTGRAY);
        graphics.setLineWidth(1);
        graphics.setLineDashes(3);

        graphics.drawLine(origin.getX(), origin.getY(), destination.getX(), origin.getY());
        graphics.drawLine(destination.getX(), origin.getY(), destination.getX(), destination.getY());
        graphics.drawLine(origin.getX(), origin.getY(), origin.getX(), destination.getY());
        graphics.drawLine(origin.getX(), destination.getY(), destination.getX(), destination.getY());

        graphics.setColor(color);
        graphics.setLineDashes(currentPattern);
        graphics.setLineWidth(lineWidth);
    }
}
