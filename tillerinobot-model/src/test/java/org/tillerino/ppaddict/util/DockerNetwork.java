package org.tillerino.ppaddict.util;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;

public class DockerNetwork {
    public static final Network NETWORK = createReusableNetwork("ppaddict-test-containers");

    public static Network createReusableNetwork(String name) {
        String id = DockerClientFactory.instance().client().listNetworksCmd().exec().stream()
                .filter(network -> network.getName().equals(name)
                        && network.getLabels().equals(DockerClientFactory.DEFAULT_LABELS))
                .map(com.github.dockerjava.api.model.Network::getId)
                .findFirst()
                .orElseGet(() -> DockerClientFactory.instance()
                        .client()
                        .createNetworkCmd()
                        .withName(name)
                        .withCheckDuplicate(true)
                        .withLabels(DockerClientFactory.DEFAULT_LABELS)
                        .exec()
                        .getId());

        return new Network() {
            @Override
            public Statement apply(Statement base, Description description) {
                return base;
            }

            @Override
            public String getId() {
                return id;
            }

            @Override
            public void close() {
                // never close
            }
        };
    }
}
