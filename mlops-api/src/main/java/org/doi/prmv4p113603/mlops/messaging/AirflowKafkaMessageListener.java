package org.doi.prmv4p113603.mlops.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.messaging.AirflowKafkaMessageDto;
import org.doi.prmv4p113603.mlops.service.AirflowKafkaMessageService;
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
public class AirflowKafkaMessageListener {

    private final ObjectMapper objectMapper;
    private final AirflowKafkaMessageService eventService;

    @Bean
    public Consumer<String> airflowEvents() {
        return payload -> {
            try {
                AirflowKafkaMessageDto dto = objectMapper.readValue(payload, AirflowKafkaMessageDto.class);
                eventService.process(dto);
            } catch (Exception e) {
                System.err.println("Kafka message error: " + e.getMessage());
            }
        };
    }

}
