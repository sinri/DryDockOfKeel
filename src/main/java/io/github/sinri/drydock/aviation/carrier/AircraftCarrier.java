package io.github.sinri.drydock.aviation.carrier;

import io.github.sinri.drydock.aviation.aircraft.Bomber;
import io.github.sinri.drydock.aviation.aircraft.Drone;
import io.github.sinri.drydock.aviation.aircraft.Fighter;
import io.github.sinri.drydock.common.health.HealthMonitor;
import io.github.sinri.drydock.common.health.HealthMonitorMixin;
import io.github.sinri.drydock.common.health.HealthMonitorWithIssueRecorder;
import io.github.sinri.drydock.common.health.HealthMonitorWithMetricRecorder;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.AliyunSLSIssueAdapterImpl;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.AliyunSLSMetricRecorder;
import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.facade.cli.KeelCliOption;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * An further implementation of AircraftCarrierDeck.
 * By default, support Health Monitor, Queue, Sundial, HTTP Server.
 * <p>
 * The start-up command line is
 * {@code java -jar X.jar [--disableQueue] [--disableSundial] [--disableReceptionist] [--receptionistPort=8080]}
 * </p>
 *
 * @since 1.5.0
 */
public abstract class AircraftCarrier extends AircraftCarrierDeck implements HealthMonitorMixin {
    public static final String optionDisableQueue = "disableQueue";
    public static final String optionDisableSundial = "disableSundial";
    public static final String optionDisableReceptionist = "disableReceptionist";
    public static final String optionReceptionistPort = "receptionistPort";
    private Bomber bomber;
    private Drone drone;
    private Fighter fighter;
    private KeelMetricRecorder metricRecorder;

    protected abstract Bomber constructBomber();

    /**
     * The component for the ability to execute time-relate scheduled tasks, through Sundial.
     *
     * @since 1.5.2
     */
    public Bomber getBomber() {
        return bomber;
    }

    protected abstract Drone constructDrone();

    /**
     * The component for the ability to execute queued tasks, through Queue.
     *
     * @since 1.5.2
     */
    public Drone getDrone() {
        return drone;
    }

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
     * @since 1.5.2
     */
    protected boolean isQueueDisabled() {
        return getCliArgs().readFlag(optionDisableQueue);
    }

    /**
     * @since 1.5.2
     */
    protected boolean isSundialDisabled() {
        return getCliArgs().readFlag(optionDisableSundial);
    }

    /**
     * @since 1.5.2
     */
    protected boolean isReceptionistDisabled() {
        return getCliArgs().readFlag(optionDisableReceptionist);
    }

    /**
     * Load the local configuration synchronously into `Keel.getConfiguration()`.
     * By default, it reads the local file "config.properties" to fetch config.
     *
     */
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    /**
     * @return the built VertxOptions instance.
     */
    protected abstract VertxOptions buildVertxOptions();

    /**
     * Load the remote configuration asynchronously into `Keel.getConfiguration()`.
     *
     * @return a future after done
     */
    protected abstract Future<Void> loadRemoteConfiguration();

    @Override
    protected final void runWithCommandLine() {
        long startTime = System.currentTimeMillis();

        // as of 2.1.0, register JsonifiableSerializer before everything.
        this.loadJsonifiableSerializer();

        loadLocalConfiguration();
        getUnitLogger().info("LOCAL CONFIG LOADED (if any)");

        VertxOptions vertxOptions = buildVertxOptions();

        // todo 此处未考虑舰队模式，如果需要要新增 cluster master 的设定
        Keel.initializeVertx(vertxOptions)
            .onSuccess(init -> {
                startWithKeelInitialized()
                        .onSuccess(over -> {
                            long endTime = System.currentTimeMillis();
                            getUnitLogger().info("Ready, spent " + (endTime - startTime) + " ms");
                        })
                        .onFailure(throwable -> {
                            getUnitLogger().exception(throwable, "SINK");
                            System.exit(4);
                        });
            })
            .onFailure(throwable -> {
                getUnitLogger().exception(throwable, "Keel-Vertx Initialization Failed");
                System.exit(2);
            });
    }

