package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.melee.Galley;
import io.vertx.core.Future;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class GalleyTest extends Galley {
    @Override
    protected void loadLocalConfiguration() {

    }

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> launchAsGalley() {
        getLogger().info("launched");
        Keel.getVertx().setTimer(2000L, timer -> {
            getLogger().info("time up");
            Keel.getVertx().close();
        });
        return Future.succeededFuture();
    }

    public static void main(String[] args) {
        new GalleyTest().launch();
    }
}
