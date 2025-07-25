package lk.dc.dfs.filesharingapplication.domain.handlers;


import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;
import lk.dc.dfs.filesharingapplication.domain.service.core.FileManager;
import lk.dc.dfs.filesharingapplication.domain.service.core.Neighbour;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.TimeoutManager;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;
import lk.dc.dfs.filesharingapplication.domain.util.StringEncoderDecoder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class SearchQueryHandler implements AbstractResponseHandler, AbstractRequestHandler {

    private static final Logger logger = Logger.getLogger(SearchQueryHandler.class.getName());

    private RoutingTable nodeRoutingTable;
    private BlockingQueue<ChannelMessage> outgoingMessages;
    private TimeoutManager requestTimeoutManager;
    private FileManager localFileManager;

    private static SearchQueryHandler instance;

    private SearchQueryHandler() {
        this.localFileManager = FileManager.getInstance("");
    }

    public static synchronized SearchQueryHandler getInstance() {
        if (instance == null) {
            instance = new SearchQueryHandler();
        }
        return instance;
    }

    public void initiateFileSearch(String searchTerm) {
        String queryPayload = buildSearchQueryPayload(searchTerm);
        ChannelMessage initialMessage = createChannelMessage(queryPayload);
        processIncomingMessage(initialMessage);
    }

    @Override
    public void sendRequest(ChannelMessage message) {
        try {
            outgoingMessages.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Message sending interrupted", e);
        }
    }

    @Override
    public void init(RoutingTable routingTable,
                     BlockingQueue<ChannelMessage> channelOut,
                     TimeoutManager timeoutManager) {
        this.nodeRoutingTable = routingTable;
        this.outgoingMessages = channelOut;
        this.requestTimeoutManager = timeoutManager;
    }

    @Override
    public void handleResponse(ChannelMessage message) {
        logger.fine("Received search request from: " + message.getAddress() +
                ":" + message.getPort());

        SearchRequest request = parseSearchRequest(message);
        processSearchRequest(request);
    }

    private String buildSearchQueryPayload(String searchTerm) {
        return String.format(Constants.QUERY_FORMAT,
                nodeRoutingTable.getAddress(),
                nodeRoutingTable.getPort(),
                StringEncoderDecoder.encode(searchTerm),
                Constants.HOP_COUNT);
    }

    private ChannelMessage createChannelMessage(String payload) {
        String formattedMessage = String.format(Constants.MSG_FORMAT,
                payload.length() + 5, payload);
        return new ChannelMessage(
                nodeRoutingTable.getAddress(),
                nodeRoutingTable.getPort(),
                formattedMessage);
    }

    private SearchRequest parseSearchRequest(ChannelMessage message) {
        String[] parts = message.getMessage().split(" ");
        return new SearchRequest(
                parts[2].trim(), // address
                Integer.parseInt(parts[3].trim()), // port
                StringEncoderDecoder.decode(parts[4].trim()), // fileName
                Integer.parseInt(parts[5].trim()) // hops
        );
    }

    private void processSearchRequest(SearchRequest request) {
        Set<String> matchingFiles = localFileManager.searchForFile(request.getFileName());

        if (!matchingFiles.isEmpty()) {
            sendQueryHitResponse(request, matchingFiles);
        }

        if (request.getHops() > 0) {
            forwardSearchRequest(request);
        }
    }

    private void sendQueryHitResponse(SearchRequest request, Set<String> matchingFiles) {
        String fileList = matchingFiles.stream()
                .map(StringEncoderDecoder::encode)
                .collect(Collectors.joining(" "));

        String payload = String.format(Constants.QUERY_HIT_FORMAT,
                matchingFiles.size(),
                nodeRoutingTable.getAddress(),
                nodeRoutingTable.getPort(),
                Constants.HOP_COUNT - request.getHops(),
                fileList);

        ChannelMessage response = createChannelMessage(payload);
        response.setDestination(request.getAddress(), request.getPort());
        sendRequest(response);
    }

    private void forwardSearchRequest(SearchRequest originalRequest) {
        nodeRoutingTable.getNeighbours().stream()
                .filter(neighbour -> !isOriginalSender(neighbour, originalRequest))
                .forEach(neighbour -> forwardToNeighbour(neighbour, originalRequest));
    }

    private boolean isOriginalSender(Neighbour neighbour, SearchRequest request) {
        return neighbour.getAddress().equals(request.getAddress()) &&
                neighbour.getClientPort() == request.getPort();
    }

    private void forwardToNeighbour(Neighbour neighbour, SearchRequest request) {
        String payload = String.format(Constants.QUERY_FORMAT,
                request.getAddress(),
                request.getPort(),
                StringEncoderDecoder.encode(request.getFileName()),
                request.getHops() - 1);

        ChannelMessage forwardedMessage = createChannelMessage(payload);
        forwardedMessage.setDestination(neighbour.getAddress(), neighbour.getPort());
        sendRequest(forwardedMessage);
    }

    private static class SearchRequest {
        private final String address;
        private final int port;
        private final String fileName;
        private final int hops;

        public SearchRequest(String address, int port, String fileName, int hops) {
            this.address = address;
            this.port = port;
            this.fileName = fileName;
            this.hops = hops;
        }

        // Getters omitted for brevity
    }
}
