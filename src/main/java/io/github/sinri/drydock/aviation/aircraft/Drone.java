package io.github.sinri.drydock.aviation.aircraft;

import io.github.sinri.drydock.aviation.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.servant.queue.*;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;

/**
 * 和AircraftCarrierDeck配合使用的舰载无人机类，用于按照战术设计按需执行任务，可基于弹性限度密集出动。
 *
 * @since 1.5.0
 */
public abstract class Drone extends Biplane {
    private final KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder;

    public Drone(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
        queueManageIssueRecorder = getIssueRecordCenter()
                .generateIssueRecorder(QueueManageIssueRecord.TopicQueue, QueueManageIssueRecord::new);
    }

    /**
     * @since 2.0.4
     */
    protected KeelIssueRecorder<QueueManageIssueRecord> getQueueManageIssueRecorder() {
        return queueManageIssueRecorder;
    }

    /**
     * Build a KeelQueue instance. A default implementation is provided.
     *
     * @return The built KeelQueue instance.
     */
    protected KeelQueue buildQueue() {
        KeelIssueRecordCenter issueRecordCenter = this.getIssueRecordCenter();
        var that = this;
        return new KeelQueue() {

            @Override
            public Future<KeelQueueSignal> readSignal() {
                return that.readSignal();
            }

            @Override
            public Future<KeelQueueTask> seekNextTask() {
                return that.seekNextTask();
            }

            @Override
            protected KeelIssueRecordCenter getIssueRecordCenter() {
                return issueRecordCenter;
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
    protected int configuredQueueWorkerPoolSize() {
        return 0;
    }

    /**
     * @since 2.0.5
     */
    abstract protected Future<KeelQueueSignal> readSignal();

    /**
     * @since 2.0.5
     */
    abstract protected Future<KeelQueueTask> seekNextTask();

    /**
     * Attempts to load and initialize a KeelQueue instance asynchronously.
     *
     * @return a future representing the asynchronous result of the KeelQueue deployment process.
     * @deprecated The operation is marked as deprecated since version 2.1.1 and may be removed in future releases.
     *         Use {@link #load()} instead.
     */
    @Deprecated(since = "2.1.1")
    public final Future<String> loadQueue() {
        return load();
    }

    /**
     * Try to build a KeelQueue instance and start it up. Do nothing if this ability is not required.
     *
     * @return a future as all work scheduled.
     */
    @Override
    public Future<String> load() {
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
    protected Future<Void> beforeLoadingQueue() {
        return Future.succeededFuture();
    }
}
