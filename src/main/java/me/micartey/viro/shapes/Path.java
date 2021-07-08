package me.micartey.viro.shapes;

import javafx.scene.paint.Color;
import me.micartey.viro.window.utilities.Position;
import me.micartey.viro.window.wrapper.GraphicsWrapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Path extends Shape {

    private final Map<Position, Integer> positions;

    public Path(Map<Position, Integer> positions, Color color, int width) {
        super(color, width);

        this.positions = new LinkedHashMap<>(positions);
    }

    @Override
    public void paint(GraphicsWrapper context) {
        for(int index = 0; index < positions.size() - 1; index++) {
            Position current = this.getByIndex(index);
            Position next = this.getByIndex(index + 1);

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
//        return IntStream.range(0, this.positions.size() - 1).anyMatch(index -> {
//            Position destination = this.getByIndex(index + 1);
//            Position origin = this.getByIndex(index);
//
//            double distanceDestination = destination.distance(position);
//            double distanceOrigin = origin.distance(position);
//
//            double distance = origin.distance(destination);
//
//            return Math.abs(distance - (distanceOrigin + distanceDestination)) < .3;
//        });

        return positions.keySet().stream().mapToDouble(it -> it.distance(position))
                .min().orElse(Integer.MAX_VALUE) < 4;
    }

    @Override
    public Set<Position> getPoints() {
        return this.positions.keySet();
    }
}
