package org.doi.prmv4p113603.mlops.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Holds MinIO configuration values from application.properties entries with prefix 'minio'.
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Validated
@Getter
@Setter
public class MinioProperties {

    @NotBlank
    private String endpoint;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String bucket;

}
