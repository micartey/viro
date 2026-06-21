package me.micartey.viro.collab;

import me.micartey.jation.JationObserver;
import me.micartey.jation.annotations.Observe;
import me.micartey.viro.events.mouse.MouseDragEvent;
import me.micartey.viro.events.mouse.MouseMoveEvent;
import me.micartey.viro.shapes.utilities.Position;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class CollabCursorBridge {

    private static final long BROADCAST_INTERVAL_MS = 40;
    private static final long CURSOR_TTL_MS = 3000;

    private final CollabBridge bridge;
    private final CollabPeerClient peerClient;
    private final CollabCursorOverlay overlay;
    private final Map<String, CollabWire.CollabCursor> cursors = new ConcurrentHashMap<>();

    private volatile Position latestLocalPosition;
    private double lastSentX = Double.NaN;
    private double lastSentY = Double.NaN;

    public CollabCursorBridge(CollabBridge bridge,
                              CollabPeerClient peerClient,
                              CollabCursorOverlay overlay,
                              JationObserver observer) {
        this.bridge = bridge;
        this.peerClient = peerClient;
        this.overlay = overlay;

        observer.subscribe(this);
    }

    @Observe
    public void onMove(MouseMoveEvent event) {
        latestLocalPosition = event.getPosition();
    }

    @Observe
    public void onDrag(MouseDragEvent event) {
        latestLocalPosition = event.getDestination();
    }

    void receiveCursor(String json) {
        CollabWire.CollabCursor cursor = CollabWire.cursorFromJson(json);
        if (bridge.origin().equals(cursor.origin())) {
            return;
        }

        cursors.put(cursor.origin(), cursor);
        overlay.render(snapshot());
    }

    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.MILLISECONDS)
    public void pruneExpiredCursors() {
        long now = System.currentTimeMillis();
        boolean changed = cursors.values().removeIf(cursor -> now - cursor.updatedAt() > CURSOR_TTL_MS);
        if (changed) {
            overlay.render(snapshot());
        }
    }

    @Scheduled(fixedDelay = BROADCAST_INTERVAL_MS, timeUnit = TimeUnit.MILLISECONDS)
    public void broadcastCursor() {
        Position position = latestLocalPosition;
        if (position == null || !hasMoved(position)) {
            return;
        }

        long now = System.currentTimeMillis();
        lastSentX = position.getX();
        lastSentY = position.getY();

        CollabWire.CollabCursor cursor = new CollabWire.CollabCursor(bridge.origin(), position, now);
        for (String peer : bridge.getPeers()) {
            peerClient.postCursor(peer, cursor);
        }
    }

    private boolean hasMoved(Position position) {
        return Double.compare(lastSentX, position.getX()) != 0 || Double.compare(lastSentY, position.getY()) != 0;
    }

    private List<CollabWire.CollabCursor> snapshot() {
        return List.copyOf(cursors.values());
    }
}
