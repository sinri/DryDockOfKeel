package io.github.sinri.drydock.air.base;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.launcher.KeelLauncherAdapter;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.3.0 Technical Preview
 */
@TechnicalPreview(since = "1.3.0")
public abstract class Biplane extends KeelVerticleBase implements KeelLauncherAdapter, Flyable {
    private KeelEventLogCenter logCenter;

    public Biplane() {
        KeelEventLogger logger = KeelOutputEventLogCenter.getInstance().createLogger("Biplane");
        setLogger(logger);
    }

    @Override
    public final KeelEventLogger getFlightLogger() {
        return getLogger();
    }

    @Override
    public final KeelEventLogCenter getLogCenter() {
        return logCenter;
    }

    @Override
    public final KeelEventLogger generateLogger(String topic, Handler<KeelEventLog> eventLogHandler) {
        return logCenter.createLogger(topic, eventLogHandler);
    }

    @Override
    public final KeelEventLogger logger() {
        return getLogger();
    }

    @Override
    public void beforeStoppingVertx() {

    }

    @Override
    public final void afterConfigParsed(JsonObject jsonObject) {
        Keel.getConfiguration().reloadDataFromJsonObject(jsonObject);
        loadLocalConfiguration();
        jsonObject.mergeIn(Keel.getConfiguration().toJsonObject());
    }

    protected void loadLocalConfiguration() {
        // load the local config file
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    @Override
    final public void beforeStartingVertx(VertxOptions vertxOptions) {
        modifyVertxOptions(vertxOptions);
    }

    abstract protected void modifyVertxOptions(VertxOptions vertxOptions);

    @Override
    public void afterStartingVertx(Vertx vertx) {
        logger().info("afterStartingVertx!");
    }

    @Override
    final public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        // override to modify vertx options
        this.modifyDeploymentOptions(deploymentOptions);
    }

    protected void modifyDeploymentOptions(DeploymentOptions deploymentOptions) {

    }

    @Override
    public void beforeStoppingVertx(Vertx vertx) {

    }

    @Override
    public void afterStoppingVertx() {

    }

    @Override
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        logger().exception(cause, "handleDeployFailed");
        vertx.close();
    }

    /**
     * Do not override only if you know what are you about to do.
     */
    @Nullable
    @Override
    public String getDefaultCommand() {
        return "run";
    }

    @Override
    final public void launch(String[] args) {
        this.launcher().dispatch(args);
    }

    @Override
    public final void start() throws Exception {
        super.start();
    }

    abstract protected Future<Void> loadRemoteConfiguration();

    protected KeelEventLogCenter buildLogCenter() {
        return KeelOutputEventLogCenter.getInstance();
    }

    abstract protected Future<Void> flyAsBiplane();

    @Nonnull
    abstract protected Future<Void> prepareDataSources();

    @Override
    public final void start(Promise<Void> startPromise) throws Exception {
        start();

        // now local config has been loaded
        Future.succeededFuture()
                .compose(v -> {
                    return loadRemoteConfiguration();
                })
                .compose(remoteConfigurationLoaded -> {
                    logCenter = buildLogCenter();
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
