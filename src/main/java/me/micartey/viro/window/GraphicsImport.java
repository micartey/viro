package me.micartey.viro.window;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Graphic;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.CanvasWrapper;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Component
public class GraphicsImport extends CanvasWrapper {

    private static final ReentrantLock LOCK = new ReentrantLock();

    private Image  image;
    private double size;

    @Autowired
    private ApplicationContext context;


    private final GraphicsWrapper graphics;

    public GraphicsImport(@Value("${application.icon}") String icon, @Value("${application.title}") String title) {
        super(icon, title, new Position(0, 0), new Position(
                Screen.getPrimary().getBounds().getMaxX(),
                Screen.getPrimary().getBounds().getMaxY()
        ));

        this.stage.setAlwaysOnTop(true);

        this.addEventHandler(ScrollEvent.SCROLL, this::onScroll);
        this.getScene().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    this.onPublish();
                    break;
                case ESCAPE:
                    LOCK.unlock();
                    this.stage.hide();
                    break;
            }
        });

        Canvas canvas = this.createCanvasOnTop();
        canvas.setMouseTransparent(true);
        this.graphics = new GraphicsWrapper(canvas.getGraphicsContext2D());
    }

    public void setup() {
        LOCK.lock();

        this.size = 1;

        File file = new FileChooser().showOpenDialog(stage);

        if (file == null) {
            LOCK.unlock();
            this.stage.hide();
            return;
        }

        this.image = new Image(file.toURI().toString());
        repaint();
    }

    private void onScroll(ScrollEvent event) {
        this.size += event.getDeltaY() / 100 * -1;
        repaint();
    }

    private void onPublish() {
        context.publishEvent(new ShapeSubmitEvent(new Graphic(
                image,
                new Position(
                        Screen.getPrimary().getBounds().getMaxX() / 2,
                        Screen.getPrimary().getBounds().getMaxY() / 2
                ),
                this.image.getWidth() / this.size,
                this.image.getHeight() / this.size,
                Color.TRANSPARENT,
                1
        )));

        this.stage.hide();

        LOCK.unlock();
    }

    @Override
    public void paintComponent(GraphicsWrapper context) {
        graphics.setColor(Color.WHITE);

        List<String> notes = Arrays.asList(
                "ESC - Close Window",
                "ENTER - Import image",
                "SCROLL - Change Image Size"
        );

        final int fontSize = 15;

        IntStream.range(0, notes.size()).forEach(index -> {
            int fontSpace = fontSize + 5;
            graphics.drawString(notes.get(index), 20, Screen.getPrimary().getBounds().getMaxY() - (50 + notes.size() * fontSpace) + index * fontSpace, fontSize);
        });

        if (image == null)
            return;

        double middleX = getWidth() / 2;
        double middleY = getHeight() / 2;

        double width = image.getWidth() / size;
        double height = image.getHeight() / size;

        context.drawImage(image,
                middleX - (width / 2),
                middleY - (height / 2),
                width,
                height
        );
    }
}