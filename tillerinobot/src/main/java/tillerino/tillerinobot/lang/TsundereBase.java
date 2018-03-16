package tillerino.tillerinobot.lang;

import java.util.Random;

import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.CommandHandler.Message;
import tillerino.tillerinobot.CommandHandler.Response;
import tillerino.tillerinobot.recommendations.Recommendation;

import javax.annotation.Nonnull;

public abstract class TsundereBase extends AbstractMutableLanguage {
	private static final long serialVersionUID = 1L;
	//random object
	static final Random rnd = new Random();
	//Recent counters, reset if inactive for a while
	private int recentRecommendations = 0;
	private int recentHugs = 0;
	private int lastHug = 0;
	private int invalidRecommendationParameterCount = 0;

	StringShuffler welcomeUserShortShuffler = new StringShuffler(rnd);
	StringShuffler welcomeUserShuffler = new StringShuffler(rnd);
	StringShuffler welcomeUserLongShuffler = new StringShuffler(rnd);
	StringShuffler invalidAccuracyShuffler = new StringShuffler(rnd);
	StringShuffler optionalCommentOnLanguageShuffler = new StringShuffler(rnd);
	StringShuffler tryWithModsShuffler = new StringShuffler(rnd);
	StringShuffler tryWithModsListShuffler = new StringShuffler(rnd);
	StringShuffler noInformationForModsShortShuffler = new StringShuffler(rnd);
	StringShuffler noInformationForModsShuffler = new StringShuffler(rnd);
	StringShuffler unknownBeatmapShuffler = new StringShuffler(rnd);

	@Override
	public Response welcomeUser(OsuApiUser apiUser, long inactiveTime) {
		String username = apiUser.getUserName();
		String greeting;
		//Greetings for <4 minutes, normal, and >4 days
		if (inactiveTime < 4 * 60 * 1000) {
			greeting = getInactiveShortGreeting(username, inactiveTime);
		} else if (inactiveTime < 4l * 24 * 60 * 60 * 1000) {
			greeting = getInactiveGreeting(username, inactiveTime);
		} else {
			greeting = getInactiveLongGreeting(username, inactiveTime);
		}
		//Recent counter reset (4 hours)
		if (inactiveTime > 4 * 60 * 60 * 1000) {
			recentRecommendations = 0;
			recentHugs = 0;
		}

		registerModification();

		return new Message(greeting);
	}

	protected abstract String getInactiveShortGreeting(String username, long inactiveTime);
	protected abstract String getInactiveGreeting(String username, long inactiveTime);
	protected abstract String getInactiveLongGreeting(String username, long inactiveTime);

	@Nonnull
	@Override
	public Response hug(OsuApiUser apiUser) {
		registerModification();
		//Responses move from tsun to dere with more hug attempts and recommendations
		recentHugs++;
		int baseLevel = (int)(Math.log(recentHugs) / Math.log(2.236) + Math.log(recentRecommendations+1) / Math.log(5)); //Sum logs base sqrt(5) and 5

		// do some random stuff
		// Ranges from -2 to 10 (but it intentionally never reaches max)
		int hugLevel = (baseLevel<8?baseLevel:8) + rnd.nextInt(3) + rnd.nextInt(3) - 3;
		// because where we check if its the last thing, and if it is we ++
		if(hugLevel >= lastHug) {
			hugLevel++;
		}
		// so basically above is a random gen with an exclude of the last one

		lastHug = hugLevel;
		return getHugResponseForHugLevel(apiUser.getUserName(), hugLevel);
	}

	@Nonnull
	protected abstract Response getHugResponseForHugLevel(String username, int hugLevel);

	@Override
	public Response optionalCommentOnRecommendation(OsuApiUser apiUser, Recommendation recommendation) {
		registerModification();
		recentRecommendations++;
		return getOptionalCommentOnRecommendationResponse(recentRecommendations);
	}

	protected abstract Response getOptionalCommentOnRecommendationResponse(int recentRecommendations);


