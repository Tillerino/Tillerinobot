package tillerino.tillerinobot;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.WebApplicationException;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.MillisSinceEpoch;
import org.tillerino.ppaddict.chat.GameChatEvent;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatResponse;
import org.tillerino.ppaddict.chat.GameChatResponse.Message;
import org.tillerino.ppaddict.chat.GameChatResponseQueue;
import org.tillerino.ppaddict.chat.IRCName;
import org.tillerino.ppaddict.chat.Joined;
import org.tillerino.ppaddict.chat.LiveActivity;
import org.tillerino.ppaddict.chat.PrivateAction;
import org.tillerino.ppaddict.chat.PrivateMessage;
import org.tillerino.ppaddict.chat.Sighted;
import org.tillerino.ppaddict.util.LoggingUtils;
import org.tillerino.ppaddict.util.MaintenanceException;
import org.tillerino.ppaddict.util.MdcUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.tillerino.ppaddict.util.PhaseTimer;
import tillerino.tillerinobot.UserDataManager.UserData;
import tillerino.tillerinobot.handlers.AccHandler;
import tillerino.tillerinobot.handlers.ComplaintHandler;
import tillerino.tillerinobot.handlers.DebugHandler;
import tillerino.tillerinobot.handlers.FixIDHandler;
import tillerino.tillerinobot.handlers.HelpHandler;
import tillerino.tillerinobot.handlers.LinkPpaddictHandler;
import tillerino.tillerinobot.handlers.NPHandler;
import tillerino.tillerinobot.handlers.OptionsHandler;
import tillerino.tillerinobot.handlers.OsuTrackHandler;
import tillerino.tillerinobot.handlers.RecentHandler;
import tillerino.tillerinobot.handlers.RecommendHandler;
import tillerino.tillerinobot.handlers.ResetHandler;
import tillerino.tillerinobot.handlers.WithHandler;
import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;
import tillerino.tillerinobot.osutrack.OsutrackDownloader;
import tillerino.tillerinobot.osutrack.UpdateResult;
import tillerino.tillerinobot.recommendations.RecommendationRequestParser;
import tillerino.tillerinobot.recommendations.RecommendationsManager;

/**
 * The name of this class is rather historical. It implements the basic chat
 * event -> response logic and is the heart of Tillerinobot. All IRC matters
 * have been moved to other classes nowadays.
 */
@Slf4j
public class IRCBot implements GameChatEventConsumer {
	private final BotBackend backend;
	private final UserDataManager userDataManager;
	private final List<CommandHandler> commandHandlers = new ArrayList<>();
	private final IrcNameResolver resolver;
	private final OsutrackDownloader osutrackDownloader;
	private final RateLimiter rateLimiter;
	private final GameChatResponseQueue queue;
	private final NPHandler npHandler;

	@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Injection")
	@Inject
	public IRCBot(BotBackend backend, RecommendationsManager manager,
			UserDataManager userDataManager,
			IrcNameResolver resolver, OsutrackDownloader osutrackDownloader, RateLimiter rateLimiter,
			LiveActivity liveActivity, GameChatResponseQueue queue) {
		this.backend = backend;
		this.userDataManager = userDataManager;
		this.resolver = resolver;
		this.osutrackDownloader = osutrackDownloader;
		this.rateLimiter = rateLimiter;
		this.queue = queue;
		this.npHandler = new NPHandler(backend, liveActivity);

		commandHandlers.add(new ResetHandler(manager));
		commandHandlers.add(new OptionsHandler(new RecommendationRequestParser(backend)));
		commandHandlers.add(new AccHandler(backend, liveActivity));
		commandHandlers.add(new WithHandler(backend, liveActivity));
		commandHandlers.add(new RecommendHandler(manager, liveActivity));
		commandHandlers.add(new RecentHandler(backend));
		commandHandlers.add(new DebugHandler(backend, resolver));
		commandHandlers.add(new HelpHandler());
		commandHandlers.add(new ComplaintHandler(manager));
		commandHandlers.add(new OsuTrackHandler(osutrackDownloader, backend));
	}

