package tillerino.tillerinobot.osutrack;

import java.io.IOException;
import java.util.Scanner;

public class OsutrackTest {
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
