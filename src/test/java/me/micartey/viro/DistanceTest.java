package me.micartey.viro;

import me.micartey.viro.shapes.utilities.Position;
import org.junit.jupiter.api.Test;

class DistanceTest {

    @Test
    public void testDistance() {
        Position position = new Position(0, 0);
        position.distance(new Position(2, 2));
    }

}
