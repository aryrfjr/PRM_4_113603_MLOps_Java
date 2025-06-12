package org.doi.prmv4p113603.mlops.util;

import java.io.IOException;
import java.nio.file.*;

/**
 * Utility class for file system operations; mimicking Python's pathlib.Path.exists().
 */
public class FileSystemUtils {

    /**
     * Checks if the path exists on the file system.
     *
     * @param pathStr the path as a string
     * @return true if the path exists and is a file or directory
     */
    public static boolean pathExists(String pathStr) {
        Path path = Paths.get(pathStr);
        return Files.exists(path);
    }

    /**
     * Resolves a child path relative to a parent.
     *
     * @param parent base directory
     * @param child  child segment or subdirectory
     * @return the resolved path as a string
     */
    public static String resolve(String parent, String child) {
        return Paths.get(parent).resolve(child).toString();
    }

    /**
     * Joins multiple path segments safely.
     *
     * @param first  the first segment
     * @param others the remaining segments
     * @return the joined path string
     */
    public static String join(String first, String... others) {
        return Paths.get(first, others).toString();
    }

    /**
     * Lists files in a directory.
     *
     * @param dir the directory to list
     * @return a DirectoryStream of file paths
     * @throws IOException if access fails
     */
    public static DirectoryStream<Path> listFiles(String dir) throws IOException {
        return Files.newDirectoryStream(Paths.get(dir));
    }

    /**
     * Gets file size in bytes.
     *
     * @param path path to the file
     * @return file size in bytes
     * @throws IOException if the file is not readable
     */
    public static long getFileSize(String path) throws IOException {
        return Files.size(Paths.get(path));
    }

    /**
     * Checks if a file is regular (not a directory).
     *
     * @param path file path string
     * @return true if the path is a regular file
     */
    public static boolean isRegularFile(String path) {
        return Files.isRegularFile(Paths.get(path));
    }
}
