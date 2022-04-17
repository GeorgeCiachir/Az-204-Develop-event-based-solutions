package com.georgeciachir.eventhubs;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/eventhubsproducer")
public class EventHubsProducerController {

    private static final Logger LOG = LoggerFactory.getLogger(EventHubsProducerController.class);

    private static final String SAS_CONNECTION_STRING_TO_EVENT_HUB = "Endpoint=sb://georgesapps.servicebus.windows.net/;SharedAccessKeyName=AllowAllForGreetingApp;SharedAccessKey=OW/u8Arq7gm20QyHyzxtjzBlwqdoI/w8RPXJ484f7A0=;EntityPath=greeting-app-event-hub";

    private static final EventHubProducerClient sasAuthenticatedClient = getSASAuthenticatedClient();

    @PostMapping("/sendBatch")
    public String sendBatch(@RequestParam int start,
                            @RequestParam int stop) {
        LOG.info("Sending a batch in interval: [{} - {}}]", start, stop);

        List<EventData> events = IntStream.rangeClosed(start, stop)
                .mapToObj(index -> "Event number " + index)
                .map(EventData::new)
                .collect(Collectors.toList());

        EventDataBatch eventBatch = sasAuthenticatedClient.createBatch();

        for (EventData event : events) {
            if (!eventBatch.tryAdd(event)) {
                throw new RuntimeException("Could not send event " + event.getBodyAsString());
            }
        }

        sasAuthenticatedClient.send(eventBatch);

        LOG.info("Batch sent ");
        return "Events have been produced and sent as batch";
    }

    @PostMapping("/send/{stringEvent}")
    public void send(@PathVariable String stringEvent,
                     @RequestParam String partitionId) {


        EventData eventData = new EventData(stringEvent);

        if (partitionId != null) {
            SendOptions sendOptions = new SendOptions().setPartitionId(partitionId);
            sasAuthenticatedClient.send(List.of(eventData), sendOptions);
        } else {
            sasAuthenticatedClient.send(List.of(eventData));
        }
    }

    private static EventHubProducerClient getSASAuthenticatedClient() {
        return new EventHubClientBuilder()
                .connectionString(SAS_CONNECTION_STRING_TO_EVENT_HUB)
                .buildProducerClient();
    }
}
