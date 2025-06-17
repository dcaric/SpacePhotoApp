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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// This application uses the standard Java Swing framework for its graphical user interface.

public class MainFrame extends JFrame {
    private JLabel titleLabel;
    private JLabel imageLabel;
    private JTextArea descriptionArea;
    private JScrollPane scrollPanel;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel dateLabel;
    private JButton viewFullImageButton;
    private JComboBox<NewsCategory> categoryComboBox;
    private Image fullSizeImage;

    private JPanel dropPanel;
    private JLabel dropLabel;

    private NewsController controller;
    private enum ViewMode { TITLE_ONLY, TITLE_DESC, FULL }
    private ViewMode currentViewMode = ViewMode.FULL;

    private JSplitPane splitPane;

    private JProgressBar loadingProgressBar; // Declared here
    private JButton refreshButton; // Declare refreshButton at class level to access it in setUIEnabled
    private JSpinner spinnerLoadCount; // Declare spinnerLoadCount at class level to access it in setUIEnabled

    JPanel navPanel;

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenuItem statsItem = new JMenuItem("Stats");
        statsItem.addActionListener(e -> showStatsWindow());
        fileMenu.add(statsItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> DialogUtils.showInfo(this, "NASA News Viewer v1.0"));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void showStatsWindow() {
        JFrame statsFrame = new JFrame("Statistics");
        statsFrame.setSize(300, 200);
        statsFrame.setLocationRelativeTo(this);

        JLabel statsLabel = new JLabel("News loaded: " + controller.getNewsList().size(), SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 16));

