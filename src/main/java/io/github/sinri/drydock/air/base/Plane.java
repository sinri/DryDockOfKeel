package io.github.sinri.drydock.air.base;

import io.github.sinri.keel.facade.launcher.KeelLauncherAdapter;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogToBeExtended;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.3.0 Technical Preview
 */
abstract class Plane extends KeelVerticleBase implements KeelLauncherAdapter, Flyable {
    private KeelEventLogCenter logCenter;

    public Plane() {
        logCenter = KeelOutputEventLogCenter.getInstance();
    }

    @Override
    public KeelEventLogCenter getLogCenter() {
        return logCenter;
    }

    protected void setLogCenter(KeelEventLogCenter logCenter) {
        this.logCenter = logCenter;
        getFlightLogger().info("io.github.sinri.drydock.air.base.Plane.setLogCenter: " + getLogCenter().getClass().getName());
    }

    @Override
    public final KeelEventLogger getFlightLogger() {
        return getLogger();
    }

    @Override
    public final KeelEventLogger logger() {
        return getLogger();
    }

    @Override
    public final KeelEventLogger generateLogger(@Nonnull String topic, @Nonnull Handler<KeelEventLogToBeExtended> eventLogHandler) {
        return getLogCenter().createLogger(topic, eventLogHandler);
    }

    /**
     * @since 1.3.4
     */
    @Override
    public <T extends KeelEventLog> KeelEventLogger generateLoggerForCertainEvent(@Nonnull String topic, @Nullable Supplier<T> baseLogBuilder) {
        return getLogCenter().createLogger(topic, baseLogBuilder);
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

    abstract protected void loadLocalConfiguration();

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

    protected KeelEventLogCenter buildLogCenter() {
        var x = KeelOutputEventLogCenter.getInstance();
        getFlightLogger().info("io.github.sinri.drydock.air.base.Plane.buildLogCenter: " + x);
        return x;
    }

    @Override
    public final void start() {
    }

    @Override
    abstract public void start(Promise<Void> startPromise);

    @Override
    final public void launch(String[] args) {
        this.launcher().dispatch(args);
    }
}
