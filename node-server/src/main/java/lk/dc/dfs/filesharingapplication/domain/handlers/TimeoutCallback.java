package lk.dc.dfs.filesharingapplication.domain.handlers;

public interface TimeoutCallback {
    void onTimeout(String messageId);
    void onResponse(String messageId);
}
