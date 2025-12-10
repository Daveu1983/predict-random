package com.service;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

public class TestProfileForRandom implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "iterations", "1",
            "urls", "http://localhost:1",
            "quarkus.http.test-port", "0"
        );
    }
}