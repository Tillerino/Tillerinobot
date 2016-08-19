package org.tillerino.ppaddict.server;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import tillerino.tillerinobot.data.util.ThreadLocalAutoCommittingEntityManager;

@Singleton
public class EntityManagerFilter implements Filter {
  @Inject
  public EntityManagerFilter(EntityManagerFactory emf, ThreadLocalAutoCommittingEntityManager em) {
    super();
    this.emf = emf;
    this.em = em;
  }

  private final EntityManagerFactory emf;
  private final ThreadLocalAutoCommittingEntityManager em;

  @Override
  public void destroy() {}

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    try {
      em.setThreadLocalEntityManager(emf.createEntityManager());
      chain.doFilter(req, res);
    } finally {
      em.close();
    }
  }

  @Override
  public void init(FilterConfig config) throws ServletException {}
}
