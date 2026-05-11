package me.micartey.viro.mcp;

import lombok.RequiredArgsConstructor;
import me.micartey.viro.mcp.objects.Color;
import me.micartey.viro.mcp.objects.Resolution;
import me.micartey.viro.window.Canvas;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CanvasTools {

    private final Canvas canvas;

    @Tool(
            name = "getScreenResolution",
            description = "Get the screen resolution in order to find a visual fitting size for shapes"
    )
    public Resolution getScreenResolution() {
        return new Resolution(
                canvas.getWidth(),
                canvas.getHeight()
        );
    }

    @Tool(
            name = "setBackgroundColor",
            description = "Set the background color of the default plane for viro"
    )
    public void setBackgroundColor(Color color) {
        canvas.setBackground(color.toFxColor());
    }
}
