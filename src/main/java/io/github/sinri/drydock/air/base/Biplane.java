package io.github.sinri.drydock.air.base;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.3.0 Technical Preview
 */
@TechnicalPreview(since = "1.3.0")
public abstract class Biplane extends Plane {
    private KeelEventLogCenter logCenter;

    public Biplane() {
        logCenter = KeelOutputEventLogCenter.getInstance();
        getFlightLogger().info("Biplane logCenter built: " + logCenter);
        KeelEventLogger logger = logCenter.createLogger("Biplane");
        setLogger(logger);
    }

    @Override
    protected void loadLocalConfiguration() {
        // load the local config file
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    abstract protected Future<Void> loadRemoteConfiguration();

    @Override
    protected KeelEventLogCenter buildLogCenter() {
        return KeelOutputEventLogCenter.getInstance();
    }

    @Override
    public final KeelEventLogCenter getLogCenter() {
        return Objects.requireNonNull(logCenter, "log center of Biplane is not set yet.");
    }

    abstract protected Future<Void> flyAsBiplane();

    @Nonnull
    abstract protected Future<Void> prepareDataSources();

    @Override
    public final void start(Promise<Void> startPromise) {
        // now local config has been loaded
        Future.succeededFuture()
                .compose(v -> {
                    return loadRemoteConfiguration();
                })
                .compose(remoteConfigurationLoaded -> {
                    logCenter = buildLogCenter();
                    getFlightLogger().info("Biplane logCenter built: " + logCenter);
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return prepareDataSources();
                })
                .compose(v -> {
                    return flyAsBiplane();
                })
                .andThen(ar -> {
                    if (ar.failed()) {
                        getFlightLogger().exception(ar.cause(), "Failed to start flying");
                        startPromise.fail(ar.cause());
                    } else {
                        startPromise.complete();
                    }
                });
    }
}
