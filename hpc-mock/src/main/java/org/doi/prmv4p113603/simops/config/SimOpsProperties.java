package org.doi.prmv4p113603.simops.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * This class is used to hold the value set in the property 'mlops-api.data-root'
 * in the file 'application.properties'.
 */
// TODO: this class is replicated in service mlops-api.
@Configuration
@ConfigurationProperties(prefix = "mlops-api")
@Validated
@Getter
@Setter
public class SimOpsProperties {

    @NotBlank
    private String dataRoot;

}
