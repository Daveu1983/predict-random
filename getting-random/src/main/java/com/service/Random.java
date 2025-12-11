package com.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/random")
public class Random {

    @ConfigProperty(name = "urls", defaultValue = "http://localhost:5000/random,http://localhost:8081/random,http://localhost:8083/random")
    String urls_env;

    @ConfigProperty(name = "iterations", defaultValue = "100")
    int iterations;

    @ConfigProperty(name = "csv.filename", defaultValue = "random-data.csv")
    String csvFileName;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String random() {
        StringBuilder result = new StringBuilder();
        String[] urls = urls_env.split(",");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        // Add CSV header
        result.append("datetime,attempt,url,status,body\n");

        for (int i = 0; i < iterations; i++) {
            for (String url : urls) {
                LocalDateTime now = LocalDateTime.now();
                String dateTime = dtf.format(now);
                try {
                    java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                            .connectTimeout(java.time.Duration.ofSeconds(10))
                            .build();

                    java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(url))
                            .header("X-App-Name", "getting-random")
                            .GET()
                            .timeout(java.time.Duration.ofSeconds(10))
                            .build();

                    java.net.http.HttpResponse<String> resp = client.send(req,
                            java.net.http.HttpResponse.BodyHandlers.ofString());

                    result.append(dateTime).append(",");
                    result.append(i + 1).append(",");
                    result.append(url).append(",");
                    result.append(resp.statusCode()).append(",");
                    String body = resp.body() == null ? "" : resp.body().trim().replace(",", ";");
                    result.append(body);

                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    result.append(dateTime).append(",");
                    result.append(i + 1).append(",");
                    result.append(url).append(",interrupted,");
                } catch (Exception e) {
                    result.append(dateTime).append(",");
                    result.append(i + 1).append(",");
                    result.append(url).append(",error,");
                    result.append(e.getMessage());
                }
                result.append("\n");
            }
        }

        File file = new File(csvFileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(result.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return "Error writing to file: " + e.getMessage();
        }

        return "File " + csvFileName + " created in the current directory.";
       }
}
