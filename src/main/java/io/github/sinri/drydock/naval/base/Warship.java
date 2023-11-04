package io.github.sinri.drydock.naval.base;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;

/**
 * 一切海军舰船的基底，首先，得具备海洋航行的能力。
 * 1. 建立航海日志记录器，藉此向标准输出记录一切运行时底层信息。
 * 2. 获取本地配置。
 * 3. 获取远程配置。
 * 4. 建立事件通信中心，用于向事件日志中心进行事件报告。
 *
 * @since 1.1.0
 */
abstract public class Warship implements Boat {
    public static final int EXIT_CODE_FOR_KEEL_INIT_FAILED = 1;
    public static final int EXIT_CODE_FOR_SELF_SINK = 0;
    private final KeelEventLogger navalLogger;
    private KeelEventLogCenter logCenter;

    public Warship() {
        this.navalLogger = KeelOutputEventLogCenter.getInstance()
                .createLogger("DryDock::Naval", x -> x
                        .put("local_address", KeelHelpers.netHelper().getLocalHostAddress()));
    }

    /**
     * 航海日志：向标准输出记录一切运行时底层信息。
     */
    @Override
    public KeelEventLogger getNavalLogger() {
        return navalLogger;
    }

    /**
     * @return 建立事件日志中心的方法。
     */
    abstract protected KeelEventLogCenter buildLogCenter();

    /**
     * @return 事件日志中心。
     */
    public KeelEventLogCenter getLogCenter() {
        return logCenter;
    }

    /**
     * 给Vertx配置。
     */
    abstract public VertxOptions buildVertxOptions();

    /**
     * 起航。
     */
    @Override
    public final void launch() {
        VertxOptions vertxOptions = buildVertxOptions();
        Keel.initializeVertx(vertxOptions)
                .compose(initialized -> {
                    getNavalLogger().info("KEEL INITIALIZED");

                    loadLocalConfiguration();
                    getNavalLogger().info(" LOCAL CONFIG LOADED (if any)");
                    return loadRemoteConfiguration();
                })
                .compose(done -> {
                    getNavalLogger().info(" REMOTE CONFIG LOADED (if any)");
                    logCenter = buildLogCenter();
                    return launchAsWarship();
                })
                .onFailure(this::shipwreck);
    }

    /**
     * 加载本地配置。
     */
    abstract protected void loadLocalConfiguration();

    /**
     * 加载远程配置。
     */

    abstract protected Future<Void> loadRemoteConfiguration();

    /**
     * 加载其他模块。
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
        Keel.getVertx().close(ar -> {
            System.exit(EXIT_CODE_FOR_SELF_SINK);
        });
    }

    /**
     * 建立一个向事件日志中心进行通报的日志记录器。
     *
     * @param topic           事件主题
     * @param eventLogHandler 事件日志处理器
     */
    @Override
    public KeelEventLogger generateLogger(String topic, Handler<KeelEventLog> eventLogHandler) {
        return KeelOutputEventLogCenter.getInstance().createLogger(topic, eventLogHandler);
    }
}
