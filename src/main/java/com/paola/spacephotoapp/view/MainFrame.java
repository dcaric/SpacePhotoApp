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

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// This application uses the standard Java Swing framework.

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
    private Image fullSizeImage; // Store full-res image separately

    // for drag and droping image into JPanel
    private JPanel dropPanel;
    private JLabel dropLabel;

    private NewsController controller;

    private enum ViewMode { TITLE_ONLY, TITLE_DESC, FULL }
    private ViewMode currentViewMode = ViewMode.FULL;

    private JSplitPane splitPane;


    // Setup menu in the bar
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
        statsFrame.setLocationRelativeTo(this); // centers relative to main

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

        // setup
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
        imageLabel.setPreferredSize(new Dimension(150, 100)); // was 600x400

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(imageLabel, BorderLayout.CENTER);
        // centerPanel.add(viewFullImageButton, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Drop panel setup
        dropPanel = new JPanel();
        dropPanel.setPreferredSize(new Dimension(600, 400));
        dropPanel.setBorder(BorderFactory.createTitledBorder("Drop Image Here"));
        dropPanel.setMinimumSize(new Dimension(50, 50));

        dropLabel = new JLabel("Drag image here", SwingConstants.CENTER);
        dropLabel.setVerticalAlignment(SwingConstants.CENTER);
        dropPanel.setLayout(new BorderLayout());
        dropPanel.add(dropLabel, BorderLayout.CENTER);

        // Enable drop
        dropPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                System.out.println("Available flavors:");
                for (DataFlavor flavor : support.getDataFlavors()) {
                    System.out.println("  - " + flavor);
                }
                return true; // Accept anything temporarily for testing
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;

                try {
                    // Get the dropped image
                    Image img = (Image) support.getTransferable().getTransferData(DataFlavor.imageFlavor);

                    if (img != null) {
                        resizeDropLabelImage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }

        });

        dropPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent e) {
                resizeDropLabelImage();
            }
        });


        // Add the panel to the layout
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dropPanel, scrollPanel);
        splitPane.setResizeWeight(0.5); // Optional: 50/50 layout
        splitPane.setDividerLocation(500); // Optional: default split
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

        //add(scrollPanel, BorderLayout.EAST);

        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        prevButton.addActionListener(e -> navigate(-1));
        nextButton.addActionListener(e -> navigate(1));
        viewFullImageButton = new JButton("View Full Image");
        viewFullImageButton.addActionListener(e -> showFullImageDialog());

        JPanel navPanel = new JPanel(new FlowLayout());
        navPanel.add(prevButton);
        navPanel.add(nextButton);
        navPanel.add(viewFullImageButton);


        add(navPanel, BorderLayout.SOUTH);


        categoryComboBox = new JComboBox<>(NewsCategory.values());
        categoryComboBox.insertItemAt(null, 0); // for "All categories"
        categoryComboBox.setSelectedIndex(0);
        categoryComboBox.addActionListener(e -> {
            NewsCategory selected = (NewsCategory) categoryComboBox.getSelectedItem();
            controller.filterByCategory(selected); // you’ll add this method
            updateDisplay();
        });

        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.add(new JLabel("Filter:"));
        comboPanel.add(categoryComboBox);

        // Wrap the image in a panel and align right
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        imagePanel.add(imageLabel);

        // New horizontal panel that holds both filter + image
        JPanel filterRow = new JPanel(new BorderLayout());
        filterRow.add(comboPanel, BorderLayout.WEST);
        filterRow.add(imagePanel, BorderLayout.EAST);

        topPanel.add(filterRow, BorderLayout.CENTER);



        // radio buttons for menu grouping
        JRadioButton rbTitleOnly = new JRadioButton("Title Only");
        JRadioButton rbTitleDesc = new JRadioButton("Title + Desc");
        JRadioButton rbFull = new JRadioButton("Full");

        // defined ButtonGroup
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

        // Add to topPanel or wherever appropriate
        topPanel.add(viewModePanel, BorderLayout.EAST);




        // refresh button
        JButton refreshButton = new JButton("Refresh Feed");

        // Spinner to choose number of news items to load
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 100, 1);
        JSpinner spinnerLoadCount = new JSpinner(spinnerModel);
        JLabel spinnerLabel = new JLabel("Items to load:");

        refreshButton.addActionListener(e -> {
            int count = (int) spinnerLoadCount.getValue(); // Get selected number
            RssParser parser = new RssParser();
            parser.parse(count); // This should insert only new news

            // Reload updated list
            NewsRepository repo = new NewsRepository();
            List<NewsRelease> updatedList = repo.findAll();
            controller.setNewsList(updatedList);
            updateDisplay();
        });


        navPanel.add(refreshButton);
        navPanel.add(spinnerLabel);
        navPanel.add(spinnerLoadCount);

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
        dropLabel.setText(""); // remove text placeholder
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

            // Description visibility
            boolean showDesc = currentViewMode != ViewMode.TITLE_ONLY;
            descriptionArea.setText(showDesc ? news.getDescription() : "");
            //descriptionArea.setVisible(showDesc);
            scrollPanel.setVisible(showDesc);


            // Image visibility
            boolean showImage = currentViewMode == ViewMode.FULL;
            imageLabel.setVisible(showImage);



            // Dynamically replace the center split based on view mode
            remove(splitPane); // remove old pane

            if (currentViewMode == ViewMode.FULL) {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dropPanel, scrollPanel);
            } else if (currentViewMode == ViewMode.TITLE_DESC) {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), scrollPanel); // No dropPanel
            } else {
                splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel()); // Both hidden
            }

            splitPane.setResizeWeight(0.5);
            splitPane.setDividerLocation(500);
            add(splitPane, BorderLayout.CENTER);
            revalidate();
            repaint();


            // Load and scale image
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
                // Checked exceptions
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
}