	String unknownRecommendationParameter() {
		String[] fakes = {
			//CLUBS
			"[http://osu.ppy.sh/b/80 Scatman John - Scatman [Insane]]   95%: 40pp | 98%: 43pp | 99%: 45pp | 100%: 48pp | 3:04 ★ 3.61 ♫ 136.02 AR4 ♣",
			"[http://osu.ppy.sh/b/123 Beck - Timebomb [Normal]]   95%: 29pp | 98%: 56pp | 99%: 71pp | 100%: 96pp | 2:47 ★ 2.2 ♫ 141.55 AR9 ♣",
			"[http://osu.ppy.sh/b/228 Marron Koshaku (feat. Shin) - Ã£Æ’â€˜Ã£Æ’ÂªÃ£â€šÂ¸Ã£Æ’Â§Ã£Æ’Å Ã¥Â¤Â§Ã¤Â½Å“Ã¦Ë†Â¦ [Normal]]   95%: 13pp | 98%: 15pp | 99%: 16pp | 100%: 18pp | 4:00 ★ 2.42 ♫ 150.98 AR3 ♣",
			"[http://osu.ppy.sh/b/235 Michael Jackson - Thriller [Easy]]   community: 5pp | best: 7pp | 2:42 ★ 1.44 ♫ 118.46 AR3 ♣",
			"[http://osu.ppy.sh/b/259 TRF - Survival dAnce ~no no cry more~ [Insane]]   95%: 49pp | 98%: 50pp | 99%: 50pp | 100%: 51pp | 0:11 ★ 3.88 ♫ 156 AR0 ♣",
			"[http://osu.ppy.sh/b/296 Trans-Siberian Orchestra - Wizards In Winter [Impossible]]   95%: 104pp | 98%: 120pp | 99%: 130pp | 100%: 143pp | 3:00 ★ 4.6 ♫ 148.4 AR7 ♣",
			"[http://osu.ppy.sh/b/298 Ken Clinger - Babilfrenzo [Hard]]   community: 27pp | best: 40pp | 2:45 ★ 2.25 ♫ 120 AR7 ♣",
			"[http://osu.ppy.sh/b/315 FAIRY FORE - Vivid [Insane]]   95%: 100pp | 98%: 109pp | 99%: 114pp | 100%: 122pp | 0:13 ★ 4.82 ♫ 168 AR7 ♣",
			"[http://osu.ppy.sh/b/328 SION - Funky Cat Maybe [Hard]]   95%: 24pp | 98%: 37pp | 99%: 44pp | 100%: 54pp | 3:31 ★ 2.58 ♫ 100 AR7 ♣",
			"[http://osu.ppy.sh/b/627 The INTERNET - At The Playground [Hard]]   95%: 25pp | 98%: 31pp | 99%: 35pp | 100%: 42pp | 1:45 ★ 2.91 ♫ 109.9 AR6 ♣",
			"[http://osu.ppy.sh/b/706 TV's Kyle - Sheep in the Morning [Normal]]   community: 11pp | best: 12pp | 2:38 ★ 2.16 ♫ 147 AR2 ♣",
			"[http://osu.ppy.sh/b/1393 Jonathan Coulton - Mandelbrot Set [Easy]]   community: 14pp | best: 19pp | 2:13 ★ 1.98 ♫ 160 AR5 ♣",
			"[http://osu.ppy.sh/b/1567 The Laziest Men on Mars - Invasion of the Gabber Robots [Hard]]   95%: 21pp | 98%: 27pp | 99%: 29pp | 100%: 34pp | 3:55 ★ 2.78 ♫ 160 AR5 ♣",
			"[http://osu.ppy.sh/b/2038 Marilyn Manson - This is Halloween [Insane]]   95%: 20pp | 98%: 26pp | 99%: 30pp | 100%: 34pp | 2:59 ★ 2.66 ♫ 168 AR5 ♣",
			"[http://osu.ppy.sh/b/2452 Erwin Beekveld - They're Taking The Hobbits To Isengard [Hard]]   95%: 19pp | 98%: 23pp | 99%: 25pp | 100%: 30pp | 1:38 ★ 2.72 ♫ 138 AR5 ♣",
			"[http://osu.ppy.sh/b/3556 Chrono Cross - Yasunori Mitsuda - Time's Scar [Insane]]   95%: 33pp | 98%: 38pp | 99%: 41pp | 100%: 45pp | 2:19 ★ 3.32 ♫ 223.75 AR5 ♣",
			"[http://osu.ppy.sh/b/4057 Dethklok - Duncan Hills Coffee Jingle [Hard]]   95%: 18pp | 98%: 24pp | 99%: 27pp | 100%: 33pp | 1:41 ★ 2.73 ♫ 120.25 AR6 ♣",
			"[http://osu.ppy.sh/b/4204 They Might Be Giants - Istanbul (Not Constantinople) [Hard]]   95%: 24pp | 98%: 28pp | 99%: 30pp | 100%: 35pp | 2:25 ★ 2.96 ♫ 114 AR5 ♣",
			"[http://osu.ppy.sh/b/4566 NicoNicoDouga - Farucon Pan! [Insane]]   95%: 55pp | 98%: 62pp | 99%: 66pp | 100%: 72pp | 1:01 ★ 3.92 ♫ 5000 AR6 ♥",
			"[http://osu.ppy.sh/b/5168 I Love Egg - Egg Song [Easy]]   95%: 4pp | 98%: 6pp | 99%: 6pp | 100%: 8pp | 1:12 ★ 1.68 ♫ 377.36 AR3 ♥",
			"[http://osu.ppy.sh/b/5186 World Famous - Robby's Song [Normal]]   community: 15pp | best: 22pp | 3:55 ★ 2.03 ♫ 1176.47 AR5 ♥",
			"[http://osu.ppy.sh/b/7714 Rick Astley - Never Gonna Give You Up 2 [Easy]]   95%: 6pp | 98%: 9pp | 99%: 10pp | 100%: 13pp | 3:07 ★ 1.81 ♫ 113.6 AR4 ♣",
			"[http://osu.ppy.sh/b/19496 Funtastic Power! - This is Sparta REMIX [This is madness!]]   95%: 3pp | 98%: 4pp | 99%: 5pp | 100%: 7pp | 2:07 ★ 1.57 ♫ 140.01 AR3 ♣",
			"[http://osu.ppy.sh/b/21240 igiulamam - What I want you to do [COOKIE!]]   community: 31pp | best: 45pp | 2:07 ★ 2.67 ♫ 125 AR7 ♣",
			"[http://osu.ppy.sh/b/24022 Hyadain - Chocobo [Black]]   95%: 62pp | 98%: 70pp | 99%: 75pp | 100%: 81pp | 2:16 ★ 3.96 ♫ 170 AR6 ♣",
			"[http://osu.ppy.sh/b/24118 Miyazaki Ui - Strategy [Don't let her shoot you!!?]]   95%: 19pp | 98%: 32pp | 99%: 40pp | 100%: 51pp | 3:11 ★ 2.37 ♫ 95 AR7 ♣",
			"[http://osu.ppy.sh/b/29933 The Lonely Island - Who Said We're Wack? [Wack]]   95%: 8pp | 98%: 9pp | 99%: 10pp | 100%: 11pp | 1:13 ★ 2.08 ♫ 190 AR3 ♣",
			"[http://osu.ppy.sh/b/31876 Atko - My Name is Boxxy (Extended) [Trollin']]   community: 9pp | best: 12pp | 1:25 ★ 1.89 ♫ 160 AR4 ♣",
			"[http://osu.ppy.sh/b/32176 NeoNintendo - Scrub Scrub Scrub [My House]]   95%: 1pp | 98%: 1pp | 99%: 1pp | 100%: 3pp | 1:11 ★ 1.13 ♫ 192.9 AR2 ♣",
			"[http://osu.ppy.sh/b/40306 Spinsmith - Sweet Religion [Satanist]]   95%: 29pp | 98%: 36pp | 99%: 40pp | 100%: 47pp | 1:43 ★ 3.02 ♫ 105 AR6 ♣",
			"[http://osu.ppy.sh/b/51907 Franz Liszt - La Campanella [Insane]]   95%: 59pp | 98%: 71pp | 99%: 78pp | 100%: 88pp | 1:56 ★ 3.83 ♫ 100 AR7 ♣",
			"[http://osu.ppy.sh/b/52862 Nico Nico Douga - Ronald's Perfect Math Class [baka baka]]   95%: 157pp | 98%: 190pp | 99%: 210pp | 100%: 237pp | 2:00 ★ 5.16 ♫ 175 AR9 ♣",
			"[http://osu.ppy.sh/b/53627 The Charlie Daniels Band - The Devil Went Down to Georgia [Furious!]]   95%: 94pp | 98%: 113pp | 99%: 125pp | 100%: 144pp | 3:10 ★ 4.49 ♫ 131 AR8 ♣",
			"[http://osu.ppy.sh/b/58781 Tenpei Sato - Is It Admiration for Overlord Laharl? [Bad-Ass Freakin' Overlord Gens]]   95%: 33pp | 98%: 46pp | 99%: 53pp | 100%: 70pp | 1:17 ★ 3.22 ♫ 91 AR8 ♣",
			"[http://osu.ppy.sh/b/60709 placeboing - Oh My God It's Christmas! [SMOKER]]   community: 2pp | best: 3pp | 0:42 ★ 1.14 ♫ 111 AR2 ♣",
			"[http://osu.ppy.sh/b/62084 NicoNicoDouga - Jaka Jaka Song [Insane]]   95%: 100pp | 98%: 121pp | 99%: 132pp | 100%: 149pp | 1:29 ★ 4.61 ♫ 162.04 AR8 ♣",
			"[http://osu.ppy.sh/b/70179 Peter Lambert - osu! tutorial [Rookie Gameplay]]   95%: 3pp | 98%: 4pp | 99%: 4pp | 100%: 6pp | 1:56 ★ 1.53 ♫ 160.38 AR4 ♣",
			"[http://osu.ppy.sh/b/75274 Perfume ft. peppy - Baby Cruising Love [Noob]]   community: 10pp | best: 15pp | 2:28 ★ 1.67 ♫ 128 AR5 ♣",
			"[http://osu.ppy.sh/b/75888 Paul K. Joyce - Can We Fix It? [Easy]]   95%: 4pp | 98%: 5pp | 99%: 5pp | 100%: 7pp | 2:59 ★ 1.67 ♫ 134 AR3 ♣",
			"[http://osu.ppy.sh/b/79200 NotShinta - Aha? [Troll]]   95%: 58pp | 98%: 64pp | 99%: 69pp | 100%: 76pp | 2:24 ★ 3.95 ♫ 177 AR8 ♣",
			"[http://osu.ppy.sh/b/79982 DJ~MoExx - Listen, Miku; Don't Say \"Bad Marisa Ass Apple Irony\" [Mashley-sama's Insane]]   95%: 81pp | 98%: 93pp | 99%: 102pp | 100%: 113pp | 2:33 ★ 4.34 ♫ 155 AR8 ♣",
			"[http://osu.ppy.sh/b/82573 Kagamine Rin - I Can Take Off My Panties! [ztrot's loli pantsu]]   95%: 77pp | 98%: 99pp | 99%: 112pp | 100%: 130pp | 3:16 ★ 4.05 ♫ 163 AR8 ♣",
			"[http://osu.ppy.sh/b/87717 Tenacious D - Classico [Insane]]   95%: 55pp | 98%: 69pp | 99%: 79pp | 100%: 93pp | 0:54 ★ 3.83 ♫ 170 AR9 ♣",
			"[http://osu.ppy.sh/b/94944 M2U - BlythE (Osuka) [Maniac]]   95%: 114pp | 98%: 135pp | 99%: 147pp | 100%: 164pp | 2:06 ★ 4.63 ♫ 180 AR3 ♣",
			"[http://osu.ppy.sh/b/116942 Go Ichinose - Team Plasma Appears! [vs. Noob Man]]   95%: 5pp | 98%: 5pp | 99%: 6pp | 100%: 7pp | 1:31 ★ 1.77 ♫ 185 AR2 ♣",
			"[http://osu.ppy.sh/b/143161 Nico Nico Douga - Fight Against Dangerous Hirasawa Yui & Susumu [Fish-tan]]   community: 2pp | best: 3pp | 0:51 ★ 1.13 ♫ 140.85 AR2 ♣",
			"[http://osu.ppy.sh/b/156352 Within Temptation - The Unforgiving [Marathon]]   95%: 218pp | 98%: 239pp | 99%: 252pp | 100%: 271pp | 52:57 ★ 5.26 ♫ 143.25 AR8 ♣",
			"[http://osu.ppy.sh/b/179425 Hanazawa Kana ft. Snoop Dogg - Weed Circulation [Smoke weed everyday]]   95%: 27pp | 98%: 36pp | 99%: 43pp | 100%: 54pp | 1:29 ★ 3.02 ♫ 120 AR8 ♣",
			"[http://osu.ppy.sh/b/229765 Shuki Levy - The Super Mario Bros. Super Show! [Lanturn's style]]   95%: 2pp | 98%: 3pp | 99%: 3pp | 100%: 4pp | 0:57 ★ 1.45 ♫ 198.1 AR3 ♣",
			"[http://osu.ppy.sh/b/237695 Larry Markes - Scooby-Doo, Where Are You? [Xin's Easy]]   95%: 3pp | 98%: 3pp | 99%: 4pp | 100%: 5pp | 0:58 ★ 1.54 ♫ 142 AR3 ♣",
			"[http://osu.ppy.sh/b/368157 gael42 & ThomasKHII - Battements d'Ame [Hard]]   95%: 36pp | 98%: 45pp | 99%: 51pp | 100%: 61pp | 1:27 ★ 3.31 ♫ 145 AR8 ♣",
			"[http://osu.ppy.sh/b/458638 3LAU - Jagger Bomb [Deagle God]]   95%: 14pp | 98%: 21pp | 99%: 26pp | 100%: 33pp | 0:34 ★ 2.49 ♫ 128 AR8 ♣",
			"[http://osu.ppy.sh/b/490316 Thunderclowns - thomas the weed engine [Insane]]   95%: 48pp | 98%: 56pp | 99%: 65pp | 100%: 80pp | 0:29 ★ 3.75 ♫ 98.9 AR8.7 ♣",
		//CLUBS (unranked)
			"[http://osu.ppy.sh/b/22538 Peter Lambert - osu! tutorial [Gameplay basics]]   95%: 0pp | 98%: 0pp | 99%: 0pp | 100%: 0pp | 1:59 ★ 0.61 ♫ 160 AR0 ♣",
			"[http://osu.ppy.sh/b/157582 Susumu Hirasawa - Bandiria Travellers [Insane]]   95%: 101pp | 98%: 123pp | 99%: 159pp | 100%: 189pp | 4:47 ★ 5.05 ♫ 160 AR7 ♣",
			"[http://osu.ppy.sh/b/180536 Seether - Fake It [I Can Fake It All!!]]   community: 48pp | best: 66pp  | 3:09 ★ 3.18 ♫ 132 AR7 ♣",
			"[http://osu.ppy.sh/b/246322 Matt Mulholland - My Heart Will Go On [INSSZZZAAAAAAVVvvne..]]   95%: 77pp | 98%: 85pp | 99%: 91pp | 100%: 99pp | 4:09 ★ 4.45 ♫ 156 AR6 ♣",
			"[http://osu.ppy.sh/b/369493 loos feat. Meramipop - Starlight Disco [Washing Machine]]   95%: 98pp | 98%: 107pp | 99%: 113pp | 100%: 120pp | 1:18 ★ 4.7 ♫ 128 AR8 ♣",
			"[http://osu.ppy.sh/b/372245 Knife Party - Centipede [Akali]]   95%: 117695pp | 98%: 121030pp | 99%: 123428pp | 100%: 127181pp | 2:16 ★ 48.00 ♫ 560 AR10 ♣",
		//CLUBS (wrong mode)
			"[http://osu.ppy.sh/b/135977 eXceed3rd (S.S.H.) - Intersect Thunderbolt [Verdi's Taiko]]   95%: 237pp | 98%: 267pp | 99%: 281pp | 100%: 297pp | 6.19 ★ 1:45 ♫ 250 ♣",
			"[http://osu.ppy.sh/b/178067 Igorrr - Pavor Nocturnus [Fatal Oni]]   95%: 227pp | 98%: 256pp | 99%: 270pp | 100%: 285pp | 3:56 ★ 5.59 ♫ 174 ♣",
			"[http://osu.ppy.sh/b/179231 weyheyhey !! - I'm Your Daddy [Fatal Oni]]   95%: 283pp | 98%: 319pp | 99%: 335pp | 100%: 355pp | 3:37 ★ 6.59 ♫ 225 ♣",
			"[http://osu.ppy.sh/b/245124 t+pazolite feat. Rizna – Distored Lovesong [Taikocalypse DX]]   95%: 263pp | 98%: 296pp | 99%: 312pp | 100%: 329pp | 7:08 ★ 6.23 ♫ 240 ♣",
			"[http://osu.ppy.sh/b/274783 Infected Mushroom – The Pretender [Fruitender]]   95%: 333pp | 98%: 338pp | 99%: 340pp | 100%: 343pp | 6:26 ★ 7.27 ♫ 174 AR9 ♣",
			"[http://osu.ppy.sh/b/327013 Electric Six - Gay Bar [Rainbow]]   95%: 219pp | 98%: 223pp | 99%: 225pp | 100%: 228pp | 2:18 ★ 6.46 ♫ 172 AR9 ♣",
			"[http://osu.ppy.sh/b/328412 KOTOKO - Wing my way [Hell-Jumping]]   95%: 261pp | 98%: 266pp | 99%: 268pp | 100%: 270pp | 2:26 ★ 6.83 ♫ 118 AR8 ♣",
			"[http://osu.ppy.sh/b/344892 LeaF - Calamity Fortune [Crystal's Overdose]]   95%: 325pp | 98%: 329pp | 99%: 331pp | 100%: 334pp | 2:14 ★ 7.24 ♫ 200 AR9 ♣",
			"[http://osu.ppy.sh/b/249346 Yuuna Sasara feat. Tai no Kobone - Imperishable Night 2006 [7K Lunatic]]   95%: 761pp | 98%: 832pp | 99%: 860pp | 100%: 897pp | 2:08 ★ 7.13 ♫ 161 ♣",
			"[http://osu.ppy.sh/b/413873 Melyceria&Disease - 357 BPM [Starstream Knot]]   95%: 398pp | 98%: 431pp | 99%: 446pp | 100%: 462pp | 1:04 ★ 5.57 ♫ 714 ♣",
			"[http://osu.ppy.sh/b/415541 DragonForce – Heroes of Our Time [Marathon Legend]]   95%: 347pp | 98%: 388pp | 99%: 402pp | 100%: 416pp | 7:11 ★ 5.42 ♫ 200 ♣",
			"[http://osu.ppy.sh/b/421066 Yuyoyuppe - AiAe [Wafles' SHD]]   95%: 645pp | 98%: 711pp | 99%: 735pp | 100%: 761pp | 3:56 ★ 6.77 ♫ 180 ♣",
		//DIAMONDS
			"[http://osu.ppy.sh/b/24722 Nico Nico Douga - BARUSA of MIKOSU [TAG4]]   95%: 872pp | 98%: 907pp | 99%: 923pp | 100%: 945pp | 3:00 ★ 9.53 ♫ 95 AR8 ♦",
			"[http://osu.ppy.sh/b/27638 Nico Nico Douga - U.N. Owen wa Kanojo nanoka? (Nico Mega Mix) [TAG4]]   95%: 1845pp | 98%: 1891pp | 99%: 1911pp | 100%: 1936pp | 3:26 ★ 11.65 ♫ 200 AR8 ♦",
			"[http://osu.ppy.sh/b/27737 DJ Sharpnel - StrangeProgram [Lesjuh's TAG]]   95%: 151pp | 98%: 166pp | 99%: 179pp | 100%: 202pp | 3:33 ★ 5.35 ♫ 215 AR8 ♦",
			"[http://osu.ppy.sh/b/29844 Hisaka Yoko - Don't say \"lazy\" (Full ver.) [TAG4]]   95%: 489pp | 98%: 509pp | 99%: 520pp | 100%: 534pp | 4:17 ★ 7.87 ♫ 181 AR7 ♦",
			"[http://osu.ppy.sh/b/32570 Blind Stare - Shotgun Symphony+ [Impossibly Intense]]   95%: 159pp | 98%: 184pp | 99%: 198pp | 100%: 219pp | 5:03 ★ 5.1 ♫ 362 AR8 ♦",
			"[http://osu.ppy.sh/b/33415 IOSYS - Border of Extacy [Doomsday]]   95%: 193pp | 98%: 218pp | 99%: 234pp | 100%: 256pp | 4:05 ★ 5.61 ♫ 204 AR8 ♦",
			"[http://osu.ppy.sh/b/35015 IOSYS - Utage wa Eien ni ~SHD~ [TAG4]]   95%: 907pp | 98%: 941pp | 99%: 958pp | 100%: 979pp | 3:20 ★ 9.71 ♫ 170 AR8 ♦",
			"[http://osu.ppy.sh/b/38426 t+pazolite - Luv-Lab-Poison 22ate! [Xtreme]]   95%: 167pp | 98%: 189pp | 99%: 201pp | 100%: 218pp | 2:13 ★ 5.5 ♫ 228 AR8 ♦",
			"[http://osu.ppy.sh/b/39076 Taiko no Tatsujin - Haya Saitama2000 [Oni]]   95%: 186pp | 98%: 208pp | 99%: 219pp | 100%: 236pp | 1:29 ★ 5.64 ♫ 250 AR8 ♦",
			"[http://osu.ppy.sh/b/39825 IOSYS - Marisa wa Taihen na Kanbu de Tomatte Ikimashita [Love]]   95%: 166pp | 98%: 189pp | 99%: 202pp | 100%: 222pp | 2:11 ★ 5.55 ♫ 199.96 AR8 ♦",
			"[http://osu.ppy.sh/b/40017 Niko - Made of Fire [Oni]]   95%: 166pp | 98%: 184pp | 99%: 195pp | 100%: 213pp | 1:17 ★ 5.43 ♫ 163 AR8 ♦",
			"[http://osu.ppy.sh/b/41044 Taiko no Tatsujin - Mekadesu. [Oni (2nd Gen)]]   95%: 183pp | 98%: 197pp | 99%: 205pp | 100%: 215pp | 1:59 ★ 5.69 ♫ 320 AR7 ♦",
			"[http://osu.ppy.sh/b/45923 YMCK - Family Dondon [Oni]]   95%: 146pp | 98%: 161pp | 99%: 169pp | 100%: 180pp | 2:09 ★ 5.25 ♫ 122 AR7 ♦",
			"[http://osu.ppy.sh/b/46298 Ochiai Yurika - Koibumi2000 [Oni(Futsuu)]]   95%: 135pp | 98%: 146pp | 99%: 151pp | 100%: 159pp | 2:00 ★ 5.16 ♫ 200 AR6 ♦",
			"[http://osu.ppy.sh/b/48098 Kucchy vs Akky - Yakumo ~ JOINT STRUGGLE [SOLO]]   95%: 207pp | 98%: 234pp | 99%: 249pp | 100%: 268pp | 2:04 ★ 5.72 ♫ 187 AR8 ♦",
			"[http://osu.ppy.sh/b/49101 m-flo loves CHEMISTRY - Astrosexy [Sexy]]   95%: 167pp | 98%: 192pp | 99%: 207pp | 100%: 230pp | 5:33 ★ 5.24 ♫ 120 AR8 ♦",
			"[http://osu.ppy.sh/b/50712 System of a Down - Vicinity of Obscenity [Impossible]]   95%: 169pp | 98%: 187pp | 99%: 199pp | 100%: 218pp | 2:40 ★ 5.56 ♫ 230 AR8 ♦",
			"[http://osu.ppy.sh/b/58064 beatMARIO - Night of Knights [TAG4]]   95%: 456pp | 98%: 486pp | 99%: 501pp | 100%: 522pp | 3:04 ★ 7.44 ♫ 153 AR8 ♦",
			"[http://osu.ppy.sh/b/63804 Boots Randolph - Yakety Sax [Ridiculous]]   95%: 185pp | 98%: 220pp | 99%: 243pp | 100%: 276pp | 4:28 ★ 5.37 ♫ 244 AR9 ♦",
			"[http://osu.ppy.sh/b/64267 Renard - Banned Forever [Nogard]]   95%: 174pp | 98%: 199pp | 99%: 214pp | 100%: 235pp | 3:29 ★ 5.43 ♫ 220 AR8 ♦",
			"[http://osu.ppy.sh/b/66246 Basshunter - Ievan Polkka Trance Remix [BeuKirby]]   95%: 180pp | 98%: 204pp | 99%: 218pp | 100%: 237pp | 3:29 ★ 5.41 ♫ 140 AR8 ♦",
			"[http://osu.ppy.sh/b/66514 Shin Hae Chul - Sticks and Stones [Madness]]   95%: 136pp | 98%: 155pp | 99%: 168pp | 100%: 186pp | 3:04 ★ 5.27 ♫ 150 AR8 ♦",
			"[http://osu.ppy.sh/b/66609 DragonForce - Revolution Deathsquad [Legend]]   95%: 271pp | 98%: 302pp | 99%: 321pp | 100%: 348pp | 7:48 ★ 5.93 ♫ 250 AR8 ♦",
			"[http://osu.ppy.sh/b/70760 DragonForce - Through The Fire And Flames [Legend]]   95%: 275pp | 98%: 297pp | 99%: 309pp | 100%: 326pp | 7:19 ★ 5.99 ♫ 200 AR8 ♦",
			"[http://osu.ppy.sh/b/71080 Kitsune^2 - He Has No Mittens [BD's Mittens]]   95%: 207pp | 98%: 224pp | 99%: 234pp | 100%: 249pp | 0:50 ★ 6.11 ♫ 210 AR9 ♦",
			"[http://osu.ppy.sh/b/72585 Kitsune^2 - Rainbow Tylenol [Hell]]   95%: 208pp | 98%: 227pp | 99%: 239pp | 100%: 258pp | 1:47 ★ 5.89 ♫ 135 AR9 ♦",
			"[http://osu.ppy.sh/b/79862 Maximum the Hormone - What's Up, People? [Lucifer]]   95%: 197pp | 98%: 236pp | 99%: 259pp | 100%: 290pp | 4:04 ★ 5.6 ♫ 161.05 AR9 ♦",
			"[http://osu.ppy.sh/b/83975 goreshit - MATZcore [Lolicore]]   95%: 276pp | 98%: 301pp | 99%: 316pp | 100%: 337pp | 2:36 ★ 6.34 ♫ 234.08 AR9 ♦",
			"[http://osu.ppy.sh/b/87570 IOSYS - Cirno's Perfect Math Class [TAG4]]   95%: 305pp | 98%: 328pp | 99%: 340pp | 100%: 359pp | 2:00 ★ 6.84 ♫ 175 AR8 ♦",
			"[http://osu.ppy.sh/b/92051 DragonForce - Cry for Eternity [Legend]]   95%: 288pp | 98%: 309pp | 99%: 322pp | 100%: 338pp | 8:08 ★ 6.07 ♫ 140 AR8 ♦",
			"[http://osu.ppy.sh/b/92780 Yousei Teikoku - Asgard [Valhalla]]   95%: 173pp | 98%: 195pp | 99%: 210pp | 100%: 233pp | 3:38 ★ 5.42 ♫ 260 AR9 ♦",
			"[http://osu.ppy.sh/b/95382 t+pazolite - chipscape [Ragnarok]]   95%: 310pp | 98%: 341pp | 99%: 358pp | 100%: 381pp | 4:05 ★ 6.37 ♫ 220 AR9 ♦",
			"[http://osu.ppy.sh/b/104229 Team Nekokan - Can't Defeat Airman [Holy Shit! It's Airman!!]]   95%: 339pp | 98%: 365pp | 99%: 378pp | 100%: 398pp | 3:21 ★ 7.03 ♫ 200 AR10 ♦",
			"[http://osu.ppy.sh/b/104842 S.S.H. - Holy Orders [LKs]]   95%: 267pp | 98%: 283pp | 99%: 294pp | 100%: 310pp | 4:05 ★ 6.28 ♫ 272.06 AR9 ♦",
			"[http://osu.ppy.sh/b/107875 Syrsa - Mad Machine [Champion]]   95%: 233pp | 98%: 245pp | 99%: 252pp | 100%: 264pp | 1:23 ★ 6.45 ♫ 270 AR9 ♦",
			"[http://osu.ppy.sh/b/112922 Renard - Because Maybe! pt. 3 [Marathon]]   95%: 302pp | 98%: 333pp | 99%: 353pp | 100%: 382pp | 17:08 ★ 5.94 ♫ 220 AR9 ♦",
			"[http://osu.ppy.sh/b/117580 Cres - End Time [Fear]]   95%: 228pp | 98%: 253pp | 99%: 266pp | 100%: 285pp | 2:08 ★ 5.9 ♫ 180 AR9 ♦",
			"[http://osu.ppy.sh/b/118068 Yousei Teikoku - Kokou no Sousei [Chaos]]   95%: 243pp | 98%: 258pp | 99%: 269pp | 100%: 285pp | 5:06 ★ 6.11 ♫ 240 AR9 ♦",
			"[http://osu.ppy.sh/b/118380 Bring Me The Horizon - Anthem [Lucifer]]   95%: 223pp | 98%: 257pp | 99%: 281pp | 100%: 316pp | 3:52 ★ 5.76 ♫ 220 AR9 ♦",
			"[http://osu.ppy.sh/b/119375 Nekomata Master - Byakuya Gentou [EX]]   95%: 143pp | 98%: 165pp | 99%: 179pp | 100%: 197pp | 1:58 ★ 5.08 ♫ 128 AR9 ♦",
			"[http://osu.ppy.sh/b/122693 S.S.H. - Intersect Thunderbolt-Remix [Exceed]]   95%: 252pp | 98%: 266pp | 99%: 275pp | 100%: 289pp | 2:03 ★ 6.23 ♫ 250 AR9 ♦",
			"[http://osu.ppy.sh/b/129891 xi - FREEDOM DiVE [FOUR DIMENSIONS]]   95%: 455pp | 98%: 488pp | 99%: 506pp | 100%: 530pp | 4:17 ★ 7.07 ♫ 222.22 AR9 ♦",
			"[http://osu.ppy.sh/b/131564 Lily - Scarlet Rose [0108 style]]   95%: 139pp | 98%: 150pp | 99%: 160pp | 100%: 175pp | 3:07 ★ 5.19 ♫ 320 AR9 ♦",
			"[http://osu.ppy.sh/b/131891 The Quick Brown Fox - The Big Black [WHO'S AFRAID OF THE BIG BLACK]]   95%: 278pp | 98%: 292pp | 99%: 301pp | 100%: 316pp | 2:18 ★ 6.58 ♫ 360.3 AR10 ♦",
			"[http://osu.ppy.sh/b/132466 Megpoid GUMI - Rubik's Cube [Cube]]   95%: 156pp | 98%: 174pp | 99%: 188pp | 100%: 213pp | 3:32 ★ 5.3 ♫ 280 AR9 ♦",
			"[http://osu.ppy.sh/b/133938 Hatsune Miku - Atama no Taisou [Nogard]]   95%: 332pp | 98%: 354pp | 99%: 367pp | 100%: 388pp | 2:53 ★ 6.91 ♫ 120 AR9 ♦",
			"[http://osu.ppy.sh/b/135142 S.S.H. - Intersect Thunderbolt [Thunderbolt]]   95%: 168pp | 98%: 180pp | 99%: 188pp | 100%: 203pp | 2:03 ★ 5.55 ♫ 250 AR9 ♦",
			"[http://osu.ppy.sh/b/142145 LEAF XCEED Music Division - YuYu Metal [DoKo]]   95%: 166pp | 98%: 185pp | 99%: 199pp | 100%: 221pp | 2:59 ★ 5.51 ♫ 136 AR9 ♦",
			"[http://osu.ppy.sh/b/142239 Ryu* vs. kors k - Force of Wind [Extra]]   95%: 189pp | 98%: 211pp | 99%: 225pp | 100%: 247pp | 2:53 ★ 5.71 ♫ 177 AR9 ♦",
			"[http://osu.ppy.sh/b/142954 Yousei Teikoku - Senketsu no Chikai [Insanity]]   95%: 258pp | 98%: 274pp | 99%: 284pp | 100%: 298pp | 4:08 ★ 6.4 ♫ 180 AR9 ♦",
			"[http://osu.ppy.sh/b/144026 DragonForce - Fallen World [Legend]]   95%: 316pp | 98%: 335pp | 99%: 346pp | 100%: 362pp | 4:02 ★ 6.6 ♫ 55 AR9 ♦",
			"[http://osu.ppy.sh/b/144029 Mutsuhiko Izumi - Red Goose [Superable]]   95%: 242pp | 98%: 264pp | 99%: 277pp | 100%: 297pp | 1:57 ★ 6.07 ♫ 200 AR9 ♦",
			"[http://osu.ppy.sh/b/145669 EZFG - Hurting for a Very Hurtful Pain [Dance]]   95%: 177pp | 98%: 194pp | 99%: 208pp | 100%: 233pp | 2:59 ★ 5.51 ♫ 140 AR9 ♦",
			"[http://osu.ppy.sh/b/146610 VY1 - Cyber Thunder Cider [Cider]]   95%: 158pp | 98%: 177pp | 99%: 191pp | 100%: 213pp | 3:04 ★ 5.29 ♫ 145 AR9 ♦",
			"[http://osu.ppy.sh/b/146957 Black Hole - Pluto [Challenge]]   95%: 234pp | 98%: 252pp | 99%: 263pp | 100%: 282pp | 1:39 ★ 6.29 ♫ 100 AR9 ♦",
			"[http://osu.ppy.sh/b/151229 Hatsune Miku - Mythologia's End [Myth0108ia]]   95%: 235pp | 98%: 255pp | 99%: 267pp | 100%: 283pp | 4:39 ★ 5.77 ♫ 195 AR9 ♦",
			"[http://osu.ppy.sh/b/153857 HujuniseikouyuuP - Talent Shredder [Lesjuh style]]   95%: 165pp | 98%: 177pp | 99%: 187pp | 100%: 204pp | 3:26 ★ 5.45 ♫ 280 AR9 ♦",
			"[http://osu.ppy.sh/b/156905 SAVE THE QUEEN - EX-Termination [Combustion]]   95%: 203pp | 98%: 221pp | 99%: 236pp | 100%: 258pp | 3:06 ★ 5.96 ♫ 256 AR9 ♦",
			"[http://osu.ppy.sh/b/168031 Beatdrop - Phase 1 [SHD]]   95%: 190pp | 98%: 207pp | 99%: 219pp | 100%: 242pp | 2:06 ★ 5.64 ♫ 155 AR9 ♦",
			"[http://osu.ppy.sh/b/168666 a-ha - Analogue [Abomination]]   95%: 113pp | 98%: 134pp | 99%: 147pp | 100%: 165pp | 3:39 ★ 4.78 ♫ 115.07 AR8 ♦",
			"[http://osu.ppy.sh/b/169841 The Ghost Of 3.13 - Forgotten [grumd]]   95%: 214pp | 98%: 246pp | 99%: 269pp | 100%: 305pp | 2:41 ★ 6 ♫ 250 AR9 ♦",
			"[http://osu.ppy.sh/b/171256 paraoka - boot [0108]]   95%: 192pp | 98%: 206pp | 99%: 215pp | 100%: 230pp | 3:17 ★ 5.61 ♫ 236 AR9 ♦",
			"[http://osu.ppy.sh/b/172662 Renard - Rainbow Dash Likes Girls (Stay Gay Pony Girl) [Holy Shit! It's Rainbow Dash!!]]   95%: 352pp | 98%: 378pp | 99%: 392pp | 100%: 412pp | 3:11 ★ 6.99 ♫ 220 AR9 ♦",
			"[http://osu.ppy.sh/b/177162 Yousei Teikoku - Kyouki Chinden (TV Size) [Madness Precipitation]]   95%: 168pp | 98%: 182pp | 99%: 194pp | 100%: 215pp | 1:34 ★ 5.57 ♫ 280 AR10 ♦",
			"[http://osu.ppy.sh/b/178645 UNDEAD CORPORATION - Yoru Naku Usagi wa Yume o Miru [CRN's Extra]]   95%: 232pp | 98%: 248pp | 99%: 258pp | 100%: 273pp | 3:30 ★ 5.97 ♫ 200 AR9 ♦",
			"[http://osu.ppy.sh/b/187501 kemu - Ikasama Life Game [Skystar]]   95%: 191pp | 98%: 213pp | 99%: 229pp | 100%: 252pp | 3:58 ★ 5.53 ♫ 200 AR9 ♦",
			"[http://osu.ppy.sh/b/199304 utsuP - Adult's Toy [Desecration]]   95%: 158pp | 98%: 173pp | 99%: 187pp | 100%: 217pp | 4:31 ★ 5.31 ♫ 280 AR9 ♦",
			"[http://osu.ppy.sh/b/202756 Yousei Teikoku - The Creator [Nyaten]]   95%: 232pp | 98%: 265pp | 99%: 288pp | 100%: 326pp | 3:41 ★ 5.92 ♫ 280 AR10 ♦",
			"[http://osu.ppy.sh/b/205941 Last Note. - Setsuna Trip (Short Ver.) [Zenkai]]   95%: 163pp | 98%: 173pp | 99%: 180pp | 100%: 195pp | 1:47 ★ 5.53 ♫ 145 AR9 ♦",
			"[http://osu.ppy.sh/b/211154 MomoKuro-tei Ichimon - Nippon Egao Hyakkei (TV Size) [Egao]]   95%: 132pp | 98%: 159pp | 99%: 177pp | 100%: 202pp | 1:27 ★ 5.12 ♫ 160 AR8 ♦",
			"[http://osu.ppy.sh/b/214248 UNDEAD CORPORATION - Yoru Naku Usagi wa Yume wo Miru [BakaNA]]   95%: 227pp | 98%: 245pp | 99%: 255pp | 100%: 270pp | 4:19 ★ 5.81 ♫ 87.5 AR9 ♦",
			"[http://osu.ppy.sh/b/215238 Rohi - Kakuzetsu Thanatos [Rin]]   95%: 202pp | 98%: 224pp | 99%: 240pp | 100%: 263pp | 3:31 ★ 5.59 ♫ 200 AR9 ♦",
			"[http://osu.ppy.sh/b/220231 Zips - Heisei Cataclysm [0108]]   95%: 194pp | 98%: 207pp | 99%: 216pp | 100%: 231pp | 2:50 ★ 5.74 ♫ 220 AR9 ♦",
			"[http://osu.ppy.sh/b/221026 Mago de Oz - Xanandra [Insane]]   95%: 218pp | 98%: 241pp | 99%: 257pp | 100%: 280pp | 4:19 ★ 5.82 ♫ 188.97 AR9 ♦",
			"[http://osu.ppy.sh/b/226605 t+pazolite feat. Rizna - Distorted Lovesong [Love]]   95%: 241pp | 98%: 264pp | 99%: 281pp | 100%: 310pp | 7:05 ★ 5.83 ♫ 240 AR9 ♦",
			"[http://osu.ppy.sh/b/234444 TAG - PRANA [Extreme]]   95%: 183pp | 98%: 206pp | 99%: 218pp | 100%: 237pp | 1:42 ★ 5.58 ♫ 188 AR9 ♦",
			"[http://osu.ppy.sh/b/240689 jippusu - Mushikui Saikede Rhythm [RLC]]   95%: 196pp | 98%: 217pp | 99%: 232pp | 100%: 254pp | 3:10 ★ 5.73 ♫ 210 AR9 ♦",
			"[http://osu.ppy.sh/b/245284 Igorrr - Unpleasant Sonata [Insane]]   95%: 227pp | 98%: 249pp | 99%: 263pp | 100%: 283pp | 2:09 ★ 6.04 ♫ 224 AR9 ♦",
			"[http://osu.ppy.sh/b/245960 DragonForce - Heroes of Our Time [Legend]]   95%: 272pp | 98%: 301pp | 99%: 320pp | 100%: 349pp | 7:02 ★ 5.96 ♫ 200 AR9 ♦",
			"[http://osu.ppy.sh/b/252238 Tatsh - IMAGE -MATERIAL- <Version 0> [Scorpiour]]   95%: 406pp | 98%: 433pp | 99%: 452pp | 100%: 480pp | 6:24 ★ 7.02 ♫ 130 AR10 ♦",
			"[http://osu.ppy.sh/b/256027 bibuko - Reizouko Mitara Pudding ga Nai [Jumpudding!]]   95%: 249pp | 98%: 264pp | 99%: 273pp | 100%: 285pp | 1:54 ★ 6.41 ♫ 190 AR9 ♦",
			"[http://osu.ppy.sh/b/264090 LeaF - I [Terror]]   95%: 255pp | 98%: 273pp | 99%: 287pp | 100%: 309pp | 2:33 ★ 6.21 ♫ 220 AR10 ♦",
			"[http://osu.ppy.sh/b/270490 Renard - Terminal [EXTRA]]   95%: 186pp | 98%: 202pp | 99%: 216pp | 100%: 240pp | 2:29 ★ 5.59 ♫ 185 AR10 ♦",
			"[http://osu.ppy.sh/b/276366 HujuniseikouyuuP - MISTAKE [Ms.0108]]   95%: 229pp | 98%: 254pp | 99%: 269pp | 100%: 291pp | 3:31 ★ 5.86 ♫ 205 AR9 ♦",
			"[http://osu.ppy.sh/b/278399 bibuko - Sorairo Gahou [HaHo-Insane]]   95%: 206pp | 98%: 224pp | 99%: 238pp | 100%: 260pp | 2:31 ★ 5.86 ♫ 250 AR9 ♦",
			"[http://osu.ppy.sh/b/279481 FOLiACETATE - Heterochromia Iridis [Terror]]   95%: 278pp | 98%: 303pp | 99%: 317pp | 100%: 336pp | 2:03 ★ 6.27 ♫ 223 AR9 ♦",
			"[http://osu.ppy.sh/b/281036 Suzaku - Anisakis -somatic mutation type \"Forza\"- [ExTrA]]   95%: 198pp | 98%: 222pp | 99%: 235pp | 100%: 253pp | 2:10 ★ 5.69 ♫ 185 AR9 ♦",
			"[http://osu.ppy.sh/b/300689 DystopiaGround - AugoEidEs [nao]]   95%: 292pp | 98%: 319pp | 99%: 337pp | 100%: 365pp (rough estimates) | 6:50 ★ 5.55 ♫ 207 AR9 ♦",
			"[http://osu.ppy.sh/b/306683 dj TAKA meets DJ YOSHITAKA ft.guit.good-cool - Elemental Creation -GITADO ROCK ver.- [Extra]]   95%: 308pp | 98%: 333pp | 99%: 347pp | 100%: 367pp | 2:03 ★ 6.55 ♫ 212.02 AR9 ♦",
			"[http://osu.ppy.sh/b/307410 Akiyama Uni - Chi no Iro wa Kiiro [Extra]]   95%: 173pp | 98%: 188pp | 99%: 202pp | 100%: 225pp | 3:00 ★ 5.51 ♫ 144 AR9 ♦",
			"[http://osu.ppy.sh/b/312959 ChouchouP - Tsukimiyo Rabbit [Tsuki]]   95%: 172pp | 98%: 188pp | 99%: 203pp | 100%: 231pp | 3:50 ★ 5.38 ♫ 240 AR9 ♦",
			"[http://osu.ppy.sh/b/316018 Akiyama Uni - The Grimoire of Alice [Extra]]   95%: 203pp | 98%: 227pp | 99%: 241pp | 100%: 262pp | 3:03 ★ 5.64 ♫ 142 AR9 ♦",
			"[http://osu.ppy.sh/b/321521 Suzuki Konomi 'n Kiba of Akiba - Watashi ga Motenai no wa Dou Kangaete mo Omaera ga Warui! [Rejection]]   95%: 164pp | 98%: 183pp | 99%: 194pp | 100%: 212pp | 1:24 ★ 5.47 ♫ 200 AR9 ♦",
			"[http://osu.ppy.sh/b/334760 Erik \"Jit\" Scheele - Negastrife [Marathon]]   95%: 278pp | 98%: 305pp | 99%: 321pp | 100%: 344pp | 6:27 ★ 6.2 ♫ 83.99 AR9 ♦",
			"[http://osu.ppy.sh/b/340652 Zhou Li Ming - Pi Li Pa La [Extra]]   95%: 254pp | 98%: 277pp | 99%: 292pp | 100%: 314pp | 3:28 ★ 6.08 ♫ 125 AR9 ♦",
			"[http://osu.ppy.sh/b/343741 Hanatan, yuikonnu & Mitani Nana - Songs Compilation [Rabbit Face]]   95%: 283pp | 98%: 303pp | 99%: 317pp | 100%: 337pp | 11:22 ★ 5.9 ♫ 98 AR9 ♦",
			"[http://osu.ppy.sh/b/363633 Suzaku - Anisakis -somatic mutation type \"Forza\"- [Extra]]   95%: 185pp | 98%: 204pp | 99%: 217pp | 100%: 238pp | 2:10 ★ 5.67 ♫ 185 AR9 ♦",
			"[http://osu.ppy.sh/b/368845 DJ TOTTO feat. Sunao Yoshikawa - Arousing [HW's Extra]]   95%: 159pp | 98%: 170pp | 99%: 178pp | 100%: 192pp | 1:54 ★ 5.52 ♫ 179 AR9.3 ♦",
			"[http://osu.ppy.sh/b/381798 Nhato - Miss You [Selentia]]   95%: 167pp | 98%: 189pp | 99%: 207pp | 100%: 235pp | 6:05 ★ 5.17 ♫ 132 AR9 ♦",
			"[http://osu.ppy.sh/b/383536 a_hisa - Cheshire,s dance [Another]]   95%: 246pp | 98%: 285pp | 99%: 309pp | 100%: 344pp | 2:56 ★ 5.89 ♫ 120 AR10 ♦",
			"[http://osu.ppy.sh/b/386760 Yooh - snow storm -euphoria- [EUPHORIC]]   95%: 206pp | 98%: 232pp | 99%: 245pp | 100%: 265pp | 1:56 ★ 5.74 ♫ 180 AR9 ♦",
			"[http://osu.ppy.sh/b/389703 kors k - Insane Techniques [HanzeR's Extreme]]   95%: 155pp | 98%: 189pp | 99%: 210pp | 100%: 239pp | 2:04 ★ 5.47 ♫ 146 AR9 ♦",
			"[http://osu.ppy.sh/b/403276 Zips - Reiwai Terrorism [Terror]]   95%: 157pp | 98%: 171pp | 99%: 184pp | 100%: 210pp | 2:46 ★ 5.33 ♫ 280 AR9 ♦",
			"[http://osu.ppy.sh/b/412288 Halozy - Sentimental Skyscraper [Myouren Hijiri]]   95%: 283pp | 98%: 311pp | 99%: 328pp | 100%: 352pp | 5:26 ★ 6.23 ♫ 183 AR9 ♦",
			"[http://osu.ppy.sh/b/421743 Sota Fujimori - WOBBLE IMPACT [fanzhen's Extra]]   95%: 146pp | 98%: 163pp | 99%: 176pp | 100%: 198pp | 1:54 ★ 5.27 ♫ 190 AR9 ♦",
			"[http://osu.ppy.sh/b/427586 Utagumi Setsugetsuka - Maware! Setsugetsuka chiptune Remix [MawareXtrA]]   95%: 184pp | 98%: 205pp | 99%: 221pp | 100%: 250pp | 5:16 ★ 5.44 ♫ 145 AR9.2 ♦",
			"[http://osu.ppy.sh/b/433346 Miyano Mamoru - Canon [Dream]]   95%: 182pp | 98%: 206pp | 99%: 221pp | 100%: 243pp | 1:39 ★ 5.71 ♫ 208 AR9 ♦",
			"[http://osu.ppy.sh/b/439264 Tokisawa Nao - BRYNHILDR IN THE DARKNESS -Ver. EJECTED- [DARKNESS]]   95%: 141pp | 98%: 158pp | 99%: 169pp | 100%: 188pp | 1:22 ★ 5.3 ♫ 198 AR9 ♦",
			"[http://osu.ppy.sh/b/529285 LeaF - Evanescent [Aspire]]   95%: 211pp | 98%: 232pp | 99%: 245pp | 100%: 267pp | 2:04 ★ 5.78 ♫ 190 AR9.4 ♦",
		//DIAMONDS (unranked)
			"[http://osu.ppy.sh/b/106965 DM Ashura - deltaMAX [Unexpected]]   95%: 317pp | 98%: 336pp | 99%: 349pp | 100%: 363pp | 1:54 ★ 6.89 ♫ 100 AR9 ♦",
			"[http://osu.ppy.sh/b/129847 Hatsune Miku - Story of my Wife [Warota]]   95%: 179pp | 98%: 195pp | 99%: 204pp | 100%: 217pp | 2:37 ★ 5.63 ♫ 148 AR9 ♦",
			"[http://osu.ppy.sh/b/135370 Jomekka - Eighto [490 Style]]   95%: 337pp | 98%: 356pp | 99%: 369pp | 100%: 384pp | 2:55 ★ 6.96 ♫ 234 AR10 ♦",
			"[http://osu.ppy.sh/b/149550 Various Artists - Stream Practice Maps 2 [FREEDOM DiVE BPM222.22]]   95%: 250pp | 98%: 273pp | 99%: 288pp | 100%: 310pp | 2:19 ★ 6.08 ♫ 222.22 AR9 ♦",
			"[http://osu.ppy.sh/b/161902 Renard - Dash Hopes 3 [300 BPM Madness!]]   95%: 445pp | 98%: 478pp | 99%: 496pp | 100%: 521pp | 1:03 ★ 7.45 ♫ 300 AR10 ♦",
			"[http://osu.ppy.sh/b/272317 IOSYS - Endless Tewi-ma Park [Tewi 2B Expert Edition]]   95%: 124pp | 98%: 131pp | 99%: 138pp | 100%: 142pp | 2:18 ★ 5.19 ♫ 32 AR8 ♦",
			"[http://osu.ppy.sh/b/308917 Manabu Namiki - Tenshi [Inibachi]]   95%: 597pp | 98%: 619pp | 99%: 637pp | 100%: 666pp | 1:03 ★ 8.31 ♫ 270 AR10 ♦",
			"[http://osu.ppy.sh/b/315900 Hari - Gwiyomi Song [Silyomi]]   95%: 149pp | 98%: 165pp | 99%: 176pp | 100%: 198pp | 1:08 ★ 5.33 ♫ 92 AR8 ♦",
			"[http://osu.ppy.sh/b/335041 MiddleIsland - Delrio [Insane]]   95%: 423pp | 98%: 456pp | 99%: 474pp | 100%: 499pp | 3:12 ★ 7.07 ♫ 210 AR10 ♦",
			"[http://osu.ppy.sh/b/354355 USAO - Miracle 5ympho X [Onii-chan ~ !]]   95%: 263pp | 98%: 288pp | 99%: 302pp | 100%: 323pp | 2:00 ★ 6.10 ♫ 210 AR9 ♦",
			"[http://osu.ppy.sh/b/366028 aran - Graces of Heaven [Normal]]   95%: 240pp | 98%: 256pp | 99%: 264pp | 100%: 277pp | 6:14 ★ 6.30 ♫ 174 AR9 ♦",
			"[http://osu.ppy.sh/b/378175 Darude - Sandstorm (Nightcore Mix) [325.67 BPM in your face!!!!!!!!]]   95%: 214pp | 98%: 233pp | 99%: 246pp | 100%: 268pp | 3:05 ★ 5.90 ♫ 326.67 AR10 ♦",
			"[http://osu.ppy.sh/b/384334 Camellia - Bangin' Burst [xSiRix's Extra [AR10]]]   95%: 253pp | 98%: 265pp | 99%: 272pp | 100%: 284pp | 1:57 ★ 6.54 ♫ 234 AR10 ♦",
			"[http://osu.ppy.sh/b/391334 Shoujo - Reminiscing [asdf]]   95%: 162pp | 98%: 183pp | 99%: 196pp | 100%: 217pp | 2:39 ★ 5.24 ♫ 175 AR9 ♦",
			"[http://osu.ppy.sh/b/399017 Various Nightcore - Jump Training #2 [We Go Down]]   95%: 218pp | 98%: 234pp | 99%: 243pp | 100%: 257pp | 2:23 ★ 5.83 ♫ 215 AR9 ♦",
		//SPADES
			"[http://osu.ppy.sh/b/3485 Freestyle - Please Tell Me Why [Normal]]   95%: 8pp | 98%: 13pp | 99%: 16pp | 100%: 21pp | 4:13 ★ 2 ♫ 80 AR5 ♠",
			"[http://osu.ppy.sh/b/14648 - You are an Idiot! [Easy]]   95%: 3pp | 98%: 4pp | 99%: 5pp | 100%: 9pp | 1:57 ★ 1.4 ♫ 172.29 AR4 ♠",
			"[http://osu.ppy.sh/b/21356 Bloodhound Gang - I Hope You Die [Normal]]   community: 14pp | best: 21pp | 3:14 ★ 2.14 ♫ 160 AR5 ♠",
			"[http://osu.ppy.sh/b/21756 Weird Al Yankovic - Dare To Be Stupid [Intense]]   community: 39pp | best: 48pp | 3:10 ★ 2.91 ♫ 89.85 AR6 ♠",
			"[http://osu.ppy.sh/b/25041 Prozzak - Sucks To Be You [Easy]]   community: 6pp | best: 9pp | 1:55 ★ 1.56 ♫ 125 AR4 ♠",
			"[http://osu.ppy.sh/b/26692 3oh!3 - I'm Not Your Boyfriend Baby [Insane]]   community: 58pp | best: 67pp | 3:40 ★ 3.51 ♫ 140 AR6 ♠",
			"[http://osu.ppy.sh/b/27185 Daisuke Ishiwatari - Home Sweet Grave [Normal]]   community: 12pp | best: 16pp | 2:39 ★ 2.13 ♫ 145 AR4 ♠",
			"[http://osu.ppy.sh/b/28286 Ashens - Silly Monkey [Brain Damage]]   95%: 61pp | 98%: 72pp | 99%: 82pp | 100%: 99pp | 1:45 ★ 4 ♫ 125 AR8 ♠",
			"[http://osu.ppy.sh/b/30160 Paramore - That's What You Get [Normal]]   95%: 8pp | 98%: 12pp | 99%: 15pp | 100%: 19pp | 2:14 ★ 2.02 ♫ 131 AR5 ♠",
			"[http://osu.ppy.sh/b/30479 Toyosaki Aki - Happy!? Sorry!! [Easy]]   community: 2pp | best: 3pp | 1:26 ★ 1.3 ♫ 179.98 AR1 ♠",
			"[http://osu.ppy.sh/b/37244 Imogen Heap - Have You Got It In You? [Easy]]   community: 6pp | best: 8pp | 3:51 ★ 1.56 ♫ 166 AR3 ♠",
			"[http://osu.ppy.sh/b/39732 La Roux - I'm Not Your Toy [Hard]]   community: 42pp | best: 55pp | 3:17 ★ 2.93 ♫ 124.3 AR7 ♠",
			"[http://osu.ppy.sh/b/40531 Emilie Autumn - I Know Where You Sleep [Psychotic]]   95%: 105pp | 98%: 126pp | 99%: 138pp | 100%: 156pp | 3:11 ★ 4.53 ♫ 160 AR8 ♠",
			"[http://osu.ppy.sh/b/42571 La Roux - Cover My Ears [Noob]]   community: 14pp | best: 18pp | 1:02 ★ 2.1 ♫ 142 AR5 ♠",
			"[http://osu.ppy.sh/b/44776 The Killers - For Reasons Unknown [Easy]]   community: 5pp | best: 7pp | 3:15 ★ 1.38 ♫ 141.4 AR3 ♠",
			"[http://osu.ppy.sh/b/50354 IOSYS - Danzai Yamaxanadu [Eternal Damnation]]   95%: 149pp | 98%: 173pp | 99%: 186pp | 100%: 206pp | 3:39 ★ 5.15 ♫ 175 AR8 ♠",
			"[http://osu.ppy.sh/b/52405 Within Temptation (feat. Keith Caputo) - What Have You Done [Normal]]   95%: 12pp | 98%: 16pp | 99%: 19pp | 100%: 24pp | 4:56 ★ 2.26 ♫ 172 AR5 ♠",
			"[http://osu.ppy.sh/b/52615 Silver Forest - EXTRA BITTER [Normal]]   95%: 5pp | 98%: 6pp | 99%: 6pp | 100%: 8pp | 1:37 ★ 1.81 ♫ 128 AR2 ♠",
			"[http://osu.ppy.sh/b/55768 Renard - You Goddamn Fish [Insane]]   95%: 43pp | 98%: 48pp | 99%: 55pp | 100%: 66pp | 1:03 ★ 3.62 ♫ 226 AR7 ♠",
			"[http://osu.ppy.sh/b/64266 Renard - Banned Forever [Lesjuh]]   95%: 158pp | 98%: 181pp | 99%: 194pp | 100%: 215pp | 3:29 ★ 5.33 ♫ 220 AR8 ♠",
			"[http://osu.ppy.sh/b/68624 HTT - NO, Thank You! [Collab]]   95%: 107pp | 98%: 129pp | 99%: 143pp | 100%: 164pp | 4:00 ★ 4.61 ♫ 181 AR8 ♠",
			"[http://osu.ppy.sh/b/70026 Evil Activities & DJ Panic ft MC Alee - Never Fall Asleep [Easy]]   95%: 2pp | 98%: 4pp | 99%: 5pp | 100%: 7pp | 3:15 ★ 1.33 ♫ 170 AR3 ♠",
			"[http://osu.ppy.sh/b/70258 EastNewSound - I Wish You Would Die [Collab]]   95%: 80pp | 98%: 105pp | 99%: 121pp | 100%: 144pp | 5:53 ★ 4.01 ♫ 142 AR8 ♠",
			"[http://osu.ppy.sh/b/70312 SUPER STAR -MITSURU- - THANK YOU FOR PLAYING [Beginner]]   95%: 4pp | 98%: 4pp | 99%: 5pp | 100%: 5pp | 1:33 ★ 1.63 ♫ 170 AR2 ♠",
			"[http://osu.ppy.sh/b/74603 Boom Boom Satellites - Shut Up And Explode [Easy]]   community: 2pp | best: 4pp | 2:38 ★ 1.1 ♫ 150 AR2 ♠",
			"[http://osu.ppy.sh/b/75362 Katy Perry - Ur So Gay [Gay]]   community: 24pp | best: 47pp | 3:30 ★ 2.67 ♫ 158 AR7 ♠",
			"[http://osu.ppy.sh/b/76363 Depeche Mode - Wrong [CrossWise]]   community: 5pp | best: 7pp | 3:09 ★ 1.45 ♫ 163.79 AR3 ♠",
			"[http://osu.ppy.sh/b/78077 Usher ft. Will.I.Am - OMG [Easy]]   community: 2pp | best: 4pp | 3:41 ★ 1.24 ♫ 130 AR2 ♠",
			"[http://osu.ppy.sh/b/79017 3rd Coast - STOP [LiteStyle]]   community: 13pp | best: 18pp | 1:22 ★ 1.99 ♫ 90 AR5 ♠",
			"[http://osu.ppy.sh/b/80756 Sum 41 - Thanks For Nothing [Normal]]   95%: 5pp | 98%: 6pp | 99%: 6pp | 100%: 8pp | 2:48 ★ 1.83 ♫ 204 AR3 ♠",
			"[http://osu.ppy.sh/b/83091 Taylor Swift - Better Than Revenge [Easy]]   95%: 6pp | 98%: 7pp | 99%: 8pp | 100%: 9pp | 3:33 ★ 1.89 ♫ 145.97 AR2 ♠",
			"[http://osu.ppy.sh/b/87230 Avril Lavigne - What The Hell [Tough]]   95%: 44pp | 98%: 56pp | 99%: 63pp | 100%: 75pp | 3:36 ★ 3.49 ♫ 150 AR8 ♠",
			"[http://osu.ppy.sh/b/93979 Evanescence - Call Me When You're Sober [Easy]]   95%: 3pp | 98%: 4pp | 99%: 5pp | 100%: 6pp | 3:26 ★ 1.6 ♫ 186.96 AR3 ♠",
			"[http://osu.ppy.sh/b/97010 Jonathan Coulton & GLaDOS - Want You Gone [Easy]]   95%: 3pp | 98%: 4pp | 99%: 4pp | 100%: 6pp | 2:12 ★ 1.62 ♫ 100 AR2 ♠",
			"[http://osu.ppy.sh/b/97301 Jonathan Coulton & GLaDOS - Want You Gone [Easy]]   95%: 4pp | 98%: 5pp | 99%: 7pp | 100%: 8pp | 2:11 ★ 1.71 ♫ 100 AR4 ♠",
			"[http://osu.ppy.sh/b/101924 Jonathan Coulton & GLaDOS - Want You Gone [Easy]]   95%: 6pp | 98%: 7pp | 99%: 7pp | 100%: 9pp | 2:12 ★ 1.86 ♫ 100 AR2 ♠",
			"[http://osu.ppy.sh/b/103153 Gigi D'Agostino - Bla Bla Bla [Bla Bla]]   95%: 6pp | 98%: 10pp | 99%: 14pp | 100%: 18pp | 3:04 ★ 1.79 ♫ 132.9 AR5 ♠",
			"[http://osu.ppy.sh/b/112840 Skrillex - Kill EVERYBODY [Easy]]   95%: 4pp | 98%: 5pp | 99%: 6pp | 100%: 7pp | 2:42 ★ 1.7 ♫ 127 AR3 ♠",
			"[http://osu.ppy.sh/b/118019 Celldweller - The Best It's Gonna Get [Best]]   95%: 38pp | 98%: 49pp | 99%: 58pp | 100%: 73pp | 4:47 ★ 3.39 ♫ 130.01 AR8 ♠",
			"[http://osu.ppy.sh/b/119517 Meg & Dia - Agree to Disagree [Easy]]   community: 10pp | best: 12pp | 3:29 ★ 1.99 ♫ 200 AR3 ♠",
			"[http://osu.ppy.sh/b/136489 4Minute - WHY [Easy]]   community: 3pp | best: 5pp | 2:59 ★ 1.42 ♫ 130 AR2 ♠",
			"[http://osu.ppy.sh/b/137782 DECO*27 feat. marina - Dummy Dummy [Easy]]   community: 6pp | best: 8pp | 2:51 ★ 1.73 ♫ 160 AR3 ♠",
			"[http://osu.ppy.sh/b/139851 Andrew W.K. - Ready To Die [DIE]]   95%: 64pp | 98%: 77pp | 99%: 85pp | 100%: 97pp | 2:53 ★ 3.91 ♫ 187.21 AR8 ♠",
			"[http://osu.ppy.sh/b/186881 milktub - BAKA Go Home (TV Size) [Easy]]   95%: 3pp | 98%: 3pp | 99%: 3pp | 100%: 5pp | 1:23 ★ 1.51 ♫ 158 AR3 ♠",
			"[http://osu.ppy.sh/b/188289 The Ting Tings - Keep Your Head [Easy]]   95%: 5pp | 98%: 7pp | 99%: 8pp | 100%: 10pp | 2:48 ★ 1.81 ♫ 154 AR3 ♠",
			"[http://osu.ppy.sh/b/223753 Foreground Eclipse - I Bet You'll Forget That Even If You Noticed That [Normal]]   95%: 9pp | 98%: 11pp | 99%: 13pp | 100%: 17pp | 3:52 ★ 2.23 ♫ 195 AR5 ♠",
			"[http://osu.ppy.sh/b/253159 Serj Tankian - Lie Lie Lie [Insane]]   95%: 34pp | 98%: 47pp | 99%: 55pp | 100%: 68pp | 2:52 ★ 3.18 ♫ 120 AR8 ♠",
			"[http://osu.ppy.sh/b/260006 The Pretty Reckless - Kill Me [Kill!!!]]   95%: 124pp | 98%: 147pp | 99%: 161pp | 100%: 182pp | 3:45 ★ 4.87 ♫ 178 AR9 ♠",
			"[http://osu.ppy.sh/b/289918 Taylor Swift - I Knew You Were Trouble [Yoeri's Normal]]   95%: 4pp | 98%: 5pp | 99%: 7pp | 100%: 11pp | 3:36 ★ 1.71 ♫ 154 AR5 ♠",
			"[http://osu.ppy.sh/b/296411 Circus-P - Lie [Lie]]   95%: 19pp | 98%: 29pp | 99%: 37pp | 100%: 49pp | 3:04 ★ 2.65 ♫ 112 AR8 ♠",
			"[http://osu.ppy.sh/b/300604 Drake Bell - You're Not Thinking [Easy]]   95%: 2pp | 98%: 3pp | 99%: 3pp | 100%: 5pp | 2:49 ★ 1.44 ♫ 172 AR3 ♠",
			"[http://osu.ppy.sh/b/304971 T-ara - Why Are You Being Like This [Easy]]   95%: 2pp | 98%: 3pp | 99%: 3pp | 100%: 5pp | 3:50 ★ 1.33 ♫ 188 AR3 ♠",
			"[http://osu.ppy.sh/b/321887 Kelly Clarkson - My Life Would Suck Without You [Easy]]   95%: 4pp | 98%: 4pp | 99%: 4pp | 100%: 5pp | 3:18 ★ 1.61 ♫ 145 AR3 ♠",
			"[http://osu.ppy.sh/b/333871 Foreground Eclipse - Calm Eyes Fixed On Me, Screaming [Easy]]   95%: 2pp | 98%: 3pp | 99%: 4pp | 100%: 7pp | 3:53 ★ 1.38 ♫ 184 AR3 ♠",
			"[http://osu.ppy.sh/b/422580 Pierce The Veil - May These Noises Startle You In Your Sleep Tonight [Insane]]   95%: 61pp | 98%: 70pp | 99%: 77pp | 100%: 90pp | 1:49 ★ 3.99 ♫ 130 AR8.5 ♠"
		};
		return fakes[rnd.nextInt(fakes.length)];
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		if (choices.contains("[nomod]")) {
			// recommendation parameter was off
			registerModification();
			/*
			 * we'll give three fake recommendations and then one proper error
			 * message. non-randomness required for unit test.
			 */
			if (invalidRecommendationParameterCount++ % 4 < 3) {
				return unknownRecommendationParameter();
			}
		}
		return getInvalidChoiceResponse(invalid, choices);
	}

	protected abstract String getInvalidChoiceResponse(String invalid, String choices);
}