package me.micartey.viro.settings;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Graphic;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.CanvasWrapper;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Objects;

@Component
public class GraphicImport extends CanvasWrapper {


    private final Settings settings;

    private Image  image;
    private double scale;

    @Autowired
    private ApplicationContext context;

    public GraphicImport(@Value("${application.icon}") String icon, @Value("${application.title}") String title, Settings settings, @Value("${application.css}") String css) {
        super(icon, title, new Position(
                Screen.getPrimary().getBounds().getMaxX() / 2 - 250,
                Screen.getPrimary().getBounds().getMaxY() / 2 - 250
        ), new Position(
                650,
                500
        ));

        this.settings = settings;
        this.scale = 1;

        scene.getStylesheets().add(
                Objects.requireNonNull(Settings.class.getResource(css)).toExternalForm()
        );

        this.stage.setAlwaysOnTop(true);
    }

    @PostConstruct
    private void setup() {
        // File Selector
        Button button = new Button("Select graphic");
        button.setOnAction(event -> {
            File file = new FileChooser().showOpenDialog(stage);
            this.image = new Image(file.toURI().toString());
            repaint();
        });

        // Scale Slider
        Slider slider = new Slider(0.5, 5, 1);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(0.5f);
        slider.setBlockIncrement(0.1f);

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.scale = newValue.doubleValue();
            repaint();
        });

        Button apply = new Button("Apply");
        apply.setOnAction(event -> {
            if (image == null)
                return;

            context.publishEvent(new ShapeSubmitEvent(new Graphic(
                    image,
                    new Position(
                            Screen.getPrimary().getBounds().getMaxX() / 2,
                            Screen.getPrimary().getBounds().getMaxY() / 2
                    ),
                    this.image.getWidth() / scale,
                    this.image.getHeight() / scale,
                    Color.TRANSPARENT,
                    1
            )));
        });

        box.getChildren().addAll(button, slider, apply);
    }

    @Override
    public void paintComponent(GraphicsWrapper context) {
        if(image == null)
            return;

        double middleX = getWidth() / 2;
        double middleY = getHeight() / 2;

        double width = image.getWidth() / this.scale;
        double height = image.getHeight() / this.scale;

        context.drawImage(image,
                middleX - (width / 2),
                middleY - (height / 2),
                width,
                height
        );
    }
}
