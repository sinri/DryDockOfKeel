package io.github.sinri.drydock.air.plane;

import io.github.sinri.drydock.air.base.Biplane;
import io.github.sinri.drydock.common.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.common.HealthMonitorMixin;
import io.github.sinri.drydock.common.HttpServerMixin;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelAsyncEventLogCenter;
import io.vertx.core.Future;

/**
 * @since 1.3.0 Technical Preview
 */
public abstract class Fighter extends Biplane implements HealthMonitorMixin, HttpServerMixin {
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
                            log -> log.context(c -> c.put("plane", getClass().getName()))
                    );
                    this.getLogger().addBypassLogger(bypassLogger);
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
