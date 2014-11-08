package org.tillerino.ppaddict.client.services;

import org.tillerino.ppaddict.client.dialogs.DialogUtil;
import org.tillerino.ppaddict.shared.PpaddictException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AbstractAsyncCallback<T> implements AsyncCallback<T> {
  @Override
  public void onFailure(Throwable caught) {
    String message =
        "Something went wrong while communicating with the server. Try reloading the page!";
    if (caught instanceof PpaddictException) {
      message = caught.getMessage();
    }
    DialogUtil.displayMessageBox(message);
    cleanUp(false);
  }

  @Override
  public void onSuccess(T result) {
    process(result);
    cleanUp(true);
  }

  protected abstract void process(T result);

  protected void cleanUp(boolean success) {

  }
}
