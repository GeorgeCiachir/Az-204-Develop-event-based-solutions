package com.georgeciachir.functions;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.georgeciachir.dto.DataObject;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class HttpFunction {

    private static final String TOPIC_URL = "https://mycustomtopic.westeurope-1.eventgrid.azure.net/api/events";
    private static final String KEY = "6pP7htfp00S+sALuY5jdRpmzIm+ExTS9vWbof2yu3j4=";

    /**
     * "/api/http-function?name=George"
     */
    @FunctionName("http-function")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET, HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
                    HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String name = request.getQueryParameters().get("name");

        String message = name == null ? "Hello, Anonymous!" : "Hello, " + name + "!";

        EventGridPublisherClient<EventGridEvent> eventGridEventClient = new EventGridPublisherClientBuilder()
                .endpoint(TOPIC_URL)
                .credential(new AzureKeyCredential(KEY))
                .buildEventGridEventPublisherClient();

        BinaryData binaryData = BinaryData.fromObject(new DataObject(message));
        EventGridEvent event = new EventGridEvent(
                "Greeting", "Greeting someone", binaryData, "1.0"
        );
        eventGridEventClient.sendEvent(event);

        return request.createResponseBuilder(HttpStatus.OK)
                .body(message).build();
    }
}
