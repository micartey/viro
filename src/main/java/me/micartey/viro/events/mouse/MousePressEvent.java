package me.micartey.viro.events.mouse;

import javafx.scene.input.MouseButton;
import lombok.Data;
import me.micartey.jation.interfaces.JationEvent;
import me.micartey.viro.window.utilities.Position;

@Data
public class MousePressEvent implements JationEvent<MousePressEvent> {

    private final Position    position;
    private final MouseButton mouseButton;

}
