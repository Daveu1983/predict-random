package com.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(TestProfileForTraining.class)
public class TrainingConsumerTest {

    static final String MODEL_FILE = "target/test-random-model.json";

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    ObjectMapper objectMapper;

    @AfterEach
    public void cleanup() {
        new File(MODEL_FILE).delete();
    }

    @Test
    public void testConsume1() throws Exception {
        InMemorySource<String> randomData = connector.source("random-data");
        String csv = "datetime,attempt,url,status,body\n"
                + "2026/04/23 10:00:00,1,http://python-random:5000/random,200,42\n"
                + "2026/04/23 10:00:01,2,http://python-random:5000/random,200,58\n"
                + "2026/04/23 10:00:02,1,http://golang-random:8081/random,200,71\n";
        randomData.send(csv);

        File modelFile = new File(MODEL_FILE);
        long deadline = System.currentTimeMillis() + 5000;
        while (!modelFile.exists() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertTrue(modelFile.exists(), "Model file should be created within 5s");

        RandomModel model = objectMapper.readValue(modelFile, RandomModel.class);
        assertEquals(3, model.totalSamples);
        assertEquals(2, model.sources.size());
        assertTrue(model.sources.containsKey("http://python-random:5000/random"));
        assertTrue(model.sources.containsKey("http://golang-random:8081/random"));
    }
}
