package com.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(TestProfileForRandom.class)
public class RandomTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @AfterEach
    public void cleanup() {
        connector.sink("random-data").clear();
    }

    @Test
    public void testRandomEndpoint() throws IOException {
        InMemorySink<String> randomData = connector.sink("random-data");

        given()
          .when().get("/random")
          .then()
             .statusCode(200)
             .body(is("File test-random-data.csv created in the current directory."));

        String csvContent = Files.readString(Paths.get("test-random-data.csv"));
        assertThat(csvContent, containsString("datetime,attempt,url,status,body"));
        assertThat(csvContent, containsString("http://localhost:1,error"));

        List<? extends Message<String>> received = randomData.received();
        assertEquals("Data processing complete. File test-random-data.csv created in the current directory.", received.get(0).getPayload());
    }
}
