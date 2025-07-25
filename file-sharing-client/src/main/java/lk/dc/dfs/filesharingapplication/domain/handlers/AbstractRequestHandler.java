package lk.dc.dfs.filesharingapplication.domain.handlers;


import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;

public interface AbstractRequestHandler extends AbstractMessageHandler {

    void sendRequest(ChannelMessage message);
}
