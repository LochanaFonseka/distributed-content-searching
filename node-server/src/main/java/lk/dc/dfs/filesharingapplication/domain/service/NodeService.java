package lk.dc.dfs.filesharingapplication.domain.service;

import lk.dc.dfs.filesharingapplication.domain.service.core.GNode;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.SearchResult;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.util.*;

@Service
public class NodeService {

    @Getter
    private final GNode currentNode;
    private final String bootstrapIp;
    private final int bootstrapPort;


    private int getAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }


    public NodeService(GNode currentNode,
                       @Value("${bootstrap.server.ip}") String bootstrapIp,
                       @Value("${bootstrap.server.port}") int bootstrapPort) throws Exception {
        this.bootstrapIp = bootstrapIp;
        this.bootstrapPort = bootstrapPort;
        String uniqueID = UUID.randomUUID().toString();
        this.currentNode = currentNode;
        currentNode.init("node" + uniqueID);
        try {
            DatagramSocket socket = new DatagramSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unregister() throws IOException {
        currentNode.unRegister();
    }

    public RoutingTable getRoutingTable() {
        return currentNode.getRoutingTable();
    }

    public Map<String, SearchResult> searchFile(String fileName) throws IOException {
        return currentNode.doSearch(fileName);
    }

    public ResponseEntity<Resource> getFile(String fileId) throws IOException {
        SearchResult file = currentNode.getFile(fileId);
        if (Objects.isNull(file)){
            return ResponseEntity.notFound().build();
        }
        // Define the file path
        File downloadedFile = new File(file.getFileName());

        // Create input stream resource
        InputStreamResource resource = new InputStreamResource(new FileInputStream(downloadedFile));


        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        // Return the file as a ResponseEntity
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(downloadedFile.length())
                .body(resource);
    }

    public ResponseEntity<Map<String,List<String>>> getCurrentNodeFiles(){
        Map<String,List<String>> files = new HashMap<>();
        files.put("fileNameList",currentNode.getFileNames());
        return ResponseEntity.ok(files);
    }


}
