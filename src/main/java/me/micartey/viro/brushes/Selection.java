package me.micartey.viro.brushes;

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
    public void onSelect(MouseDragEvent event, Window window) {
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

        radialMenu.selectBrush(this.move);
    }

    private boolean inSelection(Shape shape, Position pos1, Position pos2) {
        return shape.getPoints().stream().anyMatch(position -> position.between(pos1, pos2));
    }
}
