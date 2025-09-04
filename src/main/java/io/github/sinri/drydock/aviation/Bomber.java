package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.servant.sundial.KeelSundial;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;


public abstract class Bomber extends KeelSundial implements Biplane {
    private final AircraftCarrierDeck deck;

    public Bomber(@Nonnull AircraftCarrierDeck deck) {
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

    @Override
    public final Future<String> load() {
        return this.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
    }
}
