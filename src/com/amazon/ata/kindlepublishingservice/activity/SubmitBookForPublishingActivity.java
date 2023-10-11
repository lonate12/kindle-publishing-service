package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.models.requests.SubmitBookForPublishingRequest;
import com.amazon.ata.kindlepublishingservice.models.response.SubmitBookForPublishingResponse;
import com.amazon.ata.kindlepublishingservice.converters.BookPublishRequestConverter;
import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequest;

import com.amazon.ata.kindlepublishingservice.publishing.BookPublishRequestManager;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

//  SubmitBookForPublishingRequest {
//     "bookId": "book.b3750190-2a30-4ca8-ae1b-73d0d202dc41",
//     "title": "Run Fast. Cook Fast. Eat Slow.: Quick-Fix Recipes for Hangry Athletes: A Cookbook",
//     "author": "Shalane Flanagan",
//     "text": "When I moved off the track, from racing 5,000 m and 10,000 m distances to the marathon, my training required... ",
//     "genre": "COOKING"
//  }
//  SubmitBookForPublishingResponse {
//    publishingRecordId: "publishingStatus.69c16130-60b5-485a-8326-7f79d3feb36d"
//  }



/**
 * Implementation of the SubmitBookForPublishingActivity for ATACurriculumKindlePublishingService's
 * SubmitBookForPublishing API.
 *
 * This API allows the client to submit a new book to be published in the catalog or update an existing book.
 */
public class SubmitBookForPublishingActivity {

    private final PublishingStatusDao publishingStatusDao;
    private final CatalogDao catalogDao;
    private final BookPublishRequestManager bookPublishRequestManager;

    /**
     * Instantiates a new SubmitBookForPublishingActivity object.
     *
     * @param publishingStatusDao PublishingStatusDao to access the publishing status table.
     * @param catalogDao CatalogDao to access the Catalog table.
     * @param bookPublishRequestManager BookPublishingRequestManager to help queue up publishing jobs
     */
    @Inject
    public SubmitBookForPublishingActivity(PublishingStatusDao publishingStatusDao,
                                           BookPublishRequestManager bookPublishRequestManager,
                                           CatalogDao catalogDao) {
        this.publishingStatusDao = publishingStatusDao;
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.catalogDao = catalogDao;
    }

    /**
     * Submits the book in the request for publishing.
     *
     * @param request Request object containing the book data to be published. If the request is updating an existing
     *                book, then the corresponding book id should be provided. Otherwise, the request will be treated
     *                as a new book.
     * @return SubmitBookForPublishingResponse Response object that includes the publishing status id, which can be used
     * to check the publishing state of the book.
     */
    public SubmitBookForPublishingResponse execute(SubmitBookForPublishingRequest request) {
        final BookPublishRequest bookPublishRequest = BookPublishRequestConverter.toBookPublishRequest(request);

        if (bookPublishRequest.getBookId() != null) {
            catalogDao.validateBookExists(bookPublishRequest.getBookId());
            // Note: Still questioning if removing from the catalog should go here
            //  The requirements stated, "(The previous version of the book will be marked inactive as part of the
            //  asynchronous publishing logic)" Not clear if that means I mark it inactive now or it will
            //  happen at some point in the process. Will comment it out for now until we have more direciton later
//            catalogDao.removeBookFromCatalog(bookPublishRequest.getBookId());
        }

        bookPublishRequestManager.addBookPublishRequest(bookPublishRequest);

        PublishingStatusItem item =  publishingStatusDao.setPublishingStatus(bookPublishRequest.getPublishingRecordId(),
                PublishingRecordStatus.QUEUED,
                bookPublishRequest.getBookId());

        return SubmitBookForPublishingResponse.builder()
                .withPublishingRecordId(item.getPublishingRecordId())
                .build();
    }
}
