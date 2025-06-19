package org.doi.prmv4p113603.mlops.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * This class is used hold the value set in the property 'mlops-api.data-root'
 * in the file 'application.properties'.
 */
@Configuration
@ConfigurationProperties(prefix = "mlops-api")
@Validated
@Getter
@Setter
public class MlopsProperties {

    @NotBlank
    private String dataRoot;

}
