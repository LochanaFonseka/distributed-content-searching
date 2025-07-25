package lk.dc.dfs.filesharingapplication.domain.handlers;



import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;
import lk.dc.dfs.filesharingapplication.domain.service.core.RoutingTable;
import lk.dc.dfs.filesharingapplication.domain.service.core.TimeoutManager;

import java.util.concurrent.BlockingQueue;

interface AbstractMessageHandler {
    void init (
            RoutingTable routingTable,
            BlockingQueue<ChannelMessage> channelOut,
            TimeoutManager timeoutManager);

}
