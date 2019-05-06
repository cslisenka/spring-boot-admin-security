package com.example.demo;

import org.springframework.boot.actuate.endpoint.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Endpoint(id = "features")
public class CustomOperationsEndpoint {

        private Map<String, Boolean> features = new ConcurrentHashMap<>();

        @ReadOperation
        public Map<String, Boolean> features() {
            return features;
        }

        @ReadOperation
        public Boolean feature(@Selector String name) {
            return features.get(name);
        }

        @WriteOperation
        public void configureFeature(@Selector String name, Boolean enabled) {
            features.put(name, enabled);
        }

        @DeleteOperation
        public void deleteFeature(@Selector String name) {
            features.remove(name);
        }
}