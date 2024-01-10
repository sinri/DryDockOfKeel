package io.github.sinri.drydock.naval.melee;

import io.github.sinri.drydock.naval.base.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.naval.base.HealthMonitorMixin;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelAsyncEventLogCenter;
import io.vertx.core.Future;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * 轻快帆船。
 * Based on Galley.
 * Load one configure file.
 * Aliyun SLS support.
 * Health Monitor support.
 *
 * @since 1.0.0
 */
public abstract class Caravel extends Galley implements HealthMonitorMixin {


    @Override
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    @Override
    final protected Future<Void> launchAsGalley() {
        return Future.succeededFuture()
                .compose(v -> {
                    // 航海日志共享大计
                    var bypassLogger = generateLogger(
                            AliyunSLSAdapterImpl.TopicNaval,
                            log -> log.put("warship", getClass().getName())
                    );
                    this.getNavalLogger().addBypassLogger(bypassLogger);
                    // 加载健康检查模块
                    return loadHealthMonitor();
                })
                .compose(v -> {
                    return this.launchAsCaravel();
                });
    }

    /**
     * 为轻快帆船加载模块。
     */
    abstract protected Future<Void> launchAsCaravel();

    /**
     * @since 1.0.6 Add a Naval Log when create Aliyun SLS Log Center failed.
     */
    @Override
    protected KeelEventLogCenter buildLogCenter() {
        try {
            return new KeelAsyncEventLogCenter(new AliyunSLSAdapterImpl());
        } catch (Throwable e) {
            getNavalLogger().exception(e, "Failed in io.github.sinri.drydock.naval.melee.Caravel.buildLogCenter");
            throw e;
        }
    }
}
