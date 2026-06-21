package me.micartey.viro.collab;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.extern.slf4j.Slf4j;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Graphic;
import me.micartey.viro.shapes.Shape;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CollabBridge {

    private static final int DELETE_AFTER_MISSING = 1;

    private final CollabCanvas canvas;
    private final CollabPeerClient peersClient;
    private final CollabStore store;
    private final List<String> peers;

    public CollabBridge(CollabCanvas canvas,
                        CollabPeerClient peersClient,
                        @Value("${server.port:8099}") int port,
                        @Value("${viro.collab.origin:}") String configuredOrigin,
                        @Value("${viro.collab.peers:}") String peersConfig) {
        this.canvas = canvas;
        this.peersClient = peersClient;
        this.store = new CollabStore(resolveOrigin(configuredOrigin, port));
        this.peers = new CopyOnWriteArrayList<>(parsePeers(peersConfig));
    }

    @EventListener(ApplicationStartedEvent.class)
    public void seedExistingShapes() {
        for (Shape shape : canvas.visibleShapes()) {
            CollabShapeEntry entry = store.addLocal(shape);
            log.debug("Seeded existing shape {}", entry.id());
        }
    }

    @EventListener(ShapeSubmitEvent.class)
    public void onLocalShape(ShapeSubmitEvent event) {
        Shape shape = event.getShape();
        if (shape instanceof Graphic) {
            return;
        }

        CollabShapeEntry entry = store.addLocal(shape);
        broadcastShape(entry);
        log.debug("Broadcast local shape {}", entry.id());
    }

    @Scheduled(fixedDelay = 200, timeUnit = TimeUnit.MILLISECONDS)
    public void detectLocalChanges() {
        List<CollabShapeEntry> deleted = new ArrayList<>();
        List<CollabShapeEntry> changed = new ArrayList<>();

        for (CollabShapeEntry entry : store.snapshot()) {
            if (canvas.isVisible(entry.shape())) {
                entry.markPresent();
                if (entry.refreshHash() && isLocal(entry)) {
                    changed.add(entry);
                }
            } else if (entry.markMissing() >= DELETE_AFTER_MISSING) {
                store.remove(entry.id());
                deleted.add(entry);
            }
        }

        deleted.forEach(entry -> {
            broadcastDelete(entry.id());
            log.info("Broadcast delete for shape {}", entry.id());
        });

        changed.forEach(entry -> {
            broadcastShape(entry);
            log.debug("Broadcast update for shape {}", entry.id());
        });
    }

    @Scheduled(fixedDelay = 3, timeUnit = TimeUnit.SECONDS)
    public void syncWithPeers() {
        for (String peer : peers) {
            syncWithPeer(peer);
        }
    }

    void receiveShape(String json) {
        CollabWire.CollabShape remote = CollabWire.fromJson(json);
        if (store.origin().equals(remote.id().origin())) {
            return;
        }

        Shape previous = store.get(remote.id())
                .map(CollabShapeEntry::shape)
                .orElse(null);

        store.upsert(remote).ifPresent(entry -> {
            if (previous == null) {
                canvas.add(entry.shape());
                log.info("Added remote shape {}", entry.id());
            } else {
                canvas.replace(previous, entry.shape());
                log.debug("Updated remote shape {}", entry.id());
            }
        });
    }

    void receiveDelete(String json) {
        CollabShapeId id = CollabWire.deleteId(json);
        boolean knownDelete = store.isDeleted(id);
        store.remove(id).ifPresent(entry -> {
            canvas.remove(entry);
            log.info("Received delete for shape {}", id);
        });
        if (!knownDelete) {
            broadcastDelete(id);
        }
    }

    String getSerializedShapes() {
        List<CollabShapeEntry> visible = store.snapshot().stream()
                .filter(entry -> canvas.isVisible(entry.shape()))
                .toList();
        return CollabWire.toJsonArray(visible);
    }

    String origin() {
        return store.origin();
    }

    public void addPeer(String url) {
        String peer = normalizePeer(url);
        if (peer.isEmpty() || peers.contains(peer)) {
            return;
        }
        peers.add(peer);
        log.info("Added peer: {}", peer);
    }

    public void removePeer(String url) {
        String peer = normalizePeer(url);
        peers.remove(peer);
        log.info("Removed peer: {}", peer);
    }

    public List<String> getPeers() {
        return List.copyOf(peers);
    }

    private void syncWithPeer(String peer) {
        peersClient.fetchShapes(peer).ifPresent(peerShapes -> {
            Set<CollabShapeId> seen = new HashSet<>();
            for (JsonElement element : peerShapes) {
                CollabWire.CollabShape remote = CollabWire.fromJson(element.toString());
                if (store.origin().equals(remote.id().origin())) {
                    continue;
                }
                seen.add(remote.id());
                receiveShape(element.toString());
            }

            peersClient.fetchOrigin(peer).ifPresent(peerOrigin -> removeMissingPeerOwnedShapes(peerOrigin, seen));
        });
    }

    private void removeMissingPeerOwnedShapes(String peerOrigin, Set<CollabShapeId> seen) {
        for (CollabShapeEntry entry : store.snapshot()) {
            if (peerOrigin.equals(entry.id().origin()) && !seen.contains(entry.id())) {
                store.remove(entry.id()).ifPresent(canvas::remove);
                broadcastDelete(entry.id());
                log.info("Removed stale peer shape {}", entry.id());
            }
        }
    }

    private void broadcastShape(CollabShapeEntry entry) {
        for (String peer : peers) {
            peersClient.postShape(peer, entry);
        }
    }

    private void broadcastDelete(CollabShapeId id) {
        for (String peer : peers) {
            peersClient.postDelete(peer, id);
        }
    }

    private boolean isLocal(CollabShapeEntry entry) {
        return store.origin().equals(entry.id().origin());
    }

    private static String resolveOrigin(String configuredOrigin, int port) {
        String configured = CollabWire.stringBody(configuredOrigin);
        return configured.isEmpty() ? "localhost:" + port : configured;
    }

    private static List<String> parsePeers(String peersConfig) {
        if (peersConfig == null || peersConfig.isBlank()) {
            return List.of();
        }

        return Arrays.stream(peersConfig.split(","))
                .map(CollabBridge::normalizePeer)
                .filter(peer -> !peer.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private static String normalizePeer(String url) {
        String peer = CollabWire.stringBody(url);
        while (peer.endsWith("/")) {
            peer = peer.substring(0, peer.length() - 1);
        }
        return peer;
    }
}
