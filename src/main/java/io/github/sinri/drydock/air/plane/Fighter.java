package io.github.sinri.drydock.air.plane;

import io.github.sinri.drydock.air.base.Biplane;
import io.github.sinri.drydock.common.HttpServerMixin;
import io.github.sinri.drydock.common.health.HealthMonitorMixin;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSIssueAdapterImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsAsync;
import io.vertx.core.Future;

/**
 * @since 1.3.0 Technical Preview
 */
public abstract class Fighter extends Biplane implements HealthMonitorMixin, HttpServerMixin {

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
                    var bypassLogger = KeelEventLogger.from(generateIssueRecorder(DryDockLogTopics.TopicDryDock, () -> new KeelEventLog(DryDockLogTopics.TopicDryDock)));
                    this.getUnitLogger().getIssueRecorder().addBypassIssueRecorder(bypassLogger.getIssueRecorder());

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
}
