package org.doi.prmv4p113603.mlops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.doi.prmv4p113603.mlops.config.MlopsProperties;
import org.doi.prmv4p113603.mlops.data.dto.NominalCompositionDto;
import org.doi.prmv4p113603.mlops.data.request.ScheduleExplorationRequest;
import org.doi.prmv4p113603.mlops.domain.*;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.doi.prmv4p113603.mlops.repository.SubRunRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test class for the DataOps service.
 * <p>
 * It is focused on testing the DataOps service loading the Spring context.
 * <p>
 * NOTE: start Docker Compose before use this class with Maven Wrapper (mvnw).
 */
@ExtendWith(SpringExtension.class) // To integrate the Spring TestContext Framework into JUnit 5 tests
@SpringBootTest // Tells Spring Boot to bootstrap the entire application context for the tests
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // JUnit creates only one instance of the test class per test class
@TestPropertySource("classpath:application.properties") // The test will use properties from src/test/resources/application.properties
class DataOpsServiceTest {

    /*
     * NOTE: Configures a Testcontainers LocalStackContainer instance for integration testing
     *  with AWS services locally emulated by LocalStack. Here:
     *
     *  - LocalStackContainer is a special Testcontainers container that runs LocalStack;
     *    a fully functional local AWS cloud stack emulator.
     *
     *  - "localstack/localstack:2.3" specifies the Docker image version of LocalStack.
     *
     *  - .withServices(LocalStackContainer.Service.S3) tells the container to start only the
     *    S3 service inside LocalStack. You can add other AWS services as needed.
     *
     * NOTE: Testcontainers is a Java testing library that lets you run Docker containers programmatically.
     *
     * NOTE: LocalStack is a Docker-based AWS emulator (S3, SQS, Lambda, etc.).
     */
    static final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.3"))
            .withServices(LocalStackContainer.Service.S3);

    @BeforeAll
    static void startLocalstack() {

        /*
         * NOTE: This will be valid for all methods because of the annotation @TestInstance.
         */

        localstack.start();
        System.setProperty("minio.endpoint", localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        System.setProperty("minio.access-key", localstack.getAccessKey());
        System.setProperty("minio.secret-key", localstack.getSecretKey());
    }

    @AfterAll
    static void stopLocalstack() {
        localstack.stop();
    }

    @Autowired
    MinioStorageService minioStorageService;

    @Autowired
    NominalCompositionRepository compositionRepo;

    @Autowired
    RunRepository runRepo;

    @Autowired
    SubRunRepository subRunRepo;

    @Autowired
    MlopsProperties mlopsProperties;

    @Test
    void testScheduleExploration_success() throws Exception {

        // Content of the request payload
        String nominalCompositionName = "Zr49Cu49Al2";
        int numSimulations = 3;

        NominalCompositionDto result = getNominalCompositionDto(numSimulations, nominalCompositionName);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String resultJson = mapper.writeValueAsString(result);

        System.out.println("DataOpsService.scheduleExploration -> resultJson -> " + resultJson);

        // Asserting with Junit 5
        assertNotNull(result);
        assertEquals(numSimulations, result.getRuns().size());
        assertEquals(SimulationStatus.SCHEDULED, result.getRuns().get(0).getStatus());

    }

    // TODO: testScheduleExploitation_success

    /*
     *
     */
    private NominalCompositionDto getNominalCompositionDto(int numSimulations, String nominalCompositionName) {

        ScheduleExplorationRequest request = new ScheduleExplorationRequest();
        request.setNumSimulations(numSimulations);

        SimulationDirectoriesFactory simulationDirectoriesFactory = new SimulationDirectoriesFactory(mlopsProperties);

        DataOpsService service = new DataOpsService(
                compositionRepo,
                simulationDirectoriesFactory,
                minioStorageService,
                runRepo,
                subRunRepo);

        return service.scheduleExploration(nominalCompositionName, request);

    }

}
