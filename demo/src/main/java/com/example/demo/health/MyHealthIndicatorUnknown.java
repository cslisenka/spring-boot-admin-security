package com.example.demo.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("health-unknown")
public class MyHealthIndicatorUnknown implements ReactiveHealthIndicator {

    @Override
    public Mono<Health> health() {
        return Mono.just(Health.status(Status.UNKNOWN).build());
    }
}
