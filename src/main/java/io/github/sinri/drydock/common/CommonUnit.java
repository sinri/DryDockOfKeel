package io.github.sinri.drydock.common;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public interface CommonUnit {
    KeelEventLogger getLogger();

    /**
     * @since 1.3.4
     */
    KeelIssueRecordCenter getIssueRecordCenter();

    /**
     * @since 1.3.4
     */
    default <T extends KeelIssueRecord<?>> KeelIssueRecorder<T> generateIssueRecorder(@Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder) {
        return getIssueRecordCenter().generateIssueRecorder(topic, issueRecordBuilder);
    }

    default KeelEventLogger generateEventLogger(@Nonnull String topic) {
        return getIssueRecordCenter().generateEventLogger(topic);
    }

    /**
     * @return The instance of KeelMetricRecorder, already started.
     * @since 1.4.2
     */
    @Nonnull
    default KeelMetricRecorder getMetricRecorder() {
        throw new NotImplementedException("By default, Metric Recorder is not provided.");
    }
}
