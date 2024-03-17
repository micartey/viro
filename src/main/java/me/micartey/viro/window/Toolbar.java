package me.micartey.viro.window;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.Getter;
import me.micartey.viro.brushes.Brush;
import me.micartey.viro.events.viro.BrushSelectEvent;
import me.micartey.viro.events.viro.ColorSelectEvent;
import me.micartey.viro.events.viro.SettingUpdateEvent;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.window.components.ImageButton;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.IntStream;

//@Component
public class Toolbar {

    private final RadialMenu radialMenu;
    private final Settings   settings;
    private final Window     window;
    private final Image      icon;

    private final Map<ImageButton, Brush> buttons;
    private final List<Brush>             brushes;

    @Getter private final GraphicsWrapper graphics, overlayGraphics;
    @Getter private final Canvas canvas, overlay;

    public Toolbar(@Value("${application.icon}") String icon, Window window, RadialMenu radialMenu, Settings settings, List<Brush> brushes) {
        this.radialMenu = radialMenu;
        this.settings = settings;
        this.window = window;

        this.buttons = new HashMap<>();
        this.brushes = brushes;

        this.icon = new Image(
                Objects.requireNonNull(Toolbar.class.getResourceAsStream(icon))
        );

        this.canvas = new Canvas();
        this.canvas.setHeight(this.window.getHeight());
        this.canvas.setWidth(50);

        this.graphics = new GraphicsWrapper(this.canvas.getGraphicsContext2D());

        this.overlay = new Canvas();
        this.overlay.setHeight(this.window.getHeight());
        this.overlay.setWidth(50);

        this.overlayGraphics = new GraphicsWrapper(this.overlay.getGraphicsContext2D());

        this.overlay.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onClick);
    }

    @PostConstruct
    private void setup() {
        this.window.group.getChildren().addAll(this.canvas, this.overlay);
    }

    @PostConstruct
    @EventListener(SettingUpdateEvent.class)
    public void draw() {
        this.graphics.setColor(this.settings.getEditorColor().get());
        this.graphics.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());

        this.graphics.drawImage(this.icon,
                5,
                5,
                40,
                40
        );

        this.createButtons(
                this.brushes
        );
    }

    private void onClick(MouseEvent event) {
        this.buttons.forEach((button, brush) -> {
            button.trigger(new Position(
                    event.getX(),
                    event.getY()
            ));
        });
    }

    private void createButtons(List<Brush> brushes) {
        double positionY = this.canvas.getHeight() / 2 - (brushes.size() / 2 * 50);
        IntStream.range(0, brushes.size()).forEach(index -> {
            Brush brush = brushes.get(index);

            Position position = new Position(
                    5,
                    positionY + (index * 50)
            );

            ImageButton button = new ImageButton(
                    this.graphics,
                    this.dyeImage(brush.getIcon(), this.settings.getIconColor().get()),
                    position,
                    new Position(40, 40),
                    press -> {
                        this.radialMenu.selectBrush(brush);
                    }
            );

            this.buttons.put(button, brush);
        });
    }

    private Image dyeImage(Image image, Color newColor) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage writableImage = new WritableImage(
                width,
                height
        );

        PixelWriter writer = writableImage.getPixelWriter();
        PixelReader reader = image.getPixelReader();

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                int a = (argb >> 24) & 0xFF;

                writer.setColor(x, y, Color.color(
                        newColor.getRed(),
                        newColor.getGreen(),
                        newColor.getBlue(),
                        (double) a / 255
                ));
            }
        }

        return writableImage;
    }

    @EventListener({ApplicationStartedEvent.class, ColorSelectEvent.class, BrushSelectEvent.class})
    public void paintMarker() {
        this.overlayGraphics.reset();
        this.overlayGraphics.setColor(this.radialMenu.getColor());
        this.overlayGraphics.setLineWidth(3);

        Optional<ImageButton> imageButton = this.buttons.entrySet().stream()
                .filter(entry -> entry.getValue().equals(this.radialMenu.getBrush()))
                .map(Map.Entry::getKey)
                .findFirst();

        imageButton.ifPresent(button -> {
            this.overlayGraphics.drawLine(
                    0,
                    button.getPosition().getY(),
                    0,
                    button.getPosition().getY() + button.getSize().getY()
            );
        });
    }
}
