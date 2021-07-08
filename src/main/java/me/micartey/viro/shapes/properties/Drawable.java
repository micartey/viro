package me.micartey.viro.shapes.properties;

import me.micartey.viro.window.wrapper.GraphicsWrapper;

public interface Drawable {

    /**
     * Consumer method to draw shapes on a preselected Canvas which provides
     * the {@link GraphicsWrapper GraphicsWrapper} to draw on it
     *
     * @param context to draw on
     * @see GraphicsWrapper
     */
    void draw(GraphicsWrapper context);

}
