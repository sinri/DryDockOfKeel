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
    private final String configPropertiesFile;

    public Caravel() {
        this("config.properties");
    }

    public Caravel(String configPropertiesFile) {
        this.configPropertiesFile = configPropertiesFile;
    }

    @Override
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile(configPropertiesFile);
    }

    @Override
    final protected void launchAsGalley() {
        buildHealthMonitor()
                .deployMe(new DeploymentOptions().setWorker(true))
                .onSuccess(done -> {
                    getNavalLogger().info("Deployed HealthMonitor");
                    launchAsCaravel();
                })
                .onFailure(throwable -> {
                    getNavalLogger().exception(throwable, "Failed to deploy HealthMonitor");
                });
    }

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
