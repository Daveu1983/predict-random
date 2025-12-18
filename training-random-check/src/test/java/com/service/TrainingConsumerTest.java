package com.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(TestProfileForTraining.class)
public class TrainingConsumerTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    TrainingConsumer trainingConsumer;

    @AfterEach
    public void cleanup() {
        trainingConsumer.getMessages().clear();
    }

    @Test
    public void testConsume() throws InterruptedException {
        InMemorySource<String> randomData = connector.source("random-data");
        String message = "test message";
        randomData.send(message);
        Thread.sleep(1000);
        assertEquals(1, trainingConsumer.getMessages().size());
        assertEquals(message, trainingConsumer.getMessages().get(0));
    }
}
