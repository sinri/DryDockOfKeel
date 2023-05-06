package io.github.sinri.drydock.naval.frigate;

import io.github.sinri.drydock.naval.caravel.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.naval.caravel.Caravel;
import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.Collection;

/**
 * @since 1.0.1
 */
public abstract class Frigate extends Caravel {
    private final boolean queueEnabled;
    private final boolean sundialEnabled;

    public Frigate(boolean sundialEnabled, boolean queueEnabled) {
        this.sundialEnabled = sundialEnabled;
        this.queueEnabled = queueEnabled;
    }

    @Override
    final protected void launchAsCaravel() {
        if (queueEnabled) {
            KeelQueue queue = this.buildQueue();
            queue.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicQueue, null));
            queue.deployMe(new DeploymentOptions().setWorker(true));
        }
        if (sundialEnabled) {
            KeelSundial sundial = new KeelSundial() {
                @Override
                protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                    return fetchSundialPlans();
                }
            };
            sundial.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicSundial, null));
            sundial.deployMe(new DeploymentOptions().setWorker(true));
        }
    }

    abstract protected void launchAsFrigate();

    protected Future<Collection<KeelSundialPlan>> fetchSundialPlans() {
        // NOT MODIFIED
        return null;
    }

    abstract protected KeelQueue buildQueue();
}
