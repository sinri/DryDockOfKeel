package io.github.sinri.drydock.air.base;

import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.verticles.KeelVerticleImplWithEventLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.3.0 Technical Preview
 */
abstract class Plane extends KeelVerticleImplWithEventLogger implements Flyable {
    //    private final KeelEventLogger unitLogger;
    private KeelIssueRecordCenter issueRecordCenter;

    public Plane() {
        issueRecordCenter = KeelIssueRecordCenter.outputCenter();
//        unitLogger = issueRecordCenter.generateEventLogger(DryDockLogTopics.TopicDryDock);
    }

    @Nonnull
    @Override
    public final KeelEventLogger buildEventLoggerForLauncher() {
        return KeelIssueRecordCenter.outputCenter().generateEventLogger(DryDockLogTopics.TopicDryDock);
    }

    @Override
    public final KeelEventLogger getUnitLogger() {
        return getLogger();
    }

    @Nonnull
    @Override
    public final KeelEventLogger getLogger() {
        return super.getLogger();
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        getUnitLogger().info("afterStartingVertx!");
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
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        getUnitLogger().exception(cause, "handleDeployFailed");
        vertx.close();
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

    /**
     * @since 1.3.4
     */
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        KeelIssueRecordCenter x = KeelIssueRecordCenter.outputCenter();
        getUnitLogger().info("io.github.sinri.drydock.air.base.Plane.buildIssueRecordCenter: " + x);
        return x;
    }

    /**
     * Do not override only if you know what are you about to do.
     */
    @Nullable
    @Override
    public String getDefaultCommand() {
        return "run";
    }

    /**
     * @since 1.3.4
     */
    @Override
    public KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
    }

    /**
     * @since 1.3.4
     */
    public void setIssueRecordCenter(@Nonnull KeelIssueRecordCenter issueRecordCenter) {
        this.issueRecordCenter = issueRecordCenter;
        getUnitLogger().info("io.github.sinri.drydock.air.base.Plane.setIssueRecordCenter: " + getIssueRecordCenter().getClass().getName());
    }

    /**
     * @since 1.3.4
     */
    @Override
    public final <T extends KeelIssueRecord<?>> KeelIssueRecorder<T> generateIssueRecorder(
            @Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder
    ) {
        return getIssueRecordCenter().generateIssueRecorder(topic, issueRecordBuilder);
    }

    @Override
    protected KeelEventLogger buildEventLogger() {
        return getUnitLogger();
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
