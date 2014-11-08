package org.tillerino.ppaddict.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.tillerino.ppaddict.shared.PpaddictException;

import tillerino.tillerinobot.IRCBot;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.lang.Default;

public class ExceptionsUtil {

  public static PpaddictException getLoggedWrappedException(Logger logger, Throwable exception) {
    if (exception instanceof UserException) {
      return new PpaddictException(exception.getMessage());
    }
    if (exception instanceof IOException) {
      return new PpaddictException(new Default().externalException(IRCBot.logException(exception,
          logger)));
    }
    return new PpaddictException(new Default().internalException(IRCBot.logException(exception,
        logger)));
  }

}
