package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.data.dto.event.AirflowEventDto;
import org.springframework.stereotype.Service;

@Service
public class AirflowEventService {

    public void process(AirflowEventDto event) {
        // TODO: save to PostgreSQL using JPA repositories
        System.out.println("Received event: " + event.getRunId());
    }
}
