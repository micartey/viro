package me.micartey.viro.input;

import com.sun.javafx.application.PlatformImpl;
import dev.lukasl.jwinkey.components.KeyStateUpdate;
import dev.lukasl.jwinkey.enums.KeyState;
import dev.lukasl.jwinkey.enums.VirtualKey;
import dev.lukasl.jwinkey.observables.KeyStateObservable;
import me.micartey.jation.JationObserver;
import me.micartey.viro.events.viro.SettingUpdateEvent;
import me.micartey.viro.settings.GraphicImport;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KeyboardObserver {

    private final JationObserver observer;
    private final RadialMenu     radialMenu;
    private final GraphicImport  graphicImport;
    private final Settings       settings;
    private final Window         window;

    private KeyStateObservable toggleVisibility;
    private KeyStateObservable toggleGraphicImport;
    private KeyStateObservable redoUndoObserver;
    private KeyStateObservable clearObserver;

    public KeyboardObserver(Window window, Settings settings, GraphicImport graphicImport, RadialMenu radialMenu, JationObserver observer) {
        this.radialMenu = radialMenu;
        this.observer = observer;
        this.graphicImport = graphicImport;
        this.settings = settings;
        this.window = window;
    }

    @SuppressWarnings("all")
    @EventListener({ApplicationStartedEvent.class, SettingUpdateEvent.class})
    public void updateVisibilityObserver() {
        Stream<VirtualKey> enable = this.fromNames(settings.getEnableSelection().stream());
        Stream<VirtualKey> disable = this.fromNames(settings.getDisableSelection().stream());

        this.toggleVisibility = KeyStateObservable.of(Stream.concat(enable, disable).toArray(VirtualKey[]::new));
        this.toggleVisibility.subscribe(this::onVisibilityUpdate);
    }

    @SuppressWarnings("all")
    @EventListener({ApplicationStartedEvent.class, SettingUpdateEvent.class})
    public void updateGraphicObserver() {
        Stream<VirtualKey> toggle = this.fromNames(settings.getOpenGraphicImport().stream());

        this.toggleGraphicImport = KeyStateObservable.of(toggle.toArray(VirtualKey[]::new));
        this.toggleGraphicImport.subscribe(this::onGraphicImport);
    }

    @SuppressWarnings("all")
    @EventListener({ApplicationStartedEvent.class, SettingUpdateEvent.class})
    public void updateRedoUndoObserver() {
        Stream<VirtualKey> redo = this.fromNames(settings.getRedoSelection().stream());
        Stream<VirtualKey> undo = this.fromNames(settings.getUndoSelection().stream());

        this.redoUndoObserver = KeyStateObservable.of(Stream.concat(redo, undo).toArray(VirtualKey[]::new));
        this.redoUndoObserver.subscribe(this::onRedoUndoUpdate);
    }

    @SuppressWarnings("all")
    @EventListener({ApplicationStartedEvent.class, SettingUpdateEvent.class})
    public void updateClearObserver() {
        Stream<VirtualKey> clear = this.fromNames(settings.getClearSelection().stream());

        this.clearObserver = KeyStateObservable.of(clear.toArray(VirtualKey[]::new));
        this.clearObserver.subscribe(this::onClearUpdate);
    }

    /**
     * Consumer for key press {@link KeyStateObservable KeyStateObservable} to
     * toggle visibility
     *
     * @param update {@link KeyStateUpdate}
     */
    private void onVisibilityUpdate(KeyStateUpdate update) {
        if(!update.getKeyState().equals(KeyState.PRESSED))
            return;

        if(this.fromNames(this.settings.getEnableSelection().stream()).allMatch(this.toggleVisibility::isPressed)) {
            PlatformImpl.runLater(this.window.stage::show);
            return;
        }

        if(this.fromNames(this.settings.getDisableSelection().stream()).allMatch(this.toggleVisibility::isPressed)) {
            PlatformImpl.runLater(this.graphicImport.stage::hide);
            PlatformImpl.runLater(this.radialMenu.stage::hide);
            PlatformImpl.runLater(this.window.stage::hide);
        }
    }

    /**
     * Consumer for key press {@link KeyStateObservable KeyStateObservable} to
     * perform redo / undo
     *
     * @param update {@link KeyStateUpdate}
     */
    private void onRedoUndoUpdate(KeyStateUpdate update) {
        if(!update.getKeyState().equals(KeyState.PRESSED))
            return;

        if(this.fromNames(this.settings.getRedoSelection().stream()).allMatch(this.redoUndoObserver::isPressed)) {
            this.window.redo();
            return;
        }

        if(this.fromNames(this.settings.getUndoSelection().stream()).allMatch(this.redoUndoObserver::isPressed))
            this.window.undo();
    }

    /**
     * Consumer for key press {@link KeyStateObservable KeyStateObservable} to
     * clear all shapes
     *
     * @param update {@link KeyStateUpdate}
     */
    private void onClearUpdate(KeyStateUpdate update) {
        if(!update.getKeyState().equals(KeyState.PRESSED))
            return;

        if(this.fromNames(this.settings.getClearSelection().stream()).allMatch(this.clearObserver::isPressed)) {
            this.window.getVisible().clear();
            this.window.getInvisible().clear();
            this.window.repaint();
        }
    }

    /**
     * Consumer for key press {@link KeyStateObservable KeyStateObservable} to
     * open graphic import window
     *
     * @param update {@link KeyStateUpdate}
     */
    private void onGraphicImport(KeyStateUpdate update) {
        if(!update.getKeyState().equals(KeyState.PRESSED)
                || !this.window.stage.isShowing())
            return;

        if(this.fromNames(this.settings.getOpenGraphicImport().stream()).allMatch(this.toggleGraphicImport::isPressed))
            PlatformImpl.runLater(this.graphicImport.stage::show);
    }

    /**
     * Gets an optional of a {@link VirtualKey VirtualKey} with corresponding description
     *
     * @param name of {@link VirtualKey VirtualKey}
     * @return Optional of VirtualKey
     */
    private Optional<VirtualKey> fromName(String name) {
        return Arrays.stream(VirtualKey.values())
                .filter(key -> key.name().equals("VK_" + name))
                .findFirst();
    }

    /**
     * Gets an {@link Stream stream} of {@link VirtualKey VirtualKeys} with corresponding description
     *
     * @param stream of {@link VirtualKey VirtualKeys}
     * @return Stream of VirtualKey
     */
    private Stream<VirtualKey> fromNames(Stream<String> stream) {
        return stream.map(this::fromName).filter(Optional::isPresent).map(Optional::get);
    }
}
