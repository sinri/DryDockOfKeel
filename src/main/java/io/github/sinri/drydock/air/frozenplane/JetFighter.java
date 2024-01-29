package io.github.sinri.drydock.air.frozenplane;

import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.drydock.common.SundialMixin;
import io.vertx.core.Future;

@Deprecated(forRemoval = true)
abstract public class JetFighter extends Fighter implements QueueMixin, SundialMixin {

    @Override
    protected final Future<Void> flyAsFighter() {
        return Future.succeededFuture()
                .compose(v -> {
                    return flyAsJetFighter();
                })
                .compose(v -> {
                    return loadSundial();
                })
                .compose(compositeFuture -> {
                    return loadQueue();
                });
    }

    abstract protected Future<Void> flyAsJetFighter();
}
