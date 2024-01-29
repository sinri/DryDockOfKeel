package io.github.sinri.drydock.air.frozenplane;

import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.drydock.common.SundialMixin;
import io.vertx.core.Future;

@Deprecated(forRemoval = true)
public abstract class Bomber extends Biplane implements QueueMixin, SundialMixin {
    @Override
    protected Future<Void> flyAsBiplane() {
        return Future.succeededFuture()
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
