package io.github.sinri.drydock.naval.carrier;

import io.github.sinri.drydock.air.Bomber;
import io.github.sinri.drydock.air.Drone;
import io.github.sinri.drydock.air.Fighter;
import io.github.sinri.drydock.common.health.HealthMonitor;
import io.github.sinri.drydock.common.health.HealthMonitorMixin;
import io.github.sinri.drydock.common.health.HealthMonitorWithIssueRecorder;
import io.github.sinri.drydock.common.health.HealthMonitorWithMetricRecorder;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSIssueAdapterImpl;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSMetricRecorder;
import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsAsync;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.cli.CommandLine;
import io.vertx.core.cli.Option;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.5.0 Technical Preview
 */
@TechnicalPreview(since = "1.5.0")
public abstract class AircraftCarrier extends AircraftCarrierDeck implements HealthMonitorMixin {
    private Bomber bomber;
    private Drone drone;
    private Fighter fighter;
    private KeelMetricRecorder metricRecorder;

    protected abstract Bomber constructBomber();

    /**
     * @since 1.5.2
     */
    public Bomber getBomber() {
        return bomber;
    }

    protected abstract Drone constructDrone();

    /**
     * @since 1.5.2
     */
    public Drone getDrone() {
        return drone;
    }

    protected abstract Fighter constructFighter(@Nullable Integer port);

    /**
     * @since 1.5.2
     */
    public Fighter getFighter() {
        return this.fighter;
    }

    @Nullable
    @Override
    protected List<Option> buildCliOptions() {
        return List.of(
                new Option().setLongName("disableQueue").setFlag(true),
                new Option().setLongName("disableSundial").setFlag(true),
                new Option().setLongName("disableReceptionist").setFlag(true),
                new Option().setLongName("receptionistPort").setRequired(false)
        );
    }

    /**
     * @since 1.5.2
     */
    protected boolean isQueueDisabled(@Nonnull CommandLine commandLine) {
        return commandLine.isFlagEnabled("disableQueue");
    }

    /**
     * @since 1.5.2
     */
    protected boolean isSundialDisabled(@Nonnull CommandLine commandLine) {
        return commandLine.isFlagEnabled("disableSundial");
    }

    /**
     * @since 1.5.2
     */
    protected boolean isReceptionistDisabled(@Nonnull CommandLine commandLine) {
        return commandLine.isFlagEnabled("disableReceptionist");
    }

    /**
     * 加载本地配置。
     * 仅可以使用航海日志记录器。
     */
    protected void loadLocalConfiguration(@Nonnull CommandLine commandLine) {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    protected abstract VertxOptions buildVertxOptions(@Nonnull CommandLine commandLine);

    protected abstract Future<Void> loadRemoteConfiguration(@Nonnull CommandLine commandLine);

    @Override
    protected final void runWithCommandLine(@Nonnull CommandLine commandLine) {
        long startTime = System.currentTimeMillis();

        loadLocalConfiguration(commandLine);
        getLogger().info("LOCAL CONFIG LOADED (if any)");

        VertxOptions vertxOptions = buildVertxOptions(commandLine);

        // todo 此处未考虑舰队模式，如果需要要新增 cluster master 的设定
        Keel.initializeVertx(vertxOptions)
                .compose(initialized -> {
                    getLogger().info("KEEL INITIALIZED");
                    Keel.setLogger(getLogger());
                    return loadRemoteConfiguration(commandLine);
                })
                .compose(done -> {
                    getLogger().info("REMOTE CONFIG LOADED (if any)");
                    issueRecordCenter = buildIssueRecordCenter();
                    // 航海日志共享大计
                    if (getIssueRecordCenter() != KeelIssueRecordCenter.outputCenter()) {
                        var bypassLogger = getIssueRecordCenter().generateEventLogger(DryDockLogTopics.TopicDryDock);
                        this.getLogger().addBypassLogger(bypassLogger);
                    } else {
                        this.getLogger().info("Bypass logging is ignored.");
                    }

                    // Metric Recorder
                    this.metricRecorder = new AliyunSLSMetricRecorder();
                    this.metricRecorder.start();

                    return loadHealthMonitor();
                })
                .compose(v -> {
                    getLogger().info("Loaded Health Monitor");
                    return prepare(commandLine);
                })
                .compose(v -> {
                    getLogger().info("Prepared For Biz");
                    boolean disableQueue = commandLine.isFlagEnabled("disableQueue");
                    if (!disableQueue) {
                        drone = constructDrone();
                    }
                    if (drone != null) {
                        return drone.loadQueue()
                                .onSuccess(done -> {
                                    getLogger().info("Loaded Queue");
                                });
                    }
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    boolean disableSundial = commandLine.isFlagEnabled("disableSundial");
                    if (!disableSundial) {
                        bomber = constructBomber();
                    }
                    if (bomber != null) {
                        return bomber.loadSundial()
                                .onSuccess(done -> {
                                    getLogger().info("Loaded Sundial");
                                });
                    }
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    boolean disableReceptionist = commandLine.isFlagEnabled("disableReceptionist");
                    if (!disableReceptionist) {
                        String receptionistPortStr = commandLine.getOptionValue("receptionistPort");
                        Integer receptionistPort = receptionistPortStr == null ? null : Integer.parseInt(receptionistPortStr);
                        fighter = constructFighter(receptionistPort);
                        if (fighter != null) {
                            return fighter.loadHttpServer()
                                    .onSuccess(done -> {
                                        getLogger().info("Loaded Http Server on port: " + fighter.configuredHttpServerPort());
                                    });
                        }
                    }
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return ready(commandLine)
                            .onSuccess(done -> {
                                long endTime = System.currentTimeMillis();
                                getLogger().info("Ready, spent " + (endTime - startTime) + " ms");
                            });
                })
                .onFailure(throwable -> {
                    getLogger().exception(throwable, "SINK");
                    System.exit(1);
                });
    }

    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        boolean disabled = AliyunSLSIssueAdapterImpl.isDisabled();
        if (disabled) {
            return KeelIssueRecordCenter.outputCenter();
        } else {
            try {
                return new KeelIssueRecordCenterAsAsync(new AliyunSLSIssueAdapterImpl());
            } catch (Throwable e) {
                getLogger().exception(e, "Failed in io.github.sinri.drydock.naval.melee.Caravel.buildIssueRecordCenter");
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

    @Nonnull
    protected abstract Future<Void> prepare(@Nonnull CommandLine commandLine);

    @Nonnull
    protected abstract Future<Void> ready(@Nonnull CommandLine commandLine);

    @Nonnull
    @Override
    public KeelMetricRecorder getMetricRecorder() {
        return this.metricRecorder;
    }

}
