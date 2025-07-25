package lk.dc.dfs.filesharingapplication.domain.service.core;


import lk.dc.dfs.filesharingapplication.domain.handlers.*;
import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;
import lk.dc.dfs.filesharingapplication.domain.service.comms.UDPClient;
import lk.dc.dfs.filesharingapplication.domain.service.comms.UDPServer;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MessageBroker extends Thread {

    private final Logger logger = Logger.getLogger(MessageBroker.class.getName());

    private volatile boolean isRunning = true;

    private final UDPServer udpServer;
    private final UDPClient udpClient;

    private BlockingQueue<ChannelMessage> incomingChannel;
    private BlockingQueue<ChannelMessage> outgoingChannel;

    private RoutingTable routingTable;
    private PingHandler pingHandler;
    private LeaveHandler leaveHandler;
    private SearchQueryHandler searchHandler;
    private FileManager fileHandler;

    private TimeoutManager timeoutHandler = new TimeoutManager();

    public MessageBroker(String ipAddress, int portNumber) throws SocketException {
        incomingChannel = new LinkedBlockingQueue<>();
        DatagramSocket serverSocket = new DatagramSocket(portNumber);
        this.udpServer = new UDPServer(incomingChannel, serverSocket);

        outgoingChannel = new LinkedBlockingQueue<>();
        this.udpClient = new UDPClient(outgoingChannel, new DatagramSocket());

        this.routingTable = new RoutingTable(ipAddress, portNumber);

        this.pingHandler = PingHandler.getInstance();
        this.leaveHandler = LeaveHandler.getInstance();

        this.fileHandler = FileManager.getInstance("");

        initializeHandlers();
        setupPingTimeout();

        logger.fine("Initializing server components");
    }

    private void initializeHandlers() {
        this.pingHandler.init(this.routingTable, this.outgoingChannel, this.timeoutHandler);
        this.leaveHandler.init(this.routingTable, this.outgoingChannel, this.timeoutHandler);

        this.searchHandler = SearchQueryHandler.getInstance();
        this.searchHandler.init(routingTable, outgoingChannel, timeoutHandler);
    }

    private void setupPingTimeout() {
        timeoutHandler.registerRequest(
                Constants.R_PING_MESSAGE_ID,
                Constants.PING_INTERVAL,
                new TimeoutCallback() {
                    @Override
                    public void onTimeout(String messageId) {
                        sendPeriodicPings();
                    }

                    @Override
                    public void onResponse(String messageId) {
                        // No action needed on response
                    }
                }
        );
    }

    @Override
    public void run() {
        this.udpServer.start();
        this.udpClient.start();
        this.processMessages();
    }

    public void processMessages() {
        while (isRunning) {
            try {
                ChannelMessage receivedMessage = incomingChannel.poll(100, TimeUnit.MILLISECONDS);
                if (receivedMessage != null) {
                    logIncomingMessage(receivedMessage);
                    handleMessage(receivedMessage);
                }
                timeoutHandler.verifyTimeouts();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.log(Level.WARNING, "Message processing interrupted", e);
            }
        }
    }

    private void logIncomingMessage(ChannelMessage message) {
        logger.info(String.format("Received Message from: %s port: %d",
                message.getAddress(), message.getPort()));
    }

    private void handleMessage(ChannelMessage message) {
        String messageType = message.getMessage().split(" ")[1];
        AbstractResponseHandler handler =
                ResponseHandlerFactory.getResponseHandler(messageType, this);

        if (handler != null) {
            handler.processResponse(message);
        }
    }

    public void terminate() {
        this.isRunning = false;
        udpServer.stopProcessing();
    }

    public void sendPing(String targetAddress, int targetPort) {
        this.pingHandler.sendPing(targetAddress, targetPort);
    }

    public void initiateSearch(String searchTerm) {
        this.searchHandler.doSearch(searchTerm);
    }

    public BlockingQueue<ChannelMessage> getIncomingChannel() {
        return incomingChannel;
    }

    public BlockingQueue<ChannelMessage> getOutgoingChannel() {
        return outgoingChannel;
    }

    public TimeoutManager getTimeoutHandler() {
        return timeoutHandler;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    private void sendPeriodicPings() {
        routingTable.toList().forEach(neighbor -> {
            String[] parts = neighbor.split(":");
            sendPing(parts[0], Integer.parseInt(parts[1]));
        });
    }

    public void sendLeaveNotification() {
        this.leaveHandler.sendLeave();
    }

    public List<String> getAvailableFiles() {
        return this.fileHandler.getFileNames();
    }
}
