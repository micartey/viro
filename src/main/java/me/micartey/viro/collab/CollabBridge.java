package me.micartey.viro.collab;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import me.micartey.viro.events.viro.ShapeSubmitEvent;
import me.micartey.viro.shapes.Graphic;
import me.micartey.viro.shapes.Shape;
import me.micartey.viro.window.Canvas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CollabBridge {

    private static final int DELETE_AFTER_MISSING = 2;

    private final Canvas canvas;
    private final String origin;
    private final RestTemplate rest;
    private final List<String> peers;
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final AtomicInteger receiving = new AtomicInteger(0);
    private final Map<Integer, Entry> entries = new ConcurrentHashMap<>();

    private static class Entry {
        final int id;
        final String origin;
        Shape shape;
        String lastHash;
        int missingCycles;

        Entry(int id, String origin, Shape shape, String hash) {
            this.id = id;
            this.origin = origin;
            this.shape = shape;
            this.lastHash = hash;
        }
    }

    public CollabBridge(Canvas canvas,
                        @Value("${server.port:8099}") int port,
                        @Value("${viro.collab.peers:}") String peersConfig) {
        this.canvas = canvas;
        this.origin = String.valueOf(port);
        this.rest = new RestTemplate();

        List<String> initial = peersConfig != null && !peersConfig.isEmpty()
                ? Arrays.asList(peersConfig.split(","))
                : List.of();
        this.peers = new CopyOnWriteArrayList<>(
                initial.stream().map(String::trim).filter(s -> !s.isEmpty()).toList()
        );
    }

    @EventListener(ApplicationStartedEvent.class)
    public void seedExistingShapes() {
        synchronized (canvas.getVisible()) {
            for (Shape shape : canvas.getVisible()) {
                if (shape instanceof Graphic) {
                    continue;
                }
                int id = nextId.getAndIncrement();
                String hash = CollabWire.contentHash(shape);
                entries.put(id, new Entry(id, origin, shape, hash));
                log.debug("Seeded existing shape {}", id);
            }
        }
    }

    @EventListener(ShapeSubmitEvent.class)
    public void onLocalShape(ShapeSubmitEvent event) {
        if (isReceiving()) {
            return;
        }
        Shape shape = event.getShape();
        if (shape instanceof Graphic) {
            return;
        }
        int id = nextId.getAndIncrement();
        String hash = CollabWire.contentHash(shape);
        entries.put(id, new Entry(id, origin, shape, hash));

        if (!peers.isEmpty()) {
            String json = CollabWire.toJson(id, origin, shape);
            for (String peer : peers) {
                post(peer, "/collab/shape", json);
            }
            log.debug("Broadcast new shape {}", id);
        }
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void detectLocalChanges() {
        if (entries.isEmpty()) {
            return;
        }
        beginReceive();
        try {
            List<Entry> toDelete = new ArrayList<>();
            List<Entry> toUpdate = new ArrayList<>();

            synchronized (canvas.getVisible()) {
                for (Entry e : entries.values()) {
                    if (canvas.getVisible().contains(e.shape)) {
                        if (e.missingCycles > 0) {
                            e.missingCycles = 0;
                        }
                        String hash = CollabWire.contentHash(e.shape);
                        if (!hash.equals(e.lastHash)) {
                            e.lastHash = hash;
                            toUpdate.add(e);
                        }
                    } else {
                        e.missingCycles++;
                        if (e.missingCycles >= DELETE_AFTER_MISSING) {
                            toDelete.add(e);
                        }
                    }
                }
            }

            for (Entry e : toDelete) {
                entries.remove(e.id);
                if (!peers.isEmpty()) {
                    String body = "{\"id\":" + e.id + "}";
                    for (String peer : peers) {
                        post(peer, "/collab/shape/delete", body);
                    }
                }
                log.info("Broadcast delete for shape {}", e.id);
            }

            for (Entry e : toUpdate) {
                if (!peers.isEmpty()) {
                    String json = CollabWire.toJson(e.id, e.origin, e.shape);
                    for (String peer : peers) {
                        post(peer, "/collab/shape", json);
                    }
                }
                log.debug("Broadcast update for shape {}", e.id);
            }
        } finally {
            endReceive();
        }
    }

    @Scheduled(fixedDelay = 3, timeUnit = TimeUnit.SECONDS)
    public void syncWithPeers() {
        if (peers.isEmpty()) {
            return;
        }
        beginReceive();
        try {
            for (String peer : peers) {
                syncWithPeer(peer);
            }
        } finally {
            endReceive();
        }
    }

    void receiveShape(String json) {
        beginReceive();
        try {
            CollabWire.CollabShape cs = CollabWire.fromJson(json);
            Entry existing = entries.get(cs.id());

            if (existing != null) {
                synchronized (canvas.getVisible()) {
                    canvas.getVisible().remove(existing.shape);
                    canvas.getVisible().add(cs.shape());
                }
                existing.shape = cs.shape();
                existing.lastHash = CollabWire.contentHash(cs.shape());
                existing.missingCycles = 0;
                canvas.repaint();
                log.debug("Updated shape {} from remote", cs.id());
            } else {
                entries.put(cs.id(), new Entry(cs.id(), cs.origin(), cs.shape(),
                        CollabWire.contentHash(cs.shape())));
                synchronized (canvas.getVisible()) {
                    canvas.getVisible().add(cs.shape());
                }
                canvas.repaint();
                log.info("Added remote shape {} (origin={})", cs.id(), cs.origin());
            }
        } finally {
            endReceive();
        }
    }

    void receiveDelete(String json) {
        int id = JsonParser.parseString(json).getAsJsonObject().get("id").getAsInt();
        Entry entry = entries.remove(id);
        if (entry != null) {
            synchronized (canvas.getVisible()) {
                canvas.getVisible().remove(entry.shape);
                canvas.getInvisible().add(entry.shape);
            }
            canvas.repaint();
            log.info("Received delete for shape {}", id);
        }
    }

    String getSerializedShapes() {
        synchronized (canvas.getVisible()) {
            return entries.values().stream()
                    .filter(e -> canvas.getVisible().contains(e.shape))
                    .map(e -> CollabWire.toJson(e.id, e.origin, e.shape))
                    .collect(Collectors.joining(",", "[", "]"));
        }
    }

    public void addPeer(String url) {
        if (url == null || url.isEmpty() || peers.contains(url)) {
            return;
        }
        peers.add(url);
        log.info("Added peer: {}", url);
    }

    public void removePeer(String url) {
        peers.remove(url.trim());
        log.info("Removed peer: {}", url.trim());
    }

    public List<String> getPeers() {
        return List.copyOf(peers);
    }

    private void syncWithPeer(String peer) {
        JsonArray peerShapes = fetchPeerShapes(peer);
        if (peerShapes == null) {
            return;
        }

        Set<Integer> peerIds = new HashSet<>();
        for (JsonElement el : peerShapes) {
            peerIds.add(el.getAsJsonObject().get("id").getAsInt());
        }

        for (JsonElement el : peerShapes) {
            JsonObject obj = el.getAsJsonObject();
            int id = obj.get("id").getAsInt();
            if (!entries.containsKey(id)) {
                receiveShape(obj.toString());
            }
        }

        List<Entry> toRemove = new ArrayList<>();
        for (Entry e : entries.values()) {
            if (e.origin.equals(peer) && !peerIds.contains(e.id)) {
                toRemove.add(e);
            }
        }
        for (Entry e : toRemove) {
            entries.remove(e.id);
            synchronized (canvas.getVisible()) {
                canvas.getVisible().remove(e.shape);
                canvas.getInvisible().add(e.shape);
            }
            canvas.repaint();
            log.info("Origin {} no longer has shape {}, removed locally", peer, e.id);
        }
    }

    private JsonArray fetchPeerShapes(String peer) {
        try {
            String resp = rest.getForObject(peer + "/collab/shapes", String.class);
            if (resp == null) {
                return null;
            }
            return JsonParser.parseString(resp).getAsJsonArray();
        } catch (Exception e) {
            log.warn("Failed to fetch shapes from {}: {}", peer, e.getMessage());
            return null;
        }
    }

    private void post(String peer, String path, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            rest.postForObject(peer + path, new HttpEntity<>(body, headers), Void.class);
        } catch (Exception ex) {
            log.warn("POST {} failed: {}", peer + path, ex.getMessage());
        }
    }

    private void beginReceive() {
        receiving.incrementAndGet();
    }

    private void endReceive() {
        receiving.decrementAndGet();
    }

    private boolean isReceiving() {
        return receiving.get() > 0;
    }
}
