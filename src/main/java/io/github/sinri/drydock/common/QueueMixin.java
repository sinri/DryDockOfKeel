package io.github.sinri.drydock.common;

import io.github.sinri.keel.core.servant.queue.KeelQueue;
import io.github.sinri.keel.core.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.core.servant.queue.KeelQueueSignalReader;
import io.github.sinri.keel.core.servant.queue.QueueWorkerPoolManager;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;

/**
 * The mixin interface for a unit for Queue.
 *
 * @since 1.1.0
 */
public interface QueueMixin extends CommonUnit {
    /**
     * Build a KeelQueue instance.
     * A default implementation is provided.
     *
     * @return The built KeelQueue instance.
     */
    default KeelQueue buildQueue() {
        KeelIssueRecordCenter issueRecordCenter = this.getIssueRecordCenter();
        return new KeelQueue() {

            @Override
            protected KeelIssueRecordCenter getIssueRecordCenter() {
                return issueRecordCenter;
            }

            @Override
            protected @Nonnull KeelQueueNextTaskSeeker getNextTaskSeeker() {
                return buildQueueNextTaskSeeker();
            }

            @Override
            protected @Nonnull KeelQueueSignalReader getSignalReader() {
                return buildSignalReader();
            }

            @Nonnull
            @Override
            protected QueueWorkerPoolManager getQueueWorkerPoolManager() {
                var x = configuredQueueWorkerPoolSize();
                return new QueueWorkerPoolManager(x);
            }
        };
    }

    /**
     * @return The worker pool size; return zero for an unlimited pool.
     */
    default int configuredQueueWorkerPoolSize() {
        return 0;
    }

    /**
     * @return The built signal reader.
     */
    KeelQueueSignalReader buildSignalReader();

    /**
     * @return The built next task seeker.
     */
    KeelQueueNextTaskSeeker buildQueueNextTaskSeeker();

    /**
     * Try to build a KeelQueue instance and start it up.
     * Do nothing if this ability is not required.
     *
     * @return a future as all work scheduled.
     */
    default Future<Void> loadQueue() {
        return Future.succeededFuture(this.buildQueue())
                .compose(queue -> {
                    if (queue == null) return Future.succeededFuture();
                    return this.beforeLoadingQueue()
                            .compose(v -> {
                                return queue.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                                        .onFailure(throwable -> {
                                            getLogger().exception(throwable, "Failed to load queue");
                                        })
                                        .compose(deploymentId -> {
                                            getLogger().info("Loaded queue: " + deploymentId);
                                            return Future.succeededFuture();
                                        });
                            });
                });
    }

    /**
     * Execute before `io.github.sinri.drydock.common.QueueMixin#loadQueue()`,
     * to clean up the left RUNNING tasks (let them ERROR).
     *
     * @since 1.5.8
     */
    default Future<Void> beforeLoadingQueue() {
        return Future.succeededFuture();
    }
}
