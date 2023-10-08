package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.caravel.Caravel;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Future;

public class CaravelTest extends Caravel {
    public static void main(String[] args) {
        new CaravelTest().launch();
    }

    @Override
    protected void launchAsCaravel() {
        KeelEventLogger logger = generateLogger("CaravelTest", null);
        logger.info("launched");
        Keel.getVertx().setPeriodic(60_000L,timer->{
            logger.info("trigger time here");
        });
    }

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }
}
