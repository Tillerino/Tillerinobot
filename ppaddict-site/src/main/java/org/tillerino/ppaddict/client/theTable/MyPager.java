package org.tillerino.ppaddict.client.theTable;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.OutlineStyle;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

public class MyPager extends SimplePager {
    private class MyScrollHandler implements ScrollHandler {
        private final ScrollPanel scrollable;

        private MyScrollHandler(ScrollPanel scrollable) {
            this.scrollable = scrollable;
        }

        @Override
        public void onScroll(ScrollEvent event) {
            HasRows display = getDisplay();
            if (display == null) {
                return;
            }

            // avoid stacked loading
            if (lastLoadFired > System.currentTimeMillis() - 1000) return;

            // If scrolling up, ignore the event.
            int newScrollPos = scrollable.getVerticalScrollPosition();
            if (newScrollPos <= lastScrollPos) {
                return;
            }

            // System.out.println("scrolled to " + newScrollPos);
            int maxScrollTop = scrollable.getWidget().getOffsetHeight() - scrollable.getOffsetHeight();
            if (newScrollPos >= maxScrollTop - 1000) {
                lastScrollPos = maxScrollTop;
                if (display.getVisibleRange().getStart()
                                + display.getVisibleRange().getLength()
                        < display.getRowCount()) {
                    scrollable.getElement().focus();
                    // We are near the end, so increase the page size.
                    display.setVisibleRange(
                            display.getVisibleRange().getStart(),
                            display.getVisibleRange().getLength() + MyPager.this.pageSize);
                }
            }
            lastScrollPos = Math.max(lastScrollPos, newScrollPos);
        }
    }

    interface Resources extends SimplePager.Resources {
        @Override
        public ImageResource simplePagerNextPage();

        @Override
        public ImageResource simplePagerFastForward();

        @Override
        public ImageResource simplePagerFastForwardDisabled();

        @Override
        public ImageResource simplePagerFirstPage();

        @Override
        public ImageResource simplePagerFirstPageDisabled();

        @Override
        public ImageResource simplePagerLastPage();

        @Override
        public ImageResource simplePagerLastPageDisabled();

        @Override
        public ImageResource simplePagerNextPageDisabled();

        @Override
        public ImageResource simplePagerPreviousPage();

        @Override
        public ImageResource simplePagerPreviousPageDisabled();

        @Override
        public Style simplePagerStyle();
    }

    public int lastScrollPos;
    public long lastLoadFired;
    int pageSize;

    public MyPager(final ScrollPanel scrollable, int pageSize) {
        super(TextLocation.CENTER, (SimplePager.Resources) GWT.create(Resources.class), true, 1000, true);

        this.pageSize = pageSize;

        // Do not let the scrollable take tab focus.
        scrollable.getElement().setTabIndex(-1);
        scrollable.getElement().getStyle().setOutlineStyle(OutlineStyle.NONE);

        // Handle scroll events.
        scrollable.addScrollHandler(new MyScrollHandler(scrollable));
    }

    @Override
    public void setPageStart(int index) {
        HasRows display = getDisplay();
        if (display != null) {
            Range range = display.getVisibleRange();
            int pageSize = this.pageSize;
            if (isRangeLimited() && display.isRowCountExact()) {
                index = Math.min(index, display.getRowCount() - pageSize);
            }
            index = Math.max(0, index);
            if (index != range.getStart()) {
                display.setVisibleRange(index, pageSize);
            }
        }
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getPage() {
        if (getDisplay() == null) {
            return -1;
        }
        Range range = getDisplay().getVisibleRange();
        int pageSize = getPageSize();
        return (range.getStart() + pageSize - 1) / pageSize;
    }
}
