package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.PublishStatusConverter;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.exceptions.PublishingStatusNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import java.util.List;

public class GetPublishingStatusActivity {
    private PublishingStatusDao publishingStatusDao;
    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        String publishingId = publishingStatusRequest.getPublishingRecordId();
        List< PublishingStatusItem> publishingStatusItemList = publishingStatusDao.getPublishingStatus(publishingId);
        if (publishingStatusItemList.isEmpty()) {
            throw new PublishingStatusNotFoundException("Unable to find a publishing status for publishingID: " + publishingId);
        }

        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(PublishStatusConverter.convertToListOfPublishingStatusRecords(publishingStatusItemList))
                .build();
    }
}
