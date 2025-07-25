package lk.dc.dfs.filesharingapplication.domain.handlers;



import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;
import lk.dc.dfs.filesharingapplication.domain.service.core.Neighbour;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.TimeoutManager;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class LeaveHandler implements AbstractRequestHandler {

    private RoutingTable routingInformation;
    private BlockingQueue<ChannelMessage> outgoingChannel;
    private static LeaveHandler singletonInstance;

    public synchronized static LeaveHandler obtainInstance() {
        if (singletonInstance == null) {
            singletonInstance = new LeaveHandler();
        }
        return singletonInstance;
    }

    public void sendLeave() {
        String messageContent = String.format(Constants.LEAVE_FORMAT,
                this.routingInformation.getAddress(),
                this.routingInformation.getPort());
        String completeMessage = String.format(Constants.MSG_FORMAT,
                messageContent.length() + 5, messageContent);

        for (Neighbour neighbour : routingInformation.getNeighbours()) {
            ChannelMessage msg = new ChannelMessage(
                    neighbour.getAddress(),
                    neighbour.getPort(),
                    completeMessage);
            transmitRequest(msg);
        }
    }

    @Override
    public void init(RoutingTable routingData,
                     BlockingQueue<ChannelMessage> outputChannel,
                     TimeoutManager timeoutHandler) {
        this.routingInformation = routingData;
        this.outgoingChannel = outputChannel;
    }

    @Override
    public void sendRequest(ChannelMessage msg) {
        try {
            outgoingChannel.put(msg);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}
