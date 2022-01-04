package org.tillerino.ppaddict.server;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.MDC;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tillerino.tillerinobot.RateLimiter;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;
import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager.ResetEntityManagerCloseable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class PpaddictContextFilter implements Filter {
  private final ThreadLocalAutoCommittingEntityManager em;
  private final RateLimiter rateLimiter;

  @Override
  public void destroy() {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    MDC.clear();
    try {
      try(ResetEntityManagerCloseable cl = em.withNewEntityManager();) {
        rateLimiter.setThreadPriority(RateLimiter.REQUEST);
        chain.doFilter(req, res);
      } finally {
        rateLimiter.clearThreadPriority();
      }
    } catch (Throwable e) {
      log.error("Error serving request", e);
      throw e;
    } finally {
      MDC.clear();
    }
  }

  @Override
  public void init(FilterConfig config) throws ServletException {}
}
