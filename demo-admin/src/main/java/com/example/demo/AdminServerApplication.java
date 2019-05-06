package com.example.demo;

import com.hazelcast.config.*;
import com.hazelcast.map.merge.PutIfAbsentMapMergePolicy;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableAdminServer
@SpringBootApplication
public class AdminServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminServerApplication.class, args);

		// TODO enable security
		// https://www.baeldung.com/spring-boot-admin
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
}
