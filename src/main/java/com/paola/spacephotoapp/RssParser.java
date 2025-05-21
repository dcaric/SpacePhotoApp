package com.paola.spacephotoapp;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
// for threading
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RssParser {

    private static final String RSS_URL = "https://photojournal.jpl.nasa.gov/rss/new";

    /**
     * Main function responsible for parsing rss feed
     * @return
     */
    public List<SpacePhoto> parse() {

        // initializes list of SpacePhoto items, used for storing rss items
        List<SpacePhoto> rssFeed = new ArrayList<>();

        try {
            URL url = new URL(RSS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // ✅ Add User-Agent to avoid 403
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            System.out.println("HTTP response code: " + responseCode);

            if (responseCode != 200) {
                System.err.println("Failed to fetch RSS feed.");
                return rssFeed;
            }

            // Optional debug: print first few lines
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder rawXml = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                rawXml.append(line).append("\n");
            }

            // Print first 10 lines for debug
            System.out.println("--- RAW XML START ---");
            System.out.println(rawXml.substring(0, Math.min(1000, rawXml.length())));
            System.out.println("--- RAW XML END ---");

            // Parse the XML string
            InputStream inputStream = new java.io.ByteArrayInputStream(rawXml.toString().getBytes());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            NodeList items = doc.getElementsByTagName("item");

            // 4 parallel downloads
            ExecutorService executor = Executors.newFixedThreadPool(4);

            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);

                SpacePhoto photo = new SpacePhoto();
                photo.setTitle(getText(item, "title"));
                photo.setDescription(getText(item, "description"));
                photo.setLink(getText(item, "link"));
                photo.setGuid(getText(item, "guid"));
                photo.setPubDate(getText(item, "pubDate"));

                String descriptionHtml = getText(item, "description");
                String imageUrl = extractImageUrlFromDescription(descriptionHtml);
                photo.setImageUrl(imageUrl);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    String fileName = photo.getGuid().replaceAll("[^a-zA-Z0-9]", "_") + ".jpg";

                    // Lambda = functional programming
                    executor.submit(() -> {
                        downloadImage(imageUrl, fileName);
                    });
                    photo.setLocalImagePath("assets/" + fileName);
                }

                rssFeed.add(photo);
            }

            executor.shutdown(); // Don't accept new tasks


        } catch (Exception e) {
            e.printStackTrace();
        }

        return rssFeed;
    }

    /**
     * Downloading image
     *             Each image is downloaded in a separate thread
     *             Files are saved under the assets/ folder
     *             Console prints Downloaded: PIAxxxx.jpg for each
     */
    private void downloadImage(String imageUrl, String filename) {
        try {
            URL url = new URL(imageUrl);
            InputStream in = url.openStream();

            // Create "assets" folder if it doesn't exist
            java.nio.file.Path folder = java.nio.file.Paths.get("assets");
            if (!java.nio.file.Files.exists(folder)) {
                java.nio.file.Files.createDirectory(folder);
            }

            java.nio.file.Path targetPath = folder.resolve(filename);
            java.nio.file.Files.copy(in, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            in.close();
            System.out.println("Downloaded: " + filename);
        } catch (Exception e) {
            System.err.println("Failed to download " + imageUrl);
            e.printStackTrace();
        }
    }


    private String getText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0 && list.item(0).getTextContent() != null) {
            return list.item(0).getTextContent().trim();
        }
        return "";
    }

    private String extractImageUrlFromDescription(String html) {
        if (html == null || !html.contains("<img")) return null;

        int srcStart = html.indexOf("src='");
        if (srcStart == -1) return null;

        srcStart += 5; // move past "src='"
        int srcEnd = html.indexOf("'", srcStart);
        if (srcEnd == -1) return null;

        return html.substring(srcStart, srcEnd).trim();
    }

}
