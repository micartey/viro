package me.micartey.viro.brushes;

import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.events.mouse.MouseScrollEvent;
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

@Order(2)
@Component
public class Ruler extends Brush {

    private Position origin, destination;

    public Ruler(@Value("${viro.brush.ruler.icon}") String icon, @Value("${viro.brush.ruler.name}") String name, JationObserver observer) {
        super(icon, name, observer);
    }

    @Observe
    public void onDrag(MouseDragEvent event, Window window) {
        GraphicsWrapper wrapper = window.getPreviewGraphics();

        this.destination = event.getDestination();
        this.origin = event.getOrigin();

        this.draw(wrapper);
    }

    @Observe
    public void onScroll(MouseScrollEvent event, Window window) {
        this.draw(window.getPreviewGraphics());
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, RadialMenu radialMenu, Window window) {
        if (this.destination == null || this.origin == null)
            return;

        Map<Position, Integer> points = new LinkedHashMap<>();
        points.put(this.origin, window.getPreviewGraphics().getLineWidth());

        /*
         * Fill point in between to make erasing possible
         * This does not change anything in terms of visuals
         */
        Position direction = this.origin.direction(this.destination).normalize();
        for (double step = 0; step < this.origin.distance(this.destination); step += 3) {
            points.put(this.origin.translate(direction.multiply(step)), window.getPreviewGraphics().getLineWidth());
        }

        points.put(this.destination, window.getPreviewGraphics().getLineWidth());

        this.submit(new Path(
                points,
                radialMenu.getColor(),
                window.getPreviewGraphics().getLineWidth()
        ));

        this.destination = null;
        this.origin = null;
    }

    private void draw(GraphicsWrapper wrapper) {
        if (this.destination == null || this.origin == null)
            return;

        wrapper.reset();
        wrapper.drawLine(this.origin.getX(), this.origin.getY(), this.destination.getX(), this.destination.getY());
    }
}
