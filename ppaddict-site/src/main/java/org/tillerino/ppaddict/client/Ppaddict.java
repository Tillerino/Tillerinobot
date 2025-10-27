package org.tillerino.ppaddict.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/** Entry point classes define <code>onModuleLoad()</code>. */
public class Ppaddict implements EntryPoint {
    /** This is the entry point method. */
    @Override
    public void onModuleLoad() {
        final Main main = new Main();

        RootLayoutPanel.get().add(main);
    }
}
