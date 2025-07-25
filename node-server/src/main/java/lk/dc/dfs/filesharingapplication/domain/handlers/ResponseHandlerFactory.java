package lk.dc.dfs.filesharingapplication.domain.handlers;


import lk.dc.dfs.filesharingapplication.domain.service.core.MessageBroker;

import java.util.logging.Logger;

public class ResponseHandlerFactory {

    private static final Logger logger = Logger.getLogger(ResponseHandlerFactory.class.getName());

    private static final Map<String, Supplier<AbstractResponseHandler>> HANDLER_MAP =
            createHandlerMap();

    private static Map<String, Supplier<AbstractResponseHandler>> createHandlerMap() {
        Map<String, Supplier<AbstractResponseHandler>> map = new HashMap<>();
        map.put("PING", PingHandler::getInstance);
        map.put("BPING", PingHandler::getInstance);
        map.put("PONG", PongHandler::getInstance);
        map.put("BPONG", PongHandler::getInstance);
        map.put("SER", SearchQueryHandler::getInstance);
        map.put("SEROK", QueryHitHandler::getInstance);
        map.put("LEAVE", PingHandler::getInstance);
        return map;
    }

    public static AbstractResponseHandler getHandlerForMessage(String messageType,
                                                               MessageBroker broker) {
        Supplier<AbstractResponseHandler> handlerSupplier = HANDLER_MAP.get(messageType);

        if (handlerSupplier == null) {
            logger.severe("Unsupported message type received: " + messageType);
            return null;
        }

        AbstractResponseHandler handler = handlerSupplier.get();
        initializeHandler(handler, broker);
        return handler;
    }

    private static void initializeHandler(AbstractResponseHandler handler,
                                          MessageBroker broker) {
        handler.init(
                broker.getRoutingTable(),
                broker.getChannelOut(),
                broker.getTimeoutManager()
        );
    }
}
