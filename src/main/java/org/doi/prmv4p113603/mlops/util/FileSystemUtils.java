package org.doi.prmv4p113603.mlops.util;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Utility class for file system operations, mimicking Python's pathlib.Path.
 */
public class FileSystemUtils {

    /**
     * Checks if the path exists on the file system.
     */
    public static boolean pathExists(String pathStr) {
        return Files.exists(Paths.get(pathStr));
    }

    /**
     * Resolves a child path relative to a parent.
     */
    public static String resolve(String parent, String child) {
        return Paths.get(parent).resolve(child).toString();
    }

    /**
     * Joins multiple path segments safely.
     */
    public static String join(String first, String... others) {
        return Paths.get(first, others).toString();
    }

    /**
     * Lists files in a directory.
     */
    public static DirectoryStream<Path> listFiles(String dir) throws IOException {
        return Files.newDirectoryStream(Paths.get(dir));
    }

    /**
     * Gets file size in bytes.
     */
    public static long getFileSize(String path) throws IOException {
        return Files.size(Paths.get(path));
    }

    /**
     * Checks if a file is regular (not a directory).
     */
    public static boolean isRegularFile(String path) {
        return Files.isRegularFile(Paths.get(path));
    }

    /**
     * Calculates the SHA-256 checksum of a file.
     *
     * @param filePath the file to hash
     * @return the SHA-256 hash string
     * @throws IOException if file can't be read
     */
    public static String calculateSHA256(String filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = Files.readAllBytes(Paths.get(filePath));
            byte[] hash = digest.digest(buffer);
            return byteArrayToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static String byteArrayToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