	private GameChatResponse processPrivateAction(PrivateAction action) throws InterruptedException {
		GameChatResponse versionInfo = GameChatResponse.none();
		try {
			versionInfo = checkVersionInfo(action);
			OsuApiUser apiUser = getUserOrThrow(action.getNick());
			try (UserData userData = userDataManager.loadUserData(apiUser.getUserId())) {
				return versionInfo.then(userData.usingLanguage((Language lang) -> { 
					try {
						return npHandler.handle(action.getAction(), apiUser, userData, lang);
					} catch (RuntimeException | Error | UserException | IOException | SQLException e) {
						return handleException(e, lang);
					}
				}));
			}
		} catch (RuntimeException | Error | UserException | IOException | SQLException e) {
			return versionInfo.then(handleException(e, new Default()));
		}
	}

	private GameChatResponse handleException(Throwable e, Language lang) {
		try {
			if(e instanceof ExecutionException) {
				e = e.getCause();
			}
			if(e instanceof InterruptedException) {
				return GameChatResponse.none();
			}
			if ((e instanceof MaintenanceException) || (e instanceof SQLRecoverableException)) {
				return new Message("I'm undergoing maintenance and can't do that right now.");
			}
			if(e instanceof UserException) {
				return new Message(e.getMessage());
			} else {
				if (e instanceof ServiceUnavailableException) {
					// We're shutting down. Nothing to do here.
					return GameChatResponse.none();
				} else if (isTimeout(e)) {
					log.debug("osu api timeout");
					return new Message(lang.apiTimeoutException());
				} else {
					String string = logException(e, log);
	
					if ((e instanceof IOException) || isExternalException(e)) {
						return new Message(lang.externalException(string));
					} else {
						return new Message(lang.internalException(string));
					}
				}
			}
		} catch (Throwable e1) {
			log.error("holy balls", e1);
			return GameChatResponse.none();
		}
	}

	/**
	 * Checks if this is a JAX-RS exception that would have been thrown because
	 * an external osu resource is not available.
	 */
	public static boolean isExternalException(Throwable e) {
		if (!(e instanceof WebApplicationException)) {
			return false;
		}
		int code = ((WebApplicationException) e).getResponse().getStatus();
		/*
		 * 502 = Bad Gateway
		 * 504 = Gateway timeout
		 * 520 - 527 = Cloudflare, used by osu's web endpoints
		 */
		return code == 502 || code == 504 || (code >= 520 && code <= 527);
	}

	public static boolean isTimeout(Throwable e) {
		return (e instanceof SocketTimeoutException)
				|| ((e instanceof IOException) && e.getMessage() != null && (e.getMessage().startsWith("Premature EOF")
						|| e.getMessage().startsWith("Remote host terminated the handshake for https://osu.ppy.sh/api/")
						|| e.getMessage().startsWith("Error writing to server for https://osu.ppy.sh/api/")
						|| e.getMessage().startsWith("Unexpected end of file from server for https://osu.ppy.sh/api/")
						|| e.getMessage().startsWith("Broken pipe for https://osu.ppy.sh/api/")
						|| e.getMessage().startsWith("response code 502 for https://osu.ppy.sh/api/")
						|| e.getMessage().startsWith("response code 503 for https://osu.ppy.sh/api/")
						|| e.getMessage().startsWith("response code 520 for https://osu.ppy.sh/api/")
						|| e.getMessage().startsWith("response code 525 for https://osu.ppy.sh/api/")
						|| e.getMessage().startsWith("Connection reset for https://osu.ppy.sh/api/")
						));
	}

	public static String logException(Throwable e, Logger logger) {
		String string = LoggingUtils.getRandomString(6);
		logger.error(string + ": fucked up", e);
		return string;
	}

	/**
	 * Queues a response and ensures that the semaphore is released if it is
	 * held by the current thread.
	 */
	void sendResponse(@Nullable GameChatResponse response, GameChatEvent user) throws InterruptedException {
		if (response != null) {
			user.getMeta().setRateLimiterBlockedTime(rateLimiter.blockedTime());
			queue.onResponse(response, user);
		}
	}

