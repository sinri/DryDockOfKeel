package io.github.sinri.drydock.naval.base;

import io.github.sinri.drydock.common.Boat;
import io.github.sinri.keel.facade.cli.KeelCliProgram;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Base class for naval warships that extends KeelCliProgram and implements Boat interface.
 * <p>Provides common functionality for warship initialization, configuration loading, and lifecycle management.
 * <p>Supports both local and remote configuration loading, issue recording, and metric collection.
 *
 * @since 3.0.0
 */
abstract public class Warship extends KeelCliProgram implements Boat {
    /**
     * Exit code when Keel initialization fails.
     */
    public static final int EXIT_CODE_FOR_KEEL_INIT_FAILED = 1;
    /**
     * Exit code when warship intentionally sinks.
     */
    public static final int EXIT_CODE_FOR_SELF_SINK = 0;
    private final KeelIssueRecorder<KeelEventLog> unitLogger;
    private KeelIssueRecordCenter issueRecordCenter;
    private KeelMetricRecorder metricRecorder;


    /**
     * Constructs a new Warship with default issue record center and logger.
     */
    public Warship() {
        this.issueRecordCenter = KeelIssueRecordCenter.outputCenter();
        this.unitLogger = this.issueRecordCenter.generateIssueRecorder(
                getClass().getSimpleName(), KeelEventLog::new
        );
    }

    /**
     * Builds a custom issue record center for logging.
     * <p>
     * To provide custom logging configuration, a non-null instance should be returned.
     *
     * @return custom issue record center, or null to use default
     */
    @Nullable
    abstract protected KeelIssueRecordCenter buildIssueRecordCenter();

    /**
     * Gets the current issue record center.
     *
     * @return the issue record center
     */
    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
    }

    /**
     * Generates an issue recorder for the specified topic.
     *
     * @param topic              the logging topic
     * @param issueRecordBuilder supplier for creating issue records
     * @param <T>                the type of issue record
     * @return the generated issue recorder
     */
    public final <T extends KeelIssueRecord<T>> KeelIssueRecorder<T> generateIssueRecorder(
            @Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder
    ) {
        return getIssueRecordCenter().generateIssueRecorder(topic, issueRecordBuilder);
    }

    /**
     * Builds custom metric recorder for monitoring.
     * <p>Override to provide custom metric collection.
     *
     * @return custom metric recorder, or null to disable metrics
     */
    @Nullable
    protected KeelMetricRecorder buildMetricRecorder() {
        return null;
    }

    /**
     * Builds Vert.x options for the warship.
     * <p>Override to customize Vert.x configuration.
     *
     * @return Vert.x options configuration
     */
    @Nonnull
    abstract protected VertxOptions buildVertxOptions();

    /**
     * Builds cluster manager for distributed mode.
     * <p>Override to enable clustering support.
     *
     * @return cluster manager, or null for single-node mode
     */
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
                              this.unitLogger.topic(), () -> {
                                  KeelEventLog keelEventLog = new KeelEventLog();
                                  Handler<KeelEventLog> recordFormatter = unitLogger.getRecordFormatter();
                                  if (recordFormatter != null) {
                                      recordFormatter.handle(keelEventLog);
                                  }
                                  return keelEventLog;
                              }
                      ));
                      Keel.getLogger().addBypassIssueRecorder(builtIssueRecordCenter.generateIssueRecorder(
                              Keel.getLogger().topic(), () -> {
                                  KeelEventLog keelEventLog = new KeelEventLog();
                                  Handler<KeelEventLog> recordFormatter = Keel.getLogger().getRecordFormatter();
                                  if (recordFormatter != null) {
                                      recordFormatter.handle(keelEventLog);
                                  }
                                  return keelEventLog;
                              }
                      ));
                      getUnitLogger().info("CUSTOM ISSUE RECORD CENTER LOADED");
                  }

                  // metric recording
                  this.metricRecorder = buildMetricRecorder();
                  if (this.metricRecorder != null) {
                      this.metricRecorder.start();
                      getUnitLogger().info("CUSTOM METRIC RECORDER LOADED");
                  }
                  return Future.succeededFuture();
              })
              .compose(v -> {
                  return launchAsWarship();
              })
              .onSuccess(done -> {
                  whenLaunched(startTime);
              })
              .onFailure(this::handleError);
        affix();
    }

    /**
     * Called when warship successfully launches.
     * <p>Override to add custom post-launch logic.
     *
     * @param startTime the launch start time in milliseconds
     */
    protected void whenLaunched(long startTime) {
        long endTime = System.currentTimeMillis();
        this.getUnitLogger().notice("Warship launched, spent " + (endTime - startTime) + " ms");
    }

    /**
     * Loads the local configuration synchronously into the Keel configuration system.
     * <p>
     * By default, this method reads the "config.properties" file from the classpath
     * and loads it into the global Keel configuration.
     */
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    /**
     * Loads remote configuration from external sources if needed.
     * <p>
     * By default, it is passed over.
     * Override to implement custom remote configuration loading.
     *
     * @return future that completes when configuration is loaded
     */
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }

    /**
     * Launches the warship with all configurations loaded.
     * <p>Override to implement the main warship functionality.
     *
     * @return future that completes when warship is ready
     */
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

    /**
     * Gets the unit logger for this warship.
     *
     * @return the unit logger
     */
    public final KeelIssueRecorder<KeelEventLog> getUnitLogger() {
        return unitLogger;
    }

    /**
     * Gets the metric recorder for this warship.
     *
     * @return the metric recorder, or null if not configured
     */
    @Nullable
    @Override
    public final KeelMetricRecorder getMetricRecorder() {
        return metricRecorder;
    }

    /**
     * @since 3.0.2
     */
    protected void affix() {
        // do nothing by default, or you may need a latch to keep the main process alive.
    }
}
