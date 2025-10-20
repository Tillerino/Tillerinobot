package org.tillerino.ppaddict.client.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WelcomeDialog extends PopupPanel {

    private static WelcomeDialogUiBinder uiBinder = GWT.create(WelcomeDialogUiBinder.class);

    interface WelcomeDialogUiBinder extends UiBinder<VerticalPanel, WelcomeDialog> {}

    @UiField
    HTMLPanel versionMessagePanel;

    public WelcomeDialog() {
        add(uiBinder.createAndBindUi(this));

        setGlassEnabled(true);
        setAutoHideEnabled(true);
    }

    public void setVersionMessage(String message) {
        versionMessagePanel.getElement().setInnerHTML(message);
    }
}
