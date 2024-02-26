package io.github.sinri.drydock.air.plane;

import io.github.sinri.drydock.air.base.Biplane;
import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.drydock.common.SundialMixin;
import io.github.sinri.drydock.common.health.HealthMonitorMixin;
import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.common.logging.adapter.AliyunSLSIssueAdapterImpl;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsAsync;
import io.vertx.core.Future;

/**
 * @since 1.3.0 Technical Preview
 */
public abstract class Bomber extends Biplane implements HealthMonitorMixin, QueueMixin, SundialMixin {
    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }

    /**
     * @since 1.3.4
     */
    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        try {
            return new KeelIssueRecordCenterAsAsync(new AliyunSLSIssueAdapterImpl());
        } catch (Throwable e) {
            getLogger().exception(e, "Failed in io.github.sinri.drydock.air.plane.Bomber.buildIssueRecordCenter");
            throw e;
        }
    }

    @Override
    protected final Future<Void> flyAsBiplane() {
        return Future.succeededFuture()
                .compose(v -> {
                    // 飞行日志共享大计
                    this.getUnitLogger().addBypassLogger(generateEventLogger(DryDockLogTopics.TopicDryDock));

                    return Future.succeededFuture();
                })
                .compose(v -> {
                    return this.loadHealthMonitor();
                })
                .compose(v -> {
                    return flyAsBomber();
                })
                .compose(v -> {
                    return loadSundial();
                })
                .compose(v -> {
                    return loadQueue();
                });
    }

    abstract protected Future<Void> flyAsBomber();
}
