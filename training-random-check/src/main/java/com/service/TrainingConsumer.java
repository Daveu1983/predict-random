package com.service;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TrainingConsumer {

    private static final Logger LOGGER = Logger.getLogger(TrainingConsumer.class);
    private final List<String> messages = new ArrayList<>();

    @Incoming("random-data")
    public void consume(String message) {
        LOGGER.info("Ready to train with message: " + message);
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
