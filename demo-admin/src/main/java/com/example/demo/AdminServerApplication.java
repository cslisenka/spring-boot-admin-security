package com.example.demo;

import com.hazelcast.config.*;
import com.hazelcast.map.merge.PutIfAbsentMapMergePolicy;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;

import java.net.URI;

@EnableAdminServer
@SpringBootApplication
@EnableWebFluxSecurity
public class AdminServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminServerApplication.class, args);
	}

	// For running multiple instances of admin-server in HA mode (do not loose application events if fail)
	@Bean
	public Config hazelcastConfig() {
		MapConfig mapConfig = new MapConfig("spring-boot-admin-event-store")
				.setInMemoryFormat(InMemoryFormat.OBJECT)
				.setBackupCount(1)
				.setEvictionPolicy(EvictionPolicy.NONE)
				.setMergePolicyConfig(new MergePolicyConfig(
						PutIfAbsentMapMergePolicy.class.getName(),
						100
				));
		return new Config().setProperty("hazelcast.jmx", "true").addMapConfig(mapConfig);
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
		RedirectServerAuthenticationSuccessHandler handler = new RedirectServerAuthenticationSuccessHandler();
		handler.setLocation(URI.create("/"));

		return http
			.authorizeExchange()
				.pathMatchers("/assets/**", "/login").permitAll()
				.anyExchange().authenticated()
			.and()
				.formLogin()
					.loginPage("/login")
					.authenticationSuccessHandler(handler)
			.and()
				.logout()
					.logoutUrl("/logout")
			.and()
				.httpBasic()
			.and()
				// TODO test client and server on different hosts, there is some issue with CSRF
				.csrf().disable()
//				.csrf()
//					.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
//			.and()
				.build();
	}
}