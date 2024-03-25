package me.micartey.viro.window;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.stage.Screen;
import lombok.Getter;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.brushes.Eraser;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.events.mouse.MouseScrollEvent;
import me.micartey.viro.events.viro.*;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.components.IconButton;
import me.micartey.viro.window.wrapper.CanvasWrapper;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Stack;

@Component
public class Window extends CanvasWrapper {

    private final JationObserver observer;
    private final Settings       settings;

    @Getter private final GraphicsWrapper previewGraphics;
    @Getter private final Canvas          previewCanvas;

    @Getter private final Stack<Shape> visible   = new Stack<>();
    @Getter private final Stack<Shape> invisible = new Stack<>();

    @Value("${viro.brush.width.max}")
    private Integer maxWidth;

    public Window(@Value("${application.title}") String title, @Value("${application.icon}") String icon, @Value("${viro.brush.width.default}") Integer width, Settings settings, JationObserver observer) {
        super(icon, title, new Position(0, 0), new Position(
                Screen.getPrimary().getBounds().getMaxX(),
                Screen.getPrimary().getBounds().getMaxY()
        ));

        this.previewCanvas = this.createCanvasOnTop();
        this.previewGraphics = new GraphicsWrapper(
                this.previewCanvas.getGraphicsContext2D()
        );

        this.observer = observer;
        this.settings = settings;

        this.previewGraphics.setLineWidth(width);

//        this.stage.setAlwaysOnTop(true);
        this.stage.setOnCloseRequest(Event::consume);

        new IconButton(this, this.observer, settings)
                .setX((int) Screen.getPrimary().getBounds().getMaxX() - 40)
                .setY((int) Screen.getPrimary().getBounds().getMaxY() - 70)
                .setIcon("/assets/controls/quit.png")
                .onClick(() -> {
                    System.exit(0);
                }).draw();

        this.observer.subscribe(this);
    }

    /**
     * Apply visual settings to the {@link Window Window}
     */
    @EventListener({ApplicationStartedEvent.class, SettingUpdateEvent.class})
    public void onSettingsUpdate() {
        this.setBackground(this.settings.getBackgroundColor());
        this.repaint();

        this.stage.show();
    }

    /**
     * Set the {@link javafx.scene.paint.Color Color} for the {@link GraphicsWrapper GraphicsWrapper}
     *
     * @param event {@link ColorSelectEvent ColorSelectEvent}
     */
    @EventListener(ColorSelectEvent.class)
    public void onColorUpdate(ColorSelectEvent event) {
        this.previewGraphics.setColor(
                event.getColor()
        );
    }

    /**
     * Set the new line width and while mapping it to the min/max width.
     *
     * @param event {@link MouseScrollEvent MouseScrollEvent}
     */
    @EventListener(MouseScrollEvent.class)
    public void onScroll(MouseScrollEvent event) {
        this.previewGraphics.setLineWidth(
                Math.max(0, Math.min(this.previewGraphics.getLineWidth() + event.getVector(), this.maxWidth))
        );
    }

    /**
     * Set custom cursor for different types of {@link me.micartey.viro.brushes.Brush brushes}.
     *
     * @param event {@link BrushSelectEvent BrushSelectEvent}
     */
    @EventListener(BrushSelectEvent.class)
    public void onBrushSelect(BrushSelectEvent event) {
        this.previewGraphics.reset();

        if (event.getCurrent().getClass().equals(Eraser.class)) {
            this.scene.setCursor(Cursor.DEFAULT);
            return;
        }

        this.scene.setCursor(Cursor.CROSSHAIR);
    }

    /**
     * Add a new {@link Shape shape} to the visible stack and redraw
     * the main canvas.
     *
     * @param event {@link ShapeSubmitEvent ShapeSubmitEvent}
     */
    @EventListener(ShapeSubmitEvent.class)
    public void onShapeSubmit(ShapeSubmitEvent event) {
        this.visible.add(event.getShape());
        this.previewGraphics.reset();

        event.getShape().draw(new GraphicsWrapper(
                this.getGraphicsContext2D()
        ));
    }

    /**
     * Draw all {@link Shape shapes} in the JavaFX thread.
     *
     * @param graphics {@link javafx.scene.canvas.GraphicsContext GraphicsContext} of Canvas
     */
    @Override
    public void paintComponent(GraphicsWrapper graphics) {
        Platform.runLater(() -> {
            visible.forEach(shape -> shape.draw(graphics));
        });
    }

    /**
     * Listens to mouse clicks on the preview canvas and shows/hides
     * the RadialMenu regarding the current action
     *
     * @param event      MousePressEvent
     * @param radialMenu RadialMenu
     */
    @Observe
    public void onClick(MousePressEvent event, RadialMenu radialMenu) {
        if (radialMenu.stage.isShowing())
            radialMenu.stage.hide();

        if (!event.getMouseButton().equals(MouseButton.SECONDARY))
            return;

        this.previewGraphics.reset();

        radialMenu.updatePosition(new Position(
                event.getPosition().getX() - radialMenu.getWidth() / 2,
                event.getPosition().getY() - radialMenu.getHeight() / 2
        ));

        radialMenu.stage.show();
    }

    /**
     * Removes latest {@link Shape shape} from the visible {@link Stack<Shape> stack} which won't be
     * rendered after {@link CanvasWrapper#repaint() repaint} gets called
     */
    @EventListener(ShapeUndoEvent.class)
    public void undo() {
        if (this.visible.isEmpty())
            return;

        this.invisible.push(this.visible.pop());
        this.repaint();
    }

    /**
     * Pushes latest {@link Shape shape} to the visible {@link Stack<Shape> stack} which will be
     * rendered after {@link CanvasWrapper#repaint() repaint} gets called
     */
    @EventListener(ShapeRedoEvent.class)
    public void redo() {
        if (this.invisible.isEmpty())
            return;

        this.visible.push(this.invisible.pop());
        this.repaint();
    }
}
