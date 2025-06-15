package com.paola.spacephotoapp.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

public class LogUtils {

    // Private constructor ensures no one else can create a DatabaseService
    private LogUtils() {
        // Utility class - prevent instantiation, it will be used as static
    }

    public static void logFailedDownloads(Map<String, String> failedDownloads) {
        if (failedDownloads == null || failedDownloads.isEmpty()) return;

        Path logDir = Paths.get("logs");
        Path logFile = logDir.resolve("failed_downloads.txt");

        try {
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(logFile,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                for (Map.Entry<String, String> entry : failedDownloads.entrySet()) {
                    writer.write("GUID: " + entry.getKey() + " - " + entry.getValue());
                    writer.newLine();
                }

                System.out.println("Logged failed downloads to " + logFile.toAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Failed to write log file: " + e.getMessage());
        }
    }
}
