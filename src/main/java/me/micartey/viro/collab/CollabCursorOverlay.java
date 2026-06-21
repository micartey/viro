package me.micartey.viro.collab;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import me.micartey.viro.shapes.utilities.Position;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
final class CollabCursorOverlay {

    private static final double CURSOR_SIZE = 38;
    private static final String CURSOR_RESOURCE = "/assets/cursor.png";
    private static final Color[] PEER_COLORS = {
            Color.web("#2F80ED"),
            Color.web("#F2994A"),
            Color.web("#27AE60"),
            Color.web("#EB5757"),
            Color.web("#9B51E0"),
            Color.web("#00A8A8"),
            Color.web("#F2C94C"),
            Color.web("#D946EF"),
            Color.web("#56CCF2"),
            Color.web("#6FCF97")
    };

    private final Canvas canvas;
    private final Image cursorMask;
    private final Map<String, Image> cursorImages = new ConcurrentHashMap<>();
    private final AtomicBoolean renderQueued = new AtomicBoolean();
    private volatile List<CollabWire.CollabCursor> latest = List.of();

    CollabCursorOverlay(me.micartey.viro.window.Canvas viroCanvas) {
        this.canvas = viroCanvas.createCanvasOnTop();
        this.canvas.setMouseTransparent(true);
        this.cursorMask = new Image(Objects.requireNonNull(
                CollabCursorOverlay.class.getResourceAsStream(CURSOR_RESOURCE)
        ));
    }

    void render(List<CollabWire.CollabCursor> cursors) {
        this.latest = cursors;
        if (!renderQueued.compareAndSet(false, true)) {
            return;
        }

        Platform.runLater(() -> {
            try {
                draw(latest);
            } finally {
                renderQueued.set(false);
            }
        });
    }

    private void draw(List<CollabWire.CollabCursor> cursors) {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (CollabWire.CollabCursor cursor : cursors) {
            drawCursor(graphics, cursor);
        }
    }

    private void drawCursor(GraphicsContext graphics, CollabWire.CollabCursor cursor) {
        Position position = cursor.position();
        Color color = colorFor(cursor.origin());
        double x = position.getX();
        double y = position.getY();

        graphics.setImageSmoothing(true);
        graphics.drawImage(cursorImage(cursor.origin(), color), x, y, CURSOR_SIZE, CURSOR_SIZE);

        String label = cursor.origin();
        double labelX = x + 40;
        double labelY = y + 30;
        double labelWidth = Math.max(64, label.length() * 7.0 + 18);
        graphics.setFont(Font.font("sans serif", FontWeight.BOLD, 12));
        graphics.setFill(color);
        graphics.fillRoundRect(labelX, labelY, labelWidth, 26, 14, 14);
        graphics.setFill(textColor(color));
        graphics.fillText(label, labelX + 9, labelY + 17);
    }

    private Image cursorImage(String origin, Color color) {
        return cursorImages.computeIfAbsent(origin, ignored -> tintCursor(color));
    }

    private Image tintCursor(Color color) {
        int width = (int) cursorMask.getWidth();
        int height = (int) cursorMask.getHeight();
        WritableImage image = new WritableImage(width, height);
        PixelReader reader = cursorMask.getPixelReader();
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color source = reader.getColor(x, y);
                double alpha = maskAlpha(source);
                writer.setColor(x, y, alpha <= 0 ? Color.TRANSPARENT : withOpacity(color, alpha));
            }
        }

        return image;
    }

    private double maskAlpha(Color source) {
        if (source.getOpacity() < 1) {
            return source.getOpacity();
        }

        double brightness = Math.min(1, (source.getRed() + source.getGreen() + source.getBlue()) / 1.2);
        if (brightness <= 0.5) {
            return 0;
        }

        double normalized = (brightness - 0.5) * 2;
        return normalized * normalized * (3 - (2 * normalized));
    }

    private Color withOpacity(Color color, double opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }

    private Color colorFor(String origin) {
        int hash = spread(origin.hashCode());
        return PEER_COLORS[Math.floorMod(hash, PEER_COLORS.length)];
    }

    private int spread(int value) {
        value ^= value >>> 16;
        value *= 0x7feb352d;
        value ^= value >>> 15;
        value *= 0x846ca68b;
        value ^= value >>> 16;
        return value;
    }

    private Color textColor(Color background) {
        double luminance = (0.299 * background.getRed()) + (0.587 * background.getGreen()) + (0.114 * background.getBlue());
        return luminance > 0.62 ? Color.color(0.08, 0.08, 0.08) : Color.WHITE;
    }
}
