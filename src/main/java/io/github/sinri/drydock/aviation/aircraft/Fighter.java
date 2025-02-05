package io.github.sinri.drydock.aviation.aircraft;

import io.github.sinri.drydock.aviation.carrier.AircraftCarrierDeck;
import io.github.sinri.drydock.common.HttpServerMixin;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since 1.5.0 Technical Preview
 * 和AircraftCarrierDeck配合使用的舰载战斗机类，用于应对来犯的外部请求。
 */
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

    @Override
    public Future<Void> loadHttpServer() {
        return HttpServerMixin.super.loadHttpServer();
    }
}
