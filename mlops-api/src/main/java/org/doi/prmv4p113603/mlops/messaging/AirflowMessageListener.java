package org.doi.prmv4p113603.mlops.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.messaging.MessageDto;
import org.doi.prmv4p113603.mlops.service.AirflowMessageService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Defines a @Bean of type Consumer<String> to receive Kafka messages.
 * <p>
 * TODO: could be the implementation of an interface MessageListener.
 */
@Component
@AllArgsConstructor
public class AirflowMessageListener {

    private final ObjectMapper objectMapper;
    private final AirflowMessageService eventService;

    // NOTE: See entries "spring.cloud.stream.bindings.airflowEvents" in application.properties
    @Bean
    public Consumer<String> airflowEvents() {
        return payload -> {
            try {
                MessageDto dto = objectMapper.readValue(payload, MessageDto.class);
                eventService.process(dto);
            } catch (Exception e) {
                System.err.println("Airflow message error: " + e.getMessage());
            }
        };
    }

}
