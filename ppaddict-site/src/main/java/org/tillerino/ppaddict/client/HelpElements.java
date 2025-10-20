package org.tillerino.ppaddict.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootPanel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.tillerino.ppaddict.client.dialogs.Side;

public class HelpElements {
    public interface HasHelpElements {
        void showHelp(HelpElements elements);
    }

    public abstract static class PopupPanelWithHelpElements extends PopupPanel implements HasHelpElements {
        protected HelpElements help;

        public PopupPanelWithHelpElements(HelpElements help) {
            super(true, true);
            this.help = help;
        }

        @Override
        public void show() {
            super.show();
            if (help != null) {
                addAutoHidePartner(help.helpLink.getElement());
                help.s.push(this);
                addCloseHandler(new CloseHandler<PopupPanel>() {
                    @Override
                    public void onClose(CloseEvent<PopupPanel> event) {
                        help.s.remove(PopupPanelWithHelpElements.this);
                    }
                });
            }
        }
    }

    public enum E {
        IMAGE_COLUMN,
        ESTIMATES_COLUMNS,
        META_COLUMNS,
        RANGE_FILTER,
        NAME_FILTER,
        LOGIN_BUTTON,
        RECOMMEND_BUTTON,
        LOGIN_DIALOG,
        RECOMMEND_HELP,
        SETTINGS_RECOMMENDATIONS,
        SETTINGS_LINK,
        OMNI_EXTERNAL;
    }

    private Map<Enum<?>, PopupPanel> all = new LinkedHashMap<>();

    private Hyperlink helpLink;

    private static final String NO_TITLE = null;

    public HelpElements(Hyperlink helpLink) {
        this.helpLink = helpLink;
        {
            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.appendEscapedLines("Click the image to download the beatmap set as a .osz file. "
                    + "If you're not logged in on osu.ppy.sh, you will be asked to login there.\n"
                    + "Click the osu!direct stripe to view this map in osu!direct. "
                    + "This feature is only available to osu! supporters.");
            b.appendEscapedLines("\nUnfortunately, osu! direct will only navigate to the corresponding beatmap set.");
            b.appendHtmlConstant(
                    " If you are a supporter, you can <a href=\"https://osu.ppy.sh/forum/t/221897\" target=\"_blank\">vote here</a> to get more specific osu! direct links.");
            addHelpElement(E.IMAGE_COLUMN, "downloads", new InlineHTML(b.toSafeHtml()));
        }
        {
            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.appendEscapedLines(
                    "Thes values are the amount of pp that a Full Combo play (no misses and no slider breaks) with the displayed accuracies is worth before weightage.");
            b.appendHtmlConstant(
                    " <a href=\"https://osu.ppy.sh/wiki/Performance_Points#Weightage_system\" target=\"_blank\">(more info)</a>");
            addHelpElement(E.ESTIMATES_COLUMNS, "pp values", new InlineHTML(b.toSafeHtml()));
        }
        {
            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.appendEscapedLines("This is a selection of available meta information about each beatmap."
                    + "\nAR = approach rate" + "\nOD = overall difficulty" + "\nCS = circle size"
                    + "\ndiff = star difficulty" + "\nBPM = beats per minute.");
            b.appendHtmlConstant(
                    "<a href=\"https://osu.ppy.sh/wiki/Song_Setup#Difficulty\" target=\"_blank\">(more info)</a>");
            addHelpElement(E.META_COLUMNS, "meta information", new InlineHTML(b.toSafeHtml()));
        }
        addHelpElement(
                E.NAME_FILTER,
                "name filter",
                "Search in the name of a beatmap. The name is formed as shown above combining artist, title and version. This will override any other filters. You can change this behaviour if you're logged in.");
        addHelpElement(
                E.RANGE_FILTER,
                "range filter",
                "You can limit the range of some of the values. Click a boundary to change it! Leave a field empty to restore the original boundary.");
        addHelpElement(
                E.LOGIN_BUTTON,
                "login",
                "You can login with your existing Google/Twitter/... account. This will enable several customizations and recommendations.");
        addHelpElement(E.RECOMMEND_BUTTON, NO_TITLE, "Click r! to access recommendations.");
        addHelpElement(
                E.RECOMMEND_HELP,
                "recommendations",
                "You will always find 10 personal recommendations below. To get a new recommendation, hover over your least favorite one and click \"hide\". You can customize your recommendations in the settings.");
        addHelpElement(
                E.LOGIN_BUTTON,
                "login",
                "You can login with your Google/Twitter/... account. This will enable several customizations and recommendations.");
        addHelpElement(
                E.LOGIN_DIALOG,
                NO_TITLE,
                "You can log in using any of the providers above. "
                        + "None of your data on this side should make it to the provider and ppaddict will only look for a way to identify you.");
        {
            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.appendEscapedLines("You can customize your recommendations as if you were using Tillerinobot."
                    + " Put anything that you would send Tillerinobot after \"!recommend\" in this box.");
            b.appendHtmlConstant(
                    " <a href=\"https://github.com/Tillerino/Tillerinobot/wiki/Recommendations\" target=\"_blank\">(more info)</a>");
            b.appendEscapedLines(
                    " Putting \"*\" will give you default recommendations, or reuse the last settings (including those from Tillerinobot), if you recently used any cutomizations.");
            addHelpElement(E.SETTINGS_RECOMMENDATIONS, "Recommendations settings", new InlineHTML(b.toSafeHtml()));
        }
        addHelpElement(
                E.SETTINGS_LINK,
                "Link to osu! account",
                "You can link the account, which you used to login, to your osu! account. This is required for recommendations."
                        + " If you link multiple accounts to your osu! account, they will share settings.");
        {
            SafeHtmlBuilder b = new SafeHtmlBuilder();
            b.appendHtmlConstant(
                    "<a href=\"https://github.com/Tillerino/Tillerinobot/wiki\" target=\"_blank\">Tillerinobot wiki</a>");
            b.appendHtmlConstant(
                    "<br><a href=\"https://github.com/Tillerino/ppaddict\" target=\"_blank\">ppaddict source</a>");
            addHelpElement(E.OMNI_EXTERNAL, null, new InlineHTML(b.toSafeHtml()));
        }
    }

