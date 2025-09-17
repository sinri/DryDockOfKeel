package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.common.health.ObservationBalloonDelegate;
import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.helper.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;

/**
 * 替代原有的HealthMonitor体系的实现。
 *
 * @since 3.0.1
 */
public class ObservationBalloon extends KeelVerticleImpl implements Biplane {
    private final AircraftCarrierDeck deck;
    private final ObservationBalloonDelegate delegate;

    public ObservationBalloon(@Nonnull AircraftCarrierDeck deck, @Nonnull ObservationBalloonDelegate delegate) {
        this.deck = deck;
        this.delegate = delegate;
    }

    @Nonnull
    @Override
    public final AircraftCarrierDeck getAircraftCarrierDeck() {
        return deck;
    }

    @Override
    public Future<String> deployMe() {
        return deployMe(new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER));
    }

    @Override
    protected Future<Void> startVerticle() {
        new KeelRuntimeMonitor().startRuntimeMonitor(delegate.getInterval(), delegate::recordMonitorSnapshot);
        return Future.succeededFuture();
    }
}
