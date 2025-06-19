package org.doi.prmv4p113603.simops.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.simops.service.SimOpsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for mocking an HPC service.
 */
@Tag(name = "HPC")
@RestController
@RequestMapping("/api/v1/hpc")
@Validated
@AllArgsConstructor
public class SimOpsController {

    private final SimOpsService simOpsService;

}
