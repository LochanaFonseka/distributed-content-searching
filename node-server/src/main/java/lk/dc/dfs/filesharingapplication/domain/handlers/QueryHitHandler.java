package lk.dc.dfs.filesharingapplication.domain.handlers;


import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.SearchResult;
import lk.dc.dfs.filesharingapplication.domain.service.core.TimeoutManager;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;
import lk.dc.dfs.filesharingapplication.domain.util.StringEncoderDecoder;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class QueryHitHandler implements AbstractResponseHandler {

    private static final Logger logger = Logger.getLogger(QueryHitHandler.class.getName());

    private RoutingTable nodeRoutingTable;
    private BlockingQueue<ChannelMessage> outgoingMessages;
    private TimeoutManager requestTimeoutManager;

    private static QueryHitHandler instance;
    private Map<String, SearchResult> searchResults;
    private long searchStartTimestamp;

    private QueryHitHandler() {
        // Private constructor for singleton pattern
    }

    public static synchronized QueryHitHandler getInstance() {
        if (instance == null) {
            instance = new QueryHitHandler();
        }
        return instance;
    }

    @Override
    public synchronized void handleResponse(ChannelMessage response) {
        logger.fine("Received SEROK response from: " + response.getAddress() +
                " port: " + response.getPort());

        String[] messageParts = response.getMessage().split(" ");
        int resultCount = Integer.parseInt(messageParts[2]);
        String sourceAddress = messageParts[3].trim();
        int sourcePort = Integer.parseInt(messageParts[4].trim());
        int hopCount = Integer.parseInt(messageParts[5]);

        String nodeIdentifier = String.format(Constants.ADDRESS_KEY_FORMAT, sourceAddress, sourcePort);

        processSearchResults(messageParts, resultCount, sourceAddress, sourcePort, hopCount, nodeIdentifier);
    }

    private void processSearchResults(String[] messageParts, int resultCount,
                                      String address, int port, int hops,
                                      String nodeKey) {
        int currentIndex = 6; // Start of file names in message
        long searchDuration = System.currentTimeMillis() - searchStartTimestamp;

        while (resultCount > 0 && currentIndex < messageParts.length) {
            String fileName = StringEncoderDecoder.decode(messageParts[currentIndex]);

            if (searchResults != null) {
                String resultKey = nodeKey + fileName;
                if (!searchResults.containsKey(resultKey)) {
                    SearchResult result = new SearchResult(
                            fileName,
                            address,
                            port,
                            hops,
                            searchDuration
                    );
                    searchResults.put(resultKey, result);
                }
            }

            resultCount--;
            currentIndex++;
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

    public void setSearchResults(Map<String, SearchResult> results) {
        this.searchResults = results;
    }

    public void setSearchInitiatedTime(long startTime) {
        this.searchStartTimestamp = startTime;
    }
}