	private GameChatResponse processPrivateMessage(final PrivateMessage message) throws InterruptedException {
		try {
			try(var _ = PhaseTimer.timeTask("fixId")) {
				GameChatResponse fixIdResponse = new FixIDHandler(resolver).handle(message.getMessage(), null, null, new Default());
				if (fixIdResponse != null) {
					return fixIdResponse;
				}
			}
			OsuApiUser apiUser = getUserOrThrow(message.getNick());
			try (UserData userData = userDataManager.loadUserData(apiUser.getUserId())) {
				return userData.usingLanguage((Language lang) -> {
					GameChatResponse prelimResponse = GameChatResponse.none();

					try {
						Pattern hugPattern = Pattern.compile("\\bhugs?\\b", Pattern.CASE_INSENSITIVE);

						if (hugPattern.matcher(message.getMessage()).find() && userData.getHearts() > 0) {
							return lang.hug(apiUser);
						}

						{
							GameChatResponse linkResponse = new LinkPpaddictHandler(backend)
									.handle(message.getMessage(), apiUser, userData, lang);
							if (linkResponse != null) {
								return linkResponse;
							}
						}

						if (!message.getMessage().startsWith("!")) {
							return GameChatResponse.none();
						}
						String originalMessage = message.getMessage().substring(1).trim();

						prelimResponse = checkVersionInfo(message);

						GameChatResponse response = null;
						for (CommandHandler handler : commandHandlers) {
							if ((response = handler.handle(originalMessage, apiUser, userData, lang)) != null) {
								break;
							}
							// removes the current handler's label from the MDC since it didn't work and we
							// don't want it around for the next one
							MDC.remove(MdcUtils.MDC_HANDLER);
						}
						if (response == null) {
							throw new UserException(lang.unknownCommand(originalMessage));
						}
						return prelimResponse.then(response);
					} catch (RuntimeException | Error | UserException | IOException | SQLException e) {
						return prelimResponse.then(handleException(e, lang));
					}
				});
			}
		} catch (RuntimeException | Error | UserException | IOException | SQLException e) {
			return handleException(e, new Default());
		}
	}

	private GameChatResponse checkVersionInfo(final GameChatEvent user) throws SQLException, UserException {
		int userVersion = backend.getLastVisitedVersion(user.getNick());
		if(userVersion < CURRENT_VERSION) {
			backend.setLastVisitedVersion(user.getNick(), CURRENT_VERSION);
			if (userVersion == -1) {
				return new Message(new Default().help());
			}
			return new Message(VERSION_MESSAGE);
		}
		return GameChatResponse.none();
	}

	@Override
	public void onEvent(GameChatEvent event) {
		try {
			rateLimiter.setThreadPriority(event.isInteractive() ? RateLimiter.REQUEST : RateLimiter.EVENT);
			// clear blocked time in case it wasn't cleared by the last thread
			rateLimiter.blockedTime();

			try {
				GameChatResponse visit;
				try(var _ = PhaseTimer.timeTask("visit")) {
					visit = visit(event);
				} finally {
					event.completePhase(PhaseTimer.HANDLE);
				}
				sendResponse(visit, event);
			} catch (SQLException | IOException | UserException e) {
				GameChatResponse exceptionResponse = handleException(e, new Default());
				if (event.isInteractive()) {
					sendResponse(exceptionResponse, event);
				} else {
					// we do this just to clear the semaphore
					sendResponse(GameChatResponse.none(), event);
				}
			}

			/*
			 * We delay registering the activity until after the event has been handled to
			 * avoid a race condition and to make sure that the event handler can find
			 * out the actual last active time.
			 */
			registerActivity(event.getNick(), event.getTimestamp());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		} finally {
			rateLimiter.clearThreadPriority();
			// clear blocked time so it isn't carried over to the next request under any circumstances
			rateLimiter.blockedTime();
			MDC.clear();
		}
	}

	@SuppressFBWarnings(value = "SA_LOCAL_SELF_COMPARISON", justification = "Looks like a bug")
	private GameChatResponse visit(GameChatEvent event) throws SQLException, InterruptedException, IOException, UserException {
		if (event instanceof Joined) {
			try {
				return welcomeIfDonator(event);
			} catch (SQLException | IOException | UserException | MaintenanceException e) {
				handleException(e, new Default());
				// don't return the created response, because we don't want the user to get random error messages
				return GameChatResponse.none();
			}
		} else if (event instanceof PrivateMessage message) {
			return processPrivateMessage(message);
		} else if (event instanceof PrivateAction action) {
			return processPrivateAction(action);
		} else if (event instanceof Sighted) {
			return GameChatResponse.none();
		}
		throw new NotImplementedException("Event not implemented: " + event);
	}