        statsFrame.add(statsLabel);
        statsFrame.setVisible(true);
    }

    public MainFrame(List<NewsRelease> newsList) {
        this.controller = new NewsController(newsList);

        setTitle("NASA News Viewer");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();

        updateDisplay();
        setupMenuBar();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        titleLabel = new JLabel("", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        dateLabel = new JLabel("", SwingConstants.CENTER);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(dateLabel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(150, 100));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(imageLabel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        dropPanel = new JPanel();
        dropPanel.setPreferredSize(new Dimension(600, 400));
        dropPanel.setBorder(BorderFactory.createTitledBorder("Drop Image Here"));
        dropPanel.setMinimumSize(new Dimension(50, 50));
        dropLabel = new JLabel("Drag image here", SwingConstants.CENTER);
        dropLabel.setVerticalAlignment(SwingConstants.CENTER);
        dropPanel.setLayout(new BorderLayout());
        dropPanel.add(dropLabel, BorderLayout.CENTER);

        dropPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.imageFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    Image img = (Image) support.getTransferable().getTransferData(DataFlavor.imageFlavor);
                    if (img != null) {
                        fullSizeImage = img;
                        resizeDropLabelImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        dropPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                resizeDropLabelImage();
            }
        });

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dropPanel, scrollPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(500);
        add(splitPane, BorderLayout.CENTER);

        imageLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                Icon icon = imageLabel.getIcon();
                if (icon instanceof ImageIcon) {
                    Image image = ((ImageIcon) icon).getImage();

                    imageLabel.setTransferHandler(new TransferHandler() {
                        @Override
                        protected Transferable createTransferable(JComponent c) {
                            return new Transferable() {
                                @Override
                                public DataFlavor[] getTransferDataFlavors() {
                                    return new DataFlavor[]{DataFlavor.imageFlavor};
                                }

                                @Override
                                public boolean isDataFlavorSupported(DataFlavor flavor) {
                                    return DataFlavor.imageFlavor.equals(flavor);
                                }

                                @Override
                                public Object getTransferData(DataFlavor flavor) {
                                    return fullSizeImage;
                                }
                            };
                        }

                        @Override
                        public int getSourceActions(JComponent c) {
                            return COPY;
                        }
                    });

                    imageLabel.getTransferHandler().exportAsDrag(imageLabel, e, TransferHandler.COPY);
                }
            }
        });

        descriptionArea = new JTextArea();
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        scrollPanel = new JScrollPane(descriptionArea);
        scrollPanel.setPreferredSize(new Dimension(350, 400));
        scrollPanel.setMinimumSize(new Dimension(50, 50));

        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        prevButton.addActionListener(e -> navigate(-1));
        nextButton.addActionListener(e -> navigate(1));
        viewFullImageButton = new JButton("View Full Image");
        viewFullImageButton.addActionListener(e -> showFullImageDialog());

        navPanel = new JPanel(new FlowLayout());
        navPanel.add(prevButton);
        navPanel.add(nextButton);
        navPanel.add(viewFullImageButton);

        add(navPanel, BorderLayout.SOUTH);

        categoryComboBox = new JComboBox<>(NewsCategory.values());
        categoryComboBox.insertItemAt(null, 0);
        categoryComboBox.setSelectedIndex(0);
        categoryComboBox.addActionListener(e -> {
            NewsCategory selected = (NewsCategory) categoryComboBox.getSelectedItem();
            controller.filterByCategory(selected);
            updateDisplay();
        });

        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.add(new JLabel("Filter:"));
        comboPanel.add(categoryComboBox);

        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        imagePanel.add(imageLabel);

        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.add(comboPanel, BorderLayout.WEST);
        filterRow.add(imagePanel, BorderLayout.EAST);

        topPanel.add(filterRow, BorderLayout.CENTER);

        JRadioButton rbTitleOnly = new JRadioButton("Title Only");
        JRadioButton rbTitleDesc = new JRadioButton("Title + Desc");
        JRadioButton rbFull = new JRadioButton("Full");

        ButtonGroup viewModeGroup = new ButtonGroup();
        viewModeGroup.add(rbTitleOnly);
        viewModeGroup.add(rbTitleDesc);
        viewModeGroup.add(rbFull);
        rbFull.setSelected(true);

        rbTitleOnly.addActionListener(e -> {
            currentViewMode = ViewMode.TITLE_ONLY;
            updateDisplay();
        });
        rbTitleDesc.addActionListener(e -> {
            currentViewMode = ViewMode.TITLE_DESC;
            updateDisplay();
        });
        rbFull.addActionListener(e -> {
            currentViewMode = ViewMode.FULL;
            updateDisplay();
        });

        JPanel viewModePanel = new JPanel();
        viewModePanel.setBorder(BorderFactory.createTitledBorder("View Mode"));
        viewModePanel.add(rbTitleOnly);
        viewModePanel.add(rbTitleDesc);
        viewModePanel.add(rbFull);

        topPanel.add(viewModePanel, BorderLayout.EAST);

        refreshButton = new JButton("Refresh Feed"); // Initialize refreshButton
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 100, 1);
        spinnerLoadCount = new JSpinner(spinnerModel); // Initialize spinnerLoadCount
        JLabel spinnerLabel = new JLabel("Items to load:");

        refreshButton.addActionListener(e -> {
            startLoading(); // Activate the loading indicator.
            setUIEnabled(false); // Disable UI elements during loading.

            int count = (int) spinnerLoadCount.getValue();
            // Perform the refresh operation in a background thread to keep UI responsive.
            new Thread(() -> {
                RssParser parser = new RssParser();
                parser.parse(count);

                NewsRepository repo = new NewsRepository();
                List<NewsRelease> updatedList = repo.findAll();
                controller.setNewsList(updatedList);

                // Update UI on the Event Dispatch Thread (EDT) after background task is done.
                SwingUtilities.invokeLater(() -> {
                    stopLoading(); // Deactivate the loading indicator.
                    setUIEnabled(true); // Re-enable UI elements.
                    updateDisplay(); // Refresh UI with new data.
                });
            }).start();
        });

        navPanel.add(refreshButton);
        navPanel.add(spinnerLabel);
        navPanel.add(spinnerLoadCount);

        // --- Loading Progress Bar Setup ---
        // Initialize the JProgressBar for loading indication.
        loadingProgressBar = new JProgressBar();
        loadingProgressBar.setIndeterminate(true); // Enable indeterminate mode for animation.
        loadingProgressBar.setStringPainted(false); // Hide percentage text.
        loadingProgressBar.setVisible(false);      // Hidden by default.
        loadingProgressBar.setPreferredSize(new Dimension(100, 20)); // Give it a reasonable size.
        navPanel.add(loadingProgressBar); // Add the progress bar to the navigation panel.
    }

    private void resizeDropLabelImage() {
        if (fullSizeImage == null || dropLabel.getWidth() == 0 || dropLabel.getHeight() == 0)
            return;

        Image scaled = fullSizeImage.getScaledInstance(
                dropLabel.getWidth(),
                dropLabel.getHeight(),
                Image.SCALE_SMOOTH
        );

        dropLabel.setIcon(new ImageIcon(scaled));
        dropLabel.setText("");
    }

    private void navigate(int offset) {
        if (offset > 0 && controller.hasNext()) {
            controller.nextNews();
        } else if (offset < 0 && controller.hasPrevious()) {
            controller.previousNews();
        }
        updateDisplay();
    }

    private void updateDisplay() {
        controller.getOptionalCurrentNews().ifPresent(news -> {
            titleLabel.setText(news.getTitle());
            dateLabel.setText("Published: " + news.getPubDate());

            boolean showDesc = currentViewMode != ViewMode.TITLE_ONLY;
            descriptionArea.setText(showDesc ? news.getDescription() : "");
            scrollPanel.setVisible(showDesc);

            boolean showImage = currentViewMode == ViewMode.FULL;
            imageLabel.setVisible(showImage);

            remove(splitPane);

            if (currentViewMode == ViewMode.FULL) {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dropPanel, scrollPanel);
            } else if (currentViewMode == ViewMode.TITLE_DESC) {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), scrollPanel);
            } else {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel());
            }

            splitPane.setResizeWeight(0.5);
            splitPane.setDividerLocation(500);
            add(splitPane, BorderLayout.CENTER);
            revalidate();
            repaint();

            ImageIcon icon = null;
            try {
                String path = news.getLocalImagePath();
                if (path != null) {
                    BufferedImage img = ImageIO.read(new File(path));
                    fullSizeImage = img;
                    if (img != null) {
                        Image scaled = img.getScaledInstance(150, 100, Image.SCALE_SMOOTH);
                        icon = new ImageIcon(scaled);
                    }
                }
            } catch (Exception ignored) {}

            imageLabel.setIcon(icon);
        });
    }

    private void showFullImageDialog() {
        controller.getOptionalCurrentNews().ifPresent(news -> {
            String path = news.getLocalImagePath();
            if (path == null) {
                DialogUtils.showInfo(this, "No image available.");
                return;
            }

            try {
                BufferedImage img = ImageIO.read(new File(path));
                if (img != null) {
                    ImageIcon fullIcon = new ImageIcon(img);
                    JLabel fullImageLabel = new JLabel(fullIcon);
                    JScrollPane scrollPane = new JScrollPane(fullImageLabel);
                    scrollPane.setPreferredSize(new Dimension(1000, 700));

                    DialogUtils.showPlainDialog(this, scrollPane, "Full Image");

                } else {
                    throw new Exception("Image load failed.");
                }
            } catch (Exception e) {
                DialogUtils.showError(this, "Failed to load full image.");
            }
        });
    }

    /**
     * Starts the loading animation by making the JProgressBar visible and active.
     * Also forces the navigation panel to re-layout itself.
     */
    private void startLoading() {
        loadingProgressBar.setVisible(true);
        loadingProgressBar.setIndeterminate(true);
        // Important: Revalidate and repaint the parent container to ensure visibility change is reflected.
        SwingUtilities.invokeLater(() -> {
            navPanel.revalidate();
            navPanel.repaint();
        });
    }

    /**
     * Stops the loading animation by hiding the JProgressBar and deactivating its indeterminate mode.
     * Also forces the navigation panel to re-layout itself.
     */
    private void stopLoading() {
        loadingProgressBar.setVisible(false);
        loadingProgressBar.setIndeterminate(false);
        // Important: Revalidate and repaint the parent container to ensure visibility change is reflected.
        SwingUtilities.invokeLater(() -> {
            navPanel.revalidate();
            navPanel.repaint();
        });
    }

    /**
     * Enables or disables key UI elements to prevent user interaction during background operations.
     * @param enabled `true` to enable, `false` to disable.
     */
    private void setUIEnabled(boolean enabled) {
        refreshButton.setEnabled(enabled);
        prevButton.setEnabled(enabled);
        nextButton.setEnabled(enabled);
        viewFullImageButton.setEnabled(enabled);
        categoryComboBox.setEnabled(enabled);
        spinnerLoadCount.setEnabled(enabled); // Also disable the spinner during loading.
    }
}