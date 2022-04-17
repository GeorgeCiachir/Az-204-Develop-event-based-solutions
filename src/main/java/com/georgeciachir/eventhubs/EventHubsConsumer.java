package com.georgeciachir.eventhubs;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.PartitionProperties;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/eventhubsconsumer")
public class EventHubsConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(EventHubsConsumer.class);

    private static final String CONSUMER_GROUP_NAME = "helloEventGroup";
    private static final String SAS_CONNECTION_STRING_TO_EVENT_HUB = "Endpoint=sb://georgesapps.servicebus.windows.net/;SharedAccessKeyName=AllowAllForGreetingApp;SharedAccessKey=OW/u8Arq7gm20QyHyzxtjzBlwqdoI/w8RPXJ484f7A0=;EntityPath=greeting-app-event-hub";

    private static final EventHubConsumerClient EVENT_HUB_CONSUMER_CLIENT = createConsumerClient();

    @GetMapping("/pull")
    public String pull(@RequestParam String partitionId,
                       @RequestParam String position,
                       @RequestParam int count) {

        EventPosition startPosition;
        if ("LAST".equals(position)) {
            PartitionProperties properties = EVENT_HUB_CONSUMER_CLIENT.getPartitionProperties(partitionId);
            startPosition = EventPosition.fromSequenceNumber(properties.getLastEnqueuedSequenceNumber());
        } else {
            startPosition = EventPosition.fromSequenceNumber(Long.parseLong(position));
        }

        List<String> events = EVENT_HUB_CONSUMER_CLIENT
                .receiveFromPartition(partitionId, count, startPosition)
                .stream()
                .map(event -> event.getData().getBodyAsString())
                .collect(Collectors.toList());

        String message = "Events: " + events;
        LOG.info(message);

        return message;
    }

    private static EventHubConsumerClient createConsumerClient() {
        return new EventHubClientBuilder()
                .connectionString(SAS_CONNECTION_STRING_TO_EVENT_HUB)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildConsumerClient();
    }
}
