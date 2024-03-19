package me.micartey.viro.events.mouse;

import javafx.scene.input.MouseButton;
import lombok.Data;
import me.micartey.jation.interfaces.JationEvent;
import me.micartey.viro.shapes.utilities.Position;

@Data
public class MouseReleaseEvent implements JationEvent<MouseReleaseEvent> {

    private final Position    position;
    private final MouseButton mouseButton;

}
