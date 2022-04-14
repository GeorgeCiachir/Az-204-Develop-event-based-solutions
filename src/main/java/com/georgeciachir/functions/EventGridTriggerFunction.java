package com.georgeciachir.functions;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class EventGridTriggerFunction {

    @FunctionName("event-grid-function")
    public void run(@EventGridTrigger(name = "eventGridEvent") EventGridEvent message,
                    final ExecutionContext context) {
        context.getLogger().info("Java Event Grid trigger function executed.");
        context.getLogger().info(message.getEventType());
        context.getLogger().info(message.getData().toString());
    }
}
