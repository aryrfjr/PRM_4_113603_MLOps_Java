package org.doi.prmv4p113603.mlops.init;

import jakarta.annotation.PostConstruct;
import org.doi.prmv4p113603.mlops.config.MinioProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * One-time, startup-level bucket assurance.
 */
@Component
public class MinioBucketInitializer {

    private final S3Client s3Client;
    private final MinioProperties minioProperties;

    public MinioBucketInitializer(S3Client s3Client, MinioProperties minioProperties) {
        this.s3Client = s3Client;
        this.minioProperties = minioProperties;
    }

    @PostConstruct
    public void createBucketIfNotExists() {
        String bucketName = minioProperties.getBucket();
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            System.out.println("Bucket already exists: " + bucketName);
        } catch (S3Exception e) {
            if (e.statusCode() == 404 || "NoSuchBucket".equals(e.awsErrorDetails().errorCode())) {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                System.out.println("Created bucket: " + bucketName);
            } else {
                throw e;
            }
        }
    }
}
