package me.micartey.viro.window.wrapper;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import me.micartey.viro.window.utilities.Position;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

import java.util.Objects;

public abstract class CanvasWrapper extends Canvas {

    public final Stage stage = new Stage();

    public final Group group = new Group();
    public final VBox  box   = new VBox();
    public final Scene scene = new Scene(group);

    @Getter private Position position, size;

    /**
     * Create a new canvas which is by default transparent and acts as an overlay.
     *
     * @param icon     Resource path to the icon file
     * @param title    Window title
     * @param position Window position (top-left corner)
     * @param size     Window size (width, height)
     */
    public CanvasWrapper(String icon, String title, Position position, Position size) {
        this.init(title, icon);

        this.position = position;
        this.size = size;

        this.group.getChildren().addAll(this.box, this);

        this.resize();
    }

    /**
     * Initializes the overlay window and adds the title and icon property
     *
     * @param title Window title
     * @param icon  Window icon
     */
    private void init(String title, String icon) {
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setTitle(title);

        this.stage.getIcons().add(
                new Image(Objects.requireNonNull(CanvasWrapper.class.getResourceAsStream(icon)))
        );

        this.stage.setScene(this.scene);
        this.scene.setFill(Color.TRANSPARENT);
    }

    /**
     * Resizes the window after setting the position and size.
     * Necessary to apply changes
     */
    private void resize() {
        this.box.setPadding(new Insets(
                0,
                0,
                this.size.getY(),
                this.size.getX()
        ));

        this.stage.setX(this.position.getX());
        this.stage.setY(this.position.getY());

        this.setWidth(this.size.getX());
        this.setHeight(this.size.getY());
    }

    /**
     * Makes sure that {@link CanvasWrapper#resize()} is called
     *
     * @param position Window Position
     * @see Position
     */
    public void updatePosition(Position position) {
        this.position = position;
        this.resize();
    }

    /**
     * Makes sure that {@link CanvasWrapper#resize()} is called
     *
     * @param size Window Size
     * @see Position
     */
    public void updateSize(Position size) {
        this.size = size;
        this.resize();
    }

    /**
     * Sets the background color.
     * Alpha channel is supported
     *
     * @param color Background color
     * @see Color
     */
    public void setBackground(Color color) {
        this.box.setStyle("-fx-background-color: #" + color.toString().substring(2) + ";");
    }

    public Canvas createChildCanvas() {
        Canvas canvas = new Canvas(
                this.getWidth(),
                this.getHeight()
        );
        this.group.getChildren().add(canvas);
        return canvas;
    }

    /**
     * Implement this method and add your drawing statements here
     *
     * @param context {@link javafx.scene.canvas.GraphicsContext GraphicsContext} of Canvas
     * @see GraphicsWrapper
     */
    public abstract void paintComponent(GraphicsWrapper context);

    @EventListener(ApplicationStartedEvent.class)
    public void paintComponent() {
        this.paintComponent(new GraphicsWrapper(
                this.getGraphicsContext2D()
        ));
    }

    /**
     * Cleans the canvas and calls {@link CanvasWrapper#paintComponent() paintComponent} again
     */
    public void repaint() {
        this.getGraphicsContext2D().clearRect(
                0,
                0,
                this.getWidth(),
                this.getHeight()
        );

        this.paintComponent();
    }
}
