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
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import picocli.CommandLine;
import picocli.CommandLine.Model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

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
    protected List<Model.OptionSpec> buildCliOptions() {
        return List.of(
                Model.OptionSpec.builder("--" + optionDisableQueue)
                                .description("Disable queue functionality")
                                .build(),
                Model.OptionSpec.builder("--" + optionDisableSundial)
                                .description("Disable sundial functionality")
                                .build(),
                Model.OptionSpec.builder("--" + optionDisableReceptionist)
                                .description("Disable receptionist functionality")
                                .build(),
                Model.OptionSpec.builder("--" + optionReceptionistPort)
                                .description("Port for the receptionist")
                                .type(Integer.class)
                                .build()
        );
    }

    /**
     * @since 1.5.2
     */
    protected boolean isQueueDisabled(@Nonnull CommandLine.ParseResult parseResult) {
        return parseResult.hasMatchedOption(optionDisableQueue);
    }

    /**
     * @since 1.5.2
     */
    protected boolean isSundialDisabled(@Nonnull CommandLine.ParseResult parseResult) {
        return parseResult.hasMatchedOption(optionDisableSundial);
    }

    /**
     * @since 1.5.2
     */
    protected boolean isReceptionistDisabled(@Nonnull CommandLine.ParseResult parseResult) {
        return parseResult.hasMatchedOption(optionDisableReceptionist);
    }

    /**
     * Load the local configuration synchronously into `Keel.getConfiguration()`.
     * By default, it reads local file "config.properties" to fetch config.
     *
     * @param parseResult the parsed command line parameters.
     */
    protected void loadLocalConfiguration(@Nonnull CommandLine.ParseResult parseResult) {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    /**
     * @param parseResult the parsed command line parameters.
     * @return the built VertxOptions instance.
     */
    protected abstract VertxOptions buildVertxOptions(@Nonnull CommandLine.ParseResult parseResult);

    /**
     * Load the remote configuration asynchronously into `Keel.getConfiguration()`.
     *
     * @param parseResult the parsed command line parameters.
     * @return a future after done
     */
    protected abstract Future<Void> loadRemoteConfiguration(@Nonnull CommandLine.ParseResult parseResult);

    @Override
    protected final int runWithCommandLine(CommandLine.ParseResult parseResult) throws CommandLine.ExecutionException, CommandLine.ParameterException {
        long startTime = System.currentTimeMillis();

        // as of 2.1.0, register JsonifiableSerializer before everything.
        this.loadJsonifiableSerializer();

        loadLocalConfiguration(parseResult);
        getUnitLogger().info("LOCAL CONFIG LOADED (if any)");

        VertxOptions vertxOptions = buildVertxOptions(parseResult);

        // todo 此处未考虑舰队模式，如果需要要新增 cluster master 的设定
        Keel.initializeVertx(vertxOptions)
            .compose(initialized -> {
                getUnitLogger().info("KEEL INITIALIZED");
                // Keel.setLogger(getLogger());
                return loadRemoteConfiguration(parseResult);
            })
            .compose(done -> {
                getUnitLogger().info("REMOTE CONFIG LOADED (if any)");
                issueRecordCenter = buildIssueRecordCenter();
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
                return prepare(parseResult);
            })
            .compose(v -> {
                getUnitLogger().info("Prepared For Biz");
                boolean disableQueue = isQueueDisabled(parseResult);
                if (!disableQueue) {
                    drone = constructDrone();
                }
                if (drone != null) {
                    return drone.loadQueue()
                                .onSuccess(done -> {
                                    getUnitLogger().info("Loaded Queue");
                                });
                }
                return Future.succeededFuture();
            })
            .compose(v -> {
                boolean disableSundial = isSundialDisabled(parseResult);
                if (!disableSundial) {
                    bomber = constructBomber();
                }
                if (bomber != null) {
                    return bomber.loadSundial()
                                 .onSuccess(done -> {
                                     getUnitLogger().info("Loaded Sundial");
                                 });
                }
                return Future.succeededFuture();
            })
            .compose(v -> {
                boolean disableReceptionist = isReceptionistDisabled(parseResult);
                if (!disableReceptionist) {
                    Integer receptionistPort = parseResult.hasMatchedOption(optionReceptionistPort)
                            ? parseResult.matchedOptionValue(optionReceptionistPort, null)
                            : null;
                    fighter = constructFighter(receptionistPort);
                    if (fighter != null) {
                        return fighter.loadHttpServer()
                                      .onSuccess(done -> {
                                          getUnitLogger().info("Loaded Http Server on port: " + fighter.configuredHttpServerPort());
                                      });
                    }
                }
                return Future.succeededFuture();
            })
            .compose(v -> {
                return ready(parseResult)
                        .onSuccess(done -> {
                            long endTime = System.currentTimeMillis();
                            getUnitLogger().info("Ready, spent " + (endTime - startTime) + " ms");
                        });
            })
            .onFailure(throwable -> {
                getUnitLogger().exception(throwable, "SINK");
                System.exit(1);
            });

        return 0;
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
                getUnitLogger().exception(e, "Failed in io.github.sinri.drydock.naval.melee.Caravel" +
                        ".buildIssueRecordCenter");
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
     * @param parseResult the parsed command line parameters.
     */
    @Nonnull
    protected abstract Future<Void> prepare(@Nonnull CommandLine.ParseResult parseResult);

    /**
     * An asynchronous code block after the business of this program is initialized.
     *
     * @param parseResult the parsed command line parameters.
     */
    @Nonnull
    protected abstract Future<Void> ready(@Nonnull CommandLine.ParseResult parseResult);

    @Nonnull
    @Override
    public KeelMetricRecorder getMetricRecorder() {
        return this.metricRecorder;
    }

}
