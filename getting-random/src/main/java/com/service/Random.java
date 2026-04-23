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
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/random")
public class Random {

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    ModelLoader modelLoader;

    @Inject
    @Channel("random-data")
    Emitter<String> randomDataEmitter;

    @ConfigProperty(name = "urls", defaultValue = "http://localhost:5000/random,http://localhost:8081/random,http://localhost:8083/random")
    String urls_env;

    @ConfigProperty(name = "iterations", defaultValue = "100")
    int iterations;

    @ConfigProperty(name = "csv.filename", defaultValue = "random-data.csv")
    String csvFileName;

    @ConfigProperty(name = "gcs.bucket.name", defaultValue = "random-data-bucket")
    String gcsBucketName;

    @ConfigProperty(name = "env", defaultValue = "dev")
    String env;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String random() {
        StringBuilder result = new StringBuilder();
        String[] urls = urls_env.split(",");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        RandomModel model = modelLoader.getModel();

        // Add CSV header
        result.append("datetime,attempt,url,status,body,zscore\n");

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
                    result.append(body).append(",");
                    result.append(score(model, url, body));

                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    result.append(dateTime).append(",");
                    result.append(i + 1).append(",");
                    result.append(url).append(",interrupted,,N/A");
                } catch (Exception e) {
                    result.append(dateTime).append(",");
                    result.append(i + 1).append(",");
                    result.append(url).append(",error,");
                    result.append(e.getMessage()).append(",N/A");
                }
                result.append("\n");
            }
        }

        String message;
        if (env.equals("prd")) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("CamelGoogleStorageObjectName", csvFileName);
            producerTemplate.sendBodyAndHeaders("google-storage://" + gcsBucketName, result.toString(), headers);
            message = "File " + csvFileName + " uploaded to GCS bucket " + gcsBucketName;
        } else {
            File file = new File(csvFileName);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(result.toString());
            } catch (IOException e) {
                e.printStackTrace();
                return "Error writing to file: " + e.getMessage();
            }
            message = "File " + csvFileName + " created in the current directory.";
        }

        randomDataEmitter.send(result.toString());
        return message;
    }

    private String score(RandomModel model, String url, String body) {
        if (model == null) return "no_model";
        SourceStats stats = model.sources.get(url);
        if (stats == null) return "unknown_source";
        try {
            double value = Double.parseDouble(body);
            if (stats.stdDev == 0) return "0.00";
            return String.format("%.2f", (value - stats.mean) / stats.stdDev);
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }
}