package com.paola.spacephotoapp.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * This utility class provides helper methods for displaying various types of dialog boxes
 * to the user. These dialogs are common ways to show messages (errors, info) or
 * allow users to select files in a graphical user interface (GUI) application.
 */
public class DialogUtils {
    /**
     * Displays a standard error message dialog.
     * @param parent The parent component over which the dialog will appear (e.g., the main window).
     * @param message The error message to display.
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a standard informational message dialog.
     * @param parent The parent component over which the dialog will appear.
     * @param message The informational message to display.
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays a custom dialog box with plain message type, allowing any Swing component as its content.
     * This is useful for more complex dialogs than simple text messages.
     * @param parent The parent component over which the dialog will appear.
     * @param content The Swing component to display inside the dialog.
     * @param title The title of the dialog window.
     */
    public static void showPlainDialog(Component parent, Component content, String title) {
        JOptionPane.showMessageDialog(parent, content, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Opens a file chooser dialog, allowing the user to select a single image file.
     * @param parent The parent component over which the file chooser will appear.
     * @return The selected `File` object, or `null` if the user cancels the selection.
     */
    public static File chooseImageFile(Component parent) {
        JFileChooser chooser = new JFileChooser(); // Creates a new file chooser.
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Configures it to allow selecting only files, not directories.
        int result = chooser.showOpenDialog(parent); // Shows the "Open" file dialog.
        // Returns the selected file if the user clicked "Approve" (e.g., "Open"), otherwise returns null.
        return (result == JFileChooser.APPROVE_OPTION) ? chooser.getSelectedFile() : null;
    }
}