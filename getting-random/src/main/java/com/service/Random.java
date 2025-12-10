package com.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/random")
public class Random {

    @ConfigProperty(name = "urls", defaultValue = "http://localhost:5000/random,http://localhost:8081/random,http://localhost:8083/random")
    String urls_env;

    @ConfigProperty(name = "iterations", defaultValue = "100")
    int iterations;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String random() {
        StringBuilder result = new StringBuilder();
        String[] urls = urls_env.split(",");
        for (int i = 0; i < iterations; i++) {
            for (String url : urls) {
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

                    java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());

                    result.append("Attempt ").append(i + 1).append(": URL: ").append(url).append(" -> ");
                    result.append("status=").append(resp.statusCode()).append(", body=");
                    result.append(resp.body() == null ? "" : resp.body());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    result.append("Attempt ").append(i + 1).append(": URL: ").append(url).append(" -> interrupted");
                } catch (Exception e) {
                    result.append("Attempt ").append(i + 1).append(": URL: ").append(url).append(" -> error: ").append(e.getMessage());
                }
                result.append("\n");
            }
        }

        return result.toString();
    }
}
