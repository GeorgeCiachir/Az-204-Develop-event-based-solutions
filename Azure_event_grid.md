# Event types
- **Discrete** - **Event Grid** - report state changes and are actionable
- **Series** - **Event Hub** - report a condition, time-ordered, and analyzable
- **User notification** **Notification Hub** - prompt user on their device


# Explore Azure Event Grid
- It first needs to be enabled at the subscription level
```
az provider register --namespace Microsoft.EventGrid
az provider show --namespace Microsoft.EventGrid --query "registrationState"
```
- Azure Event Grid is an eventing backplane that enables event-driven, reactive programming
- It uses the publish-subscribe model
- has built-in support for events coming from Azure services, like storage blobs and resource groups
- has support for your own events, using custom topics

## Concepts in Azure Event Grid
- **Events** - What happened
    - Every event has common information like:
      - unique identifier
      - source of the event
      - specific information that is only relevant to the specific type of event
      - time the event took place
    - An event of size up to 64 KB is covered by General Availability (GA) Service Level Agreement (SLA)
    - The support for an event of size up to 1 MB is currently in preview
    - Events over 64 KB are charged in 64-KB increments
- **Event sources / Publishers** - Where the event took place
- **Topics** - The endpoint where publishers send events
  - The event grid topic provides an endpoint where the source sends events
  - The publisher creates the event grid topic, and decides whether an event source needs one topic or more than one topic
  - **System topics** 
    - are built-in topics provided by Azure services
    - To subscribe, you provide information about the resource you want to receive events from
  - **Custom topics** 
    - are application and third-party topics
    - when you create or are assigned access to a custom topic, you see that custom topic in your subscription
- **Event subscriptions** 
  - A subscription tells Event Grid which events on a topic you're interested in receiving
  - When creating the subscription, you provide an endpoint for handling the event
  - The endpoint or built-in mechanism to route events, sometimes to more than one handler
  - Subscriptions are also used by handlers to intelligently filter incoming events
- **Event handlers** - The app or service reacting to the event
  - You can use a supported Azure service or your own webhook as the handler
  - Depending on the type of handler, Event Grid follows different mechanisms to guarantee the delivery of the event
  - For HTTP webhook event handlers, the event is retried until the handler returns a status code of `200 – OK`
  - For Azure Storage Queue, the events are retried until the Queue service successfully processes the message push into the queue

## Discover event schemas
- events consist of a set of 4 required string properties which are common to all events from any publisher:
  - id
  - subject
  - eventType
  - eventTime
- the data object has properties that are specific to each publisher
- event sources send events to Azure Event Grid in an array(up to 1 MB), which can have several event objects
- each event in the array is limited to 1 MB
- events over 64 KB will incur operations charges as though they were multiple events (an event that is 130 KB would incur 
  operations as though it were 3 separate events)
- Event Grid sends the events to subscribers in an array that has a single event

Sometimes your subject needs more detail about what happened. For example, the Storage Accounts publisher provides the
subject `/blobServices/default/containers/<container-name>/blobs/<file>` when a file is added to a container. A subscriber
could filter by the path `/blobServices/default/containers/testcontainer` to get all events for that container but not other
containers in the storage account. A subscriber could also filter or route by the suffix .txt to only work with text files.

## Explore event delivery durability
- Event Grid provides durable delivery
- If a subscriber's endpoint doesn't acknowledge receipt of an event or if there is a failure, Event Grid retries delivery 
  based on a fixed retry schedule and retry policy
- By default, Event Grid delivers one event at a time to the subscriber, and the payload is an array with a single event

## Retry schedule
- when Event Grid receives an error for an event delivery attempt, it decides whether it should retry the delivery,
  dead-letter the event, or drop the event based on the type of the error
- If the error returned by the subscribed endpoint is a configuration-related error that can't be fixed with retries 
  (for example, if the endpoint is deleted), EventGrid will either perform dead-lettering on the event or drop the event
  if dead-letter isn't configured
- **The following table describes the types of endpoints and errors for which retry doesn't happen:**

| Endpoint Type       | 	Error codes                                                                                  |
|:--------------------|:----------------------------------------------------------------------------------------------|
| Azure Resources     | 400 Bad Request, 413 Request Entity Too Large, 403 Forbidden                                  |
| Webhook             | 400 Bad Request, 413 Request Entity Too Large, 403 Forbidden, 404 Not Found, 401 Unauthorized |

- If the error returned by the subscribed endpoint isn't among the above list, Event Grid waits 30 seconds for a response after delivering a message
- After 30 seconds, if the endpoint hasn’t responded, the message is queued for retry
- Event Grid uses an exponential backoff retry policy for event delivery
- If the endpoint responds within 3 minutes, Event Grid will attempt to remove the event from the retry queue on a best 
  effort basis but duplicates may still be received

## Retry policy
- **Maximum number of attempts** - The value must be an integer between 1 and 30. The default value is 30
- **Event time-to-live (TTL)** - The value must be an integer between 1 and 1440. The default value is 1440 minutes

Example for maximum 18 attempts:
```
az eventgrid event-subscription create \
  -g gridResourceGroup \
  --topic-name <topic_name> \
  --name <event_subscription_name> \
  --endpoint <endpoint_URL> \
  --max-delivery-attempts 18
```

## Output batching
- **Max events per batch** 
  - Must be between 1 and 5,000
  - Maximum number of events Event Grid will deliver per batch
  - This number will never be exceeded, however fewer events may be delivered if no other events are available at the time of publish 
  - Event Grid doesn't delay events to create a batch if fewer events are available
- **Preferred batch size in kilobytes**
  - Similar to max events, the batch size may be smaller if more events aren't available at the time of publish
  - It's possible that a batch is larger than the preferred batch size if a single event is larger than the preferred size
  - For example, if the preferred size is 4 KB and a 10-KB event is pushed to Event Grid, the 10-KB event will still be delivered 
    in its own batch rather than being dropped

## Delayed delivery
- similar to CB
- if the first 10 events published to an endpoint fail, Event Grid will assume that the endpoint is experiencing issues 
  and will delay all subsequent retries, and new deliveries, for some time - in some cases up to several hours
- Without back-off and delay of delivery to unhealthy endpoints, Event Grid's retry policy and volume capabilities 
  can easily overwhelm a system

## Dead-letter events
- By default, Event Grid doesn't turn on dead-lettering
- To enable it, you must specify a storage account to hold undelivered events when creating the event subscription
- if retry fails, it can send the undelivered event to a **storage account**
- Event Grid dead-letters an event when one of the following conditions is met:
  - Event isn't delivered within the time-to-live period
  - The number of tries to deliver the event exceeds the limit
- There is a five-minute delay between the last attempt to deliver an event and when it is delivered to the dead-letter location
- If the dead-letter location is unavailable for four hours, the event is dropped

## Custom delivery properties
- Event subscriptions allow you to set up to 10 HTTP headers in the delivered events
- Each header value shouldn't be greater than 4,096 (4K) bytes

## Control access to events
| Role                           | Description                                                          |
|--------------------------------|:---------------------------------------------------------------------|
| Event Grid Subscription Reader | Lets you read Event Grid event subscriptions                         |
| Event Grid Subscription        | Contributor	Lets you manage Event Grid event subscription operations |
| Event Grid Contributor         | Lets you create and manage Event Grid resources                      |
| Event Grid Data Sender         | Lets you send events to Event Grid topics                            |

- If you're using an event handler that isn't a WebHook (such as an event hub or queue storage), you need write access to that resource
- This permission check prevents an unauthorized user from sending events to your resource
- The resource that is the event source must have the `Microsoft.EventGrid/EventSubscriptions/Write` in order to send events

| Topic Type       | Description                                                                                                                                                                                                                                                                  |
|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| System topics    | Need permission to write a new event subscription at the scope of the resource publishing the event. The format of the resource is: `/subscriptions/{subscription-id}/resourceGroups/{resource-group-name}/providers/{resource-provider}/{resource-type}/{resource-name}`    |
| Custom topics    | Need permission to write a new event subscription at the scope of the event grid topic. The format of the resource is: `/subscriptions/{subscription-id}/resourceGroups/{resource-group-name}/providers/Microsoft.EventGrid/topics/{topic-name}  `                           |

## Receive events by using webhooks
- When a new event is ready, Event Grid service POSTs an HTTP request to the configured endpoint with the event in the request body
- Event Grid requires you to prove ownership of your Webhook endpoint before it starts delivering events to that endpoint
- When you use any of the three Azure services listed below, the Azure infrastructure automatically handles this validation:
  - Azure Logic Apps with Event Grid Connector
  - Azure Automation via webhook
  - Azure Functions with Event Grid Trigger

## Endpoint validation with Event Grid events
- If you're using any other type of endpoint, such as an HTTP trigger based Azure function, your endpoint code needs to
  participate in a validation handshake with Event Grid
- Event Grid supports two ways of validating the subscription: 
  - **Synchronous handshake**: At the time of event subscription creation, Event Grid sends a subscription validation 
    event to your endpoint. The schema of this event is similar to any other Event Grid event. 
    The data portion of this event includes a validationCode property. Your application verifies that the validation
    request is for an expected event subscription, and returns the validation code in the response synchronously. 
    This handshake mechanism is supported in all Event Grid versions.
  - **Asynchronous handshake**: In certain cases, you can't return the ValidationCode in response synchronously. 
    For example, if you use a third-party service (like Zapier or IFTTT), you can't programmatically respond with the validation code.

## Filter events
- When creating an event subscription, you have three options for filtering:
  - Event types
  - Subject begins with or ends with
  - Advanced fields and operators

### Event type filtering
Provide an array with the event types, or specify All to get all event types for the event source:
```
"filter": {
  "includedEventTypes": [
    "Microsoft.Resources.ResourceWriteFailure",
    "Microsoft.Resources.ResourceWriteSuccess"
  ]
}
```

### Subject filtering
You can filter the subject begins with `/blobServices/default/containers/testcontainer` to get all events for that container 
but not other containers in the storage account
```
"filter": {
  "subjectBeginsWith": "/blobServices/default/containers/mycontainer/log",
  "subjectEndsWith": ".jpg"
}
```

### Advanced filtering
To filter by values in the data fields and specify the comparison operator, use the advanced filtering option. 
In advanced filtering, you specify the:
- **operator type** - The type of comparison
- **key** - The field in the event data that you're using for filtering. It can be a number, boolean, or string
- **value or values** - The value or values to compare to the key
```
"filter": {
  "advancedFilters": [
    {
      "operatorType": "NumberGreaterThanOrEquals",
      "key": "Data.Key1",
      "value": 5
    },
    {
      "operatorType": "StringContains",
      "key": "Subject",
      "values": ["container1", "container2"]
    }
  ]
}
```