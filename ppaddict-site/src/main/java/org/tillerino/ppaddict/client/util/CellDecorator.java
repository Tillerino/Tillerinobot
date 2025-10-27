package org.tillerino.ppaddict.client.util;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import java.util.Set;

public class CellDecorator<C> implements Cell<C> {
    private final Cell<C> component;

    protected CellDecorator(Cell<C> component) {
        this.component = component;
    }

    @Override
    public boolean dependsOnSelection() {
        return component.dependsOnSelection();
    }

    @Override
    public Set<String> getConsumedEvents() {
        return component.getConsumedEvents();
    }

    @Override
    public boolean handlesSelection() {
        return component.handlesSelection();
    }

    @Override
    public boolean isEditing(Context context, Element parent, C value) {
        return component.isEditing(context, parent, value);
    }

    @Override
    public void onBrowserEvent(
            Context context, Element parent, C value, NativeEvent event, ValueUpdater<C> valueUpdater) {
        component.onBrowserEvent(context, parent, value, event, valueUpdater);
    }

    @Override
    public void render(Context context, C value, SafeHtmlBuilder sb) {
        component.render(context, value, sb);
    }

    @Override
    public boolean resetFocus(Context context, Element parent, C value) {
        return component.resetFocus(context, parent, value);
    }

    @Override
    public void setValue(Context context, Element parent, C value) {
        component.setValue(context, parent, value);
    }
}
