// (View) handles the UI.
package com.paola.spacephotoapp.view;

import com.paola.spacephotoapp.controller.NewsController;
import com.paola.spacephotoapp.domain.model.NewsCategory;
import com.paola.spacephotoapp.repository.NewsRepository;
import com.paola.spacephotoapp.parser.RssParser;
import com.paola.spacephotoapp.domain.model.NewsRelease;
import com.paola.spacephotoapp.util.DialogUtils;

import java.awt.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

import javax.swing.*;
//import javax.swing.TransferHandler; 
//import javax.swing.BorderFactory;
//import javax.swing.JComponent;

import java.awt.datatransfer.Transferable; // For drag-and-drop functionality.
import java.awt.datatransfer.DataFlavor;   // For drag-and-drop functionality.

import java.awt.event.MouseAdapter; // For handling mouse events (like clicks).
import java.awt.event.MouseEvent;   // For handling mouse events.

// This application uses the standard Java Swing framework for its graphical user interface.

public class MainFrame extends JFrame {
    // --- UI Components Declaration ---
    // These are the Swing components (labels, text areas, buttons, etc.) that make up the application's window.
    private JLabel titleLabel;
    private JLabel imageLabel;
    private JTextArea descriptionArea;
    private JScrollPane scrollPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel dateLabel;
    private JButton viewFullImageButton;
    private JComboBox<NewsCategory> categoryComboBox;
    private Image fullSizeImage; // Stores the full-resolution image for display.

    // --- Drag and Drop Components ---
    // Components specifically for the drag-and-drop feature.
    private JPanel dropPanel;
    private JLabel dropLabel;

    // --- Controller and View Mode ---
    // The controller object manages news data and navigation.
    // ViewMode enum defines different ways to display news content (title only, title+desc, full).
    private NewsController controller;
    private enum ViewMode { TITLE_ONLY, TITLE_DESC, FULL }
    private ViewMode currentViewMode = ViewMode.FULL;

    // --- Layout Components ---
    // A JSplitPane allows dividing the window into two resizable areas.
    private JSplitPane splitPane;


    /**
     * Sets up the application's menu bar at the top of the window.
     * It includes "File" and "Help" menus with standard options like Exit and About.
     */
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar(); // Creates the menu bar.

