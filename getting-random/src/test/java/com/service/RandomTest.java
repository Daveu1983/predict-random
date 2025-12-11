package com.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
@TestProfile(TestProfileForRandom.class)
public class RandomTest {

    @Test
    public void testRandomEndpoint() throws IOException {
        given()
          .when().get("/random")
          .then()
             .statusCode(200)
             .body(is("File random-data.csv created in the current directory."));

        String csvContent = Files.readString(Paths.get("random-data.csv"));
        assertThat(csvContent, containsString("datetime,attempt,url,status,body"));
        assertThat(csvContent, containsString("http://localhost:1,error"));
    }

}
