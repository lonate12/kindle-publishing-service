package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.ArrayList;
import java.util.List;

public class PublishStatusConverter {
    public PublishStatusConverter() {
    }

    public static List<PublishingStatusRecord> convertToListOfPublishingStatusRecords(List<PublishingStatusItem> listOfPublishingStatusItems) {
        List<PublishingStatusRecord> resultList = new ArrayList<>();

        for (PublishingStatusItem item : listOfPublishingStatusItems) {
            PublishingStatusRecord newRecord = PublishingStatusRecord.builder()
                    .withStatus(item.getStatus().name())
                    .withStatusMessage(item.getStatusMessage()).withBookId(item.getBookId()).build();
            resultList.add(newRecord);
        }

        return resultList;
    }
}
