package org.doi.prmv4p113603.mlops.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;

public interface FileSystemService {
    boolean pathExists(String path);
    DirectoryStream<Path> listFiles(String dirPath) throws IOException;
    boolean isRegularFile(String path);
    int getFileSize(String path) throws IOException;
    String calculateSHA256(String path) throws IOException;
    String join(String... parts);
}
