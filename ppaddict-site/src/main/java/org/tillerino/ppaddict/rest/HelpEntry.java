package org.tillerino.ppaddict.rest;

record HelpEntry(String title, String content, String side) {
    HelpEntry(String content, String side) {
        this(null, content, side);
    }

    @Override
    public String toString() {
        StringBuilder attrs = new StringBuilder();
        if (title != null) {
            attrs.append(" data-help-title=\"").append(escape(title)).append('"');
        }
        attrs.append(" data-help-content=\"").append(escape(content)).append('"');
        attrs.append(" data-help-side=\"").append(side).append('"');
        return attrs.toString();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
