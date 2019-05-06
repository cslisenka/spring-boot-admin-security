package com.example.demo;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
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
//@EnableWebFluxSecurity
public class DemoApplication {

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
		// Usually used for user-related events
//		System.out.println("Running 2...");
		AuditEvent evt = new AuditEvent("principal", "type", "data");
		auditEventRepository.add(evt);
//		throw new RuntimeException("error");
	}

	@Bean
	public TimedAspect timed(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

	// TODO
//	@Bean
//	public SecurityWebFilterChain securityWebFilterChain(
//			ServerHttpSecurity http) {
//		return http.authorizeExchange()
//				.pathMatchers("/actuator/**").permitAll()
//				.anyExchange().authenticated()
//				.and().build();
//	}

//	@Configuration
//	public static class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    /*
//        This spring security configuration does the following
//
//        1. Restrict access to the Shutdown endpoint to the ACTUATOR_ADMIN role.
//        2. Allow access to all other actuator endpoints.
//        3. Allow access to static resources.
//        4. Allow access to the home page (/).
//        5. All other requests need to be authenticated.
//        5. Enable http basic authentication to make the configuration complete.
//           You are free to use any other form of authentication.
//     */
//
//		@Override
//		protected void configure(HttpSecurity http) throws Exception {
//			http
//					.authorizeRequests()
//					.requestMatchers(EndpointRequest.to(ShutdownEndpoint.class))
//					.hasRole("ACTUATOR_ADMIN")
//					.requestMatchers(EndpointRequest.toAnyEndpoint())
//					.permitAll()
//					.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
//					.permitAll()
//					.antMatchers("/")
//					.permitAll()
//					.antMatchers("/**")
//					.authenticated()
//					.and()
//					.httpBasic();
//		}
}