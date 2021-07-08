package me.micartey.viro.window;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import me.micartey.viro.events.mouse.MouseScrollEvent;
import me.micartey.viro.events.spring.SpringTickEvent;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Brushbar {

    private final RadialMenu radialMenu;
    private final Window     window;

    private final GraphicsWrapper graphics;
    private final Settings        settings;
    private final Canvas          canvas;

    private final long stay;
    private       long time;

    public Brushbar(@Value("${viro.brushbar.icon}") String icon, @Value("${viro.brushbar.title}") String title, @Value("${viro.brushbar.stay}") Integer stay, Window window, RadialMenu radialMenu, Settings settings) {
        this.radialMenu = radialMenu;
        this.settings = settings;
        this.window = window;
        this.stay = stay;

        this.canvas = new Canvas(240, 60);
        this.graphics = new GraphicsWrapper(this.canvas.getGraphicsContext2D());
    }

    @PostConstruct
    private void setup() {
        this.canvas.setLayoutX(
                this.window.getWidth() - this.canvas.getWidth()
        );

        this.canvas.setLayoutY(
                this.window.getHeight() - this.canvas.getHeight() - 40
        );

        this.window.group.getChildren().add(this.canvas);
    }

    private void draw() {
        this.graphics.reset();

        this.graphics.setColor(settings.getEditorColor().get());
        this.graphics.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());

        this.graphics.setColor(Color.WHITE);

        this.graphics.fillRect(
                5,
                5,
                50,
                50
        );

        this.graphics.setLineWidth(this.window.getPreviewGraphics().getLineWidth());
        this.graphics.setColor(this.radialMenu.getColor());

        this.graphics.drawLine(10, 45, 20, 10);
        this.graphics.drawLine(20, 10, 40, 50);
        this.graphics.drawLine(40, 50, 50, 10);

        this.graphics.setLineWidth(1);
        this.graphics.setColor(Color.WHITESMOKE);

        this.graphics.drawString(
                String.valueOf(this.window.getPreviewGraphics().getLineWidth()),
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
    }

    @EventListener(MouseScrollEvent.class)
    public void onChange() {
        this.time = System.currentTimeMillis();

        this.canvas.setWidth(240);
        this.canvas.setLayoutX(
                this.window.getWidth() - this.canvas.getWidth()
        );

        this.draw();
    }

    @EventListener(SpringTickEvent.class)
    public void onUpdate() {
        int steps = (int) (.7 * Math.max(1, System.currentTimeMillis() - this.time - this.stay));

        this.canvas.setLayoutX(this.window.getWidth() - this.canvas.getWidth() + steps);
        this.canvas.setWidth(240 - steps);
    }
}
