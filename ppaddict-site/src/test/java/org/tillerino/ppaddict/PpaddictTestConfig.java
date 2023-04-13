package org.tillerino.ppaddict;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;


public class PpaddictTestConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new PpaddictTestModule());
  }
}
