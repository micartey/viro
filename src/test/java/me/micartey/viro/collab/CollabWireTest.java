package me.micartey.viro.collab;

import javafx.scene.paint.Color;
import me.micartey.viro.shapes.Path;
import me.micartey.viro.shapes.Rectangle;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.shapes.utilities.Position;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CollabWireTest {

    @Test
    void roundTripsShapeWithGlobalId() {
        CollabShapeId id = new CollabShapeId("peer-a", 42);
        Shape shape = new Rectangle(Color.RED, 3, new Position(10, 20), new Position(30, 40));

        CollabWire.CollabShape result = CollabWire.fromJson(CollabWire.toJson(id, shape));

        assertEquals(id, result.id());
        Rectangle rectangle = assertInstanceOf(Rectangle.class, result.shape());
        assertEquals(10, rectangle.getRectPosition().getX());
        assertEquals(20, rectangle.getRectPosition().getY());
        assertEquals(30, rectangle.getRectSize().getX());
        assertEquals(40, rectangle.getRectSize().getY());
    }

    @Test
    void roundTripsPathPointWidths() {
        Map<Position, Integer> points = new LinkedHashMap<>();
        points.put(new Position(1, 2), 4);
        points.put(new Position(3, 4), 5);

        CollabWire.CollabShape result = CollabWire.fromJson(
                CollabWire.toJson(new CollabShapeId("peer-b", 7), new Path(points, Color.BLUE, 2))
        );

        Path path = assertInstanceOf(Path.class, result.shape());
        assertEquals(2, path.getPositionWidthMap().size());
        assertEquals(List.of(4, 5), path.getPositionWidthMap().values().stream().toList());
        assertEquals(List.of(1.0, 3.0), path.getPositionWidthMap().keySet().stream().map(Position::getX).toList());
        assertEquals(List.of(2.0, 4.0), path.getPositionWidthMap().keySet().stream().map(Position::getY).toList());
    }

    @Test
    void parsesJsonStringBodiesForPeerEndpoints() {
        assertEquals("http://localhost:8099", CollabWire.stringBody("\"http://localhost:8099\""));
        assertEquals("http://localhost:8099", CollabWire.stringBody(" http://localhost:8099 "));
    }

    @Test
    void deleteMessagesCarryOriginAndSequence() {
        CollabShapeId id = new CollabShapeId("peer-c", 11);

        assertEquals(id, CollabWire.deleteId(CollabWire.deleteJson(id)));
    }

    @Test
    void roundTripsCursorPosition() {
        CollabWire.CollabCursor cursor = new CollabWire.CollabCursor(
                "peer-d",
                new Position(123, 456),
                1000
        );

        CollabWire.CollabCursor result = CollabWire.cursorFromJson(CollabWire.cursorJson(cursor));

        assertEquals(cursor.origin(), result.origin());
        assertEquals(cursor.updatedAt(), result.updatedAt());
        assertEquals(123, result.position().getX());
        assertEquals(456, result.position().getY());
    }
}
