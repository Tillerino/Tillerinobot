package org.tillerino.ppaddict.client.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import org.tillerino.ppaddict.client.services.UserDataService;
import org.tillerino.ppaddict.client.services.UserDataServiceAsync;
import org.tillerino.ppaddict.shared.Beatmap;

public class MoreDialog extends PopupPanel {

    private static MoreDialogUiBinder uiBinder = GWT.create(MoreDialogUiBinder.class);

    interface MoreDialogUiBinder extends UiBinder<Widget, MoreDialog> {}

    UserDataServiceAsync service = GWT.create(UserDataService.class);

    @UiField
    Anchor permalinkBeatmap;

    @UiField
    Anchor permalinkSet;

    public MoreDialog(Beatmap beatmap) {
        add(uiBinder.createAndBindUi(this));
        permalinkBeatmap.setHref(Window.Location.getPath() + "?b=" + beatmap.beatmapid);
        permalinkSet.setHref(Window.Location.getPath() + "?s=" + beatmap.setid);

        setAutoHideEnabled(true);
    }
}
