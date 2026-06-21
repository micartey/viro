package me.micartey.viro.collab;

import me.micartey.viro.shapes.Shape;

final class CollabShapeEntry {

    private final CollabShapeId id;
    private Shape shape;
    private String hash;
    private int missingCycles;

    CollabShapeEntry(CollabShapeId id, Shape shape) {
        this.id = id;
        this.shape = shape;
        this.hash = CollabWire.contentHash(shape);
    }

    CollabShapeId id() {
        return id;
    }

    Shape shape() {
        return shape;
    }

    String hash() {
        return hash;
    }

    int missingCycles() {
        return missingCycles;
    }

    void markPresent() {
        this.missingCycles = 0;
    }

    int markMissing() {
        return ++this.missingCycles;
    }

    void updateShape(Shape shape) {
        this.shape = shape;
        this.hash = CollabWire.contentHash(shape);
        this.missingCycles = 0;
    }

    boolean refreshHash() {
        String current = CollabWire.contentHash(shape);
        if (current.equals(hash)) {
            return false;
        }

        this.hash = current;
        return true;
    }
}
