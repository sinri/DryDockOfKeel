package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.servant.queue.KeelQueue;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;


public abstract class Drone extends KeelQueue implements Biplane {
    private final AircraftCarrierDeck deck;

    public Drone(@Nonnull AircraftCarrierDeck deck) {
        super();
        this.deck = deck;
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

    /**
     * Try to build a KeelQueue instance and start it up. Do nothing if this ability is not required.
     *
     * @return a future as all work scheduled.
     */
    @Override
    public final Future<String> load() {
        return this.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
    }
}
