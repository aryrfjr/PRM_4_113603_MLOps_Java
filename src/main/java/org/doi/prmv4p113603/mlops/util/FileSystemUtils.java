package org.doi.prmv4p113603.mlops.util;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class FileSystemUtils {

    public static boolean pathExists(String path) {
        return Files.exists(Path.of(path));
    }

    public static DirectoryStream<Path> listFiles(String dirPath) throws IOException {
        return Files.newDirectoryStream(Path.of(dirPath));
    }

    public static boolean isRegularFile(String path) {
        return Files.isRegularFile(Path.of(path));
    }

    public static int getFileSize(String path) throws IOException {
        return (int) Files.size(Path.of(path));
    }

    public static String calculateSHA256(String path) throws IOException {

        try (InputStream fis = Files.newInputStream(Path.of(path))) {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fis.readAllBytes());
            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }

    }

    public static String join(String... parts) {
        System.out.println("ARYLOG =======>>>> "+parts);
        return Path.of(parts[0], Arrays.copyOfRange(parts, 1, parts.length)).toString();
    }

}
