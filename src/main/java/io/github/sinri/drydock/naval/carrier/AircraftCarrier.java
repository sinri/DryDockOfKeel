package io.github.sinri.drydock.naval.carrier;

import io.github.sinri.drydock.aviation.Bomber;
import io.github.sinri.drydock.aviation.Drone;
import io.github.sinri.drydock.aviation.Fighter;
import io.github.sinri.drydock.aviation.ObservationBalloon;
import io.github.sinri.drydock.common.health.ObservationBalloonDelegate;
import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.github.sinri.drydock.naval.base.Warship;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.AliyunSLSDisabled;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.AliyunSLSIssueAdapterImpl;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.AliyunSLSMetricRecorder;
import io.github.sinri.keel.facade.cli.KeelCliOption;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.client.WebClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

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
public abstract class AircraftCarrier extends AircraftCarrierDeck {
    /**
     * Command line option to disable monitor functionality.
     */
    public static final String optionDisableMonitor = "disableMonitor";
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

    @Nullable
    private WebClient sharedWebClient;

    /**
     * The bomber component for scheduled task execution.
     */
    @Nullable
    private Bomber bomber;
    /**
     * The drone component for queue-based task processing.
     */
    @Nullable
    private Drone drone;
    /**
     * The fighter component for HTTP server capabilities.
     */
    @Nullable
    private Fighter fighter;
    /**
     * The observation balloon component for runtime monitoring.
     */
    @Nullable
    private ObservationBalloon observationBalloon;

    /**
     * @return the delegate for observation balloon component
     * @since 4.1.5
     */
    @Nullable
    protected ObservationBalloonDelegate constructObservationBalloonDelegate() {
        KeelMetricRecorder metricRecorder = getMetricRecorder();
        if (metricRecorder == null) {
            KeelIssueRecorder<HealthMonitorIssueRecord> issueRecorder = generateIssueRecorder(HealthMonitorIssueRecord.TopicHealthMonitor, HealthMonitorIssueRecord::new);
            return ObservationBalloonDelegate.createWithIssueRecorder(issueRecorder, null);
        } else {
            return ObservationBalloonDelegate.createWithMetricRecorder(metricRecorder, null);
        }
    }

    /**
     * Constructs the observation balloon component for runtime monitoring.
     * As of 4.1.5, it is final, use {@link AircraftCarrier#constructObservationBalloonDelegate()} instead to customize.
     *
     * @return the created {@link ObservationBalloon} instance, or null if disabled
     */
    @Nullable
    protected final ObservationBalloon constructObservationBalloon() {
        ObservationBalloonDelegate delegate = constructObservationBalloonDelegate();
        if (delegate == null) return null;
        return new ObservationBalloon(this, delegate);
    }

    /**
     * Constructs the bomber component for scheduled task execution.
     * <p>
     * This method should return a configured Bomber instance that will handle
     * time-related scheduled tasks through the Sundial system.
     *
     * @return a configured Bomber instance, or null if bomber functionality is disabled
     */
    @Nullable
    protected abstract Bomber constructBomber();

    /**
     * The component for the ability to execute time-relate scheduled tasks, through Sundial.
     *
     * @since 1.5.2
     */
    @Nullable
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
    @Nullable
    protected abstract Drone constructDrone();

    /**
     * The component for the ability to execute queued tasks, through Queue.
     *
     * @since 1.5.2
     */
    @Nullable
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
    @Nullable
    protected abstract Fighter constructFighter(@Nullable Integer port);

