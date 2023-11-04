package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.melee.Caravel;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Future;

public class CaravelTest extends Caravel {
    public static void main(String[] args) {
        new CaravelTest().launch();
    }

    @Override
    protected Future<Void> launchAsCaravel() {
        KeelEventLogger logger = generateLogger("CaravelTest", null);
        logger.info("launched");
        Keel.getVertx().setPeriodic(10_000L, timer -> {
            logger.info("trigger time here");
        });
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }
}
