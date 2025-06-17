package com.paola.spacephotoapp.parser;

import com.paola.spacephotoapp.domain.model.NewsCategory;
import com.paola.spacephotoapp.repository.NewsRepository;
import com.paola.spacephotoapp.repository.NewsRepositoryInterface;
import com.paola.spacephotoapp.domain.model.NewsRelease;
import com.paola.spacephotoapp.util.LogUtils;
import org.jsoup.Jsoup;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class RssParser {
    private static final String RSS_URL = "https://www.nasa.gov/news-release/feed/";

    /*
        ArrayList — Used in rssFeed = new ArrayList<>(): fast access by index, used in UI rendering.
        HashSet — For GUIDs, to prevent duplicates fast (O(1) lookup).
        HashMap — For failed downloads (key: GUID, value: error message).
     */

    public List<NewsRelease> parse(int count) {
        // List for ordered storage, here will be saved received RSS feed
        List<NewsRelease> rssFeed = new ArrayList<>();

        // start thread
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        // Map interface polymorphically for logging.
        Map<String, String> failedDownloads = new HashMap<>();

        // for preventing unnecessary download of feeds
        NewsRepositoryInterface repository = new NewsRepository();
        Set<String> downloadedGuids = repository.getAllGuids();


        try {

            // prepare for http request
            URL url = new URL(RSS_URL);
            // created connection object
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // make http response and save response from the server
            int responseCode = conn.getResponseCode();
            System.out.println("HTTP response code: " + responseCode);

            // check is not 200 OK received
            if (responseCode != 200) {
                // in case of not 200 ok log error and returns empty feed
                System.err.println("Failed to fetch RSS feed.");
                return rssFeed;
            }

            // reads useful data from the response - with conn.getInputStream()
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            // used StringBuilder instead of String to save memory usage since Java handles
            // string as immutable object - unlike some other languages
            StringBuilder rawXml = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // adding line by line
                rawXml.append(line).append("\n");
            }

            System.out.println("--- RAW XML START ---");
            System.out.println(rawXml.substring(0, Math.min(1000, rawXml.length())));
            System.out.println("--- RAW XML END ---");

            // --- XML Parsing Setup (using DOM) ---
            // Converts the raw XML string into an InputStream and sets up the DOM parser.
            // The entire XML document is loaded into memory as a 'Document' object for easy navigation.
            InputStream inputStream = new ByteArrayInputStream(rawXml.toString().getBytes());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            NodeList items = doc.getElementsByTagName("item"); // Extracts all <item> elements from the RSS feed.

            int addedCount = 0; // Counter for successfully added news items.

            // --- Iterating and Processing Each News Item ---
            // Loops through each extracted <item> element from the RSS feed.
            for (int i = 0; i < items.getLength(); i++) {
                if (addedCount >= count) break; // Stops if the desired number of items has been added.

                org.w3c.dom.Element item = (org.w3c.dom.Element) items.item(i);
                NewsRelease news = new NewsRelease(); // Creates a custom object to hold parsed news data.

                // --- Extracting Core News Details ---
                // Retrieves the title, link, and description from the current RSS item.
                // It handles fallbacks for description (content:encoded vs. description) and cleans HTML.
                news.setTitle(getText(item, "title"));
                news.setLink(getText(item, "link"));
                String htmlDescription = getText(item, "content:encoded");
                if (htmlDescription == null || htmlDescription.isEmpty()) {
                    htmlDescription = getText(item, "description");
                }
                news.setDescription(Jsoup.parse(htmlDescription).text()); // Uses Jsoup to convert HTML to plain text.

                // Retrieves the GUID (unique ID) and publication date.
                news.setGuid(getText(item, "guid"));
                news.setPubDate(getText(item, "pubDate"));

                // --- Image URL Extraction and Asynchronous Download ---
                // Tries to find an image URL, first from specific tags (media:thumbnail), then by extracting from HTML content.
                String imageUrl = getAttribute(item, "media:thumbnail", "url");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    String htmlContent = getText(item, "content:encoded");
                    imageUrl = extractLargestImageFromContent(htmlContent);
                }
                news.setImageUrl(imageUrl);

                // If an image URL is found and not already downloaded, it starts an asynchronous download.
                // This prevents the main process from waiting for image downloads to complete.
                if (imageUrl != null && !imageUrl.isEmpty() && !downloadedGuids.contains(news.getGuid())) {
                    final String finalImageUrl = imageUrl;
                    String fileName = news.getGuid().replaceAll("[^a-zA-Z0-9]", "_") + "_full.jpg";
                    executor.submit(() -> { // Submits download task to a background thread.
                        try {
                            downloadImage(finalImageUrl, fileName);
                        } catch (Exception ex) {
                            failedDownloads.put(news.getGuid(), "Failed: " + ex.getMessage());
                        }
                    });
                    news.setLocalImagePath("assets/" + fileName);
                    downloadedGuids.add(news.getGuid()); // Marks GUID as downloaded to avoid re-downloads.
                }

                // --- Category Detection and Data Storage ---
                // Combines title and description to detect a category for the news item.
                String combined = news.getTitle() + " " + news.getDescription();
                news.setCategory(detectCategory(combined));

                // Checks for duplicates before adding the news item to the main list and inserting it into the database.
                if (!repository.existsByGuid(news.getGuid())) {
                    rssFeed.add(news);
                    repository.insertNews(news); // Saves the news item to the local repository (database).
                    addedCount++;
                } else {
                    System.out.println("Skipped duplicate: " + news.getGuid()); // Logs if a duplicate is found.
                }
            }

            // --- Final Logging and Error Handling ---
            // Logs any image downloads that failed during the process.
            LogUtils.logFailedDownloads(failedDownloads);

        } catch (Exception e) {
            // Catches and prints any unexpected errors that occurred during the entire process.
            e.printStackTrace();
        }

        return rssFeed; // Returns the list of parsed news articles.
    }


    /**
     * Downloads an image from a given URL and saves it to the 'assets' folder.
     * If the 'assets' folder doesn't exist, it will be created.
     *
     * @param imageUrl The URL of the image to download.
     * @param filename The name to save the downloaded image as (e.g., "my_image.jpg").
     */
    private void downloadImage(String imageUrl, String filename) {
        try {
            URL url = new URL(imageUrl);
            Path folder = Paths.get("assets");

            // Ensure the assets folder exists
            if (!Files.exists(folder)) {
                Files.createDirectories(folder); // Use createDirectories to create parent directories if needed
            }
            Path targetPath = folder.resolve(filename);

            // Use try-with-resources for automatic closing of the InputStream
            try (InputStream in = url.openStream()) {
                // StandardCopyOption.REPLACE_EXISTING attempts to replace an existing file.
                // If the file is locked by another process, this will throw the FileSystemException.
                // You might consider StandardCopyOption.ATOMIC_MOVE or a more robust retry logic
                // if this issue persists with external locks. For now, let's ensure *our* stream is closed.
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } // 'in' is automatically closed here

            System.out.println("Downloaded: " + filename);
        } catch (IOException e) { // Catch IOException specifically for file-related issues
            System.err.println("Failed to download " + imageUrl + ": " + e.getMessage()); // Log specific message
            // No need for e.printStackTrace() if you are logging the error and handling it
            // by adding to failedDownloads map and then logging via LogUtils.
            // For debugging, keep it for now.
            e.printStackTrace();
        } catch (Exception e) { // Catch other potential exceptions (e.g., malformed URL)
            System.err.println("An unexpected error occurred while downloading " + imageUrl + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the value of a specific attribute from an XML element found within a parent element.
     * For example, it can get the 'url' attribute from a '<media:thumbnail>' tag.
     *
     * @param parent The parent XML element to search within (e.g., an 'item' element).
     * @param tagName The name of the child tag to find (e.g., "media:thumbnail").
     * @param attributeName The name of the attribute whose value needs to be retrieved (e.g., "url").
     * @return The value of the specified attribute, or null if the tag or attribute is not found.
     */
    private String getAttribute(org.w3c.dom.Element parent, String tagName, String attributeName) {
        NodeList list = parent.getElementsByTagName(tagName); // Finds all child elements with the given tag name.
        if (list.getLength() > 0) { // Checks if at least one such tag was found.
            org.w3c.dom.Element elem = (org.w3c.dom.Element) list.item(0); // Gets the first found element.
            return elem.getAttribute(attributeName); // Returns the value of the specified attribute from that element.
        }
        return null; // Returns null if no such tag was found.
    }

    /**
     * Extracts the plain text content from the first occurrence of a specified XML tag within a parent element.
     * For example, it can get the text inside a '<title>' or '<description>' tag.
     *
     * @param parent The parent XML element to search within.
     * @param tagName The name of the child tag whose text content is needed.
     * @return The trimmed text content of the tag, or an empty string if the tag is not found or has no content.
     */
    private String getText(org.w3c.dom.Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName); // Finds all child elements with the given tag name.
        // Checks if at least one tag was found and if its text content is not null.
        if (list.getLength() > 0 && list.item(0).getTextContent() != null) {
            return list.item(0).getTextContent().trim(); // Returns the trimmed text content of the first found tag.
        }
        return ""; // Returns an empty string if the tag or its content is not found.
    }

    /*
    // This function was commented out, but it aimed to extract the first image URL
    // from an HTML string by looking for 'src' attribute within '<img>' tags.
    // private String extractImageUrlFromDescription(String html) { ... }
    */

    /**
     * Parses an HTML string and tries to find the URL of the largest image based on its 'width' attribute.
     * This is useful for picking the most prominent image from an article's content.
     *
     * @param html The HTML string to parse (e.g., from 'content:encoded' or 'description' tags).
     * @return The URL of the largest image found, or null if no images are found or widths cannot be determined.
     */
    private String extractLargestImageFromContent(String html) {
        if (html == null) return null;
        org.jsoup.nodes.Document doc = Jsoup.parse(html); // Uses Jsoup to parse the HTML string into a navigable document.
        org.jsoup.select.Elements images = doc.select("img"); // Selects all '<img>' tags within the HTML.

        String largest = null;
        int maxWidth = 0;
        // Iterates through each '<img>' element found.
        for (org.jsoup.nodes.Element img : images) {
            String src = img.attr("src"); // Gets the 'src' attribute (image URL).
            String widthAttr = img.attr("width"); // Gets the 'width' attribute.
            int width = 0;
            try {
                width = Integer.parseInt(widthAttr); // Tries to convert the width attribute to an integer.
            } catch (NumberFormatException ignored) {} // Ignores errors if width is not a valid number.

            // If the current image's width is greater than the widest found so far, it becomes the new "largest".
            if (width > maxWidth) {
                maxWidth = width;
                largest = src;
            }
        }
        return largest; // Returns the URL of the largest image.
    }

    /*
    // This function was commented out, but it was designed to log failed image downloads
    // to a file named "failed_downloads.txt" in a 'logs' folder.
    // private void writeFailedDownloadsToLog(Map<String, String> failedDownloads) { ... }
    */

    /**
     * A constant map defining keywords associated with different news categories.
     * This is used by the `detectCategory` function to classify news articles.
     * For example, if "moon" or "lunar" are found, it suggests the MOON category.
     */
    private static final Map<NewsCategory, List<String>> CATEGORY_KEYWORDS = Map.of(
            NewsCategory.MOON, List.of("moon", "lunar"),
            NewsCategory.MARS, List.of("mars"),
            NewsCategory.ARTEMIS, List.of("artemis"),
            NewsCategory.SPACE, List.of("galaxy", "telescope", "nebula", "hubble", "webb", "cluster")
    );

    /**
     * Detects the category of a news article based on keywords present in its text (title + description).
     * It calculates a score for each category based on how many of its keywords appear in the text,
     * and then returns the category with the highest score.
     *
     * @param text The combined title and description of the news article.
     * @return The detected NewsCategory (e.g., MOON, MARS, SPACE), or UNKNOWN if no relevant keywords are found.
     */
    public static NewsCategory detectCategory(String text) {
        if (text == null || text.isBlank()) return NewsCategory.UNKNOWN; // Returns UNKNOWN if the text is empty or null.

        String lowerText = text.toLowerCase(); // Converts the text to lowercase for case-insensitive keyword matching.
        Map<NewsCategory, Integer> scoreMap = new HashMap<>(); // A map to store scores for each category.

        System.out.println("detectCategory text " + text); // Prints the text being analyzed for debugging.

        // Iterates through each category and its associated keywords.
        for (Map.Entry<NewsCategory, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            System.out.println("detectCategory entry " + entry); // Prints the current category and its keywords for debugging.

            int score = 0;
            // Checks each keyword for the current category against the news article's text.
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) { // If a keyword is found, increment the score.
                    System.out.println("detectCategory keyword " + keyword); // Prints the keyword found for debugging.
                    score++;
                }
            }
            if (score > 0) { // If the category has any matching keywords, add its score to the map.
                scoreMap.put(entry.getKey(), score);
            }
        }

        // Finds the category with the highest score from the scoreMap.
        var returnCategory = scoreMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue()) // Finds the entry with the maximum value (score).
                .map(Map.Entry::getKey) // Maps the entry to just its key (the NewsCategory).
                .orElse(NewsCategory.UNKNOWN); // If no categories have a score, default to UNKNOWN.

        System.out.println("detectCategory returnCategory " + returnCategory); // Prints the determined category for debugging.

        return returnCategory; // Returns the detected category.
    }


}
