package me.micartey.viro.brushes;

import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MousePressEvent;
import me.micartey.viro.events.mouse.MouseReleaseEvent;
import me.micartey.viro.settings.Settings;
import me.micartey.viro.shapes.Path;
import me.micartey.viro.window.RadialMenu;
import me.micartey.viro.window.Window;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Order(0)
@Component
public class Pencil extends Brush {

    private final Settings settings;

    private final Map<Position, Integer> positions;

    public Pencil(@Value("${viro.brush.pencil.icon}") String icon, @Value("${viro.brush.pencil.name}") String name, JationObserver observer, Settings settings) {
        super(icon, name, observer);

        this.positions = new LinkedHashMap<>();
        this.settings = settings;
    }

    @Observe
    public void onPress(MousePressEvent event) {
        this.positions.clear();
    }

    @Observe
    public void onDrag(MouseDragEvent event, Window window) {
        GraphicsWrapper wrapper = window.getPreviewGraphics();

        Position lastPosition = !this.positions.isEmpty() ? this.positions.keySet().toArray(new Position[]{})[this.positions.size() - 1] : event.getSource();
        Position position = new Position(
                lastPosition.getX() + ((event.getDestination().getX() - lastPosition.getX()) / this.settings.getSmoothness().get()),
                lastPosition.getY() + ((event.getDestination().getY() - lastPosition.getY()) / this.settings.getSmoothness().get())
        );

        wrapper.drawLine(lastPosition.getX(), lastPosition.getY(), position.getX(), position.getY());

        this.positions.put(position, wrapper.getLineWidth());
    }

    @Observe
    public void onRelease(MouseReleaseEvent event, RadialMenu radialMenu, Window window) {
        this.submit(new Path(
                this.positions,
                radialMenu.getColor(),
                window.getPreviewGraphics().getLineWidth()
        ));
    }
}
