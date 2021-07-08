package me.micartey.viro.brushes;

import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.shapes.Path;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Order(0)
@Component
public class Pencil extends Brush {

    private final Map<Position, Integer> positions;

    public Pencil(@Value("${viro.brush.pencil.icon}") String icon, @Value("${viro.brush.pencil.name}") String name, JationObserver observer) {
        super(icon, name, observer);

        this.positions = new LinkedHashMap<>();
    }

    @Observe
    public void onPress(MousePressEvent event) {
        this.positions.clear();
    }

    @Observe
    public void onDrag(MouseDragEvent event, Window window) {
        GraphicsWrapper wrapper = window.getPreviewGraphics();
        wrapper.drawLine(event.getSource().getX(), event.getSource().getY(), event.getDestination().getX(), event.getDestination().getY());

        this.positions.put(event.getSource(), wrapper.getLineWidth());
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, RadialMenu radialMenu, Window window) {
        this.submit(new Path(
                this.positions,
                radialMenu.getColor(),
                window.getPreviewGraphics().getLineWidth()
        ));
    }
}
