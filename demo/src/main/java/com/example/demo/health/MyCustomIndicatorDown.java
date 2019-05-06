package com.example.demo.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("health-down")
public class MyCustomIndicatorDown implements ReactiveHealthIndicator {

    @Override
    public Mono<Health> health() {
        return Mono.just(Health.up().withDetail("aaa", "bbb").build());
    }
}
