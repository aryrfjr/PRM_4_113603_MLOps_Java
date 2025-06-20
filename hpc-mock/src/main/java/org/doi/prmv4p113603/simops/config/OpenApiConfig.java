package org.doi.prmv4p113603.simops.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "REST API for the SimOps portion of the MLOps workflow used in Phys. Rev. Materials 4, 113603",
                version = "1.0.0",
                description = "REST API for mocking an HPC Job Scheduler."
        ),
        tags = {
                @Tag(name = "SimOps", description = "Tasks related to jobs scheduling and status check.")
        }
)
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {

                final String securitySchemeName = "bearerAuth";

                return new OpenAPI()
                        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                        .components(new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .in(SecurityScheme.In.HEADER)));

        }

}
