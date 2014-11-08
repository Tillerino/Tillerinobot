package org.tillerino.ppaddict.auth;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tillerino.ppaddict.server.auth.AuthArriveService;

@Singleton
public class FakeAuthenticatorWebsite extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public static final String PATH = "/MYEXTERNALLOGINWEBSITE";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    resp.getOutputStream()
        .write(
            ("<html><form action='" + AuthArriveService.PATH + "' method='get'><input type='text' name='username'>Username</input> <input type='submit'></html>")
                .getBytes());
  }
}
