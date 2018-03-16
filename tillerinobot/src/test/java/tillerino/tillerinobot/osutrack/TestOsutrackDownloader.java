package tillerino.tillerinobot.osutrack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TestOsutrackDownloader extends OsutrackDownloader {
    @Override
    public UpdateResult getUpdate(String username) throws IOException {
        InputStream inputStream = TestOsutrackDownloader.class.getResourceAsStream("/osutrack/" + username.replace(' ', '_') + ".json");
        String json = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.joining("\n"));
        return parseJson(json);
    }

    public static void main(String[] args) {
        System.out.println("provide a name to get osutrack results from:");
        try (Scanner scanner = new Scanner(System.in)) {
            final String username = scanner.nextLine();
            OsutrackDownloader downloader = new TestOsutrackDownloader();

            System.out.println("Result:");
            System.out.println(downloader.getUpdate(username));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
