package io.github.sinri.drydock.common;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public interface CommonUnit {
    KeelEventLogger getUnitLogger();

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
}
