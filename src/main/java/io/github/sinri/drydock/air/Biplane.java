package io.github.sinri.drydock.air;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;

import javax.annotation.Nonnull;

/**
 * @since 1.5.0 Technical Preview
 */
@TechnicalPreview(since = "1.5.0")
public abstract class Biplane implements CommonUnit {
    private final @Nonnull AircraftCarrierDeck deck;

    public Biplane(@Nonnull AircraftCarrierDeck deck) {
        this.deck = deck;
    }

    @Override
    public KeelEventLogger getLogger() {
        return deck.getLogger();
    }

    /**
     * @since 1.3.4
     */
    @Override
    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return deck.getIssueRecordCenter();
    }
}
