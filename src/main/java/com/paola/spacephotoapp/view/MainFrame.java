// (View) handles the UI.

package com.paola.spacephotoapp.view;

import com.paola.spacephotoapp.controller.NewsController;
import com.paola.spacephotoapp.domain.enums.NewsCategory;
import com.paola.spacephotoapp.repository.NewsRepository;
import com.paola.spacephotoapp.parser.RssParser;
import com.paola.spacephotoapp.domain.model.NewsRelease;
import com.paola.spacephotoapp.util.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

import javax.swing.TransferHandler;
import javax.swing.BorderFactory;
import javax.swing.JComponent;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {
    private JLabel titleLabel;
    private JLabel imageLabel;
    private JTextArea descriptionArea;
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

    // Setup menu in the bar
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> DialogUtils.showInfo(this, "NASA News Viewer v1.0"));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
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


        //viewFullImageButton = new JButton("View Full Image");
        //viewFullImageButton.addActionListener(e -> showFullImageDialog());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(imageLabel, BorderLayout.CENTER);
        // centerPanel.add(viewFullImageButton, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // Drop panel setup
        dropPanel = new JPanel();
        dropPanel.setPreferredSize(new Dimension(600, 400));
        dropPanel.setBorder(BorderFactory.createTitledBorder("Drop Image Here"));

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
                        // ✅ Resize to 600x400
                        Image scaledImage = img.getScaledInstance(600, 400, Image.SCALE_SMOOTH);
                        dropLabel.setIcon(new ImageIcon(scaledImage));
                        dropLabel.setText(""); // Remove placeholder text
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            }

        });




        // Add the panel to the layout
        add(dropPanel, BorderLayout.WEST);
        // Wrap dropPanel in a full-width panel
        /*JPanel bottomDropContainer = new JPanel(new BorderLayout());
        bottomDropContainer.add(dropPanel, BorderLayout.CENTER);
        bottomDropContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(bottomDropContainer, BorderLayout.SOUTH);*/

        // Enable drop
        //dropPanel.setTransferHandler(new TransferHandler("icon"));
        //add(dropPanel, BorderLayout.WEST); // or SOUTH, EAST etc.


        descriptionArea = new JTextArea();
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(350, 400));
        add(scrollPane, BorderLayout.EAST);

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
        // Combine drop panel + nav panel
        /*
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(dropPanel, BorderLayout.CENTER);
        southPanel.add(navPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
        */

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



        // refresh button
        JButton refreshButton = new JButton("Refresh Feed");
        refreshButton.addActionListener(e -> {
            RssParser parser = new RssParser();
            parser.parse(); // This should insert only new news

            // Reload updated list
            NewsRepository repo = new NewsRepository();
            List<NewsRelease> updatedList = repo.findAll();
            controller.setNewsList(updatedList);
            updateDisplay();
        });

        navPanel.add(refreshButton);

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
            descriptionArea.setText(news.getDescription());
            dateLabel.setText("Published: " + news.getPubDate());

            ImageIcon icon = null;

            try {
                String path = news.getLocalImagePath();
                if (path != null) {

                    BufferedImage img = ImageIO.read(new File(path));
                    fullSizeImage = img; // Save the original image

                    if (img != null) {
                        Image scaled = img.getScaledInstance(150, 100, Image.SCALE_SMOOTH); // thumbnail size
                        icon = new ImageIcon(scaled);
                    }
                }
            } catch (Exception ignored) {}

            imageLabel.setIcon(icon);


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
                                        return fullSizeImage; // ✅ Use the original
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
