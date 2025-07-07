package org.doi.prmv4p113603.mlops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AirflowClientConfig {

    @Bean
    public WebClient airflowWebClient() {
        return WebClient.builder()
                .baseUrl("http://airflow-webserver:8080/api/v1") // TODO: set this URL in application.properties
                .defaultHeaders(headers -> {
                    headers.setBasicAuth("admin", "admin");
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

}
