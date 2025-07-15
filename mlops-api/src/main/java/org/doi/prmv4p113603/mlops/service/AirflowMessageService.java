package org.doi.prmv4p113603.mlops.service;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.messaging.MessageDto;
import org.doi.prmv4p113603.mlops.exception.UnsupportedMessageTypeException;
import org.doi.prmv4p113603.mlops.messaging.MessageHandler;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Contains business logic for handling different message types.
 */
@Service
@AllArgsConstructor
public class AirflowMessageService {

    // NOTE: check https://theburningmonk.com/2024/11/eventbridge-best-practice-why-you-should-wrap-events-in-event-envelopes/

    // NOTE: Spring will automatically inject all beans in the application context that implement the MessageHandler interface
    private final List<MessageHandler> handlers;

    public void process(MessageDto message) {

        System.out.println("Received message: " + message);

        /*
         * NOTE: This is an application of Handler Mapping, what can be seen as a Design Pattern or
         *  an Architectural Pattern. It can embody elements of several patterns:
         *
         *    - Command Pattern: Each handler can be seen as a command that performs an action.
         *
         *    - Front Controller Pattern: A single controller intercepts all requests and delegates to handlers.
         *
         *    - Strategy Pattern: The logic of choosing the correct handler can be thought of as dynamic behavior selection.
         */
        handlers.stream()
                .filter(h -> h.canHandle(message))
                .findFirst()
                .orElseThrow(() -> new UnsupportedMessageTypeException(message.getType().toString()))
                .handle(message);

    }

}
