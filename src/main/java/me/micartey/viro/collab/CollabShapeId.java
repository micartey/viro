package me.micartey.viro.collab;

record CollabShapeId(String origin, int sequence) {
    CollabShapeId {
        if (origin == null || origin.isBlank()) {
            origin = "unknown";
        }
    }
}
