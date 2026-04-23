package com.service;

import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelTrainer {

    private static final Logger LOGGER = Logger.getLogger(ModelTrainer.class);

    public RandomModel train(String csvData) {
        Map<String, List<Double>> valuesBySource = new HashMap<>();

        String[] lines = csvData.split("\n");
        int skipped = 0;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length < 5) { skipped++; continue; }

            String url = parts[2];
            String status = parts[3];
            String body = parts[4];

            if (!"200".equals(status)) { skipped++; continue; }

            try {
                double value = Double.parseDouble(body);
                valuesBySource.computeIfAbsent(url, k -> new ArrayList<>()).add(value);
            } catch (NumberFormatException e) {
                skipped++;
            }
        }

        LOGGER.infof("Parsed %d source(s), skipped %d rows", valuesBySource.size(), skipped);

        Map<String, SourceStats> sources = new HashMap<>();
        int totalSamples = 0;
        for (Map.Entry<String, List<Double>> entry : valuesBySource.entrySet()) {
            SourceStats stats = computeStats(entry.getValue());
            sources.put(entry.getKey(), stats);
            totalSamples += stats.count;
        }

        RandomModel model = new RandomModel();
        model.version = "1.0";
        model.trainedAt = LocalDateTime.now().toString();
        model.totalSamples = totalSamples;
        model.sources = sources;
        return model;
    }

    private SourceStats computeStats(List<Double> values) {
        SourceStats stats = new SourceStats();
        stats.count = values.size();
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (double v : values) {
            sum += v;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        stats.mean = sum / stats.count;
        stats.min = min;
        stats.max = max;

        double varianceSum = 0;
        for (double v : values) {
            varianceSum += Math.pow(v - stats.mean, 2);
        }
        stats.stdDev = Math.sqrt(varianceSum / stats.count);

        return stats;
    }
}
