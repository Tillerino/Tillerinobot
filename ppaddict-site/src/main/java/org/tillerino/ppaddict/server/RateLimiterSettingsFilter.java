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

import tillerino.tillerinobot.RateLimiter;

@Singleton
public class RateLimiterSettingsFilter implements Filter {
  private final RateLimiter rateLimiter;

  @Inject
  public RateLimiterSettingsFilter(RateLimiter rateLimiter) {
    super();
    this.rateLimiter = rateLimiter;
  }

  @Override
  public void destroy() {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    try {
      rateLimiter.setThreadPriority(RateLimiter.REQUEST);
      chain.doFilter(req, res);
    } finally {
      rateLimiter.clearThreadPriority();
    }
  }

  @Override
  public void init(FilterConfig config) throws ServletException {}
}
