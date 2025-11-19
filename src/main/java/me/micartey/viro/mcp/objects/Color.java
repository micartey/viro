package me.micartey.viro.mcp.objects;

public record Color(int red, int green, int blue, int alpha) {

    public javafx.scene.paint.Color toFxColor() {
        return new javafx.scene.paint.Color(
                red / 255D,
                green / 255D,
                blue / 255D,
                alpha / 255D
        );
    }
}