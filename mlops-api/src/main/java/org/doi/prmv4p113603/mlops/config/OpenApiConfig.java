package org.doi.prmv4p113603.mlops.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "REST API for the MLOps workflow used in Phys. Rev. Materials 4, 113603",
                version = "1.0.0",
                description = "REST API for MLOps tasks like data generation, augmentation, and building feature store (DBIs)."
        ),
        tags = {
                @Tag(name = "DataOps", description = "Tasks related to data generation, configuration exploration, augmentation, and ETL/DBI creation."),
                @Tag(name = "ModelOps", description = "Tasks related to evaluating trained models on DBI test sets."),
                @Tag(name = "CRUD", description = "Create, read, update, and delete operations."),
                @Tag(name = "Misc", description = "Miscellaneous.")
        }
)
@Configuration
public class OpenApiConfig {
    // This class is just metadata; no methods required
}
