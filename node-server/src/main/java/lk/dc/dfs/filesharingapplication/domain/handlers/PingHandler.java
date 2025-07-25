package lk.dc.dfs.filesharingapplication.domain.handlers;



import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.TimeoutManager;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class PingHandler implements AbstractRequestHandler, AbstractResponseHandler {

    private final Logger logger = Logger.getLogger(PingHandler.class.getName());

    private static PingHandler instance;
    private boolean isInitialized;
    private BlockingQueue<ChannelMessage> outgoingMessages;
    private RoutingTable neighborTable;
    private TimeoutManager timeoutHandler;
    private Map<String, Integer> failedPingAttempts = new HashMap<>();
    private final TimeoutCallback timeoutCallback = new PingTimeoutHandler();

    private PingHandler() {
        this.isInitialized = true;
    }

    public synchronized static PingHandler getInstance() {
        if (instance == null) {
            instance = new PingHandler();
        }
        return instance;
    }

    @Override
    public void sendRequest(ChannelMessage msg) {
        try {
            outgoingMessages.put(msg);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleResponse(ChannelMessage receivedMsg) {
        logger.fine("PING received from: " + receivedMsg.getAddress() +
                " port: " + receivedMsg.getPort());

        String[] messageParts = receivedMsg.getMessage().split(" ");
        String messageType = messageParts[1];
        String senderAddress = messageParts[2].trim();
        int senderPort = Integer.parseInt(messageParts[3].trim());

        switch (messageType) {
            case "BPING":
                processBpingMessage(messageParts, senderAddress, senderPort);
                break;

            case "LEAVE":
                processLeaveMessage(senderAddress, senderPort);
                break;

            default:
                processStandardPing(senderAddress, senderPort, receivedMsg.getPort());
        }
    }

    private void processBpingMessage(String[] msgParts, String addr, int port) {
        int remainingHops = Integer.parseInt(msgParts[4].trim());

        if (neighborTable.isANeighbour(addr, port)) {
            if (remainingHops > 0) {
                forwardBpingMessage(addr, port, remainingHops - 1);
            }
        } else {
            if (neighborTable.getCount() < Constants.MAX_NEIGHBOURS) {
                sendBpongResponse(addr, port);
            } else if (remainingHops > 0) {
                forwardBpingMessage(addr, port, remainingHops - 1);
            }
        }
    }

    private void processLeaveMessage(String addr, int port) {
        neighborTable.removeNeighbour(addr, port);
        if (neighborTable.getCount() <= Constants.MIN_NEIGHBOURS) {
            initiateBping(addr, port);
        }
        neighborTable.printStatus();
    }

    private void processStandardPing(String addr, int port, int receivedPort) {
        int addResult = neighborTable.addNeighbour(addr, port, receivedPort);
        if (addResult != 0) {
            sendPongResponse(addr, port);
        }
    }

    public void sendPing(String targetAddress, int targetPort) {
        String messageContent = String.format(Constants.PING_FORMAT,
                neighborTable.getAddress(),
                neighborTable.getPort());

        String fullMessage = String.format(Constants.MSG_FORMAT,
                messageContent.length() + 5, messageContent);

        ChannelMessage pingMsg = new ChannelMessage(targetAddress, targetPort, fullMessage);

        String messageId = String.format(Constants.PING_MESSAGE_ID_FORMAT,
                targetAddress, targetPort);

        failedPingAttempts.putIfAbsent(messageId, 0);

        timeoutHandler.registerRequest(
                messageId,
                Constants.PING_TIMEOUT,
                timeoutCallback
        );

        sendRequest(pingMsg);
    }

    private void initiateBping(String excludedAddress, int excludedPort) {
        List<String> recipients = neighborTable.getOtherNeighbours(excludedAddress, excludedPort);
        String messageContent = String.format(Constants.BPING_FORMAT,
                neighborTable.getAddress(),
                neighborTable.getPort(),
                Constants.BPING_HOP_LIMIT);

        String fullMessage = String.format(Constants.MSG_FORMAT, messageContent.length() + 5, messageContent);

        for (String recipient : recipients) {
            String[] parts = recipient.split(":");
            ChannelMessage msg = new ChannelMessage(parts[0], Integer.parseInt(parts[1]), fullMessage);
            sendRequest(msg);
        }
    }

    private void forwardBpingMessage(String originalAddress, int originalPort, int currentHops) {
        List<String> forwardTargets = neighborTable.getOtherNeighbours(originalAddress, originalPort);
        String messageContent = String.format(Constants.BPING_FORMAT,
                originalAddress,
                originalPort,
                currentHops);

        String fullMessage = String.format(Constants.MSG_FORMAT, messageContent.length() + 5, messageContent);

        for (String target : forwardTargets) {
            String[] parts = target.split(":");
            ChannelMessage msg = new ChannelMessage(parts[0], Integer.parseInt(parts[1]), fullMessage);
            sendRequest(msg);
        }
    }

    private void sendPongResponse(String address, int port) {
        String responseContent = String.format(Constants.PONG_FORMAT,
                neighborTable.getAddress(),
                neighborTable.getPort());

        String fullMessage = String.format(Constants.MSG_FORMAT,
                responseContent.length() + 5, responseContent);

        ChannelMessage response = new ChannelMessage(address, port, fullMessage);
        sendRequest(response);
    }

    private void sendBpongResponse(String address, int port) {
        String responseContent = String.format(Constants.BPONG_FORMAT,
                neighborTable.getAddress(),
                neighborTable.getPort());

        String fullMessage = String.format(Constants.MSG_FORMAT,
                responseContent.length() + 5, responseContent);

        ChannelMessage response = new ChannelMessage(address, port, fullMessage);
        sendRequest(response);
    }

    @Override
    public void init(RoutingTable routingTable,
                     BlockingQueue<ChannelMessage> channelOut,
                     TimeoutManager timeoutManager) {
        this.neighborTable = routingTable;
        this.outgoingMessages = channelOut;
        this.timeoutHandler = timeoutManager;
    }

    private class PingTimeoutHandler implements TimeoutCallback {
        @Override
        public void onTimeout(String messageId) {
            int attempts = failedPingAttempts.getOrDefault(messageId, 0) + 1;
            failedPingAttempts.put(messageId, attempts);

            if (attempts >= Constants.PING_RETRY) {
                logger.fine("Neighbor unavailable: " + messageId);
                String[] parts = messageId.split(":");
                neighborTable.removeNeighbour(parts[1], Integer.valueOf(parts[2]));

                if (neighborTable.getCount() < Constants.MIN_NEIGHBOURS) {
                    initiateBping(parts[1], Integer.valueOf(parts[2]));
                }
            }
        }

        @Override
        public void onResponse(String messageId) {
            failedPingAttempts.put(messageId, 0);
        }
    }
}
