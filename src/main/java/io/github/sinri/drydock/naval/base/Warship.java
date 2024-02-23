package io.github.sinri.drydock.naval.base;

import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * 一切海军舰船的基底，首先，得具备海洋航行的能力。
 * 1. 建立航海日志记录器，藉此向标准输出记录一切运行时底层信息。
 * 2. 获取本地配置。
 * 3. 建立Keel引擎。
 * 4. 获取远程配置。
 * 5. 建立事件通信中心，用于向事件日志中心进行事件报告。
 *
 * @since 1.1.0
 */
abstract public class Warship implements Boat {
    public static final int EXIT_CODE_FOR_KEEL_INIT_FAILED = 1;
    public static final int EXIT_CODE_FOR_SELF_SINK = 0;
    /**
     * 航海日志记录器
     */
    private final KeelEventLogger unitLogger;
    /**
     * 应用级事件日志中心
     */
    private KeelIssueRecordCenter issueRecordCenter;

    public Warship() {
        this.issueRecordCenter = KeelIssueRecordCenter.outputCenter();
        this.unitLogger = this.issueRecordCenter.generateEventLogger(DryDockLogTopics.TopicDryDock);
        //this.navalLogger.getIssueRecorder().setRecordFormatter(r -> r.context("local_address", KeelHelpers.netHelper().getLocalHostAddress()));
    }

    /**
     * @since 1.3.4
     */
    abstract protected KeelIssueRecordCenter buildIssueRecordCenter();

    /**
     * @since 1.3.4
     */
    public final KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
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

    /**
     * 通过已加载的本地配置进行Vertx配置的构造。
     * 此时可以使用航海日志记录器。
     */
    abstract public VertxOptions buildVertxOptions();

    /**
     * 起航。
     */
    @Override
    public final void launch() {
        loadLocalConfiguration();
        getUnitLogger().info("LOCAL CONFIG LOADED (if any)");

        VertxOptions vertxOptions = buildVertxOptions();

        // todo 此处未考虑舰队模式，如果需要要新增 cluster master 的设定
        Keel.initializeVertx(vertxOptions)
                .compose(initialized -> {
                    getUnitLogger().info("KEEL INITIALIZED");

                    // since 1.2.5
                    Keel.setLogger(getUnitLogger());

                    return loadRemoteConfiguration();
                })
                .compose(done -> {
                    getUnitLogger().info("REMOTE CONFIG LOADED (if any)");
                    issueRecordCenter = buildIssueRecordCenter();
                    return launchAsWarship();
                })
                .onFailure(this::shipwreck);
    }

    /**
     * 加载本地配置。
     * 仅可以使用航海日志记录器。
     */
    abstract protected void loadLocalConfiguration();

    /**
     * 加载远程配置。
     * 此时已加载本地配置，已初始化Keel(Vert.x)。
     * 仅可以使用航海日志记录器。
     */

    abstract protected Future<Void> loadRemoteConfiguration();

    /**
     * 本地及远端配置文件已加载。
     * Keel已初始化。
     * 可使用航海日志记录器以及应用级事件日志记录器。
     * 在此方法中加载其他模块并开始航行。
     */
    abstract protected Future<Void> launchAsWarship();

    /**
     * 发生海难时的标准处理程序，即向航海日志记录事故并以指定故障码退出。
     */
    @Override
    public void shipwreck(Throwable throwable) {
        getUnitLogger().exception(throwable, "Failed to launch, shipwreck");
        System.exit(EXIT_CODE_FOR_KEEL_INIT_FAILED);
    }

    /**
     * 打开通海阀进行自沉。
     *
     * @since 1.0.4
     */
    @Override
    public void sink() {
        getUnitLogger().fatal("SINK");
        Keel.close()
                .onComplete(ar -> {
                    if (ar.failed()) {
                        getUnitLogger().exception(ar.cause(), "Failure in closing Keel.");
                    }
                    getUnitLogger().fatal("Keel Sank.");
                    System.exit(EXIT_CODE_FOR_SELF_SINK);
                });
    }

    @Override
    public KeelEventLogger getUnitLogger() {
        return unitLogger;
    }
}
