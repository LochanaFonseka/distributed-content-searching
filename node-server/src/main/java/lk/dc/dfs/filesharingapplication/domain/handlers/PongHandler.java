package lk.dc.dfs.filesharingapplication.domain.handlers;



import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.TimeoutManager;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class PongHandler implements AbstractRequestHandler, AbstractResponseHandler {

    private final Logger logger = Logger.getLogger(PongHandler.class.getName());

    private BlockingQueue<ChannelMessage> outgoingChannel;
    private RoutingTable neighborRoutingTable;
    private TimeoutManager timeoutHandler;

    private static PongHandler singletonInstance;

    private PongHandler() {
        // Private constructor for singleton pattern
    }

    public synchronized static PongHandler getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new PongHandler();
        }
        return singletonInstance;
    }

    @Override
    public void sendRequest(ChannelMessage msg) {
        // Implementation left empty as per original
    }

    @Override
    public void handleResponse(ChannelMessage receivedMessage) {
        logger.fine("PONG response received from: " + receivedMessage.getAddress() +
                " port: " + receivedMessage.getPort());

        String[] messageComponents = receivedMessage.getMessage().split(" ");
        String messageType = messageComponents[1];
        String senderAddress = messageComponents[2].trim();
        int senderPort = Integer.parseInt(messageComponents[3].trim());

        if (messageType.equals("BPONG")) {
            processBpongResponse(senderAddress, senderPort, receivedMessage.getPort());
        } else {
            processStandardPong(senderAddress, senderPort, receivedMessage.getPort());
        }
    }

    private void processBpongResponse(String address, int port, int receivedPort) {
        if (neighborRoutingTable.getNeighborCount() < Constants.MIN_NEIGHBOURS) {
            neighborRoutingTable.addNeighbor(address, port, receivedPort);
        }
    }

    private void processStandardPong(String address, int port, int receivedPort) {
        String messageId = String.format(Constants.PING_MESSAGE_ID_FORMAT, address, port);
        timeoutHandler.recordResponse(messageId);
        neighborRoutingTable.addNeighbor(address, port, receivedPort);
    }

    @Override
    public void init(RoutingTable routingTable,
                     BlockingQueue<ChannelMessage> channelOut,
                     TimeoutManager timeoutManager) {
        this.neighborRoutingTable = routingTable;
        this.outgoingChannel = channelOut;
        this.timeoutHandler = timeoutManager;
    }
}
