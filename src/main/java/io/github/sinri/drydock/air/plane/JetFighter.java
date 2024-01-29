package io.github.sinri.drydock.air.plane;

import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.drydock.common.SundialMixin;
import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.Future;

/**
 * @since 1.3.0 Technical Preview
 */
@TechnicalPreview(since = "1.3.0")
public abstract class JetFighter extends Fighter implements QueueMixin, SundialMixin {
    @Override
    protected final Future<Void> flyAsFighter() {
        return Future.succeededFuture()
                .compose(v -> {
                    return flyAsJetFighter();
                })
                .compose(v -> {
                    return loadSundial();
                })
                .compose(v -> {
                    return loadQueue();
                })
                .compose(v -> {
                    return loadHttpServer();
                });
    }

    abstract protected Future<Void> flyAsJetFighter();
}
