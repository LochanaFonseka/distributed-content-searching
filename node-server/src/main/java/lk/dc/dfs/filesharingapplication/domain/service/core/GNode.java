package lk.dc.dfs.filesharingapplication.domain.service.core;


import lk.dc.dfs.filesharingapplication.domain.service.comms.BSClient;
import lk.dc.dfs.filesharingapplication.domain.service.comms.ftp.FTPClient;
import lk.dc.dfs.filesharingapplication.domain.service.comms.ftp.FTPServer;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class GNode {
    private final Logger LOG = Logger.getLogger(GNode.class.getName());

    private final BSClient bsClient;
    @Getter
    private String userName;
    @Getter
    private String ipAddress;
    @Getter
    private int port;
    private MessageBroker messageBroker;
    private SearchManager searchManager;

    public GNode (BSClient bsClient) throws Exception {
        this.bsClient = bsClient;
    }


    public void init(String userName) throws Exception {
        try (final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            this.ipAddress = socket.getLocalAddress().getHostAddress();

        } catch (Exception e){
            throw new RuntimeException("Could not find host address");
        }
        this.userName = userName;
        this.port = getFreePort();
        FileManager fileManager = FileManager.getInstance(userName);
        FTPServer ftpServer = new FTPServer(this.port + Constants.FTP_PORT_OFFSET, userName);
        Thread t = new Thread(ftpServer);
        t.start();
        this.messageBroker = new MessageBroker(ipAddress, port);

        this.searchManager = new SearchManager(this.messageBroker);

        messageBroker.start();

        LOG.fine("Gnode initiated on IP :" + ipAddress + " and Port :" + port);

        List<InetSocketAddress> targets = this.register();
        if(targets != null) {
            for (InetSocketAddress target: targets) {
                messageBroker.sendPing(target.getAddress().toString().substring(1), target.getPort());
            }
        }
    }

    private List<InetSocketAddress> register() {
        List<InetSocketAddress> targets = null;

        try{
            targets = this.bsClient.register(this.userName, this.ipAddress, this.port);

        } catch (IOException e) {
            LOG.severe("Registering node failed");
            e.printStackTrace();
        }
        return targets;

    }

    public void unRegister() {
        try{
            this.bsClient.unRegister(this.userName, this.ipAddress, this.port);
            this.messageBroker.sendLeave();

        } catch (IOException e) {
            LOG.severe("Un-Registering node failed");
            e.printStackTrace();
        }
    }

    public Map<String, SearchResult> doSearch(String keyword){
        return this.searchManager.doSearch(keyword);
    }



    public SearchResult getFile(String fileId) {
        try {
            SearchResult fileDetail = this.searchManager.getFileDetails(fileId);
            System.out.println("The file you requested is " + fileDetail.getFileName());
            FTPClient ftpClient = new FTPClient(fileDetail.getAddress(), fileDetail.getTcpPort(),
                    fileDetail.getFileName());

            System.out.println("Waiting for file download...");
            Thread.sleep(Constants.FILE_DOWNLOAD_TIMEOUT);
            return fileDetail;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    private int getFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException e) {
            LOG.severe("Getting free port failed");
            throw new RuntimeException("Getting free port failed");
        }
    }

    public RoutingTable getRoutingTable() {
       return this.messageBroker.getRoutingTable();
    }

    public List<String> getFileNames() {
        return this.messageBroker.getFiles();
    }
}
