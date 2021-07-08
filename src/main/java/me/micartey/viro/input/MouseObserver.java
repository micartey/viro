package me.micartey.viro.input;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import me.micartey.jation.JationObserver;
import me.micartey.jation.interfaces.JationEvent;
import me.micartey.viro.brushes.Brush;
import me.micartey.viro.events.mouse.*;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import me.micartey.viro.window.utilities.Position;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class MouseObserver {

    private final JationObserver observer;
    private final RadialMenu     radialMenu;
    private final Settings       settings;
    private final Window         window;

    private Position origin, source;

    public MouseObserver(Window window, Settings settings, RadialMenu radialMenu, JationObserver observer) {
        this.radialMenu = radialMenu;
        this.observer = observer;
        this.settings = settings;
        this.window = window;

        window.getPreviewCanvas().addEventHandler(MouseEvent.MOUSE_MOVED, this::onMove);
        window.getPreviewCanvas().addEventHandler(MouseEvent.MOUSE_PRESSED, this::onPress);
        window.getPreviewCanvas().addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDrag);
        window.getPreviewCanvas().addEventHandler(MouseEvent.MOUSE_RELEASED, this::onRelease);
        window.getPreviewCanvas().addEventHandler(ScrollEvent.SCROLL, this::onScroll);
    }

    @EventListener(ApplicationStartedEvent.class)
    public void registerFilters() {
        this.registerFilter(MouseReleaseEvent.class);
        this.registerFilter(MousePressEvent.class);
        this.registerFilter(MouseDragEvent.class);
        this.registerFilter(MouseMoveEvent.class);
        this.registerFilter(MouseScrollEvent.class);
    }

    /**
     * Register {@link Brush brush} rules to only invoke the current brush
     * which is selected
     *
     * @param clazz of JationEvent
     */
    private void registerFilter(Class<? extends JationEvent<?>> clazz) {
        this.observer.forEach(clazz, (event, method, instance) -> {
            if (!(instance instanceof Brush))
                return true;

            return this.radialMenu.getBrush() == instance && !this.radialMenu.stage.isShowing();
        });
    }

    /**
     * Consumer for mouse scroll {@link ScrollEvent ScrollEvent}
     *
     * @param event {@link ScrollEvent ScrollEvent}
     */
    private void onScroll(ScrollEvent event) {
        MouseScrollEvent scrollEvent = new MouseScrollEvent(event.getDeltaY() > 0 ? 1 : -1);

        scrollEvent.publish(
                this.observer,
                this.radialMenu,
                this.settings,
                this.window,
                event
        );
    }
    /**
     * Consumer for mouse press {@link MouseEvent event}
     *
     * @param event {@link MouseEvent MouseEvent}
     */
    private void onPress(MouseEvent event) {
        this.origin = new Position(
                event.getX(),
                event.getY()
        );

        this.source = this.origin;

        MousePressEvent pressEvent = new MousePressEvent(this.origin, event.getButton());

        pressEvent.publish(
                this.observer,
                this.radialMenu,
                this.settings,
                this.window,
                event
        );
    }

    /**
     * Consumer for mouse drag {@link MouseEvent event}
     *
     * @param event {@link MouseEvent MouseEvent}
     */
    private void onDrag(MouseEvent event) {
        MouseDragEvent dragEvent = new MouseDragEvent(this.origin, this.source, new Position(
                event.getX(),
                event.getY()
        ), event.getButton());

        this.source = dragEvent.getDestination();

        dragEvent.publish(
                this.observer,
                this.radialMenu,
                this.settings,
                this.window,
                event
        );
    }

    /**
     * Consumer for mouse release {@link MouseEvent event}
     *
     * @param event {@link MouseEvent MouseEvent}
     */
    private void onRelease(MouseEvent event) {
        MouseReleaseEvent releaseEvent = new MouseReleaseEvent(new Position(
                event.getX(),
                event.getY()
        ), event.getButton());

        releaseEvent.publish(
                this.observer,
                this.radialMenu,
                this.settings,
                this.window,
                event
        );
    }

    /**
     * Consumer for mouse movement {@link MouseEvent event}
     *
     * @param event {@link MouseEvent MouseEvent}
     */
    private void onMove(MouseEvent event) {
        MouseMoveEvent moveEvent = new MouseMoveEvent(new Position(
                event.getX(),
                event.getY()
        ), event.getButton());

        moveEvent.publish(
                this.observer,
                this.radialMenu,
                this.settings,
                this.window,
                event
        );
    }
}
