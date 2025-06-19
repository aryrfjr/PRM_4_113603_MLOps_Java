package org.doi.prmv4p113603.mlops.service;

import org.doi.prmv4p113603.mlops.config.MinioProperties;
import org.doi.prmv4p113603.mlops.util.MinioUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * High-level, reusable S3 interface for domain services. This
 * is the injectable abstraction that other @Service classes will use.
 */
@Service
public class MinioStorageService {

    /*
     * NOTE: Why annotate this class with @Service (not @Component)?
     *
     * - Spring component scanning: makes it a Spring-managed bean, so it can
     *   be injected into other services or controllers.
     *
     * - Semantic meaning: makes clear to developers that This class encapsulates
     *   business or application logic that isn't tied to persistence (like a repository)
     *   or configuration (like a config class).
     *
     * - Integration with AOP (Aspect-Oriented Programming) features, i.e., it can have
     *   access (via Spring dependency injection) to different cross-cutting concerns
     *   (like transaction management, logging, metrics).
     *
     *  - Inject dependencies cleanly via constructor.
     *
     *  - @Service is a specialized form of @Component. Functionally, both work the same.
     *    But by using @Service it:
     *
     *     - Improves code readability/intent (service logic, not just a utility or config).
     *
     *     - Follows domain-driven design alignment, being part of the domain service layer.
     */

    private final S3Client s3Client;
    private final MinioProperties properties;

    public MinioStorageService(S3Client s3Client, MinioProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    /*
     * NOTE: Why not move the content of 'uploadFile' to 'MinioUtils'?
     *
     *  - It uses Spring DI: It needs 'S3Client' and 'MinioProperties', both injected
     *    via Spring. MinioUtils is a static class and can't use Spring injection.
     *
     *  - Business logic: Uploading a file to MinIO is not low-level logic like checking object
     *    size or listing keys — it's a higher-level operation tied to the application.
     *
     *  - Extensible: In future it may be necessary to add: content-type inference, retries,
     *    logging, object metadata, etc. That doesn’t belong in a utility class.
     *
     *  - Mockable in tests: 'MinioStorageService' is a Spring @Service, so it's easy to mock in
     *    unit tests for services/controllers. It is not possible to mock static utility methods easily.
     */

    public void uploadFile(String objectKey, String localPath) {
        Path filePath = Paths.get(localPath);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(objectKey)
                        .build(),
                RequestBody.fromFile(filePath)
        );
    }

    public boolean exists(String objectKey) {
        return MinioUtils.objectExists(s3Client, properties.getBucket(), objectKey);
    }

    public long getSize(String objectKey) {
        return MinioUtils.getObjectSize(s3Client, properties.getBucket(), objectKey);
    }

    public String sha256(String objectKey) throws Exception {
        return MinioUtils.calculateSHA256(s3Client, properties.getBucket(), objectKey);
    }

}
