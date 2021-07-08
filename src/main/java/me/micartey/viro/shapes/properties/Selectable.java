package me.micartey.viro.shapes.properties;

import me.micartey.viro.window.utilities.Position;

public interface Selectable {

    /**
     * Check whether a screen/mouse {@link Position position} is inside the {@link me.micartey.viro.shapes.Shape shape}
     * or rather on the {@link me.micartey.viro.shapes.Shape shape} itself.
     * Depends on the usage case
     *
     * @param position of the mouse
     * @return whether the {@link Position position} is inside the {@link me.micartey.viro.shapes.Shape shape}
     */
    boolean select(Position position);

}
