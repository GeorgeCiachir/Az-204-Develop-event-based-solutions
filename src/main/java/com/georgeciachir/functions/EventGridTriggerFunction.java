package com.georgeciachir.functions;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.georgeciachir.dto.DataObject;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.util.List;

public class EventGridTriggerFunction {

    private static final String TOPIC_URL = "https://mycustomtopic.westeurope-1.eventgrid.azure.net/api/events";
    private static final String KEY = "6pP7htfp00S+sALuY5jdRpmzIm+ExTS9vWbof2yu3j4=";

    @FunctionName("event-grid-function")
    public void run(@EventGridTrigger(name = "eventGridEvent") String message,
                    final ExecutionContext context) {
        context.getLogger().info("Java Event Grid trigger function executed.");

        List<EventGridEvent> events = EventGridEvent.fromString(message);
        EventGridEvent event = events.get(0);

        context.getLogger().info(event.getEventType());
        context.getLogger().info(event.getData().toString());

        EventGridPublisherClient<EventGridEvent> eventGridEventClient = new EventGridPublisherClientBuilder()
                .endpoint(TOPIC_URL)
                .credential(new AzureKeyCredential(KEY))
                .buildEventGridEventPublisherClient();

        BinaryData binaryData = BinaryData.fromObject(new DataObject(message));
        EventGridEvent eventToBeSent = new EventGridEvent(
                "Forwarding", "BlobCreationForwardedEventAsBinaryData", binaryData, "1.0"
        );
        eventGridEventClient.sendEvent(eventToBeSent);
    }
}
