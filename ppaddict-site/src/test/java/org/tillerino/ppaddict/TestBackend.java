package org.tillerino.ppaddict;

import static org.tillerino.osuApiModel.Mods.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.types.OsuName;
import org.tillerino.ppaddict.server.PersistentUserData;
import org.tillerino.ppaddict.server.PpaddictBackend;
import org.tillerino.ppaddict.server.auth.Credentials;
import org.tillerino.ppaddict.shared.types.PpaddictId;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.RecommendationsManager.GivenRecommendation;
import tillerino.tillerinobot.UserDataManager.UserData.BeatmapWithMods;
import tillerino.tillerinobot.UserException;
import tillerino.tillerinobot.diff.PercentageEstimates;
import tillerino.tillerinobot.lang.Default;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Backend test implementation. Not thread safe or anything. Serializes database to ppaddict-db.json
 * file in working directory on every write for easy debugging. Delete the file and restart the app
 * to reset the database.
 * 
 * @author Tillerino
 */
@Singleton
public class TestBackend implements PpaddictBackend {
  private final tillerino.tillerinobot.TestBackend botBackend;

  Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

  @Inject
  public TestBackend(tillerino.tillerinobot.TestBackend backend) {
    this.botBackend = backend;

    try (Reader reader =
        new InputStreamReader(new BufferedInputStream(new FileInputStream("ppaddict-db.json")))) {
      database = gson.fromJson(reader, Database.class);
    } catch (IOException e) {
      // that's okay
    }
  }

  void writeDatabase() {
    File file = new File("ppaddict-db.json");
    System.out.println(file.getAbsolutePath());
    try (Writer writer =
        new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)))) {
      gson.toJson(database, writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static class Database {
    public Map<String, Credentials> cookies = new HashMap<>();

    public Map<String, PersistentUserData> userData = new HashMap<>();

    public Map<String, String> forward = new HashMap<>();
  }

  Database database = new Database();

  @Override
  public PersistentUserData loadUserData(Credentials userIdentifier) {
    String string = userIdentifier.identifier;

    while (database.forward.containsKey(string)) {
      string = database.forward.get(string);
    }
    return database.userData.get(string);
  }

  @Override
  public void saveUserData(Credentials userIdentifier, PersistentUserData data) {
    String string = userIdentifier.identifier;

    while (database.forward.containsKey(string)) {
      string = database.forward.get(string);
    }
    database.userData.put(string, data);
    writeDatabase();
  }

  @Override
  public Credentials resolveCookie(String cookie) {
    return database.cookies.get(cookie);
  }

  @Override
  public String createCookie(Credentials userIdentifier) {
    String cookie = database.cookies.size() + "-cohkay";
    database.cookies.put(cookie, userIdentifier);
    writeDatabase();
    return cookie;
  }

  @Override
  public String getLinkString(@PpaddictId final String ppaddictId, String displayName) {
    new Thread() {
      @Override
      public void run() {
        String osuName = new Scanner(System.in).nextLine();
        try {
          link(ppaddictId, osuName);
        } catch (SQLException | IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    System.out.print("Enter your osu name and press enter: ");
    return "Hi, this is TestBackend. Enter your osu name into the console to link it!";
  }


  public void link(@PpaddictId String loginId, @OsuName String osuName) throws SQLException,
      IOException {
    System.out.println("linking " + loginId + " to " + osuName);

    botBackend.hintUser(osuName, false, 100, 1000);

    int osuId = botBackend.resolveIRCName(osuName);
    @PpaddictId
    String forwardedId = "osu:" + osuId;

    database.forward.put(loginId, forwardedId);
    PersistentUserData remove = database.userData.remove(loginId);
    if (!database.userData.containsKey(forwardedId)) {
      if (remove == null) {
        remove = new PersistentUserData();
      }
      remove.setLinkedOsuId(osuId);
      database.userData.put(forwardedId, remove);
    }
    writeDatabase();
  }

  @Override
  public Map<BeatmapWithMods, BeatmapData> getBeatmaps() {
    HashMap<BeatmapWithMods, BeatmapData> ret = new HashMap<>();

    for (Integer id : botBackend.getSetIds().keySet()) {
      for (long mods : new long[] {0, getMask(Hidden, HardRock), getMask(DoubleTime)}) {
        try {
          final BeatmapMeta meta = botBackend.loadBeatmap(id, mods, new Default());
          ret.put(new BeatmapWithMods(id, mods), new BeatmapData() {
            @Override
            public PercentageEstimates getEstimates() {
              return meta.getEstimates();
            }

            @Override
            public OsuApiBeatmap getBeatmap() {
              return meta.getBeatmap();
            }
          });
        } catch (SQLException | IOException | UserException e) {
          throw new RuntimeException(e);
        }
      }
    }

    return ret;
  }

  @Override
  public void hideRecommendation(int userId, int beatmapid, long mods) {
    botBackend.hideRecommendation(userId, beatmapid, mods);
  }

  @Override
  public List<GivenRecommendation> loadVisibleRecommendations(int userId) throws SQLException {
    return botBackend.loadVisibleRecommendations(userId);
  }
}