    private Future<Void> startWithKeelInitialized() {
        return Future.succeededFuture()
                     .compose(initialized -> {
                         getUnitLogger().info("KEEL INITIALIZED");
                         // Keel.setLogger(getLogger());
                         return loadRemoteConfiguration();
                     })
                     .compose(done -> {
                         getUnitLogger().info("REMOTE CONFIG LOADED (if any)");
                         issueRecordCenter = buildIssueRecordCenter();
                         Keel.setIssueRecordCenter(issueRecordCenter);
                         // 航海日志共享大计
                         if (!Objects.equals(getIssueRecordCenter(), KeelIssueRecordCenter.outputCenter())) {
                             var bypassLogger = getIssueRecordCenter().generateIssueRecorder(DryDockLogTopics.TopicDryDock, KeelEventLog::new);
                             this.getUnitLogger().addBypassIssueRecorder(bypassLogger);
                         } else {
                             this.getUnitLogger().info("Bypass logging is ignored.");
                         }

                         // Metric Recorder
                         this.metricRecorder = new AliyunSLSMetricRecorder();
                         this.metricRecorder.start();

                         return loadHealthMonitor();
                     })
                     .compose(v -> {
                         getUnitLogger().info("Loaded Health Monitor");
                         return prepare();
                     })
                     .compose(v -> {
                         getUnitLogger().info("Prepared For Biz");
                         boolean disableQueue = isQueueDisabled();
                         if (!disableQueue) {
                             drone = constructDrone();
                         }
                         if (drone != null) {
                             return drone.load()
                                         .onSuccess(done -> {
                                             getUnitLogger().info("Loaded Queue");
                                         });
                         }
                         return Future.succeededFuture();
                     })
                     .compose(v -> {
                         boolean disableSundial = isSundialDisabled();
                         if (!disableSundial) {
                             bomber = constructBomber();
                         }
                         if (bomber != null) {
                             return bomber.load()
                                          .onSuccess(done -> {
                                              getUnitLogger().info("Loaded Sundial");
                                          });
                         }
                         return Future.succeededFuture();
                     })
                     .compose(v -> {
                         boolean disableReceptionist = isReceptionistDisabled();
                         if (!disableReceptionist) {
                             String s = getCliArgs().readOption(optionReceptionistPort);
                             Integer receptionistPort = (s == null ? null : Integer.parseInt(s));
                             fighter = constructFighter(receptionistPort);
                             if (fighter != null) {
                                 return fighter.load()
                                               .onSuccess(done -> {
                                                   getUnitLogger().info("Loaded Http Server on port: " + fighter.configuredHttpServerPort());
                                               });
                             }
                         }
                         return Future.succeededFuture();
                     })
                     .compose(v -> {
                         return ready();
                     });
    }

    /**
     * @since 2.1.0
     */
    protected void loadJsonifiableSerializer() {
        JsonifiableSerializer.register();
    }

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

    /**
     * 如果不需要 HealthMonitor，重写方法使之返回null。
     */
    @Override
    public HealthMonitor<?> buildHealthMonitor() {
        if (metricRecorder == null) {
            return new HealthMonitorWithIssueRecorder(generateIssueRecorder(HealthMonitorIssueRecord.TopicHealthMonitor, HealthMonitorIssueRecord::new));
        } else {
            return new HealthMonitorWithMetricRecorder(metricRecorder);
        }
    }

    /**
     * Prepare for business, after logger and health monitor initialized.
     *
     */
    @Nonnull
    protected abstract Future<Void> prepare();

    @Nonnull
    protected abstract Future<Void> ready();

    @Nonnull
    @Override
    public KeelMetricRecorder getMetricRecorder() {
        return this.metricRecorder;
    }
}