        // --- File Menu Setup ---
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0)); // Exits the application when clicked.
        fileMenu.add(exitItem);

        JMenuItem statsItem = new JMenuItem("Stats");
        statsItem.addActionListener(e -> showStatsWindow()); // Shows a statistics window when clicked.
        fileMenu.add(statsItem);

        // --- Help Menu Setup ---
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        // Shows an informational dialog box when "About" is clicked.
        aboutItem.addActionListener(e -> DialogUtils.showInfo(this, "NASA News Viewer v1.0"));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar); // Sets the created menu bar for the frame.
    }

    /**
     * Displays a small separate window showing application statistics,
     * such as the total number of news items loaded.
     */
    private void showStatsWindow() {
        JFrame statsFrame = new JFrame("Statistics"); // Creates a new frame for statistics.
        statsFrame.setSize(300, 200);
        statsFrame.setLocationRelativeTo(this); // Centers the stats window relative to the main window.

        JLabel statsLabel = new JLabel("News loaded: " + controller.getNewsList().size(), SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Sets font for the label.

        statsFrame.add(statsLabel); // Adds the label to the stats frame.
        statsFrame.setVisible(true); // Makes the stats window visible.
    }

    /**
     * Constructor for the MainFrame. This is where the application's main window is initialized.
     * It sets up the window's basic properties and lays out its components.
     *
     * @param newsList The initial list of news articles to display.
     */
    public MainFrame(List<NewsRelease> newsList) {
        this.controller = new NewsController(newsList); // Initializes the NewsController with the news data.

        // --- Frame Basic Setup ---
        setTitle("NASA News Viewer"); // Sets the window title.
        setSize(1000, 700);            // Sets the window size.
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Defines what happens when the close button is clicked (exits application).
        setLocationRelativeTo(null);   // Centers the window on the screen.

        initComponents(); // Calls a method to create and arrange all UI components.

        // --- Initial Display Setup ---
        updateDisplay();  // Populates the UI with the first news item.
        setupMenuBar();   // Adds the menu bar to the frame.
    }

    /**
     * Initializes and arranges all the graphical user interface components within the MainFrame.
     * This method sets up the layout, creates labels, buttons, text areas, and configures their properties.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Sets the main layout manager for the frame.

        // --- Title and Date Labels Setup ---
        titleLabel = new JLabel("", SwingConstants.CENTER); // Label for news title.
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        dateLabel = new JLabel("", SwingConstants.CENTER);   // Label for news publication date.
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel topPanel = new JPanel(new BorderLayout()); // Panel to hold title and date.
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(dateLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH); // Adds the top panel to the main frame's top.

        imageLabel = new JLabel("", SwingConstants.CENTER); // Label to display news image.
        imageLabel.setPreferredSize(new Dimension(150, 100));

        JPanel centerPanel = new JPanel(new BorderLayout()); // Panel for the image.
        centerPanel.add(imageLabel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER); // Adds the image panel to the main frame's center.

        // --- Drag and Drop Panel Setup ---
        // Configures a special panel where users can drag and drop images.
        dropPanel = new JPanel();
        dropPanel.setPreferredSize(new Dimension(600, 400));
        dropPanel.setBorder(BorderFactory.createTitledBorder("Drop Image Here")); // Adds a border and title.
        dropPanel.setMinimumSize(new Dimension(50, 50));
        dropLabel = new JLabel("Drag image here", SwingConstants.CENTER);
        dropLabel.setVerticalAlignment(SwingConstants.CENTER);
        dropPanel.setLayout(new BorderLayout());
        dropPanel.add(dropLabel, BorderLayout.CENTER);

        // --- Enable Drag and Drop Functionality ---
        // Sets up how the dropPanel handles incoming data (dragged items).
        dropPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                // This method determines if the dropped data type is acceptable.
                // For simplicity, it temporarily accepts anything for testing.
                System.out.println("Available flavors:");
                for (DataFlavor flavor : support.getDataFlavors()) {
                    System.out.println("  - " + flavor);
                }
                return true;
            }

            @Override
            public boolean importData(TransferSupport support) {
                // This method is called when data is actually dropped.
                // It tries to get the dropped image and updates the display.
                if (!canImport(support)) return false;
                try {
                    Image img = (Image) support.getTransferable().getTransferData(DataFlavor.imageFlavor);
                    if (img != null) {
                        resizeDropLabelImage(); // Resizes the image to fit the drop label.
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false; // Indicates whether the import was successful.
            }
        });

        // --- Component Resize Listener for Drag and Drop Panel ---
        // Ensures the dropped image is resized whenever the drop panel changes its size.
        dropPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                resizeDropLabelImage();
            }
        });


        // --- Main Content Area with Split Pane ---
        // Uses a JSplitPane to divide the main content area horizontally,
        // allowing the drop panel and description area to be resized by the user.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dropPanel, scrollPanel);
        splitPane.setResizeWeight(0.5); // Gives both sides equal initial weight.
        splitPane.setDividerLocation(500); // Sets initial position of the divider.
        add(splitPane, BorderLayout.CENTER); // Adds the split pane to the center of the main frame.

        // --- Image Label Drag Functionality ---
        // Allows the image displayed on the imageLabel to be dragged out of the application.
        imageLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Icon icon = imageLabel.getIcon();
                if (icon instanceof ImageIcon) {
                    Image image = ((ImageIcon) icon).getImage();

                    // Sets up a TransferHandler to create a transferable image object when dragged.
                    imageLabel.setTransferHandler(new TransferHandler() {
                        @Override
                        protected Transferable createTransferable(JComponent c) {
                            return new Transferable() {
                                @Override
                                public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{DataFlavor.imageFlavor}; }
                                @Override
                                public boolean isDataFlavorSupported(DataFlavor flavor) { return DataFlavor.imageFlavor.equals(flavor); }
                                @Override
                                public Object getTransferData(DataFlavor flavor) { return fullSizeImage; } // Provides the full-size image.
                            };
                        }
                        @Override
                        public int getSourceActions(JComponent c) { return COPY; } // Allows copying the image.
                    });
                    // Initiates the drag operation.
                    imageLabel.getTransferHandler().exportAsDrag(imageLabel, e, TransferHandler.COPY);
                }
            }
        });


        // --- Description Area Setup ---
        descriptionArea = new JTextArea();
        descriptionArea.setWrapStyleWord(true); // Wraps words at line breaks.
        descriptionArea.setLineWrap(true);       // Wraps lines within the text area.
        descriptionArea.setEditable(false);      // Makes the text area read-only.
        scrollPanel = new JScrollPane(descriptionArea); // Adds a scroll pane for the description if content is long.
        scrollPanel.setPreferredSize(new Dimension(350, 400));
        scrollPanel.setMinimumSize(new Dimension(50, 50));

        // --- Navigation Buttons Setup ---
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        prevButton.addActionListener(e -> navigate(-1)); // Moves to previous news item when clicked.
        nextButton.addActionListener(e -> navigate(1));  // Moves to next news item when clicked.
        viewFullImageButton = new JButton("View Full Image");
        viewFullImageButton.addActionListener(e -> showFullImageDialog()); // Shows full image in a new dialog.

        JPanel navPanel = new JPanel(new FlowLayout()); // Panel to hold navigation buttons.
        navPanel.add(prevButton);
        navPanel.add(nextButton);
        navPanel.add(viewFullImageButton);

        add(navPanel, BorderLayout.SOUTH); // Adds the navigation panel to the bottom of the main frame.


        // --- Category Filter ComboBox Setup ---
        // Allows users to filter news by categories (e.g., Space, Moon, Mars).
        categoryComboBox = new JComboBox<>(NewsCategory.values()); // Populates dropdown with all NewsCategory enum values.
        categoryComboBox.insertItemAt(null, 0); // Adds a "null" option at the top to represent "All categories."
        categoryComboBox.setSelectedIndex(0); // Selects "All categories" by default.
        categoryComboBox.addActionListener(e -> {
            NewsCategory selected = (NewsCategory) categoryComboBox.getSelectedItem();
            controller.filterByCategory(selected); // Calls the controller to filter news.
            updateDisplay(); // Refreshes the display based on the new filter.
        });

        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel for the category filter.
        comboPanel.add(new JLabel("Filter:"));
        comboPanel.add(categoryComboBox);

        // --- Layout for Filter and Small Image ---
        // Arranges the category filter and the small image label in the top panel.
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Panel for the image label, aligned right.
        imagePanel.add(imageLabel);

        JPanel filterRow = new JPanel(new BorderLayout()); // New panel to combine filter and image.
        filterRow.add(comboPanel, BorderLayout.WEST);
        filterRow.add(imagePanel, BorderLayout.EAST);

        topPanel.add(filterRow, BorderLayout.CENTER); // Adds this row to the center of the top panel.


        // --- View Mode Radio Buttons Setup ---
        // Allows users to switch between different display modes for news content.
        JRadioButton rbTitleOnly = new JRadioButton("Title Only");
        JRadioButton rbTitleDesc = new JRadioButton("Title + Desc");
        JRadioButton rbFull = new JRadioButton("Full");

        ButtonGroup viewModeGroup = new ButtonGroup(); // Groups radio buttons so only one can be selected.
        viewModeGroup.add(rbTitleOnly);
        viewModeGroup.add(rbTitleDesc);
        viewModeGroup.add(rbFull);
        rbFull.setSelected(true); // "Full" mode is selected by default.

        // Action listeners for each radio button to update the view mode and refresh display.
        rbTitleOnly.addActionListener(e -> { currentViewMode = ViewMode.TITLE_ONLY; updateDisplay(); });
        rbTitleDesc.addActionListener(e -> { currentViewMode = ViewMode.TITLE_DESC; updateDisplay(); });
        rbFull.addActionListener(e -> { currentViewMode = ViewMode.FULL; updateDisplay(); });

        JPanel viewModePanel = new JPanel(); // Panel to group view mode radio buttons.
        viewModePanel.setBorder(BorderFactory.createTitledBorder("View Mode"));
        viewModePanel.add(rbTitleOnly);
        viewModePanel.add(rbTitleDesc);
        viewModePanel.add(rbFull);

        topPanel.add(viewModePanel, BorderLayout.EAST); // Adds view mode panel to the right of the top panel.


        // --- Refresh Button and Item Count Spinner ---
        // Allows users to manually refresh the news feed and specify how many new items to load.
        JButton refreshButton = new JButton("Refresh Feed");
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 100, 1); // Model for the spinner (default 10, min 1, max 100, step 1).
        JSpinner spinnerLoadCount = new JSpinner(spinnerModel); // Spinner component.
        JLabel spinnerLabel = new JLabel("Items to load:");

        refreshButton.addActionListener(e -> {
            int count = (int) spinnerLoadCount.getValue(); // Gets the number of items to load from the spinner.
            RssParser parser = new RssParser();
            parser.parse(count); // Calls the RSS parser to fetch and insert new news items.

            // Reloads the entire list of news from the repository to reflect newly added items.
            NewsRepository repo = new NewsRepository();
            List<NewsRelease> updatedList = repo.findAll();
            controller.setNewsList(updatedList); // Updates the controller with the new list.
            updateDisplay(); // Refreshes the UI.
        });

        // Adds the refresh button, spinner label, and spinner to the navigation panel.
        navPanel.add(refreshButton);
        navPanel.add(spinnerLabel);
        navPanel.add(spinnerLoadCount);
    }

    /**
     * Resizes the image displayed in the drop panel (if any) to fit the current size of the drop label.
     * This ensures the image scales correctly when the window is resized.
     */
    private void resizeDropLabelImage() {
        // Only proceed if an image exists and the label has a valid size.
        if (fullSizeImage == null || dropLabel.getWidth() == 0 || dropLabel.getHeight() == 0)
            return;

        // Scales the image smoothly to fit the label's dimensions.
        Image scaled = fullSizeImage.getScaledInstance(
                dropLabel.getWidth(),
                dropLabel.getHeight(),
                Image.SCALE_SMOOTH
        );

        dropLabel.setIcon(new ImageIcon(scaled)); // Sets the scaled image as the label's icon.
        dropLabel.setText(""); // Removes any placeholder text.
    }


    /**
     * Handles navigation between news articles (previous/next).
     * It updates the controller's current news item and then refreshes the display.
     *
     * @param offset The direction to navigate (-1 for previous, 1 for next).
     */
    private void navigate(int offset) {
        if (offset > 0 && controller.hasNext()) {     // If navigating next and a next item exists.
            controller.nextNews();
        } else if (offset < 0 && controller.hasPrevious()) { // If navigating previous and a previous item exists.
            controller.previousNews();
        }
        updateDisplay(); // Refreshes the UI to show the new current news item.
    }

    /**
     * Updates all UI components to display the information of the currently selected news item.
     * It also adjusts the visibility of description and image based on the selected `ViewMode`.
     */
    private void updateDisplay() {
        // Uses Optional to safely get the current news item from the controller.
        // The code inside 'ifPresent' only runs if a news item is available.
        controller.getOptionalCurrentNews().ifPresent(news -> {
            titleLabel.setText(news.getTitle()); // Sets the news title.
            dateLabel.setText("Published: " + news.getPubDate()); // Sets the publication date.

            // --- Description Visibility Control ---
            boolean showDesc = currentViewMode != ViewMode.TITLE_ONLY; // Description is hidden only in TITLE_ONLY mode.
            descriptionArea.setText(showDesc ? news.getDescription() : ""); // Sets description or clears it.
            scrollPanel.setVisible(showDesc); // Controls visibility of the scroll pane containing the description.

            // --- Image Visibility Control ---
            boolean showImage = currentViewMode == ViewMode.FULL; // Image is shown only in FULL mode.
            imageLabel.setVisible(showImage); // Controls visibility of the image label.

            // --- Dynamic SplitPane Adjustment ---
            // Removes the old split pane and creates a new one based on the current view mode.
            // This ensures the layout adapts correctly when changing modes (e.g., hiding image panel).
            remove(splitPane); // Remove the existing split pane.

            if (currentViewMode == ViewMode.FULL) {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dropPanel, scrollPanel); // Shows both image drop area and description.
            } else if (currentViewMode == ViewMode.TITLE_DESC) {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), scrollPanel); // Shows only description, hides image drop area.
            } else { // ViewMode.TITLE_ONLY
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel()); // Hides both image drop area and description.
            }

            splitPane.setResizeWeight(0.5); // Resets resizing weight.
            splitPane.setDividerLocation(500); // Resets divider location.
            add(splitPane, BorderLayout.CENTER); // Adds the newly configured split pane.
            revalidate(); // Re-calculates component layouts.
            repaint();    // Repaints the UI.


            // --- Image Loading and Scaling ---
            // Loads the news item's image from its local path and scales it for display.
            ImageIcon icon = null;
            try {
                String path = news.getLocalImagePath();
                if (path != null) {
                    BufferedImage img = ImageIO.read(new File(path)); // Reads the image file.
                    fullSizeImage = img; // Stores the original full-size image.
                    if (img != null) {
                        // Scales the image down for the smaller imageLabel display.
                        Image scaled = img.getScaledInstance(150, 100, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(scaled); // Creates an ImageIcon from the scaled image.
                    }
                }
            } catch (Exception ignored) {
                // Ignores any errors during image loading (e.g., file not found).
            }

            imageLabel.setIcon(icon); // Sets the scaled image icon on the label.
        });
    }

    /**
     * Displays a dialog box with the full-sized image of the current news item.
     * It handles cases where no image is available or loading fails.
     */
    private void showFullImageDialog() {
        // Safely gets the current news item.
        controller.getOptionalCurrentNews().ifPresent(news -> {
            String path = news.getLocalImagePath();
            if (path == null) {
                DialogUtils.showInfo(this, "No image available."); // Informs user if no image path.
                return;
            }

            try {
                BufferedImage img = ImageIO.read(new File(path)); // Reads the full-size image.
                if (img != null) {
                    ImageIcon fullIcon = new ImageIcon(img);       // Creates an icon from the full image.
                    JLabel fullImageLabel = new JLabel(fullIcon);  // Label to display the full image.
                    JScrollPane scrollPane = new JScrollPane(fullImageLabel); // Adds scroll bars if image is too large.
                    scrollPane.setPreferredSize(new Dimension(1000, 700));

                    // Shows a custom plain dialog with the scrollable full image.
                    DialogUtils.showPlainDialog(this, scrollPane, "Full Image");

                } else {
                    throw new Exception("Image load failed."); // Throws an error if image reading returns null.
                }
            } catch (Exception e) {
                DialogUtils.showError(this, "Failed to load full image."); // Shows an error if image loading fails.
            }
        });
    }
}