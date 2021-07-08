package me.micartey.viro.shapes;

import javafx.scene.paint.Color;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;

import java.util.*;

public class Polygon extends Shape{

    private final List<Position> positions;

    public Polygon(Color color, int width, Position... positions) {
        super(color, width);

        this.positions = new ArrayList<>(Arrays.asList(positions));
    }

    @Override
    public void paint(GraphicsWrapper context) {
        context.fillPolygon(
                this.positions.toArray(new Position[]{})
        );
    }

    @Override
    public Set<Position> getPoints() {
        return new HashSet<>(this.positions);
    }

    @Override
    public void translate(Position vector) {
        List<Position> backup = new ArrayList<>();
        this.positions.forEach(position -> backup.add(position.translate(vector)));
        this.positions.clear();
        this.positions.addAll(backup);
    }

    @Override
    public boolean select(Position position) {
        return this.positions.stream().anyMatch(it -> it.distance(position) < 2);
    }
}
