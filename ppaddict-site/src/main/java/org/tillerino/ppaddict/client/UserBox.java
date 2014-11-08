package org.tillerino.ppaddict.client;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import org.tillerino.ppaddict.client.AllBeatmapsTable.BundleHandler;
import org.tillerino.ppaddict.client.HelpElements.E;
import org.tillerino.ppaddict.client.HelpElements.HasHelpElements;
import org.tillerino.ppaddict.client.dialogs.AbstractSettingsDialog;
import org.tillerino.ppaddict.client.dialogs.SettingsDialog;
import org.tillerino.ppaddict.client.dialogs.Side;
import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;
import org.tillerino.ppaddict.client.services.UserDataService;
import org.tillerino.ppaddict.client.services.UserDataServiceAsync;
import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.ClientUserData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class UserBox extends Composite implements HasHelpElements {

  private static UserBoxUiBinder uiBinder = GWT.create(UserBoxUiBinder.class);
  @UiField
  InlineLabel userNameLabel;
  @UiField
  Button logButton;
  @UiField
  Button settingsButton;

  interface UserBoxUiBinder extends UiBinder<Widget, UserBox> {
  }

  private UserDataServiceAsync userDataService = GWT.create(UserDataService.class);
  private ClientUserData myData;

  public UserBox() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  private void loadUserData() {
    userDataService.getStatus(new AbstractAsyncCallback<ClientUserData>() {
      @Override
      public void process(ClientUserData result) {
        setData(result);
      }
    });
  }

  HelpElements help;

  public void setHelpButton(HelpElements help) {
    this.help = help;
  }

  @UiHandler("settingsButton")
  void onSettingsButtonClick(ClickEvent event) {
    SettingsDialog settingsDialog = new SettingsDialog(myData, this, help);
    settingsDialog.show(settingsButton.getElement(), Side.BELOW_LEFT);
  }

  @UiHandler("logButton")
  void onLogButtonClick(ClickEvent event) {
    if (myData == null) {
      loadUserData();
      return;
    }
    if (!isLoggedIn()) {
      List<SafeHtml> loginElements = myData.loginElements;
      if (loginElements == null) {
        throw new RuntimeException("nickname null, but loginElements null as well");
      }
      final PopupPanel p = new HelpElements.PopupPanelWithHelpElements(help) {
        @Override
        public void showHelp(HelpElements elements) {
          elements.positionAndShow(HelpElements.E.LOGIN_DIALOG, this.getElement(),
              Side.BELOW_RIGHT, null);
        }
      };

      FlowPanel flow = new FlowPanel();
      p.add(flow);
      flow.add(new Label("Please choose an identity provider:"));
      flow.add(new Label(""));
      for (SafeHtml provider : loginElements) {
        HTMLPanel panel = new HTMLPanel(provider);
        flow.add(panel);
      }

      AbstractSettingsDialog.popupNextTo(p, logButton.getElement(), Side.BELOW_LEFT);
    } else {
      Window.Location.assign(myData.logoutURL);
    }
  }

  interface UserDataHandler {
    void handle(@Nonnull ClientUserData data);
  }

  private List<UserDataHandler> loadHandlers = new LinkedList<UserDataHandler>();

  public void addLoadHandler(UserDataHandler handler) {
    loadHandlers.add(handler);
  }

  public boolean isLoggedIn() {
    return myData != null && myData.isLoggedIn();
  }

  public void setData(ClientUserData result) {
    myData = result;
    logButton.setEnabled(myData != null);
    settingsButton.setVisible(isLoggedIn());
    if (isLoggedIn()) {
      userNameLabel.setText(myData.nickname);
      logButton.setText("Logout");
    } else {
      logButton.setText("Login");
      userNameLabel.setText("");
    }
    if (myData == null) {
      return;
    }
    for (UserDataHandler h : loadHandlers) {
      System.out.println("invoking load handlers of " + h);
      h.handle(myData);
    }
  }

  public ClientUserData getData() {
    return myData;
  }

  @Override
  public void showHelp(HelpElements help) {
    if (!isLoggedIn()) {
      help.positionAndShow(E.LOGIN_BUTTON, logButton.getElement(), Side.BELOW_LEFT, null);
    }
  }

  public BundleHandler getBundleHandler() {
    return new BundleHandler() {

      @Override
      public void handle(BeatmapBundle bundle) {
        if (myData != null && myData.isLoggedIn() && !bundle.loggedIn) {
          loadUserData();
        }
      }
    };
  }
}
