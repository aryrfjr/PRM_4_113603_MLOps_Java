package org.doi.prmv4p113603.mlops.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used hold the value set in the property 'mlops-api.data-root'
 * in the file 'application.properties'.
 */
@Configuration
@ConfigurationProperties(prefix = "mlops-api")
@Getter
@Setter
public class MlopsProperties {

    private String dataRoot;

}
