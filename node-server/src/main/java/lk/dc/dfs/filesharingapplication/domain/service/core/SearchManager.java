package lk.dc.dfs.filesharingapplication.domain.service.core;



import lk.dc.dfs.filesharingapplication.domain.handlers.QueryHitHandler;
import lk.dc.dfs.filesharingapplication.domain.util.ConsoleTable;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchManager {

    private final MessageBroker broker;
    private Map<String, SearchResult> availableDownloads;

    public SearchManager(MessageBroker messageBroker) {
        this.broker = Objects.requireNonNull(messageBroker, "MessageBroker cannot be null");
        this.availableDownloads = new ConcurrentHashMap<>();
    }

    public Map<String, SearchResult> performSearch(String searchTerm) {
        Map<String, SearchResult> results = new ConcurrentHashMap<>();
        QueryHitHandler handler = QueryHitHandler.getInstance();

        handler.initializeSearch(results, System.currentTimeMillis());
        broker.initiateSearch(searchTerm);

        displaySearchMessage();
        waitForSearchCompletion();

        cacheSearchResults(results);
        handler.resetSearchResults();

        return Collections.unmodifiableMap(results);
    }

    private void displaySearchMessage() {
        System.out.println("Gathering search results. Please wait...");
    }

    private void waitForSearchCompletion() {
        try {
            TimeUnit.MILLISECONDS.sleep(Constants.SEARCH_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Search was interrupted: " + e.getMessage());
        }
    }

    private void cacheSearchResults(Map<String, SearchResult> results) {
        this.availableDownloads = new ConcurrentHashMap<>(results);
    }

    public Optional<SearchResult> retrieveFileDetails(String filename) {
        return Optional.ofNullable(availableDownloads.get(filename));
    }

    public void clearCachedResults() {
        availableDownloads.clear();
    }
}
