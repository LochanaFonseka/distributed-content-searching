package lk.dc.dfs.filesharingapplication.domain.service.core;

import lk.dc.dfs.filesharingapplication.domain.handlers.TimeoutCallback;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TimeoutManager {
    private static final Logger logger = Logger.getLogger(TimeoutManager.class.getName());
    private final ConcurrentMap<String, TimeoutRecord> pendingRequests = new ConcurrentHashMap<>();

    public void registerRequest(String messageId, long timeoutDuration, TimeoutCallback callback) {
        Objects.requireNonNull(messageId, "Message ID cannot be null");
        Objects.requireNonNull(callback, "Callback cannot be null");

        pendingRequests.put(messageId, new TimeoutRecord(
                System.currentTimeMillis() + timeoutDuration,
                timeoutDuration,
                callback
        ));
        logger.fine(() -> "Registered new timeout tracking for message: " + messageId);
    }

    public void acknowledgeResponse(String messageId) {
        if (pendingRequests.remove(messageId) != null) {
            logger.fine(() -> "Acknowledged response for message: " + messageId);
            if (Constants.R_PING_MESSAGE_ID.equals(messageId)) {
                logger.fine("Ping response received");
            }
        }
    }

    public void checkTimeouts() {
        long currentTime = System.currentTimeMillis();
        List<String> expiredMessages = new ArrayList<>();

        pendingRequests.forEach((messageId, record) -> {
            if (currentTime >= record.expirationTime) {
                handleTimeout(messageId, record);
                if (!Constants.R_PING_MESSAGE_ID.equals(messageId)) {
                    expiredMessages.add(messageId);
                }
            }
        });

        expiredMessages.forEach(pendingRequests::remove);
    }

    private void handleTimeout(String messageId, TimeoutRecord record) {
        try {
            record.callback.onTimeout(messageId);
            if (Constants.R_PING_MESSAGE_ID.equals(messageId)) {
                // Special handling for ping timeouts - extend the deadline
                record.extendTimeout();
                logger.fine("Extended ping timeout period");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error executing timeout callback for: " + messageId, e);
        }
    }

    private static class TimeoutRecord {
        private long expirationTime;
        private final long timeoutDuration;
        private final TimeoutCallback callback;

        TimeoutRecord(long expirationTime, long timeoutDuration, TimeoutCallback callback) {
            this.expirationTime = expirationTime;
            this.timeoutDuration = timeoutDuration;
            this.callback = callback;
        }

        void extendTimeout() {
            this.expirationTime += timeoutDuration;
        }
    }
}
