package org.tillerino.ppaddict.client.dialogs;

import org.tillerino.ppaddict.client.HelpElements;
import org.tillerino.ppaddict.client.HelpElements.PopupPanelWithHelpElements;
import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;

public abstract class AbstractSettingsDialog extends PopupPanelWithHelpElements {
  public abstract class AbstractSettingsAsyncCallback<T> extends AbstractAsyncCallback<T> {
    @Override
    protected void cleanUp(boolean success) {
      if (success) {
        setSuccessState();
      } else {
        setFailedState();
      }
    }
  }

  public AbstractSettingsDialog(HelpElements help) {
    super(help);
  }

  abstract Button getSaveButton();

  public void show(Element ref, Side side) {
    getSaveButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        startSave();
      }

    });
    popupNextTo(this, ref, side);
    afterShow();
  }

  protected void afterShow() {

  }

  protected void startSave() {
    setAutoHideEnabled(false);
    getSaveButton().setEnabled(false);
    trySave();
  }

  abstract void trySave();

  protected void setSuccessState() {
    hide();
  }

  protected void setFailedState() {
    setAutoHideEnabled(true);
    getSaveButton().setEnabled(true);
  }

  public static void popupNextTo(final PopupPanel p, final Element logButton, final Side side) {
    p.setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        Side runSide = side;
        int top = 0;
        int left = 0;

        for (int i = 1; i <= 2; i++) {
          switch (runSide) {
            case BELOW_LEFT:
              left = logButton.getAbsoluteLeft() + logButton.getOffsetWidth() - offsetWidth;
              top = logButton.getAbsoluteTop() + logButton.getOffsetHeight();
              if (top + offsetHeight > Window.getClientHeight()) {
                runSide = Side.ABOVE_LEFT;
                continue;
              }
              break;
            case BELOW_RIGHT:
              left = logButton.getAbsoluteLeft();
              top = logButton.getAbsoluteTop() + logButton.getOffsetHeight();
              if (top + offsetHeight > Window.getClientHeight()) {
                runSide = Side.ABOVE_RIGHT;
                continue;
              }
              break;
            case ABOVE_LEFT:
              left = logButton.getAbsoluteLeft() + logButton.getOffsetWidth() - offsetWidth;
              top = logButton.getAbsoluteTop() - offsetHeight;
              if (top < 0) {
                runSide = Side.BELOW_LEFT;
                continue;
              }
              break;
            case ABOVE_RIGHT:
              left = logButton.getAbsoluteLeft();
              top = logButton.getAbsoluteTop() - offsetHeight;
              if (top < 0) {
                runSide = Side.BELOW_RIGHT;
                continue;
              }
              break;
            default:
              throw new RuntimeException(runSide + "");
          }

          break;
        }

        p.setPopupPosition(left, top);
      }
    });
  }
}
