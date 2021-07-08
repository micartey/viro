package me.micartey.viro.shapes.properties;

import me.micartey.viro.window.utilities.Position;

public interface Movable extends Selectable {

    /**
     * Translate a {@link me.micartey.viro.shapes.Shape shape} by
     * a given vector
     *
     * @param vector in which the Shape will be translated
     * @see Position
     */
    void translate(Position vector);

}
