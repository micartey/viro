package me.micartey.viro.window;

import javafx.scene.paint.Color;
import me.micartey.viro.events.mouse.MouseScrollEvent;
import me.micartey.viro.events.spring.SpringTickEvent;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.window.components.IconButton;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StrokeWidthPopup {

    private final RadialMenu radialMenu;
    private final Canvas     canvas;

    private final javafx.scene.canvas.Canvas fxCanvas;
    private final GraphicsWrapper            graphics;
    private final Settings                   settings;

    private final long stay;
    private       long time;

    public StrokeWidthPopup(@Value("${viro.brushbar.stay}") Integer stay, Canvas canvas, RadialMenu radialMenu, Settings settings) {
        this.radialMenu = radialMenu;
        this.settings = settings;
        this.canvas = canvas;
        this.stay = stay;

        this.fxCanvas = new javafx.scene.canvas.Canvas(240, 60);
        this.graphics = new GraphicsWrapper(this.fxCanvas.getGraphicsContext2D());
    }

    @EventListener(ApplicationReadyEvent.class)
    private void setup() {
        this.fxCanvas.setLayoutX(
                this.canvas.getWidth() - this.fxCanvas.getWidth()
        );

        this.fxCanvas.setLayoutY(
                this.canvas.getHeight() - this.fxCanvas.getHeight() - 80
        );

        this.canvas.group.getChildren().add(this.fxCanvas);
    }

    private synchronized void draw() {
        this.graphics.reset();

        this.graphics.setColor(settings.getEditorColor().get());
        this.graphics.fillRect(0, 0, this.fxCanvas.getWidth(), this.fxCanvas.getHeight());

        this.graphics.setColor(Color.WHITE);

        this.graphics.fillRect(
                5,
                5,
                50,
                50
        );

        int width = this.canvas.getPreviewGraphics().getLineWidth();

        this.graphics.setLineWidth(width);
        this.graphics.setColor(this.radialMenu.getColor());

        this.graphics.drawLine(10, 45, 20, 10);
        this.graphics.drawLine(20, 10, 40, 50);
        this.graphics.drawLine(40, 50, 50, 10);

        this.graphics.setLineWidth(1);
        this.graphics.setColor(Color.WHITESMOKE);

        this.graphics.drawString(
                String.valueOf(this.canvas.getPreviewGraphics().getLineWidth()),
                70,
                35,
                16
        );

        this.graphics.drawString(
                "px",
                90,
                35,
                16
        );

        this.graphics.setLineWidth(width);
    }

    /**
     * Make stroke width popup visible if size changes.
     * Also destroy icon buttons with {@link IconButton#remove()}
     */
    @EventListener(MouseScrollEvent.class)
    public void onChange() {
        this.time = System.currentTimeMillis();

        this.fxCanvas.setWidth(240);
        this.fxCanvas.setLayoutX(
                this.canvas.getWidth() - this.fxCanvas.getWidth()
        );

        this.canvas.getButtons().forEach(IconButton::remove);
        this.draw();
    }

    /**
     * Animate the popup to fade away to the right corner.
     * Afterward, redraw icon buttons with {@link IconButton#draw()}
     */
    @EventListener(SpringTickEvent.class)
    public void onUpdate() {
        int steps = (int) (.7 * Math.max(1, System.currentTimeMillis() - this.time - this.stay));

        this.fxCanvas.setLayoutX(this.canvas.getWidth() - this.fxCanvas.getWidth() + steps);
        this.fxCanvas.setWidth(240 - steps);

        if (steps < 300)
            return;

        this.canvas.getButtons().stream().filter(button -> !button.isVisible()).forEach(IconButton::draw);
    }
}
