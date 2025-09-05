package io.github.sinri.drydock.naval.carrier;

import io.github.sinri.drydock.aviation.Bomber;
import io.github.sinri.drydock.aviation.Drone;
import io.github.sinri.drydock.aviation.Fighter;
import io.github.sinri.drydock.common.health.HealthMonitor;
import io.github.sinri.drydock.common.health.HealthMonitorMixin;
import io.github.sinri.drydock.common.health.HealthMonitorWithIssueRecorder;
import io.github.sinri.drydock.common.health.HealthMonitorWithMetricRecorder;
import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.AliyunSLSIssueAdapterImpl;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.AliyunSLSMetricRecorder;
import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.facade.cli.KeelCliOption;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An advanced implementation of AircraftCarrierDeck that provides a complete application framework.
 * <p>
 * This abstract class extends AircraftCarrierDeck with built-in support for:
 * <ul>
 *   <li>Health monitoring and metrics collection</li>
 *   <li>Queue-based task processing (via Drone)</li>
 *   <li>Scheduled task execution (via Bomber/Sundial)</li>
 *   <li>HTTP server capabilities (via Fighter)</li>
 * </ul>
 * <p>
 * Command line options:
 * {@code java -jar X.jar [--disableQueue] [--disableSundial] [--disableReceptionist] [--receptionistPort=8080]}
 * <p>
 * Subclasses must implement the abstract methods to provide specific component implementations.
 *
 * @since 1.5.0
 */
public abstract class AircraftCarrier extends AircraftCarrierDeck implements HealthMonitorMixin {
    /**
     * Command line option to disable queue functionality.
     */
    public static final String optionDisableQueue = "disableQueue";
    /**
     * Command line option to disable sundial functionality.
     */
    public static final String optionDisableSundial = "disableSundial";
    /**
     * Command line option to disable receptionist functionality.
     */
    public static final String optionDisableReceptionist = "disableReceptionist";
    /**
     * Command line option to specify receptionist port.
     */
    public static final String optionReceptionistPort = "receptionistPort";

    /**
     * The bomber component for scheduled task execution.
     */
    private Bomber bomber;
    /**
     * The drone component for queue-based task processing.
     */
    private Drone drone;
    /**
     * The fighter component for HTTP server capabilities.
     */
    private Fighter fighter;

    /**
     * Constructs the bomber component for scheduled task execution.
     * <p>
     * This method should return a configured Bomber instance that will handle
     * time-related scheduled tasks through the Sundial system.
     *
     * @return a configured Bomber instance, or null if bomber functionality is disabled
     */
    protected abstract Bomber constructBomber();

    /**
     * The component for the ability to execute time-relate scheduled tasks, through Sundial.
     *
     * @since 1.5.2
     */
    public Bomber getBomber() {
        return bomber;
    }

    /**
     * Constructs the drone component for queue-based task processing.
     * <p>
     * This method should return a configured Drone instance that will handle
     * queued tasks through the Queue system.
     *
     * @return a configured Drone instance, or null if queue functionality is disabled
     */
    protected abstract Drone constructDrone();

    /**
     * The component for the ability to execute queued tasks, through Queue.
     *
     * @since 1.5.2
     */
    public Drone getDrone() {
        return drone;
    }

    /**
     * Constructs the fighter component for HTTP server capabilities.
     * <p>
     * This method should return a configured Fighter instance that will provide
     * HTTP server functionality on the specified port.
     *
     * @param port the port number for the HTTP server, or null to use default
     * @return a configured Fighter instance, or null if receptionist functionality is disabled
     */
    protected abstract Fighter constructFighter(@Nullable Integer port);

    /**
     * The component for the ability to provide HTTP Service.
     *
     * @since 1.5.2
     */
    public Fighter getFighter() {
        return this.fighter;
    }

    @Nullable
    @Override
    protected List<KeelCliOption> buildCliOptions() {
        return List.of(
                new KeelCliOption()
                        .alias(optionDisableQueue)
                        .flag()
                        .description("Disable queue functionality"),
                new KeelCliOption()
                        .alias(optionDisableSundial)
                        .flag()
                        .description("Disable sundial functionality"),
                new KeelCliOption()
                        .alias(optionDisableReceptionist)
                        .flag()
                        .description("Disable receptionist functionality"),
                new KeelCliOption()
                        .alias(optionReceptionistPort)
                        .setValueValidator(s -> {
                            return Pattern.compile("^[1-9][0-9]+$")
                                          .matcher(s)
                                          .matches();
                        })
                        .description("Port for the receptionist")
        );
    }

    /**
     * Checks if queue functionality is disabled via command line option.
     *
     * @return true if queue is disabled, false otherwise
     * @since 1.5.2
     */
    protected boolean isQueueDisabled() {
        return getCliArgs().readFlag(optionDisableQueue);
    }

    /**
     * Checks if sundial functionality is disabled via command line option.
     *
     * @return true if sundial is disabled, false otherwise
     * @since 1.5.2
     */
    protected boolean isSundialDisabled() {
        return getCliArgs().readFlag(optionDisableSundial);
    }

    /**
     * Checks if receptionist functionality is disabled via command line option.
     *
     * @return true if receptionist is disabled, false otherwise
     * @since 1.5.2
     */
    protected boolean isReceptionistDisabled() {
        return getCliArgs().readFlag(optionDisableReceptionist);
    }


