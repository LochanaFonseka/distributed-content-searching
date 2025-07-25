package lk.dc.dfs.filesharingapplication.domain.service.core;



import lk.dc.dfs.filesharingapplication.domain.handlers.QueryHitHandler;
import lk.dc.dfs.filesharingapplication.domain.util.ConsoleTable;
import lk.dc.dfs.filesharingapplication.domain.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SearchManager {

    private MessageBroker messageBroker;

    private Map<String, SearchResult> fileDownloadOptions;

    SearchManager(MessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    Map<String, SearchResult> doSearch(String keyword) {

        Map<String, SearchResult> searchResults
                = new HashMap<String, SearchResult>();

        QueryHitHandler queryHitHandler = QueryHitHandler.getInstance();
        queryHitHandler.setSearchResutls(searchResults);
        queryHitHandler.setSearchInitiatedTime(System.currentTimeMillis());

        this.messageBroker.doSearch(keyword);

        System.out.println("Please be patient till the file results are returned ...");

        try {
            Thread.sleep(Constants.SEARCH_TIMEOUT);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //printSearchResults(searchResults);
        this.clearSearchResults();
        fileDownloadOptions = Map.copyOf(searchResults);
        //return fileDownloadOptions.size();
        return searchResults;
    }


    private void clearSearchResults() {
        QueryHitHandler queryHitHandler = QueryHitHandler.getInstance();

        queryHitHandler.setSearchResutls(null);
    }

    public SearchResult getFileDetails(String fileName) {
        return this.fileDownloadOptions.get(fileName);
    }
}