	public static final int CURRENT_VERSION = 13;
	public static final String VERSION_MESSAGE = "This is embarrasing. Looks like I didn't know about the HD nerf for a quite a while."
			+ " Don't worry: I'm all caught up now. I'll be updating my pp records over the following days.";

	private GameChatResponse welcomeIfDonator(GameChatEvent user) throws SQLException, InterruptedException, IOException, UserException {
		Integer userid;
		try {
			userid = resolver.resolveIRCName(user.getNick());
		} catch (IOException e) {
			if (isTimeout(e)) {
				log.debug("timeout while resolving username {} (welcomeIfDonator)", user.getNick());
				return GameChatResponse.none();
			}
			throw e;
		}
		
		if(userid == null)
			return GameChatResponse.none();
		
		OsuApiUser apiUser;
		try {
			apiUser = backend.getUser(userid, 0);
		} catch (IOException e) {
			if (isTimeout(e)) {
				log.debug("osu api timeout while getting user {} (welcomeIfDonator)", userid);
				return GameChatResponse.none();
			}
			throw e;
		}
		
		if(apiUser == null || backend.getDonator(apiUser.getUserId()) <= 0) {
			return GameChatResponse.none();
		}

		// this is a donator, let's welcome them!
		try (UserData data = userDataManager.loadUserData(userid)) {
			if (!data.isShowWelcomeMessage()) {
				return GameChatResponse.none();
			}

			long inactiveTime = System.currentTimeMillis() - backend.getLastActivity(apiUser);

			GameChatResponse response = data.usingLanguage((Language lang) -> lang.welcomeUser(apiUser,
					inactiveTime));

			if (data.isOsuTrackWelcomeEnabled()) {
				UpdateResult update = osutrackDownloader.getUpdate(apiUser.getUserId());
				response = response.then(OsuTrackHandler.updateResultToResponse(update));
			}

			return response.then(checkVersionInfo(user));
		}
	}

	private void registerActivity(final @IRCName String fNick, @MillisSinceEpoch long timestamp) {
		try {
			Integer userid = resolver.resolveIRCName(fNick);
			
			if(userid == null) {
				return;
			}
			
			backend.registerActivity(userid, timestamp);
		} catch (MaintenanceException e) {
			// ignore
		} catch (Exception e) {
			if (isTimeout(e)) {
				log.debug("osu api timeout while logging activity of user {}", fNick);
			} else {
				log.error("error logging activity", e);
			}
		}
	}

	@Nonnull
	OsuApiUser getUserOrThrow(@IRCName String nick) throws UserException, SQLException, IOException, InterruptedException {
		Integer userId;
		try (var _ = PhaseTimer.timeTask("resolve")) {
			userId = resolver.resolveIRCName(nick);
		}

		if(userId != null) {
			OsuApiUser apiUser = backend.getUser(userId, 60 * 60 * 1000L);
			
			if(apiUser != null) {
				String apiUserIrcName = IrcNameResolver.getIrcUserName(apiUser);
				if (!nick.equals(apiUserIrcName)) {
					// oh no, detected wrong mapping, mismatch between API user and IRC user
					
					// sets the mapping according to the API user (who doesnt belong to the current IRC user)
					log.info("Detected mapping weirdness. Nick {}; id {}; username {}; api->irc name {}", nick, userId,
							apiUser.getUserName(), apiUserIrcName);
					resolver.setMapping(apiUserIrcName, apiUser.getUserId());
					
					// fix the now known broken mapping with the current irc user
					apiUser = resolver.redownloadUser(nick);
				}
			}

			if (apiUser != null) {
				return apiUser;
			}
		}
		
		String string = LoggingUtils.getRandomString(8);
		log.warn("bot user not resolvable " + string + " name: " + nick);
		
		// message not in language-files, since we cant possible know language atm
		throw new UserException("Your name is confusing me. Are you banned? If not, pls check out [https://github.com/Tillerino/Tillerinobot/wiki/How-to-fix-%22confusing-name%22-error this page] on how to resolve it!"
				+ " if that does not work, pls [https://github.com/Tillerino/Tillerinobot/wiki/Contact contact Tillerino]. (reference "
				+ string + ")");
	}
}
