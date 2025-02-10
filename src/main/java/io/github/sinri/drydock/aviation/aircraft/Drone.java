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
    private final KeelIssueRecorder<QueueManageIssueRecord> queueManageIssueRecordKeelIssueRecorder;

    public Drone(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
        queueManageIssueRecordKeelIssueRecorder = getIssueRecordCenter()
                .generateIssueRecorder(QueueManageIssueRecord.TopicQueue, QueueManageIssueRecord::new);
    }

    @Override
    public final KeelQueueSignalReader buildSignalReader() {
        return this;
    }

    @Override
    public final KeelQueueNextTaskSeeker buildQueueNextTaskSeeker() {
        return this;
    }

    @Override
    public KeelIssueRecorder<QueueManageIssueRecord> getIssueRecorder() {
        return queueManageIssueRecordKeelIssueRecorder;
    }
}
