package org.tillerino.ppaddict.client;

import java.util.Date;

import javax.annotation.CheckForNull;

import org.tillerino.ppaddict.client.HelpElements.E;
import org.tillerino.ppaddict.client.HelpElements.HasHelpElements;
import org.tillerino.ppaddict.client.dialogs.Side;
import org.tillerino.ppaddict.client.dialogs.WelcomeDialog;
import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;
import org.tillerino.ppaddict.client.services.UserDataService;
import org.tillerino.ppaddict.client.services.UserDataServiceAsync;
import org.tillerino.ppaddict.shared.InitialData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

public class Main extends Composite implements HasHelpElements {

  private static MainUiBinder uiBinder = GWT.create(MainUiBinder.class);
  @UiField
  DockLayoutPanel maindock;
  @UiField
  Hyperlink helpLink;
  @UiField
  HorizontalPanel controlsPanel;
  @UiField
  UserBox userBox;

  @UiField
  Hyperlink beatmapsNaviLink;
  @UiField
  Hyperlink recommendationsNaviLink;

  interface MainUiBinder extends UiBinder<Widget, Main> {
  }

  @CheckForNull
  AllBeatmapsTable beatmapsTable;

  RecommendationsView recommendations;

  private UserDataServiceAsync userDataService = GWT.create(UserDataService.class);

  public Main() {
    initWidget(uiBinder.createAndBindUi(this));

    help = new HelpElements(helpLink);

    help.addElement(this);
    help.addElement(userBox);

    userBox.setHelpButton(help);

    recommendations = new RecommendationsView(userBox);
    help.addElement(recommendations);

    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        if (beatmapsTable != null && beatmapsTable.isAttached()) {
          beatmapsTable.dock.forceLayout();
        }
        if (recommendations != null && recommendations.isAttached()) {
          recommendations.dock.forceLayout();
        }
      }
    });

    controlsPanel.getElement().getStyle().setPosition(Position.RELATIVE);

    System.out.println("requesting initial data");

    userDataService.getInitialData(new AbstractAsyncCallback<InitialData>() {
      @Override
      public void process(InitialData result) {
        beatmapsTable = new AllBeatmapsTable(result);

        help.addElement(beatmapsTable);

        userBox.setData(result.userData);

        /*
         * add the load handler AFTER the table was loaded with the initial data, otherwise there
         * would be double loading
         */
        userBox.addLoadHandler(beatmapsTable);
        userBox.addLoadHandler(recommendations);
        beatmapsTable.addBundleHandler(userBox.getBundleHandler());
        maindock.add(beatmapsTable);
      }
    });
  }

  public static final int VERSION = 1;
  public static final String VERSION_MESSAGE =
      "<ul><li>Data for all ranked beatmaps with current pp and difficulty. Bonus: mods.</li>"
          + "<li>Log in, link your account to osu! and click r! to get recommendations.</li></ul>";

  @Override
  protected void onLoad() {
    displayWelcomeMessage();
  }

  void displayWelcomeMessage() {
    String cookie = Cookies.getCookie("welcomeDisplayed");
    if (cookie != null) {
      try {
        int visitedVersion = Integer.parseInt(cookie);
        if (visitedVersion == VERSION) {
          return;
        }
      } catch (NumberFormatException e) {

      }
    }

    WelcomeDialog welcomeDialog = new WelcomeDialog();
    welcomeDialog.setVersionMessage(VERSION_MESSAGE);
    welcomeDialog.center();

    Date expires = new Date(System.currentTimeMillis() + 365l * 86400 * 1000);
    Cookies.setCookie("welcomeDisplayed", String.valueOf(VERSION), expires);
  };

  HelpElements help;

  @UiHandler("helpLink")
  void onHelpLinkClick(ClickEvent event) {
    help.showHelp();
  }

  @UiHandler("beatmapsNaviLink")
  void onBeatmapsNaviLinkClick(ClickEvent event) {
    if (userBox.getData() == null) {
      return;
    }

    if (maindock.getWidgetIndex(beatmapsTable) < 0) {
      maindock.remove(recommendations);

      maindock.add(beatmapsTable);
    }
  }

  @UiHandler("recommendationsNaviLink")
  void onRecommendationsNaviLinkClick(ClickEvent event) {
    if (userBox.getData() == null) {
      return;
    }

    if (maindock.getWidgetIndex(recommendations) < 0) {
      maindock.remove(beatmapsTable);

      maindock.add(recommendations);
    }
  }

  @Override
  public void showHelp(HelpElements elements) {
    if (!recommendations.isAttached()) {
      elements.positionAndShow(E.RECOMMEND_BUTTON, recommendationsNaviLink.getElement(),
          Side.BELOW_RIGHT, null);
    } else if (recommendations.loggedIn && recommendations.linked) {
      help.positionAndShow(E.RECOMMEND_HELP, recommendationsNaviLink.getElement(),
          Side.RIGHT_BELOW, null);
    }
  }

  public native static void sendPageView(String location) /*-{
		$wnd.ga('send', 'pageview', location);
  }-*/;

}
