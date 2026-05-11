package me.micartey.viro.mcp;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import lombok.RequiredArgsConstructor;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Graphic;
import me.micartey.viro.shapes.utilities.Position;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class ImageTools {

    private final ApplicationContext context;

    @McpTool(name = "drawImage", description = "Draw an image that is on the local file system")
    public int drawImage(
            @McpToolParam(description = "Path to image file") String path,
            @McpToolParam(description = "Desired width") int width,
            @McpToolParam(description = "Desired height") int height) {
        Image image = new Image(new File(path).toURI().toString());

        int ratio = (int) (image.getHeight() / height);

        Graphic graphic = new Graphic(
                image,
                new Position(
                        Screen.getPrimary().getBounds().getMaxX() / 2,
                        Screen.getPrimary().getBounds().getMaxY() / 2
                ),
                image.getWidth() / ratio,
                image.getHeight() / ratio,
                Color.TRANSPARENT,
                1
        );

        context.publishEvent(new ShapeSubmitEvent(graphic));

        return graphic.hashCode();
    }

    @McpTool(name = "drawImageFromURL", description = "Draw an image that is not on the file system")
    public int drawImageFromURL(
            @McpToolParam(description = "URL to image") String path,
            @McpToolParam(description = "Desired width") int width,
            @McpToolParam(description = "Desired height") int height) {
        Image image = new Image(path, false);

        int ratio = (int) (image.getHeight() / height);

        Graphic graphic = new Graphic(
                image,
                new Position(
                        Screen.getPrimary().getBounds().getMaxX() / 2,
                        Screen.getPrimary().getBounds().getMaxY() / 2
                ),
                image.getWidth() / ratio,
                image.getHeight() / ratio,
                Color.TRANSPARENT,
                1
        );

        context.publishEvent(new ShapeSubmitEvent(graphic));

        return graphic.hashCode();
    }
}