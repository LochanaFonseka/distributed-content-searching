package lk.dc.dfs.filesharingapplication.domain.handlers;



import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.TimeoutManager;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class PongHandler implements AbstractRequestHandler, AbstractResponseHandler{

    private final Logger LOG = Logger.getLogger(PongHandler.class.getName());

    private BlockingQueue<ChannelMessage> channelOut;

    private RoutingTable routingTable;

    private static PongHandler pongHandler;
    private TimeoutManager timeoutManager;

    private PongHandler(){

    }

    public synchronized static PongHandler getInstance(){
        if (pongHandler == null){
            pongHandler = new PongHandler();
        }

        return pongHandler;
    }

    @Override
    public void sendRequest(ChannelMessage message) {

    }

    @Override
    public void handleResponse(ChannelMessage message) {
        LOG.fine("Received PONG : " + "[hidden]"
                + " from: " + message.getAddress()
                + " port: " + message.getPort());

        StringTokenizer stringToken = new StringTokenizer(message.getMessage(), " ");

        String length = stringToken.nextToken();
        String keyword = stringToken.nextToken();
        String address = stringToken.nextToken().trim();
        int port = Integer.parseInt(stringToken.nextToken().trim());
        if(keyword.equals("BPONG")) {
            if(routingTable.getCount() < Constants.MIN_NEIGHBOURS) {
                this.routingTable.addNeighbour(address, port, message.getPort());
            }
        } else {
            this.timeoutManager.registerResponse(String.format(Constants.PING_MESSAGE_ID_FORMAT,address,port));
            this.routingTable.addNeighbour(address, port, message.getPort());
        }

    }

    @Override
    public void init(
            RoutingTable routingTable,
            BlockingQueue<ChannelMessage> channelOut,
            TimeoutManager timeoutManager) {
        this.routingTable = routingTable;
        this.channelOut = channelOut;
        this.timeoutManager = timeoutManager;
    }
}
