package org.tillerino.ppaddict.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.tillerino.ppaddict.client.HelpElements.E;
import org.tillerino.ppaddict.client.HelpElements.HasHelpElements;
import org.tillerino.ppaddict.client.MinMaxCell.NumberType;
import org.tillerino.ppaddict.client.SearchesCell.LoggedIn;
import org.tillerino.ppaddict.client.dialogs.Side;
import org.tillerino.ppaddict.client.services.AbstractAsyncCallback;
import org.tillerino.ppaddict.client.services.BeatmapTableService;
import org.tillerino.ppaddict.client.services.BeatmapTableServiceAsync;
import org.tillerino.ppaddict.client.theTable.MyPager;
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
import com.google.gwt.cell.client.ValueUpdater;
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

  public static abstract class FooterValueUpdater<T> implements ValueUpdater<T> {
    @Override
    final public void update(T value) {
      doUpdate(value);
    }

    protected abstract void doUpdate(T value);
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
  MyDataProvider provider;
  final Map<Sort, Column<?, ?>> sortToColumn = new HashMap<Sort, Column<?, ?>>();
  final Map<Column<?, ?>, Sort> columnToSort = new HashMap<Column<?, ?>, Sort>();

  List<BundleHandler> bundleLoadHandlers = new LinkedList<>();

  public void addBundleHandler(BundleHandler handler) {
    bundleLoadHandlers.add(handler);
  }

  public AllBeatmapsTable(InitialData initialData) {
    initWidget(uiBinder.createAndBindUi(this));

    createTable();

    addImageColumn();

    makeSortable(addLowPPColumn(), Sort.EXPECTED);
    makeSortable(addHighPPColumn(), Sort.PERFECT);

    addNameColumn();
    addEditColumn();
    addARColumn();
    addODColumn();
    addCSColumn();
    makeSortable(addStarDiffColumn(), Sort.STAR_DIFF);
    makeSortable(addBPMColumn(), Sort.BPM);
    makeSortable(addLengthColumn(), Sort.LENGTH);

    table.setEmptyTableWidget(new Label("no beatmaps found"));

    table.setRowCount(5000, false);

    provider = new MyDataProvider();
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

    System.out.println("initial request: " + initialData.request);
    provider.setRequest(initialData.request);
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
    Header<MinMax> footer =
        new FilterFooter<MinMax>(new MinMaxCell("threecharminmaxcell", "0", "999",
            NumberType.INTEGER)) {
          @Override
          public MinMax getValue() {
            return provider.getRequest().perfectPP.getCopy();
          }
        };
    footer.setUpdater(new FooterValueUpdater<MinMax>() {
      @Override
      public void doUpdate(MinMax value) {
        provider.getRequest().perfectPP = value.getCopy();
        reloadTableWithChangedRequest(0);
      }
    });
    TextColumn<Beatmap> perfectPPColumn = addHighPPColumn(true, footer);
    return perfectPPColumn;
  }

  private TextColumn<Beatmap> addLowPPColumn() {
    Header<MinMax> footer =
        new FilterFooter<MinMax>(new MinMaxCell("threecharminmaxcell", "0", "999",
            NumberType.INTEGER)) {
          @Override
          public MinMax getValue() {
            return provider.getRequest().expectedPP.getCopy();
          }
        };
    footer.setUpdater(new FooterValueUpdater<MinMax>() {
      @Override
      public void doUpdate(MinMax value) {
        provider.getRequest().expectedPP = value.getCopy();
        reloadTableWithChangedRequest(0);
      }
    });
    TextColumn<Beatmap> expectedPPColumn = addLowPPColumn(true, footer);
    return expectedPPColumn;
  }

  static final String SHOW_FILTER = "show filters";
  static final String HIDE_FILTER = "hide filters";
  String filterState = SHOW_FILTER;
  protected final BeatmapTableServiceAsync beatmapService = GWT.create(BeatmapTableService.class);

  private Column<Beatmap, SafeHtml> addImageColumn() {
    Header<String> footer = new Header<String>(new ButtonCell()) {
      @Override
      public String getValue() {
        return filterState;
      }
    };
    footer.setUpdater(new ValueUpdater<String>() {
      @Override
      public void update(String value) {
        System.out.println("something happened");
        if (value.equals(SHOW_FILTER)) {
          filterState = HIDE_FILTER;
        }
        if (value.equals(HIDE_FILTER)) {
          filterState = SHOW_FILTER;
        }
        table.redrawFooters();
      }
    });
    Column<Beatmap, SafeHtml> column = addImageColumn(footer);
    return column;
  }

  private Column<Beatmap, SafeHtml> addLengthColumn() {
    Header<MinMax> footer =
        new FilterFooter<MinMax>(new MinMaxCell("fivecharminmaxcell", "0:00", "99:59",
            NumberType.TIME)) {
          @Override
          public MinMax getValue() {
            return provider.getRequest().mapLength.getCopy();
          }
        };
    footer.setUpdater(new FooterValueUpdater<MinMax>() {
      @Override
      public void doUpdate(MinMax value) {
        provider.getRequest().mapLength = value.getCopy();
        reloadTableWithChangedRequest(0);
      }
    });
    Column<Beatmap, SafeHtml> column = addLengthColumn(true, footer);
    return column;
  }

  private TextColumn<Beatmap> addBPMColumn() {
    Header<MinMax> footer =
        new FilterFooter<MinMax>(new MinMaxCell("threecharminmaxcell", "0", "999",
            NumberType.INTEGER)) {
          @Override
          public MinMax getValue() {
            return provider.getRequest().bpm.getCopy();
          }
        };
    footer.setUpdater(new FooterValueUpdater<MinMax>() {
      @Override
      public void doUpdate(MinMax value) {
        provider.getRequest().bpm = value.getCopy();
        reloadTableWithChangedRequest(0);
      }
    });
    TextColumn<Beatmap> column = addBPMColumn(true, footer);
    return column;
  }

  private TextColumn<Beatmap> addStarDiffColumn() {
    Header<MinMax> footer =
        new FilterFooter<MinMax>(new MinMaxCell("threecharminmaxcell", "0.0", "9.9",
            NumberType.DECIMAL)) {
          @Override
          public MinMax getValue() {
            return provider.getRequest().starDiff.getCopy();
          }
        };
    footer.setUpdater(new FooterValueUpdater<MinMax>() {
      @Override
      public void doUpdate(MinMax value) {
        provider.getRequest().starDiff = value.getCopy();
        reloadTableWithChangedRequest(0);
      }
    });
    TextColumn<Beatmap> column = addStarDiffColumn(true, footer);
    return column;
  }

  private TextColumn<Beatmap> addCSColumn() {
    Header<MinMax> footer =
        new FilterFooter<MinMax>(new MinMaxCell("onecharminmaxcell", "0", "10", NumberType.INTEGER)) {
          @Override
          public MinMax getValue() {
            return provider.getRequest().cS.getCopy();
          }
        };
    footer.setUpdater(new FooterValueUpdater<MinMax>() {
      @Override
      public void doUpdate(MinMax value) {
        provider.getRequest().cS = value.getCopy();
        reloadTableWithChangedRequest(0);
      }
    });
    TextColumn<Beatmap> column = addCSColumn(footer);
    return column;
  }

  private TextColumn<Beatmap> addARColumn() {
    Header<MinMax> footer =
        new FilterFooter<MinMax>(new MinMaxCell("onecharminmaxcell", "0", "10", NumberType.INTEGER)) {
          @Override
          public MinMax getValue() {
            return provider.getRequest().aR.getCopy();
          }
        };
    footer.setUpdater(new FooterValueUpdater<MinMax>() {
      @Override
      public void doUpdate(MinMax value) {
        provider.getRequest().aR = value.getCopy();
        reloadTableWithChangedRequest(0);
      }
    });
    TextColumn<Beatmap> column = addARColumn(footer);
    return column;
  }

  private TextColumn<Beatmap> addODColumn() {
    Header<MinMax> footer =
        new FilterFooter<MinMax>(new MinMaxCell("onecharminmaxcell", "0", "10", NumberType.INTEGER)) {
          @Override
          public MinMax getValue() {
            return provider.getRequest().oD.getCopy();
          }
        };
    footer.setUpdater(new FooterValueUpdater<MinMax>() {
      @Override
      public void doUpdate(MinMax value) {
        provider.getRequest().oD = value.getCopy();
        reloadTableWithChangedRequest(0);
      }
    });
    TextColumn<Beatmap> column = addODColumn(footer);
    return column;
  }

  private Column<Beatmap, SafeHtml> addNameColumn() {
    Header<Searches> footer = new FilterFooter<Searches>(new SearchesCell(new LoggedIn() {
      @Override
      public boolean isLoggedIn() {
        return loggedIn;
      }
    })) {
      @Override
      public Searches getValue() {
        return provider.getRequest().getSearches().getCopy();
      }
    };
    footer.setUpdater(new FooterValueUpdater<Searches>() {
      @Override
      public void doUpdate(Searches value) {
        if (value.getSearchComment().equals(provider.getRequest().getSearches().getSearchComment())
            && value.getSearchText().equals(provider.getRequest().getSearches().getSearchText())) {
          return;
        }

        provider.getRequest().setSearches(value);
        reloadTableWithChangedRequest(0);
        System.out.println("range changed");
      }
    });
    final Column<Beatmap, SafeHtml> column = addNameColumn(footer);
    return column;
  }

  abstract class FilterFooter<T> extends Header<T> {
    public FilterFooter(Cell<T> cell) {
      super(cell);
    }

    @Override
    public boolean onPreviewColumnSortEvent(Context context, Element elem, NativeEvent event) {
      return false;
    }

    @Override
    public void render(Context context, SafeHtmlBuilder sb) {
      if (filterState.equals(SHOW_FILTER)) {

      } else {
        super.render(context, sb);
      }
    }
  }

  @Override
  public void showHelp(final HelpElements help) {
    super.showHelp(help);

    final CloseHandler<PopupPanel> closeHandler;

    if (filterState.equals(SHOW_FILTER)) {
      closeHandler = new CloseHandler<PopupPanel>() {
        @Override
        public void onClose(CloseEvent<PopupPanel> event) {
          filterState = SHOW_FILTER;
          table.redrawFooters();
        }
      };

      filterState = HIDE_FILTER;
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
    // Range range = new Range(start, start + pageSize);
    // if(range.equals(table.getVisibleRange())) {
    // RangeChangeEvent.fire(table, table.getVisibleRange());
    // } else {
    // table.setVisibleRange(range);
    // }
    table.setVisibleRangeAndClearData(new Range(start, PAGE_SIZE), true);
  }
}
