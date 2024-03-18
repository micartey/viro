package me.micartey.viro.events.viro;

import javafx.scene.input.KeyCode;
import lombok.Data;
import me.micartey.jation.interfaces.JationEvent;

import java.util.Set;

@Data
public class KeyPressEvent {

    private final Set<KeyCode> keyCodes;

}