    /**
     * The component for the ability to provide HTTP Service.
     *
     * @since 1.5.2
     */
    @Nullable
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
                        .description("Port for the receptionist"),
                new KeelCliOption()
                        .alias(optionDisableMonitor)
                        .flag()
                        .description("Disable monitor functionality")
        );
    }

    /**
     * Checks if the monitor functionality is disabled via the specified command line option.
     *
     * @return true if the monitor is disabled, false otherwise
     * @since 3.0.1
     */
    protected boolean isMonitorDisabled() {
        return getCliArgs().readFlag(optionDisableMonitor);
    }

    /**
     * Checks if queue functionality is disabled via command line option.
     *
     * @return true if the queue is disabled, false otherwise
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
     * <p>
     *     As of 3.0.2, this method is final.
     *     Override {@link AircraftCarrier#prepare()} and {@link Warship#whenLaunched(long)} to handle your customized business logic.
     *
     * @return a Future that completes when all components are successfully deployed
     */
    @Override
    protected final Future<Void> launchAsWarship() {
        sharedWebClient = constructSharedWebClient();

        return Future.succeededFuture()
                     .compose(v -> {
                         boolean disableMonitor = isMonitorDisabled();
                         if (!disableMonitor) {
                             observationBalloon = constructObservationBalloon();
                         }
                         if (observationBalloon != null) {
                             return observationBalloon.deployMe()
                                                      .onSuccess(done -> {
                                                          getUnitLogger().info("Loaded Health Monitor");
                                                      });
                         }
                         return Future.succeededFuture(null);
                     })
                     .compose(v -> {
                         return prepare()
                                 .onSuccess(done -> {
                                     getUnitLogger().info("Prepared For Biz");
                                 });
                     })
                     .compose(v -> {
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
     * Builds the issue record center for logging and monitoring.
     * <p>
     * This method creates a KeelIssueRecordCenter that can send issue records
     * to Aliyun SLS (Simple Log Service) if properly configured, or falls back
     * to console output if SLS is disabled or unavailable.
     *
     * @return a configured KeelIssueRecordCenter instance
     */
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        try {
            AliyunSLSIssueAdapterImpl aliyunSLSIssueAdapter = new AliyunSLSIssueAdapterImpl();
            return KeelIssueRecordCenter.build(aliyunSLSIssueAdapter);
        } catch (AliyunSLSDisabled e) {
            getUnitLogger().exception(e, "buildIssueRecordCenter error");
            return null;
        }
    }

    @Nullable
    @Override
    protected KeelMetricRecorder buildMetricRecorder() {
        try {
            return new AliyunSLSMetricRecorder();
        } catch (AliyunSLSDisabled e) {
            getUnitLogger().exception(e, "buildMetricRecorder error");
            return null;
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


    /**
     * Constructs a shared {@link WebClient} instance for use within the application.
     * This method provides a centralized WebClient that can be shared across
     * various components to perform HTTP-based operations using the Vert.x framework.
     *
     * @return a shared {@link WebClient} instance, or null if shared WebClient is not enabled
     * @since 3.0.2
     */
    @Nullable
    protected WebClient constructSharedWebClient() {
        return WebClient.create(Keel.getVertx());
    }

    /**
     * Executes an operation using a shared or temporary {@link WebClient} instance.
     * If a shared WebClient is available, it uses the shared instance. Otherwise,
     * it creates a temporary WebClient instance for the requested operation, ensuring
     * proper cleanup of the temporary instance after use.
     *
     * @param <R>   the type of the result produced by the operation performed on the WebClient
     * @param usage the function representing the operation to be performed, which takes a
     *              {@link WebClient} as input and returns a {@link Future} of type R. Must not close the WebClient.
     * @return a {@link Future} that represents the result of the operation performed on the WebClient
     */
    @Nonnull
    public <R> Future<R> useSharedWebClient(@Nonnull Function<WebClient, Future<R>> usage) {
        return Future.succeededFuture()
                     .compose(v -> {
                         if (sharedWebClient == null) {
                             WebClient tempWebClient = WebClient.create(Keel.getVertx());
                             return Future.succeededFuture()
                                          .compose(vv -> {
                                              return usage.apply(tempWebClient);
                                          })
                                          .andThen(ar -> {
                                              tempWebClient.close();
                                          });
                         } else {
                             return usage.apply(sharedWebClient);
                         }
                     });
    }
}
