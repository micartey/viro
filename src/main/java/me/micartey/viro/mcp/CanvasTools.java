package me.micartey.viro.mcp;

import lombok.RequiredArgsConstructor;
import me.micartey.viro.mcp.entities.Color;
import me.micartey.viro.mcp.entities.Resolution;
import me.micartey.viro.window.Canvas;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CanvasTools {

    private final Canvas canvas;

    @McpTool(name = "getScreenResolution", description = "Get the screen resolution in order to find a visual fitting size for shapes")
    public Resolution getScreenResolution() {
        return new Resolution(
                canvas.getWidth(),
                canvas.getHeight()
        );
    }

    @McpTool(name = "setBackgroundColor", description = "Set the background color of the default plane for viro")
    public void setBackgroundColor(@McpToolParam(description = "Background color") Color color) {
        canvas.setBackground(color.toFxColor());
    }
}