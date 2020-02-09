package org.tillerino.ppaddict.client.dialogs;

import org.tillerino.ppaddict.client.HelpElements;
import org.tillerino.ppaddict.client.HelpElements.E;
import org.tillerino.ppaddict.client.UserBox;
import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;
import org.tillerino.ppaddict.client.services.UserDataService;
import org.tillerino.ppaddict.client.services.UserDataServiceAsync;
import org.tillerino.ppaddict.shared.ClientUserData;
import org.tillerino.ppaddict.shared.PpaddictException;
import org.tillerino.ppaddict.shared.Settings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SettingsDialog extends AbstractSettingsDialog {

  private static SettingsDialogUiBinder uiBinder = GWT.create(SettingsDialogUiBinder.class);

  interface SettingsDialogUiBinder extends UiBinder<Widget, SettingsDialog> {
  }

  private Settings mySettingsCopy;
  private ClientUserData userData;

  private UserDataServiceAsync service = GWT.create(UserDataService.class);
  private UserBox box;

  public SettingsDialog(ClientUserData userData, UserBox box, HelpElements help) {
    super(help);
    add(uiBinder.createAndBindUi(this));
    this.box = box;

    setUserData(userData);
  }

  public void setUserData(ClientUserData userData) {
    this.userData = userData;
    mySettingsCopy = new Settings(userData.settings);
    checkAutoOpenDirect.setValue(mySettingsCopy.isOpenDirectOnMapSelect());
    otherFiltersWithText.setValue(mySettingsCopy.isApplyOtherFiltersWithTextFilter());
    lowAcc.setText(String.valueOf(mySettingsCopy.getLowAccuracy()));
    highAcc.setText(String.valueOf(mySettingsCopy.getHighAccuracy()));
    if (!userData.isOsuName) {
      linkOsuAccount.setEnabled(true);
      recommendationsSettingsRow.setVisible(false);
    } else {
      linkOsuAccount.setEnabled(false);
      linkOsuAccount.setText("linked to osu! account " + userData.nickname);
      recommendationsSettingsRow.setVisible(true);
      String params = mySettingsCopy.getRecommendationsParameters();
      if (params == null) {
        params = "*";
      }
      recommendationsParameters.setText(params);
    }
  }

  @UiField
  Button linkOsuAccount;
  @UiField
  CheckBox checkAutoOpenDirect;
  @UiField
  Button buttonSave;
  @UiField
  CheckBox otherFiltersWithText;
  @UiField
  HorizontalPanel recommendationsSettingsRow;
  @UiField
  TextBox recommendationsParameters;
  @UiField
  TextBox lowAcc;
  @UiField
  TextBox highAcc;

  @Override
  public void trySave() {
    final Settings savingSettings = new Settings(mySettingsCopy);
    service.saveSettings(savingSettings, new AbstractSettingsAsyncCallback<Void>() {
      @Override
      public void process(Void result) {
        userData.settings = savingSettings;
        box.setData(userData);
      }
    });
  }

  @UiHandler("checkAutoOpenDirect")
  void onCheckAutoOpenDirectClick(ClickEvent event) {
    mySettingsCopy.setOpenDirectOnMapSelect(checkAutoOpenDirect.getValue());
  }

  @UiHandler("otherFiltersWithText")
  void onOtherFiltersWithTextClick(ClickEvent event) {
    mySettingsCopy.setApplyOtherFiltersWithTextFilter(otherFiltersWithText.getValue());
  }

  @UiHandler("recommendationsParameters")
  void onChangeRecommendationsParameters(ValueChangeEvent<String> e) {
    String param = e.getValue();
    if (param.equals("*")) {
      param = null;
    }
    mySettingsCopy.setRecommendationsParameters(param);
  }

  @UiHandler("lowAcc")
  void onChangeLowAcc(ValueChangeEvent<String> e) {
    try {
      mySettingsCopy.setLowAccuracy(PpaddictException.parseDouble("Low Accuracy", e.getValue()));
    } catch (PpaddictException e1) {
      DialogUtil.showException(e1);
    }
  }

  @UiHandler("highAcc")
  void onChangeHighAcc(ValueChangeEvent<String> e) {
    try {
      mySettingsCopy.setHighAccuracy(PpaddictException.parseDouble("High Accuracy", e.getValue()));
    } catch (PpaddictException e1) {
      DialogUtil.showException(e1);
    }
  }

  Element helpButton;

  public void setHelpButton(Element helpButton) {
    this.helpButton = helpButton;
  }

  @UiHandler("linkOsuAccount")
  void onClickLinkOsuAccount(ClickEvent event) {
    LinkOsuAccountDialog linkOsuAccountDialog = new LinkOsuAccountDialog() {
      @Override
      void updateParents(ClientUserData userData) {
        setUserData(userData);
        box.setData(userData);
      }
    };
    linkOsuAccountDialog.addCloseHandler(new CloseHandler<PopupPanel>() {
      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        linkOsuAccount.setEnabled(!userData.isOsuName);
      }
    });
    linkOsuAccount.setEnabled(false);
    if (helpButton != null) {

      linkOsuAccountDialog.addAutoHidePartner(helpButton);
    }
    linkOsuAccountDialog.center();
  }

  @UiHandler("createApiKey")
  void onClickCreateApiKey(ClickEvent event) {
    if (!userData.isOsuName) {
      DialogUtil.displayMessageBox("Link to osu! account first");
      return;
    }
    new ConfirmDialog("This will revoke any existing API key. Are you sure?", new Runnable() {
      @Override
      public void run() {
        service.createApiKey(new AbstractAsyncCallback<String>() {
          @Override
          public void process(String result) {
            String message = "Your new API key is:\n\n" + result + ""
                    + "\n\nMake sure you save it - you won't be able to access it again.";
            DialogUtil.displayMessageBox(message);
          }
        });
      }
    }).center();
  }

  @Override
  Button getSaveButton() {
    return buttonSave;
  }

  @Override
  public void showHelp(HelpElements elements) {
    if (userData.isOsuName) {
      elements.positionAndShow(E.SETTINGS_RECOMMENDATIONS, recommendationsSettingsRow.getElement(),
          Side.RIGHT_BELOW, null);
    } else {
      elements
          .positionAndShow(E.SETTINGS_LINK, linkOsuAccount.getElement(), Side.RIGHT_BELOW, null);
    }
  }
}
