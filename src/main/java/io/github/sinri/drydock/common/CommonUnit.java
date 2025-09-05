package io.github.sinri.drydock.common;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;

import javax.annotation.Nullable;

/**
 * The interface to define the common abilities of a unit.
 */
public interface CommonUnit {

    /**
     * @return the {@link KeelIssueRecordCenter} instance maintained by the implementation.
     * @since 1.3.4
     */
    KeelIssueRecordCenter getIssueRecordCenter();


    /**
     * Retrieves the {@link KeelMetricRecorder} instance associated with the current unit,
     * if existed, to record metrics.
     * <p>
     * If no metric recorder is associated, this method returns null.
     *
     * @return the {@link KeelMetricRecorder} instance, or null if not maintained.
     */
    @Nullable
    default KeelMetricRecorder getMetricRecorder() {
        return null;
    }
}
