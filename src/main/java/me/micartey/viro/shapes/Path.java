package me.micartey.viro.shapes;

import javafx.scene.paint.Color;
import me.micartey.viro.shapes.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Path extends Shape {

    private final Map<Position, Integer> positions;
    private final double[]               dashes;

    public Path(Map<Position, Integer> positions, Color color, int width, double... dashes) {
        super(color, width);

        this.positions = new LinkedHashMap<>(positions);
        this.dashes = Arrays.stream(dashes).toArray();
    }

    public Path(Map<Position, Integer> positions, Color color, int width) {
        this(positions, color, width, new double[0]);
    }

    @Override
    public void paint(GraphicsWrapper context) {
        for (int index = 0; index < positions.size() - 1; index++) {
            Position current = this.getByIndex(index);
            Position next = this.getByIndex(index + 1);

            context.setLineDashes(this.dashes);
            context.setLineWidth(this.positions.get(current));
            context.drawLine(
                    current.getX(),
                    current.getY(),
                    next.getX(),
                    next.getY()
            );
        }
    }

    private Position getByIndex(int index) {
        return positions.keySet().toArray(new Position[0])[index];
    }

    @Override
    public void translate(Position vector) {
        Map<Position, Integer> translated = new LinkedHashMap<>();

        this.positions.forEach((key, value) -> {
            translated.put(
                    key.translate(vector),
                    value
            );
        });

        this.positions.clear();
        this.positions.putAll(translated);
    }

    @Override
    public boolean select(Position position) {
        return positions.keySet().stream().mapToDouble(it -> it.distance(position))
                .min().orElse(Integer.MAX_VALUE) < 4;
    }

    @Override
    public Set<Position> getPoints() {
        return this.positions.keySet();
    }
}
