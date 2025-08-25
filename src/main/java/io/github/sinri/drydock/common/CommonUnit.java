package io.github.sinri.drydock.common;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * The interface to define the common abilities of a unit.
 */
public interface CommonUnit {

    /**
     * @return a KeelIssueRecordCenter instance maintained by the implementation unit.
     * @since 1.3.4
     */
    KeelIssueRecordCenter getIssueRecordCenter();

    /**
     * @param <T>                The actual implementation class of KeelIssueRecord.
     * @param topic              The topic of the generated KeelIssueRecorder instance.
     * @param issueRecordBuilder A supplier to generate an instance of T.
     * @return generated a new KeelIssueRecorder instance following the format defined by T, by default with the
     *         KeelIssueRecordCenter instance maintained by this unit.
     * @since 1.3.4
     */
    default <T extends KeelIssueRecord<T>> KeelIssueRecorder<T> generateIssueRecorder(
            @Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder
    ) {
        return getIssueRecordCenter().generateIssueRecorder(topic, issueRecordBuilder);
    }

    /**
     * @since 2.0.3
     */
    default KeelIssueRecorder<KeelEventLog> generateIssueRecorder(@Nonnull String topic) {
        return getIssueRecordCenter().generateIssueRecorder(topic, KeelEventLog::new);
    }

    /**
     * @return An instance of KeelMetricRecorder, which should be started already. It should not return null, raise
     *         exception instead then.
     * @since 1.4.2
     */
    @Nonnull
    default KeelMetricRecorder getMetricRecorder() {
        throw new RuntimeException("By default, Metric Recorder is not provided.");
    }
}
