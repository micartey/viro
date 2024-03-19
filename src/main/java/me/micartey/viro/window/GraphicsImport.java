package me.micartey.viro.window;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Graphic;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.CanvasWrapper;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

@Component
public class GraphicsImport extends CanvasWrapper {

    private Slider slider;
    private Image  image;

    @Autowired
    private ApplicationContext context;

    public GraphicsImport(@Value("${application.icon}") String icon, @Value("${application.title}") String title) {
        super(icon, title, new Position(0, 0), new Position(
                Screen.getPrimary().getBounds().getMaxX(),
                Screen.getPrimary().getBounds().getMaxY()
        ));

        this.setBackground(new Color(0, 0, 0, .5));

        scene.getStylesheets().add(
                Objects.requireNonNull(GraphicsImport.class.getResource("/darkmode.css")).toExternalForm()
        );

        this.stage.setAlwaysOnTop(true);
    }

    @PostConstruct
    private void setup() {
        // File Selector
        Button button = new Button("Select graphic");
        button.setTranslateY(5);
        button.setOnAction(event -> {
            File file = new FileChooser().showOpenDialog(stage);
            this.image = new Image(file.toURI().toString());
            repaint();
        });

        Label spacer1 = new Label(" ");
        Label label = new Label("Scale");

        // Scale Slider
        this.slider = new Slider(1, 10, 1);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(0.5f);
        slider.setBlockIncrement(0.1f);

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            repaint();
        });

        Label spacer2 = new Label(" ");

        Button apply = new Button("Apply");
        apply.setOnAction(event -> {
            if(image == null)
                return;

            context.publishEvent(new ShapeSubmitEvent(new Graphic(
                    image,
                    new Position(
                            Screen.getPrimary().getBounds().getMaxX() / 2,
                            Screen.getPrimary().getBounds().getMaxY() / 2
                    ),
                    this.image.getWidth() / this.slider.getValue(),
                    this.image.getHeight() / this.slider.getValue(),
                    Color.TRANSPARENT,
                    1
            )));

            this.stage.hide();
        });

        Button cancel = new Button("Cancel");
        cancel.setTranslateY(5);
        cancel.setOnAction(event -> {
            this.stage.hide();
        });

        box.getChildren().addAll(button, spacer1, label, slider, spacer2, apply, cancel);
    }

    public void resize() {
        this.slider.setValue(1);
        this.image = null;

        this.updateSize(new Position(
                Screen.getPrimary().getBounds().getMaxX() - this.slider.getWidth(),
                Screen.getPrimary().getBounds().getMaxY()
        ));

        this.box.getChildren().forEach(child -> {
            try {
                child.getClass().getMethod("setPrefWidth", double.class).invoke(child, this.slider.getWidth());
            } catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) { }
        });

        repaint();
    }

    @EventListener(MousePressEvent.class)
    public void onClick() {
        this.stage.hide();
    }

    @Override
    public void paintComponent(GraphicsWrapper context) {
        if(image == null)
            return;

        double middleX = getWidth() / 2;
        double middleY = getHeight() / 2;

        double width = image.getWidth() / this.slider.getValue();
        double height = image.getHeight() / this.slider.getValue();

        context.drawImage(image,
                middleX - (width / 2),
                middleY - (height / 2),
                width,
                height
        );
    }
}