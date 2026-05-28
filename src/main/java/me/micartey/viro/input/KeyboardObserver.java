package me.micartey.viro.input;

import javafx.scene.input.KeyCode;
import lombok.RequiredArgsConstructor;
import me.micartey.viro.events.viro.KeyPressEvent;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.window.GraphicsImport;
import me.micartey.viro.window.Canvas;
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
    private final Settings settings;
    private final Canvas   canvas;

    private final List<KeyCode> pressedKeys = new ArrayList<>();

    @EventListener({ApplicationStartedEvent.class})
    public void subscribeToKeyboardEvents() {
        this.canvas.getScene().setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
        });

        this.canvas.getScene().setOnKeyReleased(event -> {
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

        this.canvas.undo();
    }

    @EventListener({KeyPressEvent.class})
    public void onRedo(KeyPressEvent event) {
        Set<KeyCode> redoSet = this.settings.getRedoSelection();

        if (!event.getKeyCodes().containsAll(redoSet))
            return;

        this.canvas.redo();
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

        while (!this.canvas.getVisible().isEmpty())
            this.canvas.undo();

        this.canvas.setBackground(settings.getBackgroundColor());
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
