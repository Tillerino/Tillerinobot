package org.tillerino.ppaddict.live;

import static org.tillerino.ppaddict.util.DockerNetwork.NETWORK;

import java.io.File;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.tillerino.ppaddict.rabbit.RabbitMqContainer;
import org.tillerino.ppaddict.util.CustomTestContainer;

public class LiveContainer {
    private static final ImageFromDockerfile base = new ImageFromDockerfile()
            .withFileFromFile("Dockerfile", new File("../../Tillerinobot/tillerinobot-live/Dockerfile"))
            .withFileFromFile("Cargo.toml", new File("../../Tillerinobot/tillerinobot-live/Cargo.toml"))
            .withFileFromFile("Cargo.lock", new File("../../Tillerinobot/tillerinobot-live/Cargo.lock"))
            .withFileFromFile("src/main/rust", new File("../Tillerinobot/../tillerinobot-live/src/main/rust"));

    public static final CustomTestContainer LIVE = new CustomTestContainer(
                    new File("../../Tillerinobot" + "/tillerinobot-live/target/release/main").exists()
                            ? base.withFileFromFile(
                                    "target/release/main",
                                    new File("../../Tillerinobot/tillerinobot-live/target/release/main"))
                            : base)
            // accesing this static variable will make sure that RabbitMQ is started
            .withEnv("RABBIT_VHOST", RabbitMqContainer.getVirtualHost())
            .withNetwork(NETWORK)
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/ready").forStatusCode(200))
            .withCreateContainerCmdModifier(
                    cmd -> cmd.withMemory(16 * 1024 * 1024L).withMemorySwap(16 * 1024 * 1024L))
            .logging("LIVE");

    static {
        LIVE.start();
    }

    public static CustomTestContainer getLive() {
        return LIVE;
    }
}
