package org.doi.prmv4p113603.mlops.util;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

public class MinioUtils {

    public static boolean objectExists(S3Client s3, String bucket, String key) {
        try {
            s3.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404 || "NoSuchKey".equals(e.awsErrorDetails().errorCode())) {
                return false;
            }
            throw e; // rethrow other S3 errors
        }
    }

    public static List<String> listObjects(S3Client s3, String bucket, String prefix) {
        return s3.listObjects(ListObjectsRequest.builder()
                        .bucket(bucket)
                        .prefix(prefix.endsWith("/") ? prefix : prefix + "/")
                        .build())
                .contents()
                .stream()
                .map(S3Object::key)
                .toList();
    }

    public static long getObjectSize(S3Client s3, String bucket, String key) {
        HeadObjectResponse head = s3.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        return head.contentLength();
    }

    public static String calculateSHA256(S3Client s3, String bucket, String key) throws IOException {
        try (InputStream is = s3.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build())) {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(is.readAllBytes());
            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public static String join(String... parts) {
        return Path.of(parts[0], Arrays.copyOfRange(parts, 1, parts.length)).toString();
    }

}
