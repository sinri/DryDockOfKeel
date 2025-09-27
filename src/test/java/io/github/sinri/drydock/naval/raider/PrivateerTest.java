package io.github.sinri.drydock.naval.raider;

import io.vertx.core.Future;

public class PrivateerTest extends Privateer {

    @Override
    protected Future<Void> launchAsPrivateer() {
        getUnitLogger().info("Launching Privateer test");
        // return Keel.asyncSleep(1000L);
        return Future.succeededFuture();
    }
}