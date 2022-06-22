package me.micartey.viro.settings;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import lombok.Getter;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Graphic;
import me.micartey.viro.window.utilities.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

@Component
public class GraphicImport {

    @Getter private final ObjectProperty<File> graphicFile = new SimpleObjectProperty<>();

    @Getter private final IntegerProperty scale = new SimpleIntegerProperty(1);

    private final PreferencesFx preferencesFx;

    @Autowired
    private ApplicationContext context;

    public GraphicImport(@Value("${application.icon}") String icon, @Value("${application.css}") String css) {
        this.preferencesFx = this.createPreferences()
                .dialogTitle("Graphic Import")
                .instantPersistent(true)
                .buttonsVisibility(true)
                .saveSettings(true);

        this.preferencesFx.getView().getScene().getStylesheets().add(
                Objects.requireNonNull(GraphicImport.class.getResource(css)).toExternalForm()
        );

        this.preferencesFx.dialogIcon(new Image(
                Objects.requireNonNull(GraphicImport.class.getResourceAsStream(icon))
        ));

        this.preferencesFx.addEventHandler(PreferencesFxEvent.EVENT_PREFERENCES_SAVED, handler -> {
            File file = this.getGraphicFile().getValue();
            Image image = new Image(file.toURI().toString());

            this.context.publishEvent(new ShapeSubmitEvent(new Graphic(
                    image,
                    new Position(
                            50,
                            50
                    ),
                    image.getWidth() / scale.get(),
                    image.getHeight() / scale.get(),
                    Color.WHITE,
                    1
            )));
        });
    }

    private PreferencesFx createPreferences() {
        return PreferencesFx.of(GraphicImport.class,
                Category.of("File",
                        Group.of("Select File",
                                Setting.of("File", graphicFile, false),
                                Setting.of("Scale", scale, 1, 20)
                        )
                )
        );
    }


    public void show() {
        this.preferencesFx.show();
    }
}
