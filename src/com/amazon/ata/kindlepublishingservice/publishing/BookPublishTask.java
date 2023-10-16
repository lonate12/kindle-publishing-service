package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import javax.inject.Inject;

public final class BookPublishTask implements Runnable {
    private final PublishingStatusDao publishingStatusDao;
    private final BookPublishRequestManager bookPublishRequestManager;
    private final CatalogDao catalogDao;

    @Inject
    public BookPublishTask(PublishingStatusDao publishingStatusDao,
                           BookPublishRequestManager bookPublishRequestManager,
                           CatalogDao catalogDao) {
        this.publishingStatusDao = publishingStatusDao;
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.catalogDao = catalogDao;
    }

    @Override
    public void run() {
        BookPublishRequest publishRequest = bookPublishRequestManager.getBookPublishRequestToProcess();
        // If null, return
        if (publishRequest == null) {
            return;
        }

        // Set publish request to IN_PROGRESS
        this.setPublishingStatus(publishRequest.getPublishingRecordId(),
                PublishingRecordStatus.IN_PROGRESS,
                publishRequest.getBookId(),
                null);

        // Get a KindleFormattedBook
        KindleFormattedBook kindleFormattedBook = KindleFormatConverter.format(publishRequest);

        // Submit to the catalogDao
        ;
        CatalogItemVersion book = null;
        try {
            book = this.createOrUpdateBook(kindleFormattedBook);
        } catch (BookNotFoundException e) {
            setPublishingStatus(publishRequest.getPublishingRecordId(),
                    PublishingRecordStatus.FAILED,
                    publishRequest.getBookId(),
                    e.getMessage());
            return;
        }

        setPublishingStatus(publishRequest.getPublishingRecordId(), PublishingRecordStatus.SUCCESSFUL, book.getBookId(), null);
    }

    private void setPublishingStatus(String publishingStatusId, PublishingRecordStatus recordStatus, String bookId, String message) {
        if (message == null) {
            publishingStatusDao.setPublishingStatus(publishingStatusId, recordStatus, bookId);
        } else {
            publishingStatusDao.setPublishingStatus(publishingStatusId, recordStatus, bookId, message);
        }
    }

    private CatalogItemVersion createOrUpdateBook(KindleFormattedBook kindleFormattedBook) throws BookNotFoundException {
        CatalogItemVersion book;
        try {
            if (kindleFormattedBook.getBookId() == null) {
                book = catalogDao.createBook(kindleFormattedBook);
            } else {
                book = catalogDao.updateBook(kindleFormattedBook);
            }
        } catch (BookNotFoundException e) {
            throw new BookNotFoundException(e.getMessage());
        }

        return book;
    }
}
