package io.github.sinri.drydock.naval.ranged;

import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.drydock.common.SundialMixin;
import io.github.sinri.drydock.common.health.HealthMonitorMixin;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSIssueAdapterImpl;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSMetricRecorder;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsAsync;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * 护卫舰。
 * 专门搞队列和定时任务，不提供HTTP服务，为单节点服务设计。
 *
 * @since 1.0.1
 * @since 1.1.0
 */
public abstract class Frigate extends Quadrireme implements QueueMixin, SundialMixin, HealthMonitorMixin {
    protected KeelMetricRecorder metricRecorder;

    /**
     * 在本地和远端配置加载完毕、航海和应用日志记录完备之后，准备数据源，如MySQL等。
     *
     * @since 1.2.0
     */
    @Nonnull
    abstract protected Future<Void> prepareDataSources();

    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        try {
            return new KeelIssueRecordCenterAsAsync(new AliyunSLSIssueAdapterImpl());
        } catch (Throwable e) {
            getLogger().exception(e, "Failed in Frigate.buildIssueRecordCenter");
            throw e;
        }
    }

    @Override
    final protected Future<Void> launchAsQuadrireme() {
        return Future.succeededFuture()
                .compose(v -> {
                    // Metric Recorder since 3.2.5
                    this.metricRecorder = new AliyunSLSMetricRecorder();
                    this.metricRecorder.start();
                    return prepareDataSources();
                })
                .compose(v -> {
                    return Future.all(
                            loadHealthMonitor(),
                            loadSundial(),
                            loadQueue()
                    );
                })
                .compose(v -> {
                    return launchAsFrigate();
                });
    }

    abstract protected Future<Void> launchAsFrigate();

    /**
     * @since 3.2.5
     */
    @Nonnull
    @Override
    public KeelMetricRecorder getMetricRecorder() {
        return metricRecorder;
    }
}
