package org.tillerino.ppaddict.client.dialogs;

import org.tillerino.ppaddict.client.HelpElements;
import org.tillerino.ppaddict.client.services.UserDataService;
import org.tillerino.ppaddict.client.services.UserDataServiceAsync;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.Beatmap.Personalization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class EditBeatmapDialog extends AbstractSettingsDialog {

  private static EditBeatmapDialogUiBinder uiBinder = GWT.create(EditBeatmapDialogUiBinder.class);

  interface EditBeatmapDialogUiBinder extends UiBinder<Widget, EditBeatmapDialog> {
  }

  UserDataServiceAsync service = GWT.create(UserDataService.class);

  @UiField
  TextBox comment;
  @UiField
  Button save;
  private Beatmap beatmap;
  private Runnable afterSave;

  public EditBeatmapDialog(Beatmap beatmap, Runnable afterSave, HelpElements help) {
    super(help);
    add(uiBinder.createAndBindUi(this));
    this.beatmap = beatmap;
    if (beatmap.personalization != null && beatmap.personalization.comment != null
        && beatmap.personalization.comment.trim().length() > 0) {
      comment.setText(beatmap.personalization.comment);
    }
    this.afterSave = afterSave;
  }

  @Override
  protected void afterShow() {
    super.afterShow();

    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        comment.setFocus(true);
        comment.setCursorPos(comment.getText().length());
      }
    });
  }

  @Override
  Button getSaveButton() {
    return save;
  }

  @UiHandler("comment")
  public void onKeyPress(KeyPressEvent e) {
    if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      startSave();
    }
  }

  @Override
  void trySave() {
    final String text = comment.getText();
    service.saveComment(beatmap.beatmapid, beatmap.mods, text,
        new AbstractSettingsAsyncCallback<Void>() {
          @Override
          public void process(Void result) {
            if (beatmap.personalization == null) {
              beatmap.personalization = new Personalization();
            }
            beatmap.personalization.comment = text;
            beatmap.personalization.commentDate = "a moment ago";
            afterSave.run();
          }
        });
  }

  @Override
  public void showHelp(HelpElements elements) {

  }
}
