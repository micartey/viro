package me.micartey.viro.events.mouse;

import javafx.scene.input.MouseButton;
import lombok.Data;
import me.micartey.jation.interfaces.JationEvent;
import me.micartey.viro.shapes.utilities.Position;

@Data
public class MouseDragEvent implements JationEvent<MouseDragEvent> {

    private final Position    origin, source, destination;
    private final MouseButton mouseButton;

}
