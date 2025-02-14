package io.github.sinri.drydock.aviation.aircraft;

import io.github.sinri.drydock.aviation.carrier.AircraftCarrierDeck;
import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.keel.core.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.core.servant.queue.KeelQueueSignalReader;
import io.github.sinri.keel.core.servant.queue.QueueManageIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

import javax.annotation.Nonnull;

/**
 * @since 1.5.0 Technical Preview 和AircraftCarrierDeck配合使用的舰载无人机类，用于按照战术设计按需执行任务，可基于弹性限度密集出动。
 */
public abstract class Drone extends Biplane implements QueueMixin, KeelQueueSignalReader, KeelQueueNextTaskSeeker {
    private final KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder;

    public Drone(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
        queueManageIssueRecorder = getIssueRecordCenter()
                .generateIssueRecorder(QueueManageIssueRecord.TopicQueue, QueueManageIssueRecord::new);
    }

    @Override
    public KeelQueueSignalReader buildSignalReader(KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder) {
        return this;
    }

    @Override
    public final KeelQueueNextTaskSeeker buildQueueNextTaskSeeker(KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder) {
        return this;
    }

    /**
     * @since 2.0.4
     */
    public KeelIssueRecorder<QueueManageIssueRecord> getQueueManageIssueRecorder() {
        return queueManageIssueRecorder;
    }
}