    private List<HasHelpElements> elementsWithHelp = new ArrayList<>();

    public void positionAndShow(E key, final Element element, final Side side, CloseHandler<PopupPanel> closeHandler) {
        final PopupPanel panel = prepareShowing(key, closeHandler);
        PositionCallback positionCallback = new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                final int startLeft;
                final int startTop;
                switch (side) {
                    case BELOW_RIGHT:
                        startLeft = element.getAbsoluteLeft();
                        startTop = element.getAbsoluteTop() + element.getOffsetHeight();
                        break;
                    case ABOVE_RIGHT:
                        startLeft = element.getAbsoluteLeft();
                        startTop = element.getAbsoluteTop() - offsetHeight;
                        break;
                    case BELOW_LEFT:
                        startLeft = element.getAbsoluteLeft() + element.getOffsetWidth() - offsetWidth;
                        startTop = element.getAbsoluteTop() + element.getOffsetHeight();
                        break;
                    case ABOVE_LEFT:
                        startLeft = element.getAbsoluteLeft() + element.getOffsetWidth() - offsetWidth;
                        startTop = element.getAbsoluteTop() - offsetHeight;
                        break;
                    case RIGHT_BELOW:
                        startTop = element.getAbsoluteTop();
                        startLeft = element.getAbsoluteLeft() + element.getOffsetWidth();
                        break;
                    default:
                        throw new RuntimeException("" + side);
                }
                Position myPosition = new Position();
                myPosition.left = startLeft;
                myPosition.top = startTop;
                myPosition.width = offsetWidth;
                myPosition.height = offsetHeight;
                avoidCoverRuns:
                for (; ; ) {
                    for (Position p : positions.values()) {
                        if (p.moveVerticalAvoidCover(myPosition, side)) {
                            continue avoidCoverRuns;
                        }
                    }
                    break;
                }
                panel.setPopupPosition(myPosition.left, myPosition.top);
                positions.put(panel, myPosition);
            }
        };
        panel.setPopupPositionAndShow(positionCallback);
    }

    private PopupPanel prepareShowing(E key, CloseHandler<PopupPanel> closeHandler) {
        final PopupPanel panel = all.get(key);
        panel.setGlassEnabled(positions.size() == 0);
        if (closeHandler != null) {
            // this is pretty ugly, but we want to remove the close handler after it is run
            final HandlerRegistration[] reg = {panel.addCloseHandler(closeHandler), null};
            HandlerRegistration removalHandler = panel.addCloseHandler(new CloseHandler<PopupPanel>() {
                @Override
                public void onClose(CloseEvent<PopupPanel> event) {
                    reg[0].removeHandler();
                    reg[1].removeHandler();
                }
            });
            reg[1] = removalHandler;
        }
        return panel;
    }

    void addHelpElement(E key, String title, String text) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendEscapedLines(text);
        InlineHTML l = new InlineHTML(builder.toSafeHtml());
        addHelpElement(key, title, l);
    }

    void addHelpElement(E key, String title, InlineHTML l) {
        // since help elements can pop up over dialogs, they need to be modal. This forces us to add
        // close handlers, which close all other open help elements.
        final PopupPanel popup = new PopupPanel(true, true);
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                // we'll need to clear positions first or we'll run into an infinite loop
                ArrayList<PopupPanel> currentlyDisplaying = new ArrayList<>(positions.keySet());
                positions.clear();
                for (PopupPanel position : currentlyDisplaying) {
                    position.hide();
                }
            }
        });

        popup.setStyleName("helppopup", true);
        FlowPanel flow = new FlowPanel();
        popup.add(flow);
        if (title != null) {
            Label lbl = new Label(title + ":", true);
            lbl.setStyleName("helppopuptitle");
            flow.add(lbl);
        }
        flow.add(l);
        for (PopupPanel existing : all.values()) {
            existing.addAutoHidePartner(popup.getElement());
            popup.addAutoHidePartner(existing.getElement());
        }
        all.put(key, popup);
    }

    static class Position {
        int left;
        int top;
        int width;
        int height;

        boolean moveVerticalAvoidCover(Position p, Side s) {
            if (coversMe(p) || p.coversMe(this)) {
                switch (s) {
                    case BELOW_RIGHT:
                    case BELOW_LEFT:
                        p.top = this.top + this.height + 10;
                        System.out.println("moved below");
                        break;
                    case ABOVE_RIGHT:
                        p.top = this.top - p.height - 10;
                        System.out.println("moved above");
                        break;
                    default:
                        throw new RuntimeException("" + s);
                }
                return true;
            } else {
                return false;
            }
        }

        boolean coversMe(Position p) {
            if (isInMe(p.left, p.top)) {
                System.out.println("top left of " + p + " is in " + this);
                return true;
            }
            if (isInMe(p.left, p.top + p.height)) {
                System.out.println("bottom left of " + p + " is in " + this);
                return true;
            }
            if (isInMe(p.left + p.width, p.top)) {
                System.out.println("top right of " + p + " is in " + this);
                return true;
            }
            if (isInMe(p.left + p.width, p.top + p.height)) {
                System.out.println("bottom right of " + p + " is in " + this);
                return true;
            }
            return false;
        }

        boolean isInMe(int left, int top) {
            if (left < this.left) {
                return false;
            }
            if (left > this.left + this.width) {
                return false;
            }
            if (top < this.top) {
                return false;
            }
            if (top > this.top + this.height) {
                return false;
            }
            return true;
        }
    }

    Map<PopupPanel, Position> positions = new LinkedHashMap<>();

    public <T extends Composite & HasHelpElements> void addElement(T elem) {
        elementsWithHelp.add(elem);
    }

    Stack<PopupPanelWithHelpElements> s = new Stack<>();

    public void showHelp() {
        // display github links in top right corner
        final PopupPanel dialogBox = prepareShowing(E.OMNI_EXTERNAL, null);
        dialogBox.setPopupPositionAndShow(new PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                dialogBox.setPopupPosition(RootPanel.get().getOffsetWidth() - dialogBox.getOffsetWidth(), 0);

                Position myPosition = new Position();
                myPosition.left = dialogBox.getAbsoluteLeft();
                myPosition.top = dialogBox.getAbsoluteTop();
                myPosition.width = offsetWidth;
                myPosition.height = offsetHeight;
                positions.put(dialogBox, myPosition);
            }
        });

        if (!s.isEmpty()) {
            s.peek().showHelp(this);
            return;
        }

        for (HasHelpElements hasHelpElements : elementsWithHelp) {
            Composite composite = (Composite) hasHelpElements;

            if (composite.isAttached()) {
                hasHelpElements.showHelp(this);
            }
        }
    }
}
