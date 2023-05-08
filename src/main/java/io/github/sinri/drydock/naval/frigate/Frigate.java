package io.github.sinri.drydock.naval.frigate;

import io.github.sinri.drydock.naval.caravel.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.naval.caravel.Caravel;
import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.Collection;

/**
 * @since 1.0.1
 */
public abstract class Frigate extends Caravel {

    public Frigate() {
        super();
    }

    @Override
    final protected void launchAsCaravel() {
        KeelQueue queue = this.buildQueue();
        if (queue != null) {
            queue.deployMe(new DeploymentOptions().setWorker(true));
        }

        KeelSundial sundial = this.buildSundial();
        if (sundial != null) {
            sundial.deployMe(new DeploymentOptions().setWorker(true));
        }

        launchAsFrigate();
    }

    abstract protected void launchAsFrigate();

    /**
     * @return an instance of KeelSundial, or null to turn off sundial
     */
    protected KeelSundial buildSundial() {
        var sundial = new KeelSundial() {
            @Override
            protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                return fetchSundialPlans();
            }
        };
        sundial.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicSundial, null));
        return sundial;
    }

    /**
     * If sundial is turned on, it would be called to refresh sundial plans regularly.
     *
     * @return a future of: Collection of KeelSundialPlan, or null for NOT MODIFIED
     */
    protected Future<Collection<KeelSundialPlan>> fetchSundialPlans() {
        // NOT MODIFIED
        return null;
    }

    /**
     * You must override buildQueueNextTaskSeeker and buildSignalReader if queue is turned on.
     *
     * @return an instance of KeelQueue, or null to turn off queue
     */
    protected KeelQueue buildQueue() {
        var queue = new KeelQueue() {
            @Override
            protected KeelQueueNextTaskSeeker getNextTaskSeeker() {
                return buildQueueNextTaskSeeker();
            }

            @Override
            protected SignalReader getSignalReader() {
                return buildSignalReader();
            }
        };
        queue.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicQueue, null));
        return queue;
    }

    /**
     * @return an instance of KeelQueueNextTaskSeeker
     */
    protected KeelQueueNextTaskSeeker buildQueueNextTaskSeeker() {
        return () -> Future.failedFuture(new Exception("buildQueueNextTaskSeeker TO BE OVERRODE"));
    }

    protected KeelQueue.SignalReader buildSignalReader() {
        return new KeelQueue.SignalReader() {
            @Override
            public Future<KeelQueue.QueueSignal> readSignal() {
                return Future.failedFuture(new Exception("readSignal TO BE OVERRODE"));
            }
        };
    }
}
