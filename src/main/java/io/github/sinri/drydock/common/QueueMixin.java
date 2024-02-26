package io.github.sinri.drydock.common;

import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.servant.queue.QueueManageIssueRecord;
import io.github.sinri.keel.servant.queue.QueueWorkerPoolManager;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;

/**
 * @since 1.1.0
 */
public interface QueueMixin extends CommonUnit {
    default KeelQueue buildQueue() {
        KeelIssueRecorder<QueueManageIssueRecord> issueRecorder = this.generateIssueRecorder(QueueManageIssueRecord.TopicQueue, QueueManageIssueRecord::new);
        return new KeelQueue() {
            @Nonnull
            @Override
            protected KeelIssueRecorder<QueueManageIssueRecord> buildIssueRecorder() {
                return issueRecorder;
            }

            @Override
            protected @Nonnull KeelQueueNextTaskSeeker getNextTaskSeeker() {
                return buildQueueNextTaskSeeker();
            }

            @Override
            protected @Nonnull SignalReader getSignalReader() {
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
                    return queue.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                            .onFailure(throwable -> {
                                getUnitLogger().exception(throwable, "Failed to load queue");
                            })
                            .compose(deploymentId -> {
                                getUnitLogger().info("Loaded queue: " + deploymentId);
                                return Future.succeededFuture();
                            });
                });
    }
}
