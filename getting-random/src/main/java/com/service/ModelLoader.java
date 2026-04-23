package com.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@ApplicationScoped
public class ModelLoader {

    private static final Logger LOGGER = Logger.getLogger(ModelLoader.class);

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "env", defaultValue = "dev")
    String env;

    @ConfigProperty(name = "gcs.bucket.name", defaultValue = "random-data-bucket")
    String gcsBucketName;

    @ConfigProperty(name = "model.filename", defaultValue = "random-model.json")
    String modelFilename;

    private volatile RandomModel model;

    @PostConstruct
    void load() {
        try {
            String json;
            if ("prd".equals(env)) {
                org.apache.camel.Exchange exchange = producerTemplate.request(
                        "google-storage://" + gcsBucketName + "?autoCreateBucket=false",
                        ex -> {
                            ex.getIn().setHeader("CamelGoogleStorageObjectName", modelFilename);
                            ex.getIn().setHeader("CamelGoogleStorageOperation", "getObject");
                        });
                try (InputStream is = exchange.getMessage().getBody(InputStream.class)) {
                    json = new String(is.readAllBytes());
                }
            } else {
                File file = new File(modelFilename);
                if (!file.exists()) {
                    LOGGER.infof("No model found at %s — scoring will be skipped until model is trained.",
                            file.getAbsolutePath());
                    return;
                }
                json = Files.readString(file.toPath());
            }
            model = objectMapper.readValue(json, RandomModel.class);
            LOGGER.infof("Model loaded: v%s, trained at %s, %d samples across %d source(s).",
                    model.version, model.trainedAt, model.totalSamples, model.sources.size());
        } catch (Exception e) {
            LOGGER.warnf("Could not load model (%s). Scoring will be skipped.", e.getMessage());
        }
    }

    public RandomModel getModel() {
        return model;
    }
}
