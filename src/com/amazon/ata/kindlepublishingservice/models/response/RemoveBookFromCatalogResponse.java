package com.amazon.ata.kindlepublishingservice.models.response;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.models.Book;

import java.util.Objects;

public class RemoveBookFromCatalogResponse {
    private Book deletedBook;
    public RemoveBookFromCatalogResponse() {
    }
    public RemoveBookFromCatalogResponse(Book deletedBook) {
        this.deletedBook = deletedBook;
    }

    public Book getDeletedBook() {
        return deletedBook;
    }

    public void setDeletedBook(Book deletedBook) {
        this.deletedBook = deletedBook;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoveBookFromCatalogResponse)) return false;
        RemoveBookFromCatalogResponse that = (RemoveBookFromCatalogResponse) o;
        return Objects.equals(getDeletedBook(), that.getDeletedBook());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeletedBook());
    }

    @Override
    public String toString() {
        return "RemoveBookFromCatalogResponse{" +
                "deletedBook=" + deletedBook +
                '}';
    }
}
