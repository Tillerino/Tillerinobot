package org.tillerino.ppaddict.chat.irc;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

public class NgircdContainer {
	public static final GenericContainer NGIRCD = new GenericContainer<>("linuxserver/ngircd:60428df3-ls19")
			.withNetwork(NETWORK)
			.withNetworkAliases("irc")
			.withClasspathResourceMapping("/irc/ngircd.conf", "/config/ngircd.conf", BindMode.READ_ONLY)
			.withClasspathResourceMapping("/irc/ngircd.motd", "/etc/ngircd/ngircd.motd", BindMode.READ_ONLY)
			.withExposedPorts(6667)
			.withLogConsumer(frame -> System.out.println("NGIRCD: " + frame.getUtf8String().trim()));

	static {
		NGIRCD.start();
	}
}