    /**
     * Builds the VertxOptions instance for the Vert.x runtime configuration.
     * <p>
     * This method should return a properly configured VertxOptions instance
     * that defines the behavior of the Vert.x runtime.
     *
     * @return the configured VertxOptions instance
     */
    @Nonnull
    protected abstract VertxOptions buildVertxOptions();

    /**
     * Launches the aircraft carrier as a warship with all configured components.
     * <p>
     * This method orchestrates the startup sequence:
     * <ol>
     *   <li>Loads health monitoring system</li>
     *   <li>Prepares for business logic</li>
     *   <li>Deploys queue system (if enabled)</li>
     *   <li>Deploys sundial system (if enabled)</li>
     *   <li>Deploys HTTP server (if enabled)</li>
     * </ol>
     *
     * @return a Future that completes when all components are successfully deployed
     */
    @Override
    protected Future<Void> launchAsWarship() {
        return loadHealthMonitor()
                .compose(v -> {
                    getUnitLogger().info("Loaded Health Monitor: " + v);
                    return prepare();
                })
                .compose(v -> {
                    getUnitLogger().info("Prepared For Biz");
                    boolean disableQueue = isQueueDisabled();
                    if (!disableQueue) {
                        drone = constructDrone();
                    }
                    if (drone != null) {
                        return drone.deployMe()
                                    .onSuccess(done -> {
                                        getUnitLogger().info("Loaded Queue");
                                    });
                    }
                    return Future.succeededFuture(null);
                })
                .compose(v -> {
                    boolean disableSundial = isSundialDisabled();
                    if (!disableSundial) {
                        bomber = constructBomber();
                    }
                    if (bomber != null) {
                        return bomber.deployMe()
                                     .onSuccess(done -> {
                                         getUnitLogger().info("Loaded Sundial");
                                     });
                    }
                    return Future.succeededFuture(null);
                })
                .compose(v -> {
                    boolean disableReceptionist = isReceptionistDisabled();
                    if (!disableReceptionist) {
                        String s = getCliArgs().readOption(optionReceptionistPort);
                        Integer receptionistPort = (s == null ? null : Integer.parseInt(s));
                        fighter = constructFighter(receptionistPort);
                        if (fighter != null) {
                            return fighter.deployMe()
                                          .onSuccess(done -> {
                                              getUnitLogger().info("Loaded Http Server on port: " + fighter.getHttpServerPort());
                                          });
                        }
                    }
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return Future.succeededFuture();
                });
    }

    /**
     * Loads and registers the JsonifiableSerializer for JSON serialization support.
     * <p>
     * This method registers the JsonifiableSerializer with the Keel framework,
     * enabling automatic JSON serialization for objects that implement Jsonifiable.
     *
     * @since 2.1.0
     */
    protected void loadJsonifiableSerializer() {
        JsonifiableSerializer.register();
    }

    /**
     * Builds the issue record center for logging and monitoring.
     * <p>
     * This method creates a KeelIssueRecordCenter that can send issue records
     * to Aliyun SLS (Simple Log Service) if properly configured, or falls back
     * to console output if SLS is disabled or unavailable.
     *
     * @return a configured KeelIssueRecordCenter instance
     */
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        AliyunSLSIssueAdapterImpl aliyunSLSIssueAdapter = new AliyunSLSIssueAdapterImpl();
        boolean disabled = aliyunSLSIssueAdapter.isDisabled();
        if (disabled) {
            return KeelIssueRecordCenter.outputCenter();
        } else {
            try {
                return KeelIssueRecordCenter.build(aliyunSLSIssueAdapter);
            } catch (Throwable e) {
                getUnitLogger().exception(e, "buildIssueRecordCenter error");
                throw e;
            }
        }
    }

    @Nullable
    @Override
    protected KeelMetricRecorder buildMetricRecorder() {
        return new AliyunSLSMetricRecorder();
    }

    /**
     * Builds the health monitor instance for this aircraft carrier.
     * <p>
     * This method creates a health monitor that can record both issues and metrics.
     * If a metric recorder is available, it uses HealthMonitorWithMetricRecorder;
     * otherwise, it falls back to HealthMonitorWithIssueRecorder.
     * <p>
     * To disable health monitoring, override this method to return null.
     *
     * @return a configured HealthMonitor instance, or null to disable health monitoring
     */
    @Override
    public HealthMonitor<?> buildHealthMonitor() {
        KeelMetricRecorder metricRecorder = getMetricRecorder();
        if (metricRecorder == null) {
            return new HealthMonitorWithIssueRecorder(generateIssueRecorder(HealthMonitorIssueRecord.TopicHealthMonitor, HealthMonitorIssueRecord::new));
        } else {
            return new HealthMonitorWithMetricRecorder(metricRecorder);
        }
    }

    /**
     * Prepares the aircraft carrier for business operations.
     * <p>
     * This method is called after the logger and health monitor are initialized,
     * but before the queue, sundial, and HTTP server components are deployed.
     * Subclasses should implement their initialization logic here.
     *
     * @return a Future that completes when preparation is finished
     */
    @Nonnull
    protected abstract Future<Void> prepare();

}
