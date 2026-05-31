package me.micartey.viro.collab;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collab")
@RequiredArgsConstructor
public class CollabController {

    private final CollabBridge bridge;

    @PostMapping("/shape")
    public void receiveShape(@RequestBody String json) {
        bridge.receiveShape(json);
    }

    @PostMapping("/shape/delete")
    public void receiveDelete(@RequestBody String json) {
        bridge.receiveDelete(json);
    }

    @GetMapping(value = "/shapes", produces = "application/json")
    public String getShapes() {
        return bridge.getSerializedShapes();
    }

    @PostMapping("/peers")
    public void addPeer(@RequestBody String url) {
        bridge.addPeer(url.trim());
    }

    @DeleteMapping("/peers")
    public void removePeer(@RequestBody String url) {
        bridge.removePeer(url.trim());
    }

    @GetMapping("/peers")
    public List<String> listPeers() {
        return bridge.getPeers();
    }
}
