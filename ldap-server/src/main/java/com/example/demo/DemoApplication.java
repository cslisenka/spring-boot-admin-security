package com.example.demo;

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	public void initLdapServer() throws Exception {
		DefaultDirectoryService service = new DefaultDirectoryService();
		service.getChangeLog().setEnabled( false );

//		Partition apachePartition = addPartition("apache", "dc=apache,dc=org");

		LdapServer ldapService = new LdapServer();
		ldapService.setTransports(new TcpTransport(389));
		ldapService.setDirectoryService(service);

		service.startup();
		ldapService.start();
	}
}
