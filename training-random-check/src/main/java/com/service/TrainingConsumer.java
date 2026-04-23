package com.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TrainingConsumer {

    private static final Logger LOGGER = Logger.getLogger(TrainingConsumer.class);

    @Inject
    ModelStore modelStore;

    private final ModelTrainer trainer = new ModelTrainer();

    @Incoming("random-data")
    public void consume(String csvData) {
        LOGGER.info("Received training data, starting model training...");
        try {
            RandomModel model = trainer.train(csvData);
            modelStore.save(model);
            LOGGER.infof("Training complete. %d samples from %d source(s).",
                    model.totalSamples, model.sources.size());
        } catch (Exception e) {
            LOGGER.error("Training failed", e);
        }
    }
}
