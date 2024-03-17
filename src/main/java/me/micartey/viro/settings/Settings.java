package me.micartey.viro.settings;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import dev.lukasl.jwinkey.enums.VirtualKey;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import lombok.Getter;
import me.micartey.viro.events.viro.SettingUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
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

    private final PreferencesFx preferencesFx;

    @Autowired
    private ApplicationContext context;

    public Settings(@Value("${application.icon}") String icon, @Value("${application.css}") String css) {
        this.preferencesFx = this.createPreferences()
                .dialogTitle("Settings")
                .instantPersistent(true)
                .buttonsVisibility(false)
                .saveSettings(true);

        this.preferencesFx.getView().getScene().getStylesheets().add(
                Objects.requireNonNull(Settings.class.getResource(css)).toExternalForm()
        );

        this.preferencesFx.dialogIcon(new Image(
                Objects.requireNonNull(Settings.class.getResourceAsStream(icon))
        ));

        this.preferencesFx.addEventHandler(PreferencesFxEvent.EVENT_PREFERENCES_SAVED, handler -> {
            this.context.publishEvent(new SettingUpdateEvent(this));
        });
    }

    private PreferencesFx createPreferences() {
        return PreferencesFx.of(Settings.class,
                Category.of("General",
                        Group.of("Editor",
                                Setting.of("Editor Color", editorColor),
                                Setting.of("Icon Color", iconColor)
                        ),
                        Group.of("Background",
                                Setting.of("Color", backgroundColor)
                        ),
                        Group.of("Drawing",
                                Setting.of("Smoothness", smoothness, 1, 20)
                        )
                ).subCategories(
                        Category.of("Keybindings",
                                Group.of("Visibility",
                                        Setting.of("Show",
                                                keybindings,
                                                enableSelection
                                        ),
                                        Setting.of("Hide",
                                                keybindings,
                                                disableSelection
                                        )
                                ),
                                Group.of("Editor",
                                        Setting.of("Import Graphics",
                                                keybindings,
                                                openGraphicImport
                                        ),
                                        Setting.of("Undo",
                                                keybindings,
                                                undoSelection
                                        ),
                                        Setting.of("Redo",
                                                keybindings,
                                                redoSelection
                                        ),
                                        Setting.of("Clear",
                                                keybindings,
                                                clearSelection
                                        )
                                )
                        )
                ).expand()
        );
    }

    public Color getBackgroundColor() {
        Color color = backgroundColor.get();
        return Color.color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0.01, color.getOpacity())
        );
    }

    public void show() {
        this.preferencesFx.show();
    }
}
