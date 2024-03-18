package io.github.sinri.drydock.air.plane;

import io.github.sinri.drydock.air.base.Biplane;
import io.github.sinri.drydock.common.HttpServerMixin;
import io.github.sinri.drydock.common.health.HealthMonitorMixin;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSIssueAdapterImpl;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSMetricRecorder;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsAsync;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * @since 1.3.0 Technical Preview
 */
public abstract class Fighter extends Biplane implements HealthMonitorMixin, HttpServerMixin {

    private KeelMetricRecorder metricRecorder;

    /**
     * @since 1.3.4
     */
    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        try {
            return new KeelIssueRecordCenterAsAsync(new AliyunSLSIssueAdapterImpl());
        } catch (Throwable e) {
            getLogger().exception(e, "Failed in io.github.sinri.drydock.air.plane.Fighter.buildIssueRecordCenter");
            throw e;
        }
    }

    @Override
    protected final Future<Void> flyAsBiplane() {
        return Future.succeededFuture()
                .compose(v -> {
                    // 飞行日志共享大计
                    this.getLogger().addBypassLogger(generateEventLogger(DryDockLogTopics.TopicDryDock));

                    return Future.succeededFuture();
                })
                .compose(v -> {
                    // Metric Recorder
                    this.metricRecorder = new AliyunSLSMetricRecorder();
                    this.metricRecorder.start();
                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return this.loadHealthMonitor();
                })
                .compose(v -> {
                    return flyAsFighter();
                })
                .compose(v -> {
                    return loadHttpServer();
                });
    }

    protected abstract Future<Void> flyAsFighter();

    @Nonnull
    @Override
    public KeelMetricRecorder getMetricRecorder() {
        return metricRecorder;
    }
}
