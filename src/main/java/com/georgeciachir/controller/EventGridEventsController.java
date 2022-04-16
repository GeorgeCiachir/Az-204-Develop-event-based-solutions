package com.georgeciachir.controller;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationEventData;
import com.azure.messaging.eventgrid.systemevents.SubscriptionValidationResponse;
import com.georgeciachir.dto.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class EventGridEventsController {

    private static final Logger LOG = LoggerFactory.getLogger(EventGridEventsController.class);

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        LOG.info("Greeting [{}]", name);
        return String.format("Hello, %s!", name);
    }

    @PostMapping("/webhook")
    public Object receiveEventGridEvent(
            @RequestHeader Map<String, String> headers,
            @RequestBody String eventGridEventJsonData) {
        LOG.info("Receiving via webhook: {}", eventGridEventJsonData);

        boolean isHandShake = headers.get("aeg-event-type").equals("SubscriptionValidation");

        if (isHandShake) {
            List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridEventJsonData);
            EventGridEvent event = eventGridEvents.get(0);

            SubscriptionValidationEventData validationEventData = event.getData().toObject(SubscriptionValidationEventData.class);
            SubscriptionValidationResponse validationResponse = new SubscriptionValidationResponse();
            validationResponse.setValidationResponse(validationEventData.getValidationCode());
            return validationResponse;
        }

        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridEventJsonData);

        EventGridEvent event = eventGridEvents.get(0);

        switch (event.getEventType()) {
            case "Microsoft.Storage.BlobCreated":
                LOG.info("Event is: {}", event.getData().toString());
                return event.getData().toString();
            case "GreetingSomeone":
                DataObject dataObject = event.getData().toObject(DataObject.class);
                LOG.info("Event is: {}", dataObject);
                return dataObject.toString();
            case "BlobCreationForwardedEventAsBinaryData":
                LOG.info("Event is: {}", event.getData().toString());
                return event.getData().toString();
            default:
                throw new IllegalArgumentException("Not yet implemented");
        }
    }
}
