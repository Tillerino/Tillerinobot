package org.tillerino.ppaddict.client.dialogs;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import org.tillerino.ppaddict.shared.PpaddictException;

public class DialogUtil {

    public static void displayMessageBox(String message) {
        PopupPanel box = new PopupPanel(true, true);
        box.setGlassEnabled(true);
        box.add(new HTML(new SafeHtmlBuilder().appendEscapedLines(message).toSafeHtml()));
        box.setWidth("300px");
        box.center();
    }

    public static void showException(PpaddictException e) {
        displayMessageBox("Error: " + e.getMessage());
    }
}
