package tillerino.tillerinobot.lang;

// suppress warning about enum constant naming pattern
@SuppressWarnings("squid:S00115")
public enum LanguageIdentifier {
	Default(Default.class),
	English(Default.class),
	Tsundere(TsundereEnglish.class),
	TsundereGerman(TsundereGerman.class),
	Italiano(Italiano.class),
	Français(Francais.class),
	Polski(Polski.class),
	Nederlands(Nederlands.class),
	עברית(Hebrew.class),
	Farsi(Farsi.class),
	Português_BR(Portuguese.class),
	Deutsch(Deutsch.class),
	Čeština(Czech.class),
	Magyar(Hungarian.class),
	한국어(Korean.class),
	Dansk(Dansk.class),
	Türkçe(Turkish.class),
	日本語(Japanese.class),
	Español(Spanish.class),
	Ελληνικά(Greek.class),
	Русский(Russian.class),
	Lietuvių(Lithuanian.class),
	Português_PT(PortuguesePortugal.class),
	Svenska(Svenska.class),
	Romana(Romana.class),
	繁體中文(ChineseTraditional.class),
	български(Bulgarian.class),
	Norsk(Norwegian.class),
	Indonesian(Indonesian.class),
	简体中文(ChineseSimple.class),
	Català(Catalan.class),
	Slovenščina(Slovenian.class),
	; // please end identifier entries with a comma and leave this semicolon here
	
	public final Class<? extends Language> cls;

	private LanguageIdentifier(Class<? extends Language> cls) {
		this.cls = cls;
	}
}