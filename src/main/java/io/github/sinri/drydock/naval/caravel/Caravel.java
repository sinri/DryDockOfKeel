package io.github.sinri.drydock.naval.caravel;

import io.github.sinri.drydock.naval.galley.Galley;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelAsyncEventLogCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 轻快帆船。
 * Based on Galley.
 * Load one configure file.
 * Aliyun SLS support.
 * Health Monitor support.
 *
 * @since 1.0.0
 */
public abstract class Caravel extends Galley {


    @Override
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    @Override
    final protected void launchAsGalley() {
        // 航海日志共享大计
        this.getNavalLogger().addBypassLogger(
                generateLogger(
                        AliyunSLSAdapterImpl.TopicNaval,
                        log -> log.put("warship", getClass().getName())
                )
        );
        // 加载健康检查模块
        buildHealthMonitor().deployMe(
                        new DeploymentOptions()
                                .setWorker(true)
                )
                .onSuccess(done -> {
                    getNavalLogger().info("Deployed HealthMonitor");
                    // 成功加载健康检查模块之后，为轻快帆船加载模块。
                    launchAsCaravel();
                })
                .onFailure(throwable -> {
                    getNavalLogger().exception(throwable, "Failed to deploy HealthMonitor");
                    // 如果无法加载健康检查模块，直接自沉。
                    sink();
                });
    }

    /**
     * 为轻快帆船加载模块。
     */
    abstract protected void launchAsCaravel();

    public HealthMonitor buildHealthMonitor() {
        var topicEventLogger = generateLogger(AliyunSLSAdapterImpl.TopicHealthMonitor, null);
        return new HealthMonitor(topicEventLogger);
    }

    /**
     * @since 1.0.6 Add a Naval Log when create Aliyun SLS Log Center failed.
     */
    @Override
    protected KeelEventLogCenter buildLogCenter() {
        try {
            return new KeelAsyncEventLogCenter(new AliyunSLSAdapterImpl());
        } catch (Throwable e) {
            getNavalLogger().exception(e, "Failed in io.github.sinri.drydock.naval.caravel.Caravel.buildLogCenter");
            throw e;
        }
    }

    @Override
    public KeelEventLogger generateLogger(@NotNull String topic, @Nullable Handler<KeelEventLog> eventLogHandler) {
        return getLogCenter().createLogger(topic, eventLogHandler);
    }
}
