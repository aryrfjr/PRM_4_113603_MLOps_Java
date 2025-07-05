package org.doi.prmv4p113603.mlops.data.dto.event;

import lombok.Data;

@Data
public class AirflowEventDto {

    private String runId;
    private String payloadType;

}
