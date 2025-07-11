package org.doi.prmv4p113603.mlops.data.request;

import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Request payload for scheduling configuration space exploration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleExplorationRequest {

    /*
     * NOTE: This DTO represents an incoming HTTP request body and not
     *  an internal transport / transformation object. Its name doesn't
     *  end with Dto as per standard REST API naming conventions that
     *  recommends ending with Request/Response for request/response bodies.
     */

    @Min(value = 1, message = "Number of simulations must be at least 1")
    private int numSimulations;

}
