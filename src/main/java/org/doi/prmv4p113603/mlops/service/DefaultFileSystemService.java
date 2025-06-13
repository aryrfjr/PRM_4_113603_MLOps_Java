package org.doi.prmv4p113603.mlops.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

@Service
public class DefaultFileSystemService implements FileSystemService {

    @Override
    public boolean pathExists(String path) {
        return Files.exists(Path.of(path));
    }

    @Override
    public DirectoryStream<Path> listFiles(String dirPath) throws IOException {
        return Files.newDirectoryStream(Path.of(dirPath));
    }

    @Override
    public boolean isRegularFile(String path) {
        return Files.isRegularFile(Path.of(path));
    }

    @Override
    public int getFileSize(String path) throws IOException {
        return (int) Files.size(Path.of(path));
    }

    @Override
    public String calculateSHA256(String path) throws IOException {
        try (InputStream fis = Files.newInputStream(Path.of(path))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fis.readAllBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    @Override
    public String join(String... parts) {
        return Path.of(parts[0], Arrays.copyOfRange(parts, 1, parts.length)).toString();
    }
}
