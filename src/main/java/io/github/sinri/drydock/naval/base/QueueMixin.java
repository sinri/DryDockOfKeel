package io.github.sinri.drydock.naval.base;

import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.servant.queue.QueueWorkerPoolManager;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.1.0
 */
public interface QueueMixin extends Boat {
    default KeelQueue buildQueue() {
        var queue = new KeelQueue() {
            @Override
            protected @NotNull KeelQueueNextTaskSeeker getNextTaskSeeker() {
                return buildQueueNextTaskSeeker();
            }

            @Override
            protected @NotNull SignalReader getSignalReader() {
                return buildSignalReader();
            }

            @NotNull
            @Override
            protected QueueWorkerPoolManager getQueueWorkerPoolManager() {
                var x = configuredQueueWorkerPoolSize();
                return new QueueWorkerPoolManager(x);
            }
        };
        queue.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicQueue, null));
        return queue;
    }

    /**
     * @return zero for unlimited.
     */
    default int configuredQueueWorkerPoolSize() {
        return 0;
    }

    KeelQueue.SignalReader buildSignalReader();

    KeelQueueNextTaskSeeker buildQueueNextTaskSeeker();

    default Future<Void> loadQueue() {
        return Future.succeededFuture(this.buildQueue())
                .compose(queue -> {
                    if (queue == null) return Future.succeededFuture();
                    return queue.deployMe(new DeploymentOptions().setWorker(true))
                            .onFailure(throwable -> {
                                getNavalLogger().exception(throwable, "load queue failed");
                            })
                            .compose(deploymentId -> {
                                getNavalLogger().info("load queue: " + deploymentId);
                                return Future.succeededFuture();
                            });
                });
    }
}
