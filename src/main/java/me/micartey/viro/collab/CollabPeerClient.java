package me.micartey.viro.collab;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
final class CollabPeerClient {

    private final RestTemplate rest = new RestTemplate();
    private final Set<String> unavailableCursorPeers = ConcurrentHashMap.newKeySet();

    Optional<JsonArray> fetchShapes(String peer) {
        try {
            String response = rest.getForObject(peer + "/collab/shapes", String.class);
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(JsonParser.parseString(response).getAsJsonArray());
        } catch (Exception e) {
            log.warn("Failed to fetch shapes from {}: {}", peer, e.getMessage());
            return Optional.empty();
        }
    }

    Optional<String> fetchOrigin(String peer) {
        try {
            String response = rest.getForObject(peer + "/collab/origin", String.class);
            String origin = CollabWire.stringBody(response);
            return origin.isEmpty() ? Optional.empty() : Optional.of(origin);
        } catch (Exception e) {
            log.warn("Failed to fetch origin from {}: {}", peer, e.getMessage());
            return Optional.empty();
        }
    }

    void postShape(String peer, CollabShapeEntry entry) {
        post(peer, "/collab/shape", CollabWire.toJson(entry));
    }

    void postDelete(String peer, CollabShapeId id) {
        post(peer, "/collab/shape/delete", CollabWire.deleteJson(id));
    }

    void postCursor(String peer, CollabWire.CollabCursor cursor) {
        post(peer, "/collab/cursor", CollabWire.cursorJson(cursor), true);
    }

    private void post(String peer, String path, String body) {
        post(peer, path, body, false);
    }

    private void post(String peer, String path, String body, boolean quietUntilAvailable) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            rest.postForObject(peer + path, new HttpEntity<>(body, headers), Void.class);
            unavailableCursorPeers.remove(peer);
        } catch (Exception ex) {
            if (quietUntilAvailable && !unavailableCursorPeers.add(peer)) {
                return;
            }
            log.warn("POST {} failed: {}", peer + path, ex.getMessage());
        }
    }
}
