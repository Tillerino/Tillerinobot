package org.tillerino.ppaddict.client;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowHoverEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import javax.annotation.CheckForNull;
import org.tillerino.ppaddict.client.HelpElements.E;
import org.tillerino.ppaddict.client.HelpElements.HasHelpElements;
import org.tillerino.ppaddict.client.UserBox.UserDataHandler;
import org.tillerino.ppaddict.client.dialogs.AbstractSettingsDialog;
import org.tillerino.ppaddict.client.dialogs.EditBeatmapDialog;
import org.tillerino.ppaddict.client.dialogs.MoreDialog;
import org.tillerino.ppaddict.client.dialogs.Side;
import org.tillerino.ppaddict.client.theTable.CustomDataGrid;
import org.tillerino.ppaddict.client.theTable.CustomDataGrid.MyDataGridResources;
import org.tillerino.ppaddict.shared.Beatmap;
import org.tillerino.ppaddict.shared.Beatmap.Personalization;
import org.tillerino.ppaddict.shared.ClientUserData;
import org.tillerino.ppaddict.shared.Searches;
import org.tillerino.ppaddict.shared.Settings;

public abstract class AbstractBeatmapTable extends Composite implements UserDataHandler, HasHelpElements {

    protected CustomDataGrid<Beatmap> table;

    public static final int PAGE_SIZE = 100;

    protected NumberFormat format = NumberFormat.getFormat("#.##");
    NumberFormat ppFormat = NumberFormat.getFormat("#pp");

    protected Column<Beatmap, SafeHtml> nameColumn;

    protected boolean loggedIn = false;

    protected Settings settings = new Settings();

    private TextColumn<Beatmap> arColumn;

