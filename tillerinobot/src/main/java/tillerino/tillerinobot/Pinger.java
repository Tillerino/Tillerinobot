package tillerino.tillerinobot;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;

import org.pircbotx.Utils;
import org.pircbotx.hooks.events.UnknownEvent;

import tillerino.tillerinobot.BotRunnerImpl.CloseableBot;
import tillerino.tillerinobot.mbeans.AbstractMBeanRegistration;
import tillerino.tillerinobot.mbeans.PingerMXBean;
import tillerino.tillerinobot.rest.BotInfoService;

public class Pinger {
	@Singleton
	public static class MXBean extends AbstractMBeanRegistration.WithNotifications implements
			PingerMXBean {
		public MXBean() {
			super(IRCBot.class.getPackage(), Pinger.class, null);
		}

		@Override
		public void setPingDeathPending(boolean b) {
			this.triggerPingDeath = b;
		}

		@Override
		public boolean isPingDeathPending() {
			return triggerPingDeath;
		}

		@Override
		public long getLastPing() {
			return lastPing;
		}

		AtomicLong sequence = new AtomicLong();

		public void setLastPing(long l) {
			long oldPing = getLastPing();
			this.lastPing = l;
			sendNotification(new AttributeChangeNotification(this,
					sequence.incrementAndGet(), System.currentTimeMillis(),
					"Ping " + lastPing, "LastPing", "long", oldPing, lastPing));
		}

		@Override
		public MBeanNotificationInfo[] getNotificationInfo() {
			return new MBeanNotificationInfo[] { new MBeanNotificationInfo(
					new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE },
					AttributeChangeNotification.class.getName(),
					"Attribute change") };
		}

		long lastPing = 0;

		boolean triggerPingDeath = false;

		private long lastPingDeath;

		public void setLastPingDeath(long l) {
			this.lastPingDeath = l;
		}

		@Override
		public long getLastPingDeath() {
			return lastPingDeath;
		}
	}

	volatile String pingMessage = null;
	volatile CountDownLatch pingLatch = null;
	final AtomicBoolean quit = new AtomicBoolean(false);
	
	final BotInfoService botInfoService;
	private Pinger.MXBean bean;

	@Inject
	public Pinger(BotInfoService infoService, Pinger.MXBean bean) {
		this.botInfoService = infoService;
		this.bean = bean;
	}

	long lastquit = 0l;

	/*
	 * this method is synchronized through the sender semaphore
	 */
	void ping(CloseableBot bot) throws IOException, InterruptedException {
		try {
			if(quit.get()) {
				throw new IOException("ping gate closed");
			}
			
			if(!bot.isConnected()) {
				throw new IOException("bot is no longer connected");			
			}

			if (bean.isPingDeathPending()) {
				bean.setPingDeathPending(false);
				throw new IOException("Ping death was triggered");
			}

			long time = System.currentTimeMillis();

			synchronized (this) {
				pingLatch = new CountDownLatch(1);
				pingMessage = IRCBot.getRandomString(16);
			}

			Utils.sendRawLineToServer(bot, "PING " + pingMessage);

			if(!pingLatch.await(10, TimeUnit.SECONDS)) {
				throw new IOException("ping timed out");
			}

			bean.setLastPing(System.currentTimeMillis() - time);

			if (bean.getLastPing() > 1500) {
				if (botInfoService != null) {
					bean.setLastPingDeath(System.currentTimeMillis());
					botInfoService.setLastPingDeath(System
							.currentTimeMillis());
				}
				throw new IOException("death ping: " + bean.getLastPing());
			}
		} catch(IOException e) {
			if (lastquit < System.currentTimeMillis() - 1000 || !quit.get()) {
				quit.set(true);
				bot.sendIRC().quitServer();
				try {
					bot.getSocket().close();
				} catch (IOException e1) {
					// swallow
				}
				lastquit = System.currentTimeMillis();
			}
			throw e;
		}
	}
	
	void handleUnknownEvent(@SuppressWarnings("rawtypes") UnknownEvent event) {
		synchronized(this) {
			if (pingMessage == null)
				return;

			boolean contains = event.getLine().contains(" PONG ");
			boolean endsWith = event.getLine().endsWith(pingMessage);
			if (contains
					&& endsWith) {
				pingLatch.countDown();
			}
		}
	}
}