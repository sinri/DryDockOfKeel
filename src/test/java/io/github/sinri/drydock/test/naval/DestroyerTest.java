package io.github.sinri.drydock.test.naval;

import io.github.sinri.drydock.naval.destroyer.Destroyer;
import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;

import java.util.Collection;

public class DestroyerTest extends Destroyer {

    public static void main(String[] args) {
        new DestroyerTest().launch();
    }


    @Override
    public KeelQueue.SignalReader buildSignalReader() {
        return null;
    }

    @Override
    public KeelQueueNextTaskSeeker buildQueueNextTaskSeeker() {
        return null;
    }

    @Override
    public Future<Collection<KeelSundialPlan>> fetchSundialPlans() {
        return null;
    }

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        return null;
    }

    @Override
    protected Future<Void> launchAsDestroyer() {
        return null;
    }

    @Override
    protected void getHttpServerRouteHandler(Router router) {

    }
}