    protected void createTable() {
        MyDataGridResources resources = GWT.create(CustomDataGrid.MyDataGridResources.class);
        table = new CustomDataGrid<Beatmap>(PAGE_SIZE, resources);

        final SingleSelectionModel<Beatmap> selectionModel = new SingleSelectionModel<Beatmap>();
        table.setSelectionModel(selectionModel);

        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (settings.isOpenDirectOnMapSelect()) {
                    Window.Location.assign(AbstractBeatmapTable.directUrl(selectionModel.getSelectedObject()));
                }
            }
        });
    }

    protected void setAlignRight(Column<Beatmap, ?> column) {
        column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        int columnIndex = table.getColumnIndex(column);
        if (table.getHeader(columnIndex) != null) {
            table.getHeader(columnIndex).setHeaderStyleNames("alignright");
        }
        if (table.getFooter(columnIndex) != null) {
            table.getFooter(columnIndex).setHeaderStyleNames("alignright");
        }
    }

    protected void setAlignLeft(Column<Beatmap, ?> column) {
        column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        int columnIndex = table.getColumnIndex(column);
        table.getHeader(columnIndex).setHeaderStyleNames("alignleft");
        if (table.getFooter(columnIndex) != null) {
            table.getFooter(columnIndex).setHeaderStyleNames("alignleft");
        }
    }

    protected Column<Beatmap, SafeHtml> addImageColumn(@CheckForNull Header<?> footer) {
        Column<Beatmap, SafeHtml> column = new Column<Beatmap, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(Beatmap object) {

                return new SafeHtmlBuilder()
                        .appendHtmlConstant("<a href=\"http://osu.ppy.sh/beatmapsets/" + object.setid + "/download\">"
                                + "<img src=\"//b.ppy.sh/thumb/" + object.setid
                                + ".jpg\" height=\"60\" style=\"vertical-align:middle\">" + "</a>"
                                + "<a href=\"" + directUrl(object) + "\">")
                        .appendHtmlConstant(
                                " <img src=\"/osuDownloadDirect.png\" height=60 width=10 style=\"vertical-align:middle\">")
                        .appendHtmlConstant("</a>")
                        .toSafeHtml();
            }
        };
        TextHeader header = new TextHeader("");
        addColumn(column, header, footer);
        table.setColumnWidth(column, 115, Unit.PX);
        setAlignLeft(column);
        return column;
    }

    protected Column<Beatmap, SafeHtml> addLengthColumn(boolean buttonHeader, Header<?> footer) {
        Column<Beatmap, SafeHtml> column = new Column<Beatmap, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(Beatmap object) {
                return new SafeHtmlBuilder()
                        .appendHtmlConstant("<div style=\"padding-right: 20px\">")
                        .appendEscaped(object.getFormattedLength())
                        .appendHtmlConstant("</div>")
                        .toSafeHtml();
            }
        };
        Header<?> header = createHeader("length", buttonHeader);
        addColumn(column, header, footer);
        setAlignRight(column);
        table.setColumnWidth(column, 90, Unit.PX);
        return column;
    }

    protected TextColumn<Beatmap> addBPMColumn(boolean buttonHeader, @CheckForNull Header<?> footer) {
        TextColumn<Beatmap> column = new TextColumn<Beatmap>() {

            @Override
            public String getValue(Beatmap object) {
                return ((int) object.bpm) + "";
            }
        };
        Header<?> header = createHeader("BPM", buttonHeader);
        addColumn(column, header, footer);
        setAlignRight(column);
        table.setColumnWidth(column, 55, Unit.PX);
        return column;
    }

    protected TextColumn<Beatmap> addStarDiffColumn(boolean buttonHeader, @CheckForNull Header<?> footer) {
        TextColumn<Beatmap> column = new TextColumn<Beatmap>() {

            @Override
            public String getValue(Beatmap object) {
                return object.starDifficulty != null ? format.format(object.starDifficulty) : "";
            }
        };
        Header<?> header = createHeader("diff", buttonHeader);
        addColumn(column, header, footer);
        setAlignRight(column);
        table.setColumnWidth(column, 60, Unit.PX);
        return column;
    }

    protected TextColumn<Beatmap> addCSColumn(@CheckForNull Header<?> footer) {
        TextColumn<Beatmap> column = new TextColumn<Beatmap>() {
            @Override
            public String getValue(Beatmap object) {
                return "CS" + format.format(object.circleSize);
            }
        };
        TextHeader header = new TextHeader("CS");
        addColumn(column, header, footer);
        setAlignRight(column);
        table.setColumnWidth(column, 55, Unit.PX);
        return column;
    }

    protected TextColumn<Beatmap> addARColumn(@CheckForNull Header<?> footer) {
        TextColumn<Beatmap> column = new TextColumn<Beatmap>() {
            @Override
            public String getValue(Beatmap object) {
                return "AR" + format.format(object.approachRate);
            }
        };
        TextHeader header = new TextHeader("AR");
        addColumn(column, header, footer);
        setAlignRight(column);
        table.setColumnWidth(column, 75, Unit.PX);
        return this.arColumn = column;
    }

    protected TextColumn<Beatmap> addODColumn(@CheckForNull Header<?> footer) {
        TextColumn<Beatmap> column = new TextColumn<Beatmap>() {
            @Override
            public String getValue(Beatmap object) {
                return "OD" + format.format(object.overallDiff);
            }
        };
        TextHeader header = new TextHeader("OD");
        addColumn(column, header, footer);
        setAlignRight(column);
        table.setColumnWidth(column, 75, Unit.PX);
        return this.arColumn = column;
    }

    protected Column<Beatmap, SafeHtml> addNameColumn(@CheckForNull Header<Searches> footer) {
        nameColumn = new Column<Beatmap, SafeHtml>(new SafeHtmlCell()) {
            @Override
            public SafeHtml getValue(Beatmap object) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder()
                        .appendHtmlConstant(
                                "<a href=\"http://osu.ppy.sh/b/" + object.beatmapid + "\" target=\"_blank\">")
                        .appendEscaped(object.artist + " - " + object.title)
                        .appendHtmlConstant(" <span style=\"white-space: nowrap\">")
                        .appendEscaped("[" + object.version + "]")
                        .appendHtmlConstant("</span></a>");
                if (object.mods != null) {
                    builder.appendEscaped(" " + object.mods);
                }
                Personalization personalization = object.personalization;
                if (personalization != null
                        && personalization.comment != null
                        && personalization.comment.trim().length() > 0) {
                    builder.appendHtmlConstant("<div class=\"comments\">");
                    builder.appendEscaped(personalization.comment.trim());
                    builder.appendHtmlConstant(" <span class=\"commentsdate\">");
                    builder.appendEscaped(personalization.commentDate);
                    builder.appendHtmlConstant("</span>");
                    builder.appendHtmlConstant("</div>");
                }
                return builder.toSafeHtml();
            }
        };
        TextHeader header = new TextHeader("");
        addColumn(nameColumn, header, footer);
        setAlignLeft(nameColumn);
        return nameColumn;
    }

    NumberFormat percentageFormat = NumberFormat.getFormat("#.#");

    protected TextColumn<Beatmap> addHighPPColumn(boolean buttonHeader, @CheckForNull Header<?> footer) {
        TextColumn<Beatmap> perfectPPColumn;
        perfectPPColumn = new TextColumn<Beatmap>() {
            @Override
            public String getValue(Beatmap object) {
                return ppFormat.format(object.highPP);
            }
        };
        Cell<String> headerCell = buttonHeader ? new ButtonCell() : new TextCell();
        Header<?> header = new Header<String>(headerCell) {
            @Override
            public String getValue() {
                return percentageFormat.format(settings.getHighAccuracy()) + "%";
            }
        };
        addColumn(perfectPPColumn, header, footer);
        setAlignRight(perfectPPColumn);
        table.setColumnWidth(perfectPPColumn, 73, Unit.PX);
        return perfectPPColumn;
    }

    protected TextColumn<Beatmap> addLowPPColumn(boolean buttonHeader, @CheckForNull Header<?> footer) {
        TextColumn<Beatmap> expectedPPColumn = new TextColumn<Beatmap>() {
            @Override
            public String getValue(Beatmap object) {
                return ppFormat.format(object.lowPP);
            }
        };
        Cell<String> headerCell = buttonHeader ? new ButtonCell() : new TextCell();
        Header<?> header = new Header<String>(headerCell) {
            @Override
            public String getValue() {
                return percentageFormat.format(settings.getLowAccuracy()) + "%";
            }
        };
        addColumn(expectedPPColumn, header, footer);
        setAlignRight(expectedPPColumn);
        table.setColumnWidth(expectedPPColumn, 73, Unit.PX);
        return expectedPPColumn;
    }

    /**
     * @param index index in visible elements
     * @param object
     */
    protected void showEditDialog(final int index, Beatmap object) {
        EditBeatmapDialog dialog = new EditBeatmapDialog(
                object,
                new Runnable() {
                    @Override
                    public void run() {
                        table.redrawRow(index);
                    }
                },
                null);
        TableRowElement rowElement =
                table.getRowElement(index - table.getVisibleRange().getStart());
        dialog.show(rowElement.getChild(table.getColumnIndex(nameColumn)).<Element>cast(), Side.BELOW_RIGHT);
    }

    /**
     * @param index index in visible elements
     * @param object
     * @param column TODO
     */
    protected void showMoreDialog(final int index, Beatmap object, Column<Beatmap, ?> column) {
        MoreDialog dialog = new MoreDialog(object);
        TableRowElement rowElement =
                table.getRowElement(index - table.getVisibleRange().getStart());
        AbstractSettingsDialog.popupNextTo(
                dialog, rowElement.getChild(table.getColumnIndex(column)).<Element>cast(), Side.BELOW_LEFT);
    }

    protected Column<Beatmap, String> addEditColumn() {
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

                    Element button = event.getHoveringRow()
                            .getChild(table.getColumnIndex(column))
                            .<TableCellElement>cast()
                            .getChild(0)
                            .<DivElement>cast()
                            .getChild(0)
                            .<ButtonElement>cast();

                    button.setInnerText(event.isUnHover() ? "" : "Edit");
                }
            }
        });
        table.addColumn(column);
        table.setColumnWidth(column, 48, Unit.PX);
        column.setFieldUpdater(new FieldUpdater<Beatmap, String>() {
            @Override
            public void update(final int index, Beatmap object, String value) {
                showEditDialog(index, object);
            }
        });
        setAlignRight(column);
        return column;
    }

    protected Column<Beatmap, String> addMoreColumn() {
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
                Element button = event.getHoveringRow()
                        .getChild(table.getColumnIndex(column))
                        .<TableCellElement>cast()
                        .getChild(0)
                        .<DivElement>cast()
                        .getChild(0)
                        .<ButtonElement>cast();

                button.setInnerText(event.isUnHover() ? "" : "...");
            }
        });
        table.addColumn(column);
        table.setColumnWidth(column, 30, Unit.PX);
        column.setFieldUpdater(new FieldUpdater<Beatmap, String>() {
            @Override
            public void update(final int index, Beatmap object, String value) {
                showMoreDialog(index, object, column);
            }
        });
        return column;
    }

    public static Header<?> createHeader(String text, boolean button) {
        if (button) {
            return new AllBeatmapsTable.ButtonHeader(text);
        }
        return new TextHeader(text);
    }

    public static String directUrl(Beatmap object) {
        return "osu://b/" + object.beatmapid;
    }

    private void addColumn(Column<Beatmap, ?> column, Header<?> header, @CheckForNull Header<?> footer) {
        if (footer != null) {
            table.addColumn(column, header, footer);
        } else {
            table.addColumn(column, header);
        }
    }

    @Override
    public void handle(ClientUserData data) {
        loggedIn = data.isLoggedIn();

        settings = new Settings(data.settings);
    }

    @Override
    public void showHelp(HelpElements help) {
        NodeList<TableCellElement> tableHeaderCells =
                table.getTableHeadElement().getRows().getItem(0).getCells();
        help.positionAndShow(E.ESTIMATES_COLUMNS, tableHeaderCells.getItem(2), Side.BELOW_RIGHT, null);
        help.positionAndShow(E.IMAGE_COLUMN, tableHeaderCells.getItem(0), Side.BELOW_RIGHT, null);
        help.positionAndShow(
                E.META_COLUMNS, tableHeaderCells.getItem(table.getColumnIndex(arColumn)), Side.BELOW_RIGHT, null);
    }
}
