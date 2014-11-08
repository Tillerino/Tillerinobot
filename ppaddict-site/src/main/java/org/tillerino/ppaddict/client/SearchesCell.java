package org.tillerino.ppaddict.client;

import java.util.LinkedList;
import java.util.List;

import org.tillerino.ppaddict.client.MinMaxCell.HasFillerCell;
import org.tillerino.ppaddict.shared.Searches;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class SearchesCell extends CompositeCell<Searches> {
  // public final class TagSuggester extends SuggestOracle {
  // @Override
  // public void requestDefaultSuggestions(Request request,
  // Callback callback) {
  // callback.onSuggestionsReady(request, new Response(Collections.singleton(new Suggestion() {
  //
  // @Override
  // public String getReplacementString() {
  // return "*";
  // }
  //
  // @Override
  // public String getDisplayString() {
  // return "(any)";
  // }
  // })));
  // }
  //
  // @Override
  // public void requestSuggestions(Request request, Callback callback) {
  //
  // }
  // }

  public interface LoggedIn {
    public boolean isLoggedIn();
  }

  // private GetElement getElement;
  // private LoggedIn loggedIn;

  public SearchesCell(LoggedIn loggedIn/* , getElement getElement */) {
    super(getList(loggedIn));
    // this.loggedIn = loggedIn;
    // this.getElement = getElement;
  }

  // @Override
  // public void render(com.google.gwt.cell.client.Cell.Context context,
  // Searches value, SafeHtmlBuilder sb) {
  // super.render(context, value, sb);
  //
  // Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
  // @Override
  // public void execute() {
  // wrapSuggester();
  // }
  // });
  // }
  //
  // SuggestBox detatchLater = null;

  // private void wrapSuggester() {
  // if(detatchLater != null) {
  // // RootPanel.detachNow(detatchLater);
  // detatchLater = null;
  // }
  //
  // if(!loggedIn.isLoggedIn())
  // return;
  //
  // SuggestOracle oracle = new TagSuggester();
  //
  // InputElement inputElement =
  // getElement.getElement().getChild(3).<SpanElement>cast().getChild(0).<InputElement>cast();
  // TextBox textBox = new InputTextBox(inputElement);
  // SuggestBoxWithPublicOnAttach suggestBox = new SuggestBoxWithPublicOnAttach(oracle, textBox);
  //
  // // Mark it attached and remember it for cleanup.
  // suggestBox.onAttach();
  //
  // // detatchLater = suggestBox;
  // }

  // static class InputTextBox extends TextBox {
  // private InputTextBox(Element element) {
  // super(element);
  // }
  // }
  //
  // static class SuggestBoxWithPublicOnAttach extends SuggestBox {
  // private SuggestBoxWithPublicOnAttach(SuggestOracle oracle,
  // ValueBoxBase<String> box) {
  // super(oracle, box);
  // }
  //
  // @Override
  // public void onAttach() {
  // addDomHandler(new FocusHandler() {
  // @Override
  // public void onFocus(FocusEvent event) {
  // showSuggestionList();
  // }
  // }, FocusEvent.getType());
  //
  // super.onAttach();
  // }
  // }

  // public interface GetElement {
  // Element getElement();
  // }

  private static List<HasCell<Searches, ?>> getList(final LoggedIn loggedIn) {
    LinkedList<HasCell<Searches, ?>> list = new LinkedList<>();

    list.add(new HasFillerCell<Searches>(null) {
      @Override
      public String getValue(Searches object) {
        if (loggedIn.isLoggedIn()) {
          return "Name: ";
        }
        return "";
      }
    });

    list.add(new HasCell<Searches, String>() {

      @Override
      public Cell<String> getCell() {
        return new TextInputCell();
      }

      @Override
      public FieldUpdater<Searches, String> getFieldUpdater() {
        return new FieldUpdater<Searches, String>() {

          @Override
          public void update(int index, Searches object, String value) {
            object.setSearchText(value);
          }
        };
      }

      @Override
      public String getValue(Searches object) {
        return object.getSearchText();
      }
    });

    list.add(new HasFillerCell<Searches>(null) {
      @Override
      public String getValue(Searches object) {
        if (loggedIn.isLoggedIn())
          return "<br />Notes: ";
        return "";
      }
    });

    list.add(new HasCell<Searches, String>() {

      @Override
      public Cell<String> getCell() {
        return new TextInputCell() {
          @Override
          public void render(com.google.gwt.cell.client.Cell.Context context, String value,
              SafeHtmlBuilder sb) {
            if (loggedIn.isLoggedIn())
              super.render(context, value, sb);
          }
        };
      }

      @Override
      public FieldUpdater<Searches, String> getFieldUpdater() {
        return new FieldUpdater<Searches, String>() {

          @Override
          public void update(int index, Searches object, String value) {
            object.setSearchComment(value);
          }
        };
      }

      @Override
      public String getValue(Searches object) {
        return object.getSearchComment();
      }
    });

    list.add(new HasCell<Searches, String>() {

      @Override
      public Cell<String> getCell() {
        return new ButtonCell() {
          @Override
          public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data,
              SafeHtmlBuilder sb) {
            if (loggedIn.isLoggedIn())
              super.render(context, data, sb);
          }
        };
      }

      @Override
      public FieldUpdater<Searches, String> getFieldUpdater() {
        return new FieldUpdater<Searches, String>() {
          @Override
          public void update(int index, Searches object, String value) {
            object.setSearchComment("*");
          }
        };
      }

      @Override
      public String getValue(Searches object) {
        return "(any)";
      }
    });

    return list;
  }
}
