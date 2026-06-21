package me.micartey.viro.collab;

import javafx.scene.paint.Color;
import me.micartey.viro.shapes.Rectangle;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.shapes.utilities.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CollabStoreTest {

    @Test
    void tombstonePreventsDeletedRemoteShapeFromBeingRecreated() {
        CollabStore store = new CollabStore("localhost:8101");
        CollabShapeId id = new CollabShapeId("localhost:8102", 1);
        Shape shape = new Rectangle(Color.RED, 2, new Position(1, 2), new Position(3, 4));

        store.upsert(new CollabWire.CollabShape(id, shape));
        store.remove(id);

        assertTrue(store.isDeleted(id));
        assertTrue(store.upsert(new CollabWire.CollabShape(id, shape)).isEmpty());
        assertTrue(store.get(id).isEmpty());
    }
}
