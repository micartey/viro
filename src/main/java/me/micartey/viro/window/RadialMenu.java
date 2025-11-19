package me.micartey.viro.window;

import javafx.scene.canvas.Canvas;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.Getter;
import me.micartey.viro.brushes.Brush;
import me.micartey.viro.events.viro.BrushSelectEvent;
import me.micartey.viro.events.viro.ColorSelectEvent;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.CanvasWrapper;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;

@Component
public class RadialMenu extends CanvasWrapper {

    private final Canvas  selected, overlay;
    private final Image   image;
    private final Integer radius;

    private final Map<Brush, Position> positions;

    @Getter private Color color;
    @Getter private Brush brush;

    private final ApplicationContext context;

    public RadialMenu(ApplicationContext context, @Value("${application.icon}") String icon, @Value("${viro.radialMenu.title}") String title, @Value("${viro.radialMenu.image}") String image, @Value("${viro.radialMenu.radius}") Integer radius, List<Brush> brushes) {
        super(icon, title, new Position(0, 0), new Position(
                500,
                500
        ));

        this.context = context;

        this.color = Color.BLACK;
        brushes.stream().findFirst().ifPresent(brush -> {
            this.brush = brush;
        });

        this.selected = this.createCanvasOnTop();
        this.overlay = this.createCanvasOnTop();

        this.positions = new HashMap<>();
        this.radius = radius;

        this.image = new Image(
                Objects.requireNonNull(RadialMenu.class.getResourceAsStream(image))
        );

        this.computeRadialPositions(brushes, radius);

        this.stage.addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseMove);
        this.stage.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMousePress);
        this.stage.setAlwaysOnTop(true);

        this.setup();
        this.selectDefaultBrush();
    }

    private void setup() {
        this.selected.setEffect(new DropShadow(18, Color.color(color.getRed(), color.getGreen(), color.getBlue())));
        this.selected.getGraphicsContext2D().setFill(Color.rgb(44, 44, 44));

        this.setEffect(new DropShadow(15, Color.rgb(0, 0, 0)));
        this.getGraphicsContext2D().setFill(Color.rgb(21, 21, 21));
    }

    private void selectDefaultBrush() {
        this.positions.keySet().stream().findFirst().ifPresent(this::selectBrush);
    }

    private void computeRadialPositions(List<Brush> brushes, int radius) {
        IntStream.range(0, brushes.size()).forEach(index -> {
            Brush brush = brushes.get(index);

            double slice = 2 * Math.PI / brushes.size();
            double part = slice * index;

            double positionX = radius * Math.cos(part) + this.getWidth() / 2;
            double positionY = radius * Math.sin(part) + this.getHeight() / 2;

            this.positions.put(brush, new Position(
                    positionX,
                    positionY
            ));
        });
    }

    private void onMouseMove(MouseEvent event) {
        Position cursor = new Position(
                event.getX(),
                event.getY()
        );

        if (this.radius - 20 > this.getDistanceToMiddle(cursor))
            return;

        this.selectBrush(
                this.getClosestBrush(cursor)
        );
    }

    private void onMousePress(MouseEvent event) {
        Position cursor = new Position(
                event.getX(),
                event.getY()
        );

        double distance = this.getDistanceToMiddle(cursor);

        if (distance < 56 || distance > 135) {
            this.stage.hide();
            return;
        }

        this.color = this.image.getPixelReader().getColor(
                (int) ((event.getX() - 100) * (this.image.getWidth() / 300)),
                (int) ((event.getY() - 100) * (this.image.getHeight() / 300))
        );

        this.selected.setEffect(new DropShadow(18, this.color));
        this.context.publishEvent(new ColorSelectEvent(this.color));
    }

    @Override
    public void paintComponent(GraphicsWrapper context) {
        this.positions.forEach((brush, position) -> {
            context.fillOval(
                    position.getX() - 23,
                    position.getY() - 23,
                    46,
                    46
            );

            this.overlay.getGraphicsContext2D().drawImage(brush.getIcon(),
                    position.getX() - 15,
                    position.getY() - 15,
                    30,
                    30
            );
        });

        context.drawImage(this.image,
                100,
                100,
                300,
                300
        );
    }

    public void selectBrush(Brush brush) {
        if (this.brush == brush)
            return;

        Position position = this.positions.get(brush);

        this.selected.getGraphicsContext2D().clearRect(
                0,
                0,
                this.getWidth(),
                this.getHeight()
        );

        this.selected.getGraphicsContext2D().fillOval(
                position.getX() - 23,
                position.getY() - 23,
                46,
                46
        );

        Brush previous = this.brush;
        this.brush = brush;

        context.publishEvent(new BrushSelectEvent(previous, brush));
    }

    private double getDistanceToMiddle(Position cursor) {
        return cursor.distance(new Position(
                this.getWidth() / 2,
                this.getHeight() / 2
        ));
    }

    private Brush getClosestBrush(Position position) {
        return this.positions.entrySet().stream()
                .min(Comparator.comparingDouble(entry -> position.distance(entry.getValue())))
                .map(Map.Entry::getKey)
                .orElse(this.brush);
    }
}
