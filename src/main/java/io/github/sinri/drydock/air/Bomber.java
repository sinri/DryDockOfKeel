package io.github.sinri.drydock.air;

import io.github.sinri.drydock.common.SundialMixin;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogger;

import javax.annotation.Nonnull;

/**
 * @since 1.5.0 Technical Preview
 */
@TechnicalPreview(since = "1.5.0")
public abstract class Bomber extends Biplane implements SundialMixin {
    public Bomber(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
    }

    @Override
    public KeelEventLogger getLogger() {
        return generateEventLogger(DryDockLogTopics.TopicSundial);
    }
}
