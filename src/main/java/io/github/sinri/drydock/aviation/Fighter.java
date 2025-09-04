package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public abstract class Fighter extends KeelHttpServer implements Biplane {
    private static final int DEFAULT_PORT = 8080;
    private final AircraftCarrierDeck deck;
    private final int port;

    public Fighter(@Nonnull AircraftCarrierDeck deck, @Nullable Integer port) {
        super();
        this.deck = deck;
        this.port = Objects.requireNonNullElse(port, DEFAULT_PORT);
    }

    @Nonnull
    @Override
    public final AircraftCarrierDeck getAircraftCarrierDeck() {
        return deck;
    }

    @Override
    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return getAircraftCarrierDeck().getIssueRecordCenter();
    }

    @Override
    public final int getHttpServerPort() {
        return port;
    }

    /**
     * Deploys the current server instance based on the available threading model.
     * If virtual threads are supported, the server is deployed with virtual threading enabled.
     *
     * @return a future that resolves to a deployment identifier string upon successful deployment.
     */
    @Override
    public final Future<String> load() {
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        if (Keel.reflectionHelper().isVirtualThreadsAvailable()) {
            deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
        }
        return this.deployMe(deploymentOptions);
    }

}
