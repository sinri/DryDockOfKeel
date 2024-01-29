package io.github.sinri.drydock.air.frozenplane;

import io.github.sinri.drydock.air.base.Plane;
import io.github.sinri.drydock.common.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.common.HealthMonitorMixin;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelAsyncEventLogCenter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * 对标 io.github.sinri.drydock.naval.melee.Caravel
 */
@TechnicalPreview
@Deprecated(forRemoval = true)
public abstract class Biplane extends Plane implements HealthMonitorMixin {

    protected KeelEventLogCenter buildLogCenter() {
        try {
            return new KeelAsyncEventLogCenter(new AliyunSLSAdapterImpl());
        } catch (Throwable e) {
            getLogger().exception(e, "Failed in io.github.sinri.drydock.air.plane.Fighter.buildLogCenter");
            throw e;
        }
    }

    @Nonnull
    abstract protected Future<Void> prepareDataSources();

    @Override
    public final Future<Void> flyAsPlane() {
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
                    return prepareDataSources();
                })
                .compose(v -> {
                    return loadHealthMonitor();
                })
                .compose(healthMonitorLoaded -> {
                    return flyAsBiplane();
                });
    }

    abstract protected Future<Void> flyAsBiplane();

}
