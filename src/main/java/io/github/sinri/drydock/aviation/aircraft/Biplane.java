package io.github.sinri.drydock.aviation.aircraft;

import io.github.sinri.drydock.aviation.carrier.AircraftCarrierDeck;
import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

import javax.annotation.Nonnull;

/**
 * @since 1.5.0 Technical Preview 和AircraftCarrierDeck配合使用的舰载机基类。
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

    /**
     * @since 2.0.3
     */
    @Override
    public KeelIssueRecorder<KeelEventLog> getUnitLogger() {
        return deck.getUnitLogger();
    }
}
