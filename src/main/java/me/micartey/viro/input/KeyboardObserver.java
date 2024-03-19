package me.micartey.viro.input;

import javafx.scene.input.KeyCode;
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
public class KeyboardObserver {

    private final JationObserver observer;
    private final GraphicsImport graphicsImport;
    private final RadialMenu     radialMenu;
    private final Settings       settings;
    private final Window         window;

    private final List<KeyCode> pressedKeys;

    @Autowired
    private ApplicationContext context;

    public KeyboardObserver(Window window, Settings settings, RadialMenu radialMenu, GraphicsImport graphicsImport, JationObserver observer) {
        this.graphicsImport = graphicsImport;
        this.radialMenu = radialMenu;
        this.observer = observer;
        this.settings = settings;
        this.window = window;

        this.pressedKeys = new ArrayList<>();
    }

    @EventListener({ApplicationStartedEvent.class})
    public void subscribeToKeyboardEvents() {
        this.window.getScene().setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            KeyPressEvent keyPressEvent = new KeyPressEvent(new HashSet<>(pressedKeys));
            context.publishEvent(keyPressEvent);
        });

        this.window.getScene().setOnKeyReleased(event -> {
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

    @EventListener({KeyPressEvent.class})
    public void onImport(KeyPressEvent event) {
        Set<KeyCode> importSet = this.settings.getGraphicImportSelection();

        if (!event.getKeyCodes().containsAll(importSet))
            return;

        this.graphicsImport.stage.show();
        this.graphicsImport.resize();
    }

//    @SuppressWarnings("all")
//    @EventListener({ApplicationStartedEvent.class, SettingUpdateEvent.class})
//    public void updateClearObserver() {
//        Stream<VirtualKey> clear = this.fromNames(settings.getClearSelection().stream());
//
//        this.clearObserver = KeyStateObservable.of(clear.toArray(VirtualKey[]::new));
//        this.clearObserver.subscribe(this::onClearUpdate);
//    }

//    private void onRedoUndoUpdate(KeyStateUpdate update) {
//        if(!update.getKeyState().equals(KeyState.PRESSED))
//            return;
//
//        if(this.fromNames(this.settings.getRedoSelection().stream()).allMatch(this.redoUndoObserver::isPressed)) {
//            this.window.redo();
//            return;
//        }
//
//        if(this.fromNames(this.settings.getUndoSelection().stream()).allMatch(this.redoUndoObserver::isPressed))
//            this.window.undo();
//    }

//    private void onClearUpdate(KeyStateUpdate update) {
//        if(!update.getKeyState().equals(KeyState.PRESSED))
//            return;
//
//        if(this.fromNames(this.settings.getClearSelection().stream()).allMatch(this.clearObserver::isPressed)) {
//            this.window.getVisible().clear();
//            this.window.getInvisible().clear();
//            this.window.repaint();
//        }
//    }

//    private void onGraphicImport(KeyStateUpdate update) {
//        if(!update.getKeyState().equals(KeyState.PRESSED)
//                || !this.window.stage.isShowing())
//            return;
//
//        if(this.fromNames(this.settings.getOpenGraphicImport().stream()).allMatch(this.toggleGraphicImport::isPressed)) {
//        }
//    }
}
