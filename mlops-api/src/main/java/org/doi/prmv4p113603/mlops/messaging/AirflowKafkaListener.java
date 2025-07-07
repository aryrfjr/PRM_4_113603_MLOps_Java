package org.doi.prmv4p113603.mlops.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.event.AirflowEventDto;
import org.doi.prmv4p113603.mlops.service.AirflowEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@AllArgsConstructor
public class AirflowKafkaListener {

    private final ObjectMapper objectMapper;
    private final AirflowEventService eventService;

    @Bean
    public Consumer<String> airflowEvents() {
        return payload -> {
            try {
                AirflowEventDto dto = objectMapper.readValue(payload, AirflowEventDto.class);
                eventService.process(dto);  // Do something here (e.g., persist to PostgreSQL)
            } catch (Exception e) {
                System.err.println("Kafka message error: " + e.getMessage());
            }
        };
    }
}
