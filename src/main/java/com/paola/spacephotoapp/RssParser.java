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

public class RssParser {

    private static final String RSS_URL = "https://photojournal.jpl.nasa.gov/rss/new";

    public List<SpacePhoto> parse() {
        List<SpacePhoto> photos = new ArrayList<>();

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
                return photos;
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


                photos.add(photo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return photos;
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
