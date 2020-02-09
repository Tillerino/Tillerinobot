package org.tillerino.ppaddict.client.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple confirmation dialog. Displays the a message followed by an "Ok" button.
 * If the button is clicked, a configurable action is performed.
 */
public class ConfirmDialog extends PopupPanel {
  private static ConfirmDialogUiBinder uiBinder = GWT.create(ConfirmDialogUiBinder.class);

  interface ConfirmDialogUiBinder extends UiBinder<Widget, ConfirmDialog> {
  }

  private final Runnable onConfirm;

  public ConfirmDialog(String message, Runnable onConfirm) {
    super(true, true);
    add(uiBinder.createAndBindUi(this));
    setGlassEnabled(true);
    this.message.setText(message);
    this.onConfirm = onConfirm;
  }

  @UiField
  Label message;

  @UiField
  Button done;

  @UiHandler("done")
  public void clickDone(ClickEvent event) {
    onConfirm.run();
    hide();
  }
}
