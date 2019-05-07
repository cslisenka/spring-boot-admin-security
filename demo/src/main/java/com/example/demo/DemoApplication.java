package com.example.demo;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableWebFlux
@EnableScheduling
@EnableWebFluxSecurity
public class DemoApplication {

	public static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	private final Counter counter;
	private final Timer timer;
	private final AuditEventRepository auditEventRepository;

	public DemoApplication(MeterRegistry registry, AuditEventRepository auditEventRepository) {
		this.counter = Counter.builder("my.counter")
				.description("description")
				.tag("tag", "value")
				.register(registry);

		this.timer = Timer.builder("my.time.2")
				.description("bla bla bla")
				// For exporting whole histogram
				.maximumExpectedValue(Duration.ofMillis(500))
				.minimumExpectedValue(Duration.ofMillis(100))
				.publishPercentiles(0.5, 0.98, 0.99)
				.sla(Duration.ofMillis(100), Duration.ofMillis(400), Duration.ofMillis(600))
				.publishPercentileHistogram()
				.register(registry);

		this.auditEventRepository = auditEventRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@GetMapping("/")
	public Mono<String> webService() {
		return Mono.just("response");
	}

	@Timed(value = "timed.test", description = "timed.description", histogram = true, percentiles = {0.5, 0.95, 0.98}, extraTags = {"tag1", "tag2"})
	@Scheduled(fixedDelay = 1000)
	public void scheduledTask() {
		log.info("task 1 info");
		log.info("task 1 debug");

		timer.record(() -> {
//			System.out.println("Running...");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			counter.increment();
		});

	}

	@Scheduled(fixedDelay = 1000)
	public void scheduledTask2() {
		log.info("task 2 info");
		log.info("task 2 debug");

		// Usually used for user-related events
		AuditEvent evt = new AuditEvent("principal", "type", "data");
		auditEventRepository.add(evt);
	}

	@Bean
	public TimedAspect timed(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

	@Bean
	public MapReactiveUserDetailsService userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder()
				.username("user")
				.password("user")
				.roles("USER")
				.build();

		UserDetails admin = User.withDefaultPasswordEncoder()
				.username("admin")
				.password("admin")
				.roles("USER", "ADMIN")
				.build();
		return new MapReactiveUserDetailsService(user, admin);
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.authorizeExchange()
				.pathMatchers("/**").authenticated()
				.and()
					.httpBasic()
				.and()
					.csrf().disable()
				.build();
	}
}