package com.amazon.ata.kindlepublishingservice.publishing;


import javax.inject.Inject;
import java.util.LinkedList;
import java.util.Queue;

public class BookPublishRequestManager {
    // "processed in the same order that they are submitted." --> queue
    private final Queue<BookPublishRequest> publishRequestQueue;

    @Inject
    public BookPublishRequestManager() {
        this.publishRequestQueue = new LinkedList<>();
    }

    public void addBookPublishRequest(BookPublishRequest publishRequest) {
        publishRequestQueue.offer(publishRequest);
    }

    public BookPublishRequest getBookPublishRequestToProcess() {
        return publishRequestQueue.poll();
    }
}
