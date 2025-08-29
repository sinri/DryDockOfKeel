package io.github.sinri.drydock.aviation.aircraft;

import io.github.sinri.drydock.aviation.carrier.AircraftCarrierDeck;
import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * 和AircraftCarrierDeck配合使用的舰载机基类。
 *
 * @since 1.5.0
 */
public abstract class Biplane implements CommonUnit {
    private final @Nonnull AircraftCarrierDeck deck;

    public Biplane(@Nonnull AircraftCarrierDeck deck) {
        this.deck = deck;
    }

    /**
     * @since 1.3.4
     */
    @Override
    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return deck.getIssueRecordCenter();
    }

    protected KeelIssueRecorder<KeelEventLog> getUnitLogger() {
        return deck.getUnitLogger();
    }

    /**
     * Attempts to load and initialize this class asynchronously.
     * All the configurations should be set before this method is called.
     *
     * @return a future for the deployment id when the deployment is done; otherwise, a failed future.
     * @since 2.1.1
     */
    abstract public Future<String> load();
}
