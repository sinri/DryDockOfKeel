package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.melee.Caravel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class CaravelTest extends Caravel {
    public static void main(String[] args) {
        new CaravelTest().launch();
    }

    @Nonnull
    @Override
    protected Future<Void> prepareDataSources() {
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> launchAsCaravel() {
        KeelEventLogger logger = getIssueRecordCenter().generateEventLogger("CaravelTest");
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
