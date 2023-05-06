package io.github.sinri.drydock.naval.galley;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;

/**
 * 桨帆船。
 * Absolutely customized VertxOptions to initialize, standalone or clustered.
 * Pure STDOUT logger.
 * Fundamental Keel Initialization.
 *
 * @since 1.0.0
 */
public abstract class Galley {
    private KeelEventLogCenter logCenter;
    public static final int EXIT_CODE_FOR_KEEL_INIT_FAILED = 1;

    public Galley() {

    }

    protected KeelEventLogCenter buildLogCenter() {
        return KeelOutputEventLogCenter.getInstance();
    }

    public KeelEventLogCenter getLogCenter() {
        return logCenter;
    }

    public VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    public final void launch() {
        VertxOptions vertxOptions = buildVertxOptions();
        Keel.initializeVertx(vertxOptions)
                .onSuccess(done -> {
                    logCenter = buildLogCenter();
                    launchAsGalley();
                })
                .onFailure(this::shipwreck);
    }

    abstract protected void launchAsGalley();

    protected void shipwreck(Throwable throwable) {
        KeelOutputEventLogCenter.instantLogger().exception(throwable, "Keel Initialized");
        System.exit(EXIT_CODE_FOR_KEEL_INIT_FAILED);
    }

    public KeelEventLogger generateLogger(String topic, Handler<KeelEventLog> eventLogHandler) {
        return KeelOutputEventLogCenter.getInstance().createLogger(topic, eventLogHandler);
    }
}
