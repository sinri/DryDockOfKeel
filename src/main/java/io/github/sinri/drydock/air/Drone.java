package io.github.sinri.drydock.air;

import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.core.servant.queue.KeelQueueSignalReader;
import io.github.sinri.keel.core.servant.queue.QueueManageIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

import javax.annotation.Nonnull;

/**
 * @since 1.5.0 Technical Preview
 */
@TechnicalPreview(since = "1.5.0")
public abstract class Drone extends Biplane implements QueueMixin, KeelQueueSignalReader, KeelQueueNextTaskSeeker {
    public Drone(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
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
        return getIssueRecordCenter().generateIssueRecorder(QueueManageIssueRecord.TopicQueue, QueueManageIssueRecord::new);
    }
}
