package io.github.sinri.drydock.naval.base;

import io.github.sinri.drydock.common.AliyunSLSAdapterImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

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
    private final KeelEventLogger navalLogger;
    /**
     * 应用级事件日志中心
     */
    private KeelEventLogCenter logCenter;

    public Warship() {
        this.logCenter = KeelOutputEventLogCenter.getInstance();
        this.navalLogger = this.logCenter.createLogger(
                AliyunSLSAdapterImpl.TopicNaval,//"DryDock::Naval",
                x -> x.context(c -> c.put("local_address", KeelHelpers.netHelper().getLocalHostAddress())));
    }

    /**
     * 航海日志：向标准输出记录一切运行时底层信息。
     */
    @Override
    public final KeelEventLogger getNavalLogger() {
        return navalLogger;
    }

    /**
     * 建立应用级事件日志中心。
     * 此时已加载本地和远程的配置。
     * 可以使用航海日志记录器。
     */
    abstract protected KeelEventLogCenter buildLogCenter();

    /**
     * @return 事件日志中心。
     */
    public final KeelEventLogCenter getLogCenter() {
        return logCenter;
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
        getNavalLogger().info("LOCAL CONFIG LOADED (if any)");

        VertxOptions vertxOptions = buildVertxOptions();

        // todo 此处未考虑舰队模式，如果需要要新增 cluster master 的设定
        Keel.initializeVertx(vertxOptions)
                .compose(initialized -> {
                    getNavalLogger().info("KEEL INITIALIZED");

                    // since 1.2.5
                    Keel.setLogger(getNavalLogger());

                    return loadRemoteConfiguration();
                })
                .compose(done -> {
                    getNavalLogger().info("REMOTE CONFIG LOADED (if any)");
                    logCenter = buildLogCenter();
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
        getNavalLogger().exception(throwable, "Failed to launch, shipwreck");
        System.exit(EXIT_CODE_FOR_KEEL_INIT_FAILED);
    }

    /**
     * 打开通海阀进行自沉。
     *
     * @since 1.0.4
     */
    @Override
    public void sink() {
        getNavalLogger().fatal("SINK");
        Keel.close()
                .onComplete(ar -> {
                    if (ar.failed()) {
                        getNavalLogger().exception(ar.cause(), "Failure in closing Keel.");
                    }
                    getNavalLogger().fatal("Keel Sank.");
                    System.exit(EXIT_CODE_FOR_SELF_SINK);
                });
    }

    /**
     * 建立一个向应用级事件日志中心进行通报的应用日志记录器。
     *
     * @param topic           事件主题
     * @param eventLogHandler 事件日志处理器
     */
    @Override
    public final KeelEventLogger generateLogger(String topic, Handler<KeelEventLog> eventLogHandler) {
        return getLogCenter().createLogger(topic, eventLogHandler);
    }
}
