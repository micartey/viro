package me.micartey.viro.window.components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseMoveEvent;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Accessors(chain = true)
public class IconButton {

    private final GraphicsContext graphicsContext;
    private final List<Runnable>  listeners;

    private final DropShadow effect = new DropShadow(18, Color.rgb(200, 200, 200));

    private String icon;
    private Color  color;

    private int size = 30;
    private int x, y;

    public IconButton(Window window, JationObserver jationObserver, Settings settings) {
        this.listeners = new ArrayList<>();

        /*
         * Create a new overlay canvas
         * We need to propagate the mouse events to the underlaying canvas (preview canvas)
         */
        Canvas canvas = window.createCanvasOnTop();
        canvas.setMouseTransparent(true);

        this.graphicsContext = canvas.getGraphicsContext2D();
        this.color = settings.getEditorColor().get();

        jationObserver.subscribe(this);
    }

    public void draw() {
        this.graphicsContext.clearRect(
                this.x - this.size - 100,
                this.y - this.size - 100,
                this.x + this.size + 100,
                this.y + this.size + 100
        );

        int halfSize = this.size / 2;

        this.graphicsContext.setFill(color);
        this.graphicsContext.fillOval(
                this.x - halfSize - 8,
                this.y - halfSize - 8,
                this.size + 16,
                this.size + 16
        );

        if (this.icon == null)
            return;

        Image image = new Image(
                Objects.requireNonNull(IconButton.class.getResourceAsStream(this.icon))
        );

        this.graphicsContext.drawImage(image,
                this.x - halfSize,
                this.y - halfSize,
                this.size,
                this.size
        );
    }

    public IconButton onClick(Runnable runnable) {
        this.listeners.add(runnable);
        return this;
    }

    @Observe
    public void onMove(MouseMoveEvent event) {
        /*
         * Remove effect and redraw
         */
        if (event.getPosition().distance(new Position(this.x, this.y)) > this.size) {
            this.graphicsContext.setEffect(null);
            this.draw();
            return;
        }

        /*
         * Check if effect has already been aplied and abort if so
         */
        if (this.graphicsContext.getEffect(null) instanceof DropShadow)
            return;

        this.graphicsContext.setEffect(effect);
        this.draw();
    }

    @Observe
    public void onPress(MousePressEvent event) {
        if (!event.getMouseButton().equals(MouseButton.PRIMARY))
            return;

        if (event.getPosition().distance(new Position(this.x, this.y)) > this.size)
            return;

        this.listeners.forEach(Runnable::run);
    }
}
