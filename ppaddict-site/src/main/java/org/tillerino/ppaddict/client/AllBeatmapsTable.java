package org.tillerino.ppaddict.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.tillerino.ppaddict.client.HelpElements.E;
import org.tillerino.ppaddict.client.HelpElements.HasHelpElements;
import org.tillerino.ppaddict.client.MinMaxCell.HasFillerCell;
import org.tillerino.ppaddict.client.MinMaxCell.NumberType;
import org.tillerino.ppaddict.client.SearchesCell.LoggedIn;
import org.tillerino.ppaddict.client.dialogs.Side;
import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;
import org.tillerino.ppaddict.client.services.BeatmapTableService;
import org.tillerino.ppaddict.client.services.BeatmapTableServiceAsync;
import org.tillerino.ppaddict.client.theTable.MyPager;
import org.tillerino.ppaddict.client.util.CellDecorator;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.BeatmapBundle;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest;
import org.tillerino.ppaddict.shared.BeatmapRangeRequest.Sort;
import org.tillerino.ppaddict.shared.ClientUserData;
import org.tillerino.ppaddict.shared.InitialData;
import org.tillerino.ppaddict.shared.MinMax;
import org.tillerino.ppaddict.shared.Searches;
import org.tillerino.ppaddict.shared.Settings;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CustomScrollPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

public class AllBeatmapsTable extends AbstractBeatmapTable implements HasHelpElements {
  public static class ButtonHeader extends Header<String> {
    private String title;

    public ButtonHeader(String title) {
      super(new ButtonCell());
      this.title = title;
    }

    @Override
    public String getValue() {
      return title;
    }
  }

  public interface BundleHandler {
    void handle(BeatmapBundle bundle);
  }

  public class MyDataProvider extends AsyncDataProvider<Beatmap> {
    @Nonnull
    private BeatmapRangeRequest request = new BeatmapRangeRequest();
    private boolean enabled = true;

    private MyDataProvider() {}

    @Override
    protected void onRangeChanged(HasData<Beatmap> display) {
      if (!enabled) {
        System.out.println("ignored range change");
        return;
      }

      Range r = display.getVisibleRange();

      getRequest().start = r.getStart();
      getRequest().length = r.getLength();
      System.out.println("initial: " + getRequest());

      boolean wasScrolled = false;
      try {
        for (int i = 0; i < r.getLength(); i++) {
          if (display.getVisibleItem(i) != null) {
            getRequest().start++;
            getRequest().length--;
            wasScrolled = true;
          }
        }
      } catch (IndexOutOfBoundsException e) {
        // guess there's no data?
      }
      if (!wasScrolled) {
        pager.lastScrollPos = 0;
      }

      AsyncCallback<BeatmapBundle> callback = new AbstractAsyncCallback<BeatmapBundle>() {
        @Override
        public void process(BeatmapBundle result) {
          System.out.println(result.beatmaps.size() + " beatmaps loaded");

          for (BundleHandler handler : bundleLoadHandlers) {
            handler.handle(result);
          }

          applyBundle(getRequest(), result);
        }
      };

      System.out.println("clean: " + getRequest());

      Main.sendPageView("/ppaddict/beatmaps");
      beatmapService.getRange(getRequest(), callback);
      pager.lastLoadFired = System.currentTimeMillis();
    }

    @Nonnull
    public BeatmapRangeRequest getRequest() {
      return request;
    }

