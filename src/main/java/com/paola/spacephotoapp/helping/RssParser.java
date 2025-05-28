package com.paola.spacephotoapp.helping;

import com.paola.spacephotoapp.model.NewsRelease;
import org.jsoup.Jsoup;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RssParser {
    private static final String RSS_URL = "https://www.nasa.gov/news-release/feed/";

    public List<NewsRelease> parse() {
        List<NewsRelease> rssFeed = new ArrayList<>();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        try {
            URL url = new URL(RSS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            System.out.println("HTTP response code: " + responseCode);
            if (responseCode != 200) {
                System.err.println("Failed to fetch RSS feed.");
                return rssFeed;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder rawXml = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                rawXml.append(line).append("\n");
            }

            System.out.println("--- RAW XML START ---");
            System.out.println(rawXml.substring(0, Math.min(1000, rawXml.length())));
            System.out.println("--- RAW XML END ---");

            InputStream inputStream = new ByteArrayInputStream(rawXml.toString().getBytes());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            NodeList items = doc.getElementsByTagName("item");

            for (int i = 0; i < items.getLength(); i++) {
                org.w3c.dom.Element item = (org.w3c.dom.Element) items.item(i);
                NewsRelease news = new NewsRelease();

                news.setTitle(getText(item, "title"));
                news.setLink(getText(item, "link"));

                String htmlDescription = getText(item, "content:encoded");
                if (htmlDescription == null || htmlDescription.isEmpty()) {
                    htmlDescription = getText(item, "description");
                }
                news.setDescription(Jsoup.parse(htmlDescription).text());

                news.setGuid(getText(item, "guid"));
                news.setPubDate(getText(item, "pubDate"));

                String imageUrl = getAttribute(item, "media:thumbnail", "url");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    String htmlContent = getText(item, "content:encoded");
                    imageUrl = extractLargestImageFromContent(htmlContent);
                }
                news.setImageUrl(imageUrl);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    final String finalImageUrl = imageUrl;
                    String fileName = news.getGuid().replaceAll("[^a-zA-Z0-9]", "_") + "_full.jpg";
                    executor.submit(() -> downloadImage(finalImageUrl, fileName));
                    news.setLocalImagePath("assets/" + fileName);
                }

                NewsRepository repository = new NewsRepository();
                if (!repository.existsByGuid(news.getGuid())) {
                    rssFeed.add(news);
                    repository.save(news);
                } else {
                    System.out.println("Skipped duplicate: " + news.getGuid());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rssFeed;
    }

    private void downloadImage(String imageUrl, String filename) {
        try {
            URL url = new URL(imageUrl);
            InputStream in = url.openStream();
            Path folder = Paths.get("assets");
            if (!Files.exists(folder)) {
                Files.createDirectory(folder);
            }
            Path targetPath = folder.resolve(filename);
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            System.out.println("Downloaded: " + filename);
        } catch (Exception e) {
            System.err.println("Failed to download " + imageUrl);
            e.printStackTrace();
        }
    }

    private String getAttribute(org.w3c.dom.Element parent, String tagName, String attributeName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            org.w3c.dom.Element elem = (org.w3c.dom.Element) list.item(0);
            return elem.getAttribute(attributeName);
        }
        return null;
    }

    private String getText(org.w3c.dom.Element parent, String tagName) {
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
        srcStart += 5;
        int srcEnd = html.indexOf("'", srcStart);
        if (srcEnd == -1) return null;
        return html.substring(srcStart, srcEnd).trim();
    }

    private String extractLargestImageFromContent(String html) {
        if (html == null) return null;
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        org.jsoup.select.Elements images = doc.select("img");

        String largest = null;
        int maxWidth = 0;
        for (org.jsoup.nodes.Element img : images) {
            String src = img.attr("src");
            String widthAttr = img.attr("width");
            int width = 0;
            try {
                width = Integer.parseInt(widthAttr);
            } catch (NumberFormatException ignored) {}

            if (width > maxWidth) {
                maxWidth = width;
                largest = src;
            }
        }
        return largest;
    }
}
