package org.tillerino.ppaddict;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.*;
import org.slf4j.LoggerFactory;

public class AbstractPlaywrightTest extends AbstractServingPpaddictTest {
    @RegisterExtension
    Lifecycle lifecycle = new Lifecycle();

    static Playwright playwright;
    static Browser browser;

    protected Page page;

    @BeforeAll
    static void launchPlaywright() {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch();
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(
                            () -> {
                                browser.close();
                                playwright.close();
                            },
                            "Playwright Cleanup"));
        }
    }

    protected void assertScreenshotMatches(String image) throws IOException {
        byte[] actualScreenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));

        Path expectedPath = Paths.get("src/test/resources/baselines/" + image);

        // First run: create baseline
        if (!Files.exists(expectedPath)) {
            Files.createDirectories(expectedPath.getParent());
            Files.write(expectedPath, actualScreenshot);
            throw new AssertionError("✓ Baseline created: " + image + "\nRun the test again to compare.");
        }

        BufferedImage expectedImage = ImageIO.read(expectedPath.toFile());
        BufferedImage actualImage = ImageIO.read(new ByteArrayInputStream(actualScreenshot));

        assertThat(actualImage.getWidth())
                .as("Width mismatch - page layout may have changed")
                .isEqualTo(expectedImage.getWidth());
        assertThat(actualImage.getHeight())
                .as("Height mismatch - page content may have changed")
                .isEqualTo(expectedImage.getHeight());

        long diffPixels = 0;
        for (int y = 0; y < expectedImage.getHeight(); y++) {
            for (int x = 0; x < expectedImage.getWidth(); x++) {
                if (expectedImage.getRGB(x, y) != actualImage.getRGB(x, y)) {
                    diffPixels++;
                }
            }
        }

        double diffPercent = (diffPixels * 100.0) / (expectedImage.getWidth() * expectedImage.getHeight());
        if (diffPercent > 0.1) {
            Path actualPath = getFreeActualScreenshotPath(image);
            Files.createDirectories(actualPath.getParent());
            Files.write(actualPath, actualScreenshot);
            assertThat(diffPercent)
                    .as(
                            "❌ Visual difference between%nExpected: %s%nActual:   %s",
                            expectedPath.toAbsolutePath(), actualPath.toAbsolutePath())
                    .isLessThan(0.1);
        }
    }

    private static Path getFreeActualScreenshotPath(String image) {
        int dotIndex = image.lastIndexOf('.');
        String base = (dotIndex >= 0) ? image.substring(0, dotIndex) : image;
        String ext = (dotIndex >= 0) ? image.substring(dotIndex) : "";
        int counter = 1;
        Path candidate;
        do {
            candidate = Paths.get("target/snapshots/actual", base + "-" + counter + ext);
            counter++;
        } while (Files.exists(candidate));
        return candidate;
    }

    public class Lifecycle implements BeforeEachCallback, AfterEachCallback {
        @Override
        public void beforeEach(ExtensionContext context) {
            page = browser.newPage();
        }

        @Override
        public void afterEach(ExtensionContext context) {
            String testName = context.getTestMethod().get().getName();
            Path path = getFreeActualScreenshotPath(testName + ".png");

            try {
                page.screenshot(new Page.ScreenshotOptions().setPath(path));
            } finally {
                if (page != null) {
                    page.close();
                }
            }

            LoggerFactory.getLogger(context.getTestClass().get()).warn("Saved screenshot of failed test at {}", path);
        }
    }
}
