package me.micartey.viro.settings;

import dev.lukasl.jwinkey.enums.VirtualKey;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class Settings {

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.color(.2, .2, .2, .2));

    private final ListProperty<String> keybindings = new SimpleListProperty<>(FXCollections.observableList(
            Arrays.stream(VirtualKey.values()).map(VirtualKey::name).map(name -> name.substring(3)).collect(Collectors.toList())
    ));

    @Getter private final ListProperty<String> openGraphicImport  = new SimpleListProperty<>(FXCollections.observableList(
            Arrays.asList("CONTROL", "I")
    ));

    @Getter private final ListProperty<String> enableSelection  = new SimpleListProperty<>(FXCollections.observableList(
            Collections.singletonList("PAUSE")
    ));
    @Getter private final ListProperty<String> disableSelection = new SimpleListProperty<>(FXCollections.observableList(
            Collections.singletonList("ESCAPE")
    ));

    @Getter private final ListProperty<String> undoSelection = new SimpleListProperty<>(FXCollections.observableList(
            Arrays.asList("CONTROL", "Z")
    ));
    @Getter private final ListProperty<String> redoSelection = new SimpleListProperty<>(FXCollections.observableList(
            Arrays.asList("CONTROL", "Y")
    ));

    @Getter private final ListProperty<String> clearSelection = new SimpleListProperty<>(FXCollections.observableList(
            Collections.singletonList("DELETE")
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