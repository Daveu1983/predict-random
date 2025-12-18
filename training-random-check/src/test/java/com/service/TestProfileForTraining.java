package com.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class TestProfileForTraining implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "mp.messaging.incoming.random-data.connector", "smallrye-in-memory"
        );
    }
}
