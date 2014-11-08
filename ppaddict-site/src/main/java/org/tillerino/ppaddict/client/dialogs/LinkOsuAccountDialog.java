package org.tillerino.ppaddict.client.dialogs;

import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;
import org.tillerino.ppaddict.client.services.UserDataService;
import org.tillerino.ppaddict.client.services.UserDataServiceAsync;
import org.tillerino.ppaddict.shared.ClientUserData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class LinkOsuAccountDialog extends PopupPanel {
  private UserDataServiceAsync service = GWT.create(UserDataService.class);

  private static LinkOsuAccountDialogUiBinder uiBinder = GWT
      .create(LinkOsuAccountDialogUiBinder.class);

  interface LinkOsuAccountDialogUiBinder extends UiBinder<Widget, LinkOsuAccountDialog> {
  }

  public LinkOsuAccountDialog() {
    super(true, true);
    add(uiBinder.createAndBindUi(this));
    setGlassEnabled(true);
  }

  @Override
  protected void onLoad() {
    service.getLinkString(new AbstractAsyncCallback<String>() {
      @Override
      public void process(String result) {
        code.setText(result);
      }
    });
  }

  @UiField
  Label code;

  @UiField
  Button done;

  @UiHandler("done")
  public void clickDone(ClickEvent event) {
    service.getStatus(new AbstractAsyncCallback<ClientUserData>() {
      @Override
      public void process(ClientUserData result) {
        updateParents(result);
        if (result.isOsuName) {
          DialogUtil.displayMessageBox("Your account has been linked to " + result.nickname);
          hide();
        } else {
          DialogUtil.displayMessageBox("Your account has not yet been linked. Try again!");
        }
      }
    });
  }

  abstract void updateParents(ClientUserData userData);
}
