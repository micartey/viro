package me.micartey.viro.events.mouse;

import lombok.Data;
import me.micartey.jation.interfaces.JationEvent;

@Data
public class MouseScrollEvent implements JationEvent<MouseScrollEvent> {

    private final int vector;

}
