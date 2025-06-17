package com.paola.spacephotoapp.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

/**
 * This utility class provides methods for logging specific events, such as failed downloads.
 * It's designed as a **static utility class**, meaning all its methods are `static`
 * and you don't create an object of this class. It focuses solely on logging operations.
 */
public class LogUtils {

    // --- Private Constructor ---
    // This private constructor prevents anyone from accidentally creating an instance of LogUtils.
    // Since all its methods are static, there's no need to create objects of this class.
    private LogUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Logs details of failed downloads to a text file named "failed_downloads.txt"
     * located in a "logs" subfolder. Each entry includes the GUID and the reason for failure.
     * If the log directory or file does not exist, they will be created.
     *
     * @param failedDownloads A `Map` where keys are the GUIDs of failed downloads
     * and values are the corresponding error messages.
     */
    public static void logFailedDownloads(Map<String, String> failedDownloads) {
        // Exits early if there are no failed downloads to log.
        if (failedDownloads == null || failedDownloads.isEmpty()) return;

        Path logDir = Paths.get("logs"); // Defines the path for the 'logs' directory.
        Path logFile = logDir.resolve("failed_downloads.txt"); // Defines the full path to the log file.

        try {
            // Checks if the 'logs' directory exists, and creates it along with any necessary parent directories.
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            // Opens a BufferedWriter to write to the log file.
            // StandardOpenOption.CREATE: Creates the file if it doesn't exist.
            // StandardOpenOption.APPEND: Appends new content to the end of the file if it already exists.
            try (BufferedWriter writer = Files.newBufferedWriter(logFile,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                // Iterates through each failed download entry in the map.
                for (Map.Entry<String, String> entry : failedDownloads.entrySet()) {
                    // Writes the GUID and error message to the log file, followed by a new line.
                    writer.write("GUID: " + entry.getKey() + " - " + entry.getValue());
                    writer.newLine();
                }

                // Informs the console that the logs have been written successfully, showing the absolute file path.
                System.out.println("Logged failed downloads to " + logFile.toAbsolutePath());
            }

        } catch (IOException e) {
            // Catches any errors that occur during file system operations (e.g., creating directories, writing to file).
            System.err.println("Failed to write log file: " + e.getMessage());
        }
    }
}