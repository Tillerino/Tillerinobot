package org.tillerino.ppaddict.client;

import java.util.LinkedList;
import java.util.List;

import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.MinMax;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class MinMaxCell extends CompositeCell<MinMax> {
  public static class NumberType {
    public static final NumberType INTEGER = new NumberType();
    public static final NumberType DECIMAL = new NumberType();
    public static final NumberType TIME = new NumberType();
  }

  static class MyTextCell extends TextInputCell {
    String styleClass;

    public MyTextCell(String styleClass) {
      this.styleClass = styleClass;
    }

    interface Template extends SafeHtmlTemplates {
      @Template("<input type=\"text\" value=\"{0}\" tabindex=\"-1\" class=\"chillinput {1}\"></input>")
      SafeHtml input(String value, String styleClass);
    }

    Template template = GWT.create(Template.class);

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, String value,
        SafeHtmlBuilder sb) {
      Object key = context.getKey();
      ViewData viewData = getViewData(key);
      if (viewData != null && viewData.getCurrentValue().equals(value)) {
        clearViewData(key);
        viewData = null;
      }

      String s = (viewData != null) ? viewData.getCurrentValue() : value;
      sb.append(template.input(s != null ? s : "", styleClass));
    }
  }

  static class HasFillerCell<T> implements HasCell<T, String> {
    private String value;

    HasFillerCell(String value) {
      this.value = value;
    }

    @Override
    public Cell<String> getCell() {
      return new AbstractCell<String>() {
        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
          sb.appendHtmlConstant(value);
        }
      };
    }

    @Override
    public FieldUpdater<T, String> getFieldUpdater() {
      return new FieldUpdater<T, String>() {
        @Override
        public void update(int index, T object, String value) {}
      };
    }

    @Override
    public String getValue(T object) {
      return value;
    }
  }

  public MinMaxCell(String styleClass, String min, String max, NumberType numberType) {
    super(MinMaxCell.getList(styleClass, min, max, numberType));
  }

  private static List<HasCell<MinMax, ?>> getList(final String styleClass, final String min,
      final String max, final NumberType numberType) {
    List<HasCell<MinMax, ?>> subCells = new LinkedList<>();

    subCells.add(new HasFillerCell<>("&ge;"));

    subCells.add(new HasCell<MinMax, String>() {
      @Override
      public Cell<String> getCell() {
        return new MyTextCell(styleClass);
      }

      @Override
      public FieldUpdater<MinMax, String> getFieldUpdater() {
        return new FieldUpdater<MinMax, String>() {
          @Override
          public void update(int index, MinMax object, String value) {
            object.min = stringToInt(value, min, numberType);
          }
        };
      }

      @Override
      public String getValue(MinMax object) {
        return intToString(object.min, min, numberType);
      }
    });

    subCells.add(new HasFillerCell<MinMax>("<br />&le;"));

    subCells.add(new HasCell<MinMax, String>() {

      @Override
      public Cell<String> getCell() {
        return new MyTextCell(styleClass);
      }

      @Override
      public FieldUpdater<MinMax, String> getFieldUpdater() {
        return new FieldUpdater<MinMax, String>() {
          @Override
          public void update(int index, MinMax object, String value) {
            object.max = stringToInt(value, max, numberType);
          }
        };
      }

      @Override
      public String getValue(MinMax object) {
        return intToString(object.max, max, numberType);
      }
    });

    return subCells;
  }

  enum Type {
    MIN, MAX
  }

  static String intToString(Integer i, String nullString, NumberType numberType) {
    if (i == null) {
      return nullString;
    }
    if (numberType == NumberType.TIME) {
      return Beatmap.secondsToMinuteColonSecond(i);
    }
    if (numberType == NumberType.INTEGER)
      return String.valueOf(i);
    if (numberType == NumberType.DECIMAL)
      return String.valueOf(i / 100d);
    throw new RuntimeException();
  }

  static Integer stringToInt(String s, String nullString, NumberType numberType) {
    s = s.trim();
    if (s.equals(nullString))
      return null;
    try {
      if (numberType == NumberType.DECIMAL)
        return (int) Math.round(Double.valueOf(s) * 100);
      else if (numberType == NumberType.INTEGER)
        return Integer.valueOf(s);
      else
        return Beatmap.minuteColonSecondToSeconds(s);
    } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
      return null;
    }
  }
}
