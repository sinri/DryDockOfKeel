package io.github.sinri.drydock.air.frozenplane;

import io.github.sinri.drydock.common.HttpServerMixin;
import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.Future;

/**
 * 对标 io.github.sinri.drydock.naval.melee.Ironclad
 */
@TechnicalPreview
@Deprecated(forRemoval = true)
abstract public class Fighter extends Biplane implements HttpServerMixin {

    @Override
    protected final Future<Void> flyAsBiplane() {
        return Future.succeededFuture()
                .compose(v -> {
                    return flyAsFighter();
                })
                .compose(v -> {
                    return loadHttpServer();
                });
    }

    abstract protected Future<Void> flyAsFighter();
}
