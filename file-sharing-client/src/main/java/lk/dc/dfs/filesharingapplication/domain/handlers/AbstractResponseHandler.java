package lk.dc.dfs.filesharingapplication.domain.handlers;


import lk.dc.dfs.filesharingapplication.domain.service.comms.ChannelMessage;

public interface AbstractResponseHandler extends AbstractMessageHandler {

    void handleResponse(ChannelMessage message);
}
