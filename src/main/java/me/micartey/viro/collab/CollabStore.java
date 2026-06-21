package me.micartey.viro.collab;

import me.micartey.viro.shapes.Shape;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

final class CollabStore {

    private final String origin;
    private final AtomicInteger nextSequence = new AtomicInteger(1);
    private final ConcurrentMap<CollabShapeId, CollabShapeEntry> entries = new ConcurrentHashMap<>();
    private final Set<CollabShapeId> tombstones = ConcurrentHashMap.newKeySet();

    CollabStore(String origin) {
        this.origin = origin;
    }

    String origin() {
        return origin;
    }

    CollabShapeEntry addLocal(Shape shape) {
        CollabShapeId id = new CollabShapeId(origin, nextSequence.getAndIncrement());
        CollabShapeEntry entry = new CollabShapeEntry(id, shape);
        entries.put(id, entry);
        return entry;
    }

    Optional<CollabShapeEntry> upsert(CollabWire.CollabShape remote) {
        if (tombstones.contains(remote.id())) {
            return Optional.empty();
        }

        CollabShapeEntry existing = entries.get(remote.id());
        if (existing == null) {
            CollabShapeEntry created = new CollabShapeEntry(remote.id(), remote.shape());
            CollabShapeEntry raced = entries.putIfAbsent(remote.id(), created);
            return Optional.of(raced == null ? created : raced);
        }

        if (existing.hash().equals(CollabWire.contentHash(remote.shape()))) {
            existing.markPresent();
            return Optional.empty();
        }

        existing.updateShape(remote.shape());
        return Optional.of(existing);
    }

    Optional<CollabShapeEntry> get(CollabShapeId id) {
        return Optional.ofNullable(entries.get(id));
    }

    Optional<CollabShapeEntry> remove(CollabShapeId id) {
        tombstones.add(id);
        return Optional.ofNullable(entries.remove(id));
    }

    boolean isDeleted(CollabShapeId id) {
        return tombstones.contains(id);
    }

    List<CollabShapeEntry> snapshot() {
        return List.copyOf(entries.values());
    }

    Collection<CollabShapeEntry> entries() {
        return entries.values();
    }
}
