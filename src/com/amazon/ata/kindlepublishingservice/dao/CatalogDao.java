package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormatConverter;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }

    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression<CatalogItemVersion>()
            .withHashKeyValues(book)
            .withScanIndexForward(false)
            .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public CatalogItemVersion removeBookFromCatalog(String bookId) {
        // Get the most current version from the catalog
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        // If for whatever reason, we don't have anything, we need to throw a BookNotFoundException
        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }
        // Set inactive to true
        book.setInactive(true);
        // Save the book to the catalog
        dynamoDbMapper.save(book);

        return book;
    }

    public void validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null) {
            throw new BookNotFoundException("Publish request for existing book failed. Could not find book with bookId: " + bookId);
        }
    }

    public CatalogItemVersion createBook(KindleFormattedBook kindleFormattedBook) {
        // Create new CatalogItemVersion item.
        String newBookId = KindlePublishingUtils.generateBookId();
        CatalogItemVersion catalogItemVersion = new CatalogItemVersion(newBookId,
                1,
                false,
                kindleFormattedBook.getTitle(),
                kindleFormattedBook.getAuthor(),
                kindleFormattedBook.getText(),
                kindleFormattedBook.getGenre()
        );

        dynamoDbMapper.save(catalogItemVersion);

        return catalogItemVersion;
    }

    public CatalogItemVersion updateBook(KindleFormattedBook kindleFormattedBook) {
        try {
            validateBookExists(kindleFormattedBook.getBookId());
        } catch (BookNotFoundException e) {
            throw new BookNotFoundException(e.getMessage());
        }

        CatalogItemVersion versionToBeDeactivated = getLatestVersionOfBook(kindleFormattedBook.getBookId());
        CatalogItemVersion versionToBeCreated = new CatalogItemVersion(
                versionToBeDeactivated.getBookId(),
                versionToBeDeactivated.getVersion() + 1,
                false,
                kindleFormattedBook.getTitle(),
                kindleFormattedBook.getAuthor(),
                kindleFormattedBook.getText(),
                kindleFormattedBook.getGenre()
                );
        this.removeBookFromCatalog(versionToBeDeactivated.getBookId());
        dynamoDbMapper.save(versionToBeCreated);
        return versionToBeCreated;
    }
}
