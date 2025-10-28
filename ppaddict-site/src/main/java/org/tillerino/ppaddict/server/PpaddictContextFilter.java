package org.tillerino.ppaddict.server;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import tillerino.tillerinobot.RateLimiter;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class PpaddictContextFilter implements Filter {
    private final RateLimiter rateLimiter;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        MDC.clear();
        try {
            try {
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
}
