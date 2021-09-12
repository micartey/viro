package me.micartey.viro.window.wrapper;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import me.micartey.viro.window.utilities.Position;

import java.util.Arrays;

public class GraphicsWrapper {

    private final GraphicsContext graphics;

    /**
     * Specify a {@link GraphicsContext GraphicsContext} which will be wrapped to provide
     * easy to use context. The pattern is mostly inspired by Java-Swing
     *
     * @param graphics which will be wrapped
     */
    public GraphicsWrapper(GraphicsContext graphics) {
        this.graphics = graphics;

        this.graphics.setLineJoin(StrokeLineJoin.ROUND);
        this.graphics.setLineCap(StrokeLineCap.ROUND);
    }

    /**
     * Set a {@link Color color} in which will
     * be drawn
     *
     * @param color of the stroke
     */
    public void setColor(Color color) {
        this.graphics.setStroke(color);
        this.graphics.setFill(color);
    }

    /**
     * Get the current Stroke color
     *
     * @return current Stroke color
     */
    public Color getColor() {
        return (Color) this.graphics.getStroke();
    }

    /**
     * Draw a straight line between two points
     *
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     */
    public void drawLine(double x1, double y1, double x2, double y2) {
        graphics.beginPath();
        graphics.moveTo(x1, y1);
        graphics.lineTo(x2, y2);
        graphics.stroke();
        graphics.closePath();
    }

    /**
     * Draw an oval
     *
     * @param x      x-coordinate of upper-left corner
     * @param y      y-coordinate of upper-left corner
     * @param width  width of the oval
     * @param height width of the oval
     */
    public void drawOval(double x, double y, double width, double height) {
        this.graphics.strokeOval(
                x,
                y,
                width,
                height
        );
    }

    /**
     * Draw an rect
     *
     * @param x      x-coordinate of upper-left corner
     * @param y      y-coordinate of upper-left corner
     * @param width  width of the rect
     * @param height width of the rect
     */
    public void drawRect(double x, double y, double width, double height) {
        this.graphics.strokeRect(
                x,
                y,
                width,
                height
        );
    }

    /**
     * Draw a String
     *
     * @param text message
     * @param x    x-coordinate of the text
     * @param y    y-coordinate of the text
     */
    public void drawString(String text, double x, double y, double fontSize) {
        this.graphics.setFont(Font.font("sans serif", FontWeight.EXTRA_LIGHT, fontSize));
        this.graphics.strokeText(
                text,
                x,
                y
        );
    }

    /**
     * Draw a filled rectangle
     *
     * @param x      x-coordinate of upper-left corner
     * @param y      y-coordinate of upper-left corner
     * @param width  width of the rectangle
     * @param height width of the rectangle
     */
    public void fillRect(double x, double y, double width, double height) {
        this.graphics.fillRect(
                x,
                y,
                width,
                height
        );
    }

    /**
     * Draw a filled oval
     *
     * @param x      x-coordinate of upper-left corner
     * @param y      y-coordinate of upper-left corner
     * @param width  width of the oval
     * @param height width of the oval
     */
    public void fillOval(double x, double y, double width, double height) {
        this.graphics.fillOval(
                x,
                y,
                width,
                height
        );
    }

    /**
     * Draw filled Polygon
     *
     * @param points List of points
     */
    public void fillPolygon(Position... points) {
        this.graphics.fillPolygon(
                Arrays.stream(points).mapToDouble(Position::getX).toArray(),
                Arrays.stream(points).mapToDouble(Position::getY).toArray(),
                points.length
        );
    }

    /**
     * Draw an {@link Image image} an a given location with custom
     * width and height
     *
     * @param image  which will be drawn
     * @param x      coordinate of image
     * @param y      coordinate of image
     * @param width  of image
     * @param height of image
     */
    public void drawImage(Image image, double x, double y, double width, double height) {
        this.graphics.drawImage(image,
                x,
                y,
                width,
                height
        );
    }

    /**
     * Set a width for the stroke to drawn in different strengths
     *
     * @param width of the stroke
     * @see GraphicsWrapper#getLineWidth()
     */
    public void setLineWidth(int width) {
        this.graphics.setLineWidth(width);
    }

    /**
     * Get the current stroke width
     *
     * @return stroke width
     * @see GraphicsWrapper#setLineWidth(int)
     */
    public int getLineWidth() {
        return (int) this.graphics.getLineWidth();
    }

    /**
     * Set a stroke line dash pattern
     *
     * @param dashes pattern
     * @see GraphicsWrapper#getLineDashes()
     */
    public void setLineDashes(double... dashes) {
        this.graphics.setLineDashes(dashes);
    }

    /**
     * Get the line dashes
     *
     * @return line dashes
     * @see GraphicsWrapper#setLineDashes(double...)
     */
    public double[] getLineDashes() {
        return this.graphics.getLineDashes();
    }

    /**
     * Reset the whole {@link GraphicsContext GraphicsContext}
     */
    public void reset() {
        this.graphics.clearRect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}
