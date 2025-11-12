package me.micartey.viro.input;

import javafx.scene.input.KeyCode;
import lombok.RequiredArgsConstructor;
import me.micartey.jation.JationObserver;
import me.micartey.viro.events.viro.KeyPressEvent;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.window.GraphicsImport;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class KeyboardObserver {

    private final ApplicationContext context;

    private final GraphicsImport graphicsImport;
    private final Settings       settings;
    private final Window         window;

    private final List<KeyCode> pressedKeys = new ArrayList<>();

    @EventListener({ApplicationStartedEvent.class})
    public void subscribeToKeyboardEvents() {
        this.window.getScene().setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
        });

        this.window.getScene().setOnKeyReleased(event -> {
            KeyPressEvent keyPressEvent = new KeyPressEvent(new HashSet<>(pressedKeys));
            context.publishEvent(keyPressEvent);

            pressedKeys.remove(event.getCode());
        });
    }

    @EventListener({KeyPressEvent.class})
    public void onUndo(KeyPressEvent event) {
        Set<KeyCode> undoSet = this.settings.getUndoSelection();

        if (!event.getKeyCodes().containsAll(undoSet))
            return;

        this.window.undo();
    }

    @EventListener({KeyPressEvent.class})
    public void onRedo(KeyPressEvent event) {
        Set<KeyCode> redoSet = this.settings.getRedoSelection();

        if (!event.getKeyCodes().containsAll(redoSet))
            return;

        this.window.redo();
    }

    /**
     * Clear Operation:
     * <ul>
     *     <li>Remove all shapes</li>
     *     <li>Reset background color</li>
     * </ul>
     *
     * @param event key event
     */
    @EventListener({KeyPressEvent.class})
    public void onClear(KeyPressEvent event) {
        Set<KeyCode> clearSet = this.settings.getClearSelection();

        if (!event.getKeyCodes().containsAll(clearSet))
            return;

        while (!this.window.getVisible().isEmpty())
            this.window.undo();

        this.window.setBackground(settings.getBackgroundColor());
    }

    @EventListener({KeyPressEvent.class})
    public void onImport(KeyPressEvent event) {
        Set<KeyCode> importSet = this.settings.getGraphicImportSelection();

        if (!event.getKeyCodes().containsAll(importSet))
            return;

        this.graphicsImport.stage.show();
        this.graphicsImport.setup();
    }
}
