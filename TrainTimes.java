package com.trains.fetcher;

import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class TrainTimes {

    public static void main(String[] args) {
        try {
            // Irish Rail API Get for Pearse Station, 90 minutes ahead for displaying
            URL dataURL = new URL("http://api.irishrail.ie/realtime/realtime.asmx/getStationDataByCodeXML_WithNumMins?StationCode=PERSE&NumMins=90&format=xml\r\n");

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) dataURL.openConnection();
            connection.setRequestMethod("GET");

            // Read XML data from the response and parse
            InputStream xmlStream = connection.getInputStream();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xmlDoc = builder.parse(xmlStream);
            xmlDoc.getDocumentElement().normalize();

            NodeList trainNodes = xmlDoc.getElementsByTagName("objStationData");

            System.out.println("Total trains found: " + trainNodes.getLength());
            // Was initally used for debugging as WAS displaying no trains

            // HTML output building for train_times
            StringBuilder htmlOutput = new StringBuilder();
            htmlOutput.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            htmlOutput.append("<title>Upcoming Train Info</title>");
            htmlOutput.append("<style>");
            htmlOutput.append("table {border-collapse: collapse; width: 100%;}");
            htmlOutput.append("th, td {border: 1px solid #000; padding: 8px; text-align: left;}");
            htmlOutput.append("th {background-color: #eee;}");
            htmlOutput.append("</style></head><body>");

            htmlOutput.append("<h2>Train Times - Pearse Station (Next 90 Minutes)</h2>");
            htmlOutput.append("<table>");
            htmlOutput.append("<tr>");
            htmlOutput.append("<th>Origin</th><th>Destination</th><th>Expected Arrival</th>");
            htmlOutput.append("<th>Expected Departure</th><th>Arrival at Destination</th>");
            htmlOutput.append("</tr>");

            // Loop through all entries and display 
            for (int i = 0; i < trainNodes.getLength(); i++) {
                Element train = (Element) trainNodes.item(i);

                //Getting values from the data in GET request
                String from = extractValue("Origin", train);
                String to = extractValue("Destination", train);
                String eta = extractValue("Exparrival", train);
                String etd = extractValue("Expdepart", train);
                String destArrival = extractValue("Scharrival", train); // supposed dest arrival time 

                // HTML output builder for appending for each row
                htmlOutput.append("<tr>");
                htmlOutput.append("<td>").append(from).append("</td>");
                htmlOutput.append("<td>").append(to).append("</td>");
                htmlOutput.append("<td>").append(eta).append("</td>");
                htmlOutput.append("<td>").append(etd).append("</td>");
                htmlOutput.append("<td>").append(destArrival).append("</td>");
                htmlOutput.append("</tr>");
            }

            htmlOutput.append("</table></body></html>");

            // Write to a local HTML file which is in the root of the project
            Files.write(Paths.get("train_times.html"), htmlOutput.toString().getBytes());
            System.out.println("HTML file created: train_times.html"); //lets me know its created

        } catch (Exception ex) {
            System.out.println("Something went wrong while fetching or processing the train data.");
            ex.printStackTrace(); // If trying doesnt work to receive data will give error
        }
    }


    // Cleanly extracts content from tag inside XML for given station, uses searching by tag name rather than iterating GETS to API
    // https://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
    private static String extractValue(String tagName, Element element) {
        NodeList tagList = element.getElementsByTagName(tagName);
        if (tagList.getLength() > 0) {
            Node node = tagList.item(0);
            return node != null ? node.getTextContent() : "";
        }
        return "";
    }
}
