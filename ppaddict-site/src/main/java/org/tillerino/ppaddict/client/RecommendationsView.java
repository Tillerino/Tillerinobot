package org.tillerino.ppaddict.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.tillerino.ppaddict.client.dialogs.DialogUtil;
import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;
import org.tillerino.ppaddict.client.services.RecommendationsService;
import org.tillerino.ppaddict.client.services.RecommendationsServiceAsync;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.ClientUserData;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowHoverEvent;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class RecommendationsView extends AbstractBeatmapTable {

  private static RecommendationsViewUiBinder uiBinder = GWT
      .create(RecommendationsViewUiBinder.class);

  interface RecommendationsViewUiBinder extends UiBinder<Widget, RecommendationsView> {
  }

  final UserBox userBox;

  @UiField
  DockLayoutPanel dock;

  RecommendationsServiceAsync recommendations = GWT.create(RecommendationsService.class);

  public RecommendationsView(UserBox userBox) {
    super();
    this.userBox = userBox;
    initWidget(uiBinder.createAndBindUi(this));

    createTable();

    addImageColumn(null);
    addLowPPColumn(false, null);
    addHighPPColumn(false, null);
    addNameColumn(null);
    addHideColumn();
    addEditColumn();
    addMoreColumn();
    addARColumn(null);
    addODColumn(null);
    addCSColumn(null);
    addBPMColumn(false, null);
    addLengthColumn(false, null);

    dock.add(table);
  }

  private Column<Beatmap, String> addHideColumn() {
    final Column<Beatmap, String> column = new Column<Beatmap, String>(new ButtonCell()) {
      @Override
      public String getValue(Beatmap object) {
        /*
         * leave cell empty until hovered over
         */
        return "";
      }
    };
    table.addRowHoverHandler(new RowHoverEvent.Handler() {

      @Override
      public void onRowHover(RowHoverEvent event) {
        if (loggedIn) {
          /*
           * this is super risky if gwt changes, but changing the column value didn't work because
           * redrawing undid the hover event
           */

          Element button =
              event.getHoveringRow().getChild(table.getColumnIndex(column))
                  .<TableCellElement>cast().getChild(0).<DivElement>cast().getChild(0)
                  .<ButtonElement>cast();

          button.setInnerText(event.isUnHover() ? "" : "Hide");
        }
      }
    });
    table.addColumn(column);
    table.setColumnWidth(column, 55, Unit.PX);
    column.setFieldUpdater(new FieldUpdater<Beatmap, String>() {
      @Override
      public void update(final int index, Beatmap object, String value) {
        hideRecommendation(index, object);
      }

    });
    setAlignRight(column);
    return column;
  }

  protected void hideRecommendation(final int index, Beatmap object) {
    Main.sendPageView("/ppaddict/recommendations");
    recommendations.hideRecommendation(object.beatmapid, object.mods,
        new AbstractAsyncCallback<Beatmap>() {
          @Override
          protected void process(Beatmap result) {
            rowData.remove(index);
            List<Beatmap> newRowData = new ArrayList<>();
            newRowData.add(result);
            newRowData.addAll(rowData);
            table.setRowData(rowData = newRowData);
          }
        });
  }

  boolean linked = false;

  protected List<Beatmap> rowData = new ArrayList<>();

  @Override
  protected void onLoad() {
    super.onLoad();

    if (!userBox.isLoggedIn()) {
      DialogUtil.displayMessageBox("Please log in to see recommendations!");
      table.setRowData(Collections.<Beatmap>emptyList());
      return;
    }

    loggedIn = true;

    if (!userBox.getData().isOsuName) {
      DialogUtil.displayMessageBox("Please link this account to an osu! account in your Settings!");
      table.setRowData(Collections.<Beatmap>emptyList());
      return;
    }

    linked = true;

    loadRecommendations();
  }

  @Override
  public void handle(ClientUserData data) {
    loggedIn = data.isLoggedIn();
    if (RecommendationsView.this.isAttached()) {
      if (data.isOsuName && !linked) {
        linked = true;
        loadRecommendations();
      } else if (!Objects.equals(settings, data.settings)) {
        loadRecommendations();
      }
    }
    settings = data.settings;

    super.handle(data);
  }

  protected void loadRecommendations() {
    Main.sendPageView("/ppaddict/recommendations");
    recommendations.getRecommendations(new AbstractAsyncCallback<List<Beatmap>>() {
      @Override
      protected void process(List<Beatmap> result) {
        rowData = result;
        table.setRowData(result);
      }
    });
  }

  @Override
  public void showHelp(HelpElements help) {
    if (!loggedIn || !linked) {
      onLoad();
    } else {
      super.showHelp(help);
    }
  }
}
