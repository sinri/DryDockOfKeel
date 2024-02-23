package io.github.sinri.drydock.naval.melee;

import io.github.sinri.drydock.common.HealthMonitorMixin;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSIssueAdapterImpl;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsAsync;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

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

    /**
     * 在本地和远端配置加载完毕、航海和应用日志记录完备之后，准备数据源，如MySQL等。
     *
     * @since 1.2.0
     */
    @Nonnull
    abstract protected Future<Void> prepareDataSources();

    @Override
    final protected Future<Void> launchAsGalley() {
        return Future.succeededFuture()
                .compose(v -> {
                    // 航海日志共享大计
                    var bypassLogger = getIssueRecordCenter().generateEventLogger(DryDockLogTopics.TopicNaval);
                    this.getUnitLogger().getIssueRecorder().addBypassIssueRecorder(bypassLogger.getIssueRecorder());
                    // 加载数据源（例如MySQL等）
                    return prepareDataSources();
                })
                .compose(v -> {
                    // 加载健康检查模块
                    return loadHealthMonitor();
                })
                .compose(v -> {
                    return this.launchAsCaravel();
                });
    }

    /**
     * 航海日志已可报告给应用日志中心。
     * 数据源等已加载。
     * 在桨帆船的基础上增加了健康检查模块。
     * 为轻快帆船加载模块。
     */
    abstract protected Future<Void> launchAsCaravel();

    /**
     * 轻快帆船默认采用Aliyun SLS提供应用日志服务。
     *
     * @since 1.0.6 Add a Naval Log when create Aliyun SLS Log Center failed.
     */
    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        try {
            return new KeelIssueRecordCenterAsAsync(new AliyunSLSIssueAdapterImpl());
        } catch (Throwable e) {
            getUnitLogger().exception(e, "Failed in io.github.sinri.drydock.naval.melee.Caravel.buildIssueRecordCenter");
            throw e;
        }
    }
}
