package com.georgeciachir.controller;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.georgeciachir.dto.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class AppController {

    private static final Logger LOG = LoggerFactory.getLogger(AppController.class);

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        return String.format("Hello, %s!", name);
    }

    @PostMapping("/webhook")
    public void receiveEventGridEvent(@RequestBody String eventGridEventJsonData) {
        List<EventGridEvent> eventGridEvents = EventGridEvent.fromString(eventGridEventJsonData);

        EventGridEvent event = eventGridEvents.get(0);

        DataObject dataObject = event.getData().toObject(DataObject.class);
        LOG.info(dataObject.toString());
    }
}
