package org.tillerino.ppaddict;

import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.servlet.ServletContextEvent;

import org.tillerino.ppaddict.server.PpaddictUserDataService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

import tillerino.tillerinobot.BotBackend;
import tillerino.tillerinobot.IrcNameResolver;
import tillerino.tillerinobot.TestBackend;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager.ResetEntityManagerCloseable;
import tillerino.tillerinobot.handlers.LinkPpaddictHandler;


public class PpaddictTestConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new PpaddictTestModule());
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    super.contextInitialized(servletContextEvent);
    Injector injector =
        (Injector) servletContextEvent.getServletContext().getAttribute(Injector.class.getName());
    listenForLinkKeysInTheConsole(injector);
  }

  private void listenForLinkKeysInTheConsole(Injector injector) {
    TestBackend botBackend = (TestBackend) injector.getInstance(BotBackend.class);
    IrcNameResolver resolver = injector.getInstance(IrcNameResolver.class);
    PpaddictUserDataService userDataService = injector.getInstance(PpaddictUserDataService.class);
    ThreadLocalAutoCommittingEntityManager em =
        injector.getInstance(ThreadLocalAutoCommittingEntityManager.class);
    new Thread(() -> {
      Scanner scanner = new Scanner(System.in);
      for (;;) {
        try {
          System.out.println("Please give me your link token");
          String token = scanner.nextLine();
          if (!LinkPpaddictHandler.TOKEN_PATTERN.matcher(token).matches()) {
            continue;
          }
          System.out.println("Please tell me your osu name");
          String name = scanner.nextLine();
          try(ResetEntityManagerCloseable cl = em.withNewEntityManager()) {
            botBackend.hintUser(name, false, 100000, 1000);
            int osuId = resolver.resolveIRCName(name);
            System.out.println(userDataService.tryLinkToPpaddict(token, osuId));
          }
        } catch (NoSuchElementException e) {
          // no more lines. shutting down.
          break;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }).start();
  }
}
