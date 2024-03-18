package me.micartey.viro.settings;

import dev.lukasl.jwinkey.enums.VirtualKey;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Settings {

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.color(.2, .2, .2, .2));

    @Getter private final Set<KeyCode> graphicImportSelection = new HashSet<>(Arrays.asList(
            KeyCode.CONTROL, KeyCode.I
    ));

    @Getter private final Set<KeyCode> undoSelection = new HashSet<>(Arrays.asList(
            KeyCode.CONTROL, KeyCode.Z
    ));

    @Getter private final Set<KeyCode> redoSelection = new HashSet<>(Arrays.asList(
            KeyCode.CONTROL, KeyCode.Y
    ));

    @Getter private final Set<KeyCode> clearSelection = new HashSet<>(Arrays.asList(
            KeyCode.DELETE
    ));

    @Getter private final ObjectProperty<Color> editorColor = new SimpleObjectProperty<>(Color.rgb(21, 21, 21));
    @Getter private final ObjectProperty<Color> iconColor   = new SimpleObjectProperty<>(Color.rgb(159, 159, 159));

    @Getter private final IntegerProperty smoothness = new SimpleIntegerProperty(5);

    public Color getBackgroundColor() {
        Color color = backgroundColor.get();
        return Color.color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0.01, color.getOpacity())
        );
    }
}