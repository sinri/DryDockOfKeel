package io.github.sinri.drydock.naval.ranged;

import io.github.sinri.drydock.naval.base.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.naval.base.QueueMixin;
import io.github.sinri.drydock.naval.base.SundialMixin;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelAsyncEventLogCenter;
import io.vertx.core.Future;

/**
 * 护卫舰。
 * 专门搞队列和定时任务，不提供HTTP服务，为单节点服务设计。
 *
 * @since 1.0.1
 * @since 1.1.0
 */
public abstract class Frigate extends Quadrireme implements QueueMixin, SundialMixin {

    @Override
    protected KeelEventLogCenter buildLogCenter() {
        try {
            return new KeelAsyncEventLogCenter(new AliyunSLSAdapterImpl());
        } catch (Throwable e) {
            getNavalLogger().exception(e, "Failed in Frigate.buildLogCenter");
            throw e;
        }
    }

    @Override
    final protected Future<Void> launchAsQuadrireme() {
        return Future.all(
                        loadSundial(),
                        loadQueue()
                )
                .compose(v -> {
                    return launchAsFrigate();
                });
    }

    abstract protected Future<Void> launchAsFrigate();
}
