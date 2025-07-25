package lk.dc.dfs.filesharingapplication.application.controller;

import lk.dc.dfs.filesharingapplication.domain.service.NodeService;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.SearchResult;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client")
@CrossOrigin("http://localhost:3000")
public class ClientController {

    private final NodeService nodeService;

    public ClientController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping("/unregister")
    public ResponseEntity<String> unregister() {
        try {
            nodeService.unregister();
            return ResponseEntity.ok("Unregistered successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Unregistration failed");
        }
    }

    @GetMapping("/routing-table")
    public ResponseEntity<RoutingTable> getRoutingTable() {
        return ResponseEntity.ok(nodeService.getRoutingTable());
    }


    @GetMapping("/search-file")
    public Map<String, SearchResult> searchFile(@RequestParam String filename) throws IOException {
        return nodeService.searchFile(filename);
    }

    @GetMapping("/files")
    public ResponseEntity<Map<String,List<String>>> currentNodeFiles() {
        return nodeService.getCurrentNodeFiles();
    }

    @GetMapping("/download-file")
    public ResponseEntity<Resource> getFile(@RequestParam String filename) throws IOException {
         return nodeService.getFile(filename);
    }
}

