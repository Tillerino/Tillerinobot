package org.tillerino.ppaddict.shared;

import java.io.Serializable;

import javax.annotation.CheckForNull;

public class Beatmap implements Serializable {

  private static final long serialVersionUID = 1L;
  public String title;
  public String artist;
  public String version;

  public int beatmapid;
  public int setid;
  public double approachRate;
  public double circleSize;
  public double overallDiff;
  public double bpm;
  public double length;

  public double lowPP;
  public double highPP;

  @CheckForNull
  public Double starDifficulty;

  @CheckForNull
  public String mods;

  /**
   * null if no personalization
   */
  @CheckForNull
  public Personalization personalization;

  public Beatmap() {}

  public static Beatmap empty(int id) {
    Beatmap b = new Beatmap();
    b.beatmapid = id;
    b.setid = id;
    return b;
  }

  public Beatmap withPersonalization(Personalization p) {
    Beatmap b = new Beatmap();

    b.title = title;
    b.artist = artist;
    b.version = version;

    b.beatmapid = beatmapid;
    b.setid = setid;
    b.approachRate = approachRate;
    b.circleSize = circleSize;
    b.bpm = bpm;
    b.length = length;
    b.highPP = highPP;

    b.starDifficulty = starDifficulty;
    b.lowPP = lowPP;

    b.personalization = p;

    return b;
  }

  public static class Personalization implements Serializable {
    private static final long serialVersionUID = 1L;
    public String comment;
    public String commentDate;

  }

  public String getFormattedLength() {
    return secondsToMinuteColonSecond((int) length);
  }

  public static String secondsToMinuteColonSecond(int length) {
    return length / 60 + ":" + leftPad(String.valueOf(length % 60));
  }

  public void setFormattedLength(String s) {
    length = minuteColonSecondToSeconds(s);
  }

  public static int minuteColonSecondToSeconds(String s) {
    return Integer.parseInt(s.substring(0, s.indexOf(':'))) * 60
        + Integer.parseInt(s.substring(s.indexOf(':') + 1));
  }

  private static String leftPad(String s) {
    if (s.length() == 1) {
      return "0" + s;
    }
    return s;
  }
}
