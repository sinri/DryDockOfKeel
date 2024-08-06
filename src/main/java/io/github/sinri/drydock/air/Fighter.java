package io.github.sinri.drydock.air;

import io.github.sinri.drydock.common.HttpServerMixin;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogger;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since 1.5.0 Technical Preview
 */
@TechnicalPreview(since = "1.5.0")
public abstract class Fighter extends Biplane implements HttpServerMixin {

    private final int port;
    /**
     * @since 1.4.17
     */
    private final AtomicBoolean stopServerSwitch = new AtomicBoolean(false);

    public Fighter(@Nonnull AircraftCarrierDeck deck, int port) {
        super(deck);
        this.port = port;
    }

    @Override
    public int configuredHttpServerPort() {
        return port;
    }

    @Override
    public final void stopServer() {
        this.stopServerSwitch.set(true);
    }

    @Override
    public final boolean isToStopServer() {
        return stopServerSwitch.get();
    }

    @Override
    public KeelEventLogger getLogger() {
        return generateEventLogger(DryDockLogTopics.TopicHttpServer);
    }
}
