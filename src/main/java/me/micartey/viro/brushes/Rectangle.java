package me.micartey.viro.brushes;

import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.events.mouse.MouseScrollEvent;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Canvas;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(5)
@Component
public class Rectangle extends Brush {

    private Position position, size;

    public Rectangle(@Value("${viro.brush.rectangle.icon}") String icon, @Value("${viro.brush.rectangle.name}") String name, JationObserver observer) {
        super(icon, name, observer);
    }

    @Observe
    public void onDrag(MouseDragEvent event, Canvas canvas) {
        this.position = new Position(
                Math.min(event.getOrigin().getX(), event.getDestination().getX()),
                Math.min(event.getOrigin().getY(), event.getDestination().getY())
        );

        this.size = new Position(
                Math.abs(event.getOrigin().getX() - event.getDestination().getX()),
                Math.abs(event.getOrigin().getY() - event.getDestination().getY())
        );

        this.draw(canvas.getPreviewGraphics());
    }

    @Observe
    public void onScroll(MouseScrollEvent event, Canvas canvas) {
        this.draw(canvas.getPreviewGraphics());
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, RadialMenu radialMenu, Canvas canvas) {
        if (this.position == null || this.size == null)
            return;

        this.submit(new me.micartey.viro.shapes.Rectangle(
                radialMenu.getColor(),
                canvas.getPreviewGraphics().getLineWidth(),
                this.position,
                this.size
        ));

        this.position = null;
        this.size = null;
    }

    private void draw(GraphicsWrapper wrapper) {
        wrapper.reset();

        if (this.position == null || this.size == null)
            return;

        wrapper.drawRect(
                this.position.getX(),
                this.position.getY(),
                this.size.getX(),
                this.size.getY()
        );
    }
}
