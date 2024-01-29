package io.github.sinri.drydock.air.plane;

import io.github.sinri.drydock.air.base.Biplane;
import io.github.sinri.drydock.common.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.common.HealthMonitorMixin;
import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.drydock.common.SundialMixin;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelAsyncEventLogCenter;
import io.vertx.core.Future;

/**
 * @since 1.3.0 Technical Preview
 */
@TechnicalPreview(since = "1.3.0")
public abstract class Bomber extends Biplane implements HealthMonitorMixin, QueueMixin, SundialMixin {
    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }

    @Override
    protected KeelEventLogCenter buildLogCenter() {
        try {
            return new KeelAsyncEventLogCenter(new AliyunSLSAdapterImpl());
        } catch (Throwable e) {
            getLogger().exception(e, "Failed in io.github.sinri.drydock.air.plane.Fighter.buildLogCenter");
            throw e;
        }
    }

    @Override
    protected final Future<Void> flyAsBiplane() {
        return Future.succeededFuture()
                .compose(v -> {
                    // 飞行日志共享大计
                    var bypassLogger = generateLogger(
                            AliyunSLSAdapterImpl.TopicFlight,
                            log -> log.put("plane", getClass().getName())
                    );
                    this.getLogger().addBypassLogger(bypassLogger);
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
