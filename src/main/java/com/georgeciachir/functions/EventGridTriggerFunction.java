package com.georgeciachir.functions;

//import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.implementation.models.EventGridEvent;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

import java.util.List;

public class EventGridTriggerFunction {

    @FunctionName("event-grid-function")
    public void run(@EventGridTrigger(name = "eventGridEvent") EventGridEvent event,
                    final ExecutionContext context) {
        context.getLogger().info("Java Event Grid trigger function executed.");

//        List<EventGridEvent> events = EventGridEvent.fromString(message);
//        EventGridEvent event = events.get(0);

        context.getLogger().info(event.getEventType());
        context.getLogger().info(event.getData().toString());
    }
}