    private void setRequest(@Nonnull BeatmapRangeRequest request) {
      this.request = request;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  private static TheTableUiBinder uiBinder = GWT.create(TheTableUiBinder.class);
  @UiField
  DockLayoutPanel dock;

  interface TheTableUiBinder extends UiBinder<Widget, AllBeatmapsTable> {
  }

  MyPager pager;
  final MyDataProvider provider = new MyDataProvider();
  final Map<Sort, Column<?, ?>> sortToColumn = new HashMap<Sort, Column<?, ?>>();
  final Map<Column<?, ?>, Sort> columnToSort = new HashMap<Column<?, ?>, Sort>();

  List<BundleHandler> bundleLoadHandlers = new LinkedList<>();

  public void addBundleHandler(BundleHandler handler) {
    bundleLoadHandlers.add(handler);
  }

  public AllBeatmapsTable(InitialData initialData) {
    System.out.println("initial request: " + initialData.request);
    provider.setRequest(initialData.request);

    initWidget(uiBinder.createAndBindUi(this));

    createTable();

    addImageColumn();

    makeSortable(addLowPPColumn(), Sort.EXPECTED);
    makeSortable(addHighPPColumn(), Sort.PERFECT);

    addNameColumn();
    addEditColumn();
    addMoreColumn();
    addARColumn();
    addODColumn();
    addCSColumn();
    makeSortable(addStarDiffColumn(), Sort.STAR_DIFF);
    makeSortable(addBPMColumn(), Sort.BPM);
    makeSortable(addLengthColumn(), Sort.LENGTH);

    table.setEmptyTableWidget(new Label("no beatmaps found"));

    table.setRowCount(5000, false);

    provider.setEnabled(false);

    table.addColumnSortHandler(new Handler() {
      @Override
      public void onColumnSort(ColumnSortEvent event) {
        ColumnSortList columnSortList = table.getColumnSortList();
        if (columnSortList != null && columnSortList.size() > 0) {
          ColumnSortInfo info = columnSortList.get(0);
          provider.getRequest().sortBy = columnToSort.get(info.getColumn());
          provider.getRequest().direction = info.isAscending() ? 1 : -1;
        }

        table.setVisibleRangeAndClearData(new Range(0, AbstractBeatmapTable.PAGE_SIZE), true);
      }
    });

    pager =
        new MyPager((CustomScrollPanel) ((HeaderPanel) table.getWidget()).getContentWidget(),
            PAGE_SIZE);
    pager.setDisplay(table);

    provider.addDataDisplay(table);

    dock.addSouth(pager, 20);
    dock.add(table);

    /* hackz to get the pager centered */
    pager.getElement().getStyle().setProperty("marginLeft", "auto");
    pager.getElement().getStyle().setProperty("marginRight", "auto");
    pager.getElement().getStyle().setPosition(Position.RELATIVE);

    if (initialData.request.sortBy != null) {
      table.getColumnSortList().push(sortToColumn.get(initialData.request.sortBy));
    }

    handle(initialData.userData);

    applyBundle(provider.getRequest(), initialData.beatmapBundle);

    provider.setEnabled(true);
  }

  private void makeSortable(Column<Beatmap, ?> column, Sort type) {
    sortToColumn.put(type, column);
    columnToSort.put(column, type);
    column.setSortable(true);
  }

  private TextColumn<Beatmap> addHighPPColumn() {
    Header<MinMax> footer = new FilterFooter<>(
        new MinMaxCell("threecharminmaxcell", "0", "999", NumberType.INTEGER),
        () -> provider.getRequest().perfectPP.getCopy());

    footer.setUpdater(value -> {
      provider.getRequest().perfectPP = value.getCopy();
      reloadTableWithChangedRequest(0);
    });

    return addHighPPColumn(true, footer);
  }

  private TextColumn<Beatmap> addLowPPColumn() {
    Header<MinMax> footer = new FilterFooter<>(
        new MinMaxCell("threecharminmaxcell", "0", "999", NumberType.INTEGER),
        () -> provider.getRequest().expectedPP.getCopy());

    footer.setUpdater(value -> {
      provider.getRequest().expectedPP = value.getCopy();
      reloadTableWithChangedRequest(0);
    });

    return addLowPPColumn(true, footer);
  }

  // these values are inverted since we use the _text_ of the button as a value
  static final String HIDING_FILTER = "show filters";
  static final String SHOWING_FILTER = "hide filters";
  String filterState = HIDING_FILTER;

  private class HideStateAndRankedOnly {
    String filterState = AllBeatmapsTable.this.filterState;
    boolean rankedOnly = AllBeatmapsTable.this.provider.getRequest().rankedOnly;
  }

  protected final BeatmapTableServiceAsync beatmapService = GWT.create(BeatmapTableService.class);

  private Column<Beatmap, SafeHtml> addImageColumn() {
    Header<HideStateAndRankedOnly> f = new Header<HideStateAndRankedOnly>(new CompositeCell<>(Arrays.asList(
        hasCell(ButtonCell::new,
            (index, object, value) -> object.filterState = value.equals(HIDING_FILTER) ? SHOWING_FILTER : HIDING_FILTER,
            object -> object.filterState),
          renderIfShowingFilter(new HasFillerCell<>("<br />")),
          hasCell(() -> renderIfShowingFilter(new CheckboxCell()) , (index, object, value) -> {
            object.rankedOnly = value;
          }, object -> object.rankedOnly),
          renderIfShowingFilter(new HasFillerCell<>("ranked only"))))) {
      HideStateAndRankedOnly value = new HideStateAndRankedOnly();
      @Override
      public HideStateAndRankedOnly getValue() {
        return value;
      }
    };

    f.setUpdater(value -> {
      if (!value.filterState.equals(filterState)) {
        filterState = value.filterState;
        table.redrawFooters();
      }
      if (value.rankedOnly != provider.getRequest().rankedOnly) {
        provider.getRequest().rankedOnly = value.rankedOnly;
        reloadTableWithChangedRequest(0);
      }
    });

    return addImageColumn(f);
  }

  private Column<Beatmap, SafeHtml> addLengthColumn() {
    Header<MinMax> footer = new FilterFooter<>(
        new MinMaxCell("fivecharminmaxcell", "0:00", "99:59", NumberType.TIME),
        () -> provider.getRequest().mapLength.getCopy());

    footer.setUpdater(value -> {
      provider.getRequest().mapLength = value.getCopy();
      reloadTableWithChangedRequest(0);
    });

    return addLengthColumn(true, footer);
  }

  private TextColumn<Beatmap> addBPMColumn() {
    Header<MinMax> footer = new FilterFooter<>(
        new MinMaxCell("threecharminmaxcell", "0", "999", NumberType.INTEGER),
        () -> provider.getRequest().bpm.getCopy());

    footer.setUpdater(value -> {
      provider.getRequest().bpm = value.getCopy();
      reloadTableWithChangedRequest(0);
    });

    return addBPMColumn(true, footer);
  }

  private TextColumn<Beatmap> addStarDiffColumn() {
    Header<MinMax> footer = new FilterFooter<>(
        new MinMaxCell("threecharminmaxcell", "0.0", "9.9", NumberType.DECIMAL),
        () -> provider.getRequest().starDiff.getCopy());

    footer.setUpdater(value -> {
      provider.getRequest().starDiff = value.getCopy();
      reloadTableWithChangedRequest(0);
    });

    return addStarDiffColumn(true, footer);
  }

  private TextColumn<Beatmap> addCSColumn() {
    Header<MinMax> footer = new FilterFooter<>(
        new MinMaxCell("threecharminmaxcell", "0.0", "10.0", NumberType.DECIMAL),
        () -> provider.getRequest().cS.getCopy());

    footer.setUpdater(value -> {
      provider.getRequest().cS = value.getCopy();
      reloadTableWithChangedRequest(0);
    });

    return addCSColumn(footer);
  }

  private TextColumn<Beatmap> addARColumn() {
    Header<MinMax> footer = new FilterFooter<>(
        new MinMaxCell("threecharminmaxcell", "0.0", "12.0", NumberType.DECIMAL),
        () -> provider.getRequest().aR.getCopy());

    footer.setUpdater(value -> {
      provider.getRequest().aR = value.getCopy();
      reloadTableWithChangedRequest(0);
    });

    return addARColumn(footer);
  }

  private TextColumn<Beatmap> addODColumn() {
    Header<MinMax> footer = new FilterFooter<>(
        new MinMaxCell("threecharminmaxcell", "0.0", "12.0", NumberType.DECIMAL),
        () -> provider.getRequest().oD.getCopy());

    footer.setUpdater(value -> {
      provider.getRequest().oD = value.getCopy();
      reloadTableWithChangedRequest(0);
    });

    return addODColumn(footer);
  }

  private Column<Beatmap, SafeHtml> addNameColumn() {
    Header<Searches> footer = new FilterFooter<>(new SearchesCell(new LoggedIn() {
      @Override
      public boolean isLoggedIn() {
        return loggedIn;
      }
    }), () -> provider.getRequest().getSearches().getCopy());

    footer.setUpdater(value -> {
      if (value.getSearchComment().equals(provider.getRequest().getSearches().getSearchComment())
          && value.getSearchText().equals(provider.getRequest().getSearches().getSearchText())) {
        return;
      }

      provider.getRequest().setSearches(value);
      reloadTableWithChangedRequest(0);
    });

    return addNameColumn(footer);
  }

  class FilterFooter<T> extends Header<T> {
    private final Supplier<T> value;

    FilterFooter(Cell<T> cell, Supplier<T> value) {
      super(cell);
      this.value = value;
    }

    @Override
    public boolean onPreviewColumnSortEvent(Context context, Element elem, NativeEvent event) {
      return false;
    }

    @Override
    public void render(Context context, SafeHtmlBuilder sb) {
      if (filterState.equals(HIDING_FILTER)) {

      } else {
        super.render(context, sb);
      }
    }

    @Override
    public T getValue() {
      return value.get();
    }
  }

  @Override
  public void showHelp(final HelpElements help) {
    super.showHelp(help);

    final CloseHandler<PopupPanel> closeHandler;

    if (filterState.equals(HIDING_FILTER)) {
      closeHandler = new CloseHandler<PopupPanel>() {
        @Override
        public void onClose(CloseEvent<PopupPanel> event) {
          filterState = HIDING_FILTER;
          table.redrawFooters();
        }
      };

      filterState = SHOWING_FILTER;
      table.redrawFooters();
    } else {
      closeHandler = null;
    }

    Scheduler.get().scheduleFinally(new ScheduledCommand() {
      @Override
      public void execute() {
        NodeList<TableCellElement> tableFooter =
            table.getTableFootElement().getRows().getItem(0).getCells();

        help.positionAndShow(E.NAME_FILTER, tableFooter.getItem(3), Side.ABOVE_RIGHT, closeHandler);
        help.positionAndShow(E.RANGE_FILTER, tableFooter.getItem(5), Side.ABOVE_RIGHT, closeHandler);
      }
    });
  }

  @Override
  public void handle(/* not null */ClientUserData data) {
    boolean reloadTable = false;
    boolean redrawFooter = false;

    if (loggedIn != data.isLoggedIn()) {
      redrawFooter = true;
    }

    loggedIn = data.isLoggedIn();

    provider.getRequest().loadedUserRequest = loggedIn;

    if (!Objects.equals(data.settings, settings)) {
      settings = new Settings(data.settings);
      reloadTable = true;
      table.redrawHeaders();
    }

    if (reloadTable) {
      reloadTableWithChangedRequest(provider.getRequest().start);
    }
    if (redrawFooter) {
      table.redrawFooters();
    }
  }

  private void applyBundle(BeatmapRangeRequest request, BeatmapBundle result) {
    table.setRowCount(result.available, true);
    table.setRowData(request.start, result.beatmaps);
  }

  protected void reloadTableWithChangedRequest(int start) {
    table.setVisibleRangeAndClearData(new Range(start, PAGE_SIZE), true);
  }

  <C> Cell<C> renderIfShowingFilter(Cell<C> actual) {
    return new CellDecorator<C>(actual) {
      @Override
      public void render(Context context, C value, SafeHtmlBuilder sb) {
        if (filterState.equals(SHOWING_FILTER)) {
          super.render(context, value, sb);
        }
      }
    };
  }

  <T, C> HasCell<T, C> renderIfShowingFilter(HasCell<T, C> actual) {
    return hasCell(() -> renderIfShowingFilter(actual.getCell()), actual.getFieldUpdater(), actual::getValue);
  }

  static <T, C> HasCell<T, C> hasCell(Supplier<Cell<C>> cell, FieldUpdater<T, C> fieldUpdater, Function<T, C> value) {
    return new HasCell<T, C>() {
      @Override
      public Cell<C> getCell() {
        return cell.get();
      }

      @Override
      public FieldUpdater<T, C> getFieldUpdater() {
        return fieldUpdater;
      }

      @Override
      public C getValue(T object) {
        return value.apply(object);
      }};
  }
}
