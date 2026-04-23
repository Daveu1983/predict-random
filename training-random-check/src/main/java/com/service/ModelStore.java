package com.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ModelStore {

    private static final Logger LOGGER = Logger.getLogger(ModelStore.class);

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

    public void save(RandomModel model) throws Exception {
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);

        if ("prd".equals(env)) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("CamelGoogleStorageObjectName", modelFilename);
            producerTemplate.sendBodyAndHeaders("google-storage://" + gcsBucketName, json, headers);
            LOGGER.infof("Model saved to GCS: gs://%s/%s", gcsBucketName, modelFilename);
        } else {
            File file = new File(modelFilename);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
            LOGGER.infof("Model saved to file: %s", file.getAbsolutePath());
        }
    }
}
