package org.tillerino.ppaddict.client.theTable;

import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Widget;

public class CustomDataGrid<T> extends DataGrid<T> {
  public static interface MyDataGridResources extends DataGrid.Resources {
    @Override
    public Style dataGridStyle();

    @Override
    public ImageResource dataGridSortAscending();

    @Override
    public ImageResource dataGridSortDescending();
  }

  public CustomDataGrid(int pageSize,
      com.google.gwt.user.cellview.client.DataGrid.Resources resources) {
    super(pageSize, resources);
  }

  @Override
  public Widget getWidget() {
    return super.getWidget();
  }

  @Override
  public TableSectionElement getTableFootElement() {
    return super.getTableFootElement();
  }

  @Override
  public TableSectionElement getTableBodyElement() {
    return super.getTableBodyElement();
  }

  @Override
  public TableSectionElement getTableHeadElement() {
    return super.getTableHeadElement();
  }
}
