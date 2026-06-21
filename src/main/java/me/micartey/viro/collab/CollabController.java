package me.micartey.viro.collab;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collab")
@RequiredArgsConstructor
public class CollabController {

    private final CollabBridge bridge;
    private final CollabCursorBridge cursors;

    @PostMapping("/shape")
    public void receiveShape(@RequestBody String json) {
        bridge.receiveShape(json);
    }

    @PostMapping("/shape/delete")
    public void receiveDelete(@RequestBody String json) {
        bridge.receiveDelete(json);
    }

    @PostMapping("/cursor")
    public void receiveCursor(@RequestBody String json) {
        cursors.receiveCursor(json);
    }

    @GetMapping(value = "/shapes", produces = "application/json")
    public String getShapes() {
        return bridge.getSerializedShapes();
    }

    @GetMapping("/origin")
    public String origin() {
        return bridge.origin();
    }

    @PostMapping("/peers")
    public void addPeer(@RequestBody String url) {
        bridge.addPeer(url);
    }

    @DeleteMapping("/peers")
    public void removePeer(@RequestBody String url) {
        bridge.removePeer(url);
    }

    @GetMapping("/peers")
    public List<String> listPeers() {
        return bridge.getPeers();
    }
}
