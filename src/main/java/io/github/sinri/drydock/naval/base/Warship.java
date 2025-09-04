package io.github.sinri.drydock.naval.base;

import io.github.sinri.drydock.common.Boat;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.facade.cli.KeelCliProgram;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.0
 */
abstract public class Warship extends KeelCliProgram implements Boat {
    public static final int EXIT_CODE_FOR_KEEL_INIT_FAILED = 1;
    public static final int EXIT_CODE_FOR_SELF_SINK = 0;
    private final KeelIssueRecorder<KeelEventLog> unitLogger;
    private KeelIssueRecordCenter issueRecordCenter;
    private KeelMetricRecorder metricRecorder;


    public Warship() {
        this.issueRecordCenter = KeelIssueRecordCenter.outputCenter();
        this.unitLogger = this.issueRecordCenter.generateIssueRecorder(
                DryDockLogTopics.TopicDryDock, KeelEventLog::new
        );
    }

    @Nullable
    abstract protected KeelIssueRecordCenter buildIssueRecordCenter();

    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
    }

    public final <T extends KeelIssueRecord<T>> KeelIssueRecorder<T> generateIssueRecorder(
            @Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder
    ) {
        return getIssueRecordCenter().generateIssueRecorder(topic, issueRecordBuilder);
    }

    @Nullable
    abstract protected KeelMetricRecorder buildMetricRecorder();

    @Nonnull
    abstract protected VertxOptions buildVertxOptions();

    @Nullable
    protected ClusterManager buildClusterManager() {
        return null;
    }

    @Override
    protected final void runWithCommandLine() {
        long startTime = System.currentTimeMillis();

        loadLocalConfiguration();
        this.getUnitLogger().info("LOCAL CONFIG LOADED (if any)");

        VertxOptions vertxOptions = buildVertxOptions();
        ClusterManager clusterManager = buildClusterManager();

        Future.succeededFuture()
              .compose(v -> {
                  if (clusterManager == null) {
                      // NOT SUPPORT CLUSTER MODE
                      return Keel.initializeVertx(vertxOptions);
                  } else {
                      return Keel.initializeVertx(vertxOptions, clusterManager);
                  }
              })
              .compose(initialized -> {
                  this.getUnitLogger().info("KEEL INITIALIZED");
                  return loadRemoteConfiguration();
              })
              .compose(done -> {
                  this.getUnitLogger().info("REMOTE CONFIG LOADED (if any)");

                  // customized logging
                  var builtIssueRecordCenter = buildIssueRecordCenter();
                  if (builtIssueRecordCenter != null && builtIssueRecordCenter != issueRecordCenter) {
                      issueRecordCenter = builtIssueRecordCenter;
                      this.unitLogger.addBypassIssueRecorder(builtIssueRecordCenter.generateIssueRecorder(
                              DryDockLogTopics.TopicDryDock, KeelEventLog::new
                      ));
                      Keel.setIssueRecordCenter(issueRecordCenter);
                  }

                  // metric recording
                  this.metricRecorder = buildMetricRecorder();
                  if (this.metricRecorder != null) {
                      this.metricRecorder.start();
                  }

                  return launchAsWarship();
              })
              .onSuccess(done -> {
                  whenLaunched(startTime);
              })
              .onFailure(this::handleError);
    }

    protected void whenLaunched(long startTime) {
        long endTime = System.currentTimeMillis();
        this.getUnitLogger().notice("Warship launched, spent " + (endTime - startTime) + " ms");
    }

    abstract protected void loadLocalConfiguration();

    abstract protected Future<Void> loadRemoteConfiguration();

    abstract protected Future<Void> launchAsWarship();

    @Override
    public void handleError(Throwable throwable) {
        this.getUnitLogger().exception(throwable, "Failed to launch, shipwreck");
        System.exit(EXIT_CODE_FOR_KEEL_INIT_FAILED);
    }

    @Override
    public void finish() {
        this.getUnitLogger().fatal("SINK");
        System.exit(EXIT_CODE_FOR_SELF_SINK);
    }

    public final KeelIssueRecorder<KeelEventLog> getUnitLogger() {
        return unitLogger;
    }

    @Nullable
    @Override
    public final KeelMetricRecorder getMetricRecorder() {
        return metricRecorder;
    }
}
