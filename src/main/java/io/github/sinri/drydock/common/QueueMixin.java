package io.github.sinri.drydock.common;

import io.github.sinri.keel.core.servant.queue.*;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
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
     * Build a KeelQueue instance. A default implementation is provided.
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
                KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder = getQueueManageIssueRecorder();
                return buildQueueNextTaskSeeker(queueManageIssueRecorder);
            }

            @Override
            protected @Nonnull KeelQueueSignalReader getSignalReader() {
                KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder = getQueueManageIssueRecorder();
                return buildSignalReader(queueManageIssueRecorder);
            }

            @Nonnull
            @Override
            protected QueueWorkerPoolManager getQueueWorkerPoolManager() {
                KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder = getQueueManageIssueRecorder();
                var x = configuredQueueWorkerPoolSize(queueManageIssueRecorder);
                return new QueueWorkerPoolManager(x);
            }
        };
    }

    /**
     * @param queueManageIssueRecorder as of 2.0.4
     * @return The worker pool size; return zero for an unlimited pool.
     */
    default int configuredQueueWorkerPoolSize(KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder) {
        return 0;
    }

    /**
     * @param queueManageIssueRecorder as of 2.0.4
     * @return The built signal reader.
     */
    KeelQueueSignalReader buildSignalReader(KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder);

    /**
     * @param queueManageIssueRecorder as of 2.0.4
     * @return The built next task seeker.
     */
    KeelQueueNextTaskSeeker buildQueueNextTaskSeeker(KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder);

    /**
     * Try to build a KeelQueue instance and start it up. Do nothing if this ability is not required.
     *
     * @return a future as all work scheduled.
     */
    default Future<String> loadQueue() {
        return Future.succeededFuture(this.buildQueue())
                     .compose(queue -> {
                         if (queue == null) return Future.succeededFuture();
                         return this.beforeLoadingQueue()
                                    .compose(v -> {
                                        return queue.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
                                    });
                     });
    }

    /**
     * Execute before `io.github.sinri.drydock.common.QueueMixin#loadQueue()`, to clean up the left RUNNING tasks (let
     * them ERROR).
     *
     * @since 1.5.8
     */
    default Future<Void> beforeLoadingQueue() {
        return Future.succeededFuture();
    }
}
