package io.github.sinri.drydock.common;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;

import javax.annotation.Nullable;

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
     * Retrieves a KeelMetricRecorder instance associated with the current unit.
     * If no metric recorder is associated, this method returns null.
     *
     * @return the KeelMetricRecorder instance, or null if not available.
     */
    @Nullable
    default KeelMetricRecorder getMetricRecorder() {
        return null;
    }
}
