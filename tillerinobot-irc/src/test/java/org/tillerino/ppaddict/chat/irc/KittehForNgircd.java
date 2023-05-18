package org.tillerino.ppaddict.chat.irc;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.Client.Builder.Server.SecurityType;
import org.kitteh.irc.client.library.exception.KittehNagException;
import org.kitteh.irc.client.library.feature.sending.QueueProcessingThreadSender;

public class KittehForNgircd {

	public static Client buildKittehClient(String nick) {
		return Client.builder()
			.nick(nick)
			.server().host(NgircdContainer.NGIRCD.getHost()).port(NgircdContainer.NGIRCD.getMappedPort(6667), SecurityType.INSECURE)
			.then().listeners().exception(e -> {
				if (!(e instanceof KittehNagException)) {
					e.printStackTrace();
				}
			})
			.then().management().messageSendingQueueSupplier(m -> new QueueProcessingThreadSender(m.getClient(), "no pause"))
			.then().build();
	}

}
