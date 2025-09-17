package io.github.sinri.drydock.common.health;

import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.github.sinri.drydock.common.logging.metric.HealthMonitorMetricRecord;
import io.github.sinri.keel.core.helper.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @since 3.0.1
 */
public interface ObservationBalloonDelegate {
    static ObservationBalloonDelegate createWithIssueRecorder(
            @Nonnull KeelIssueRecorder<HealthMonitorIssueRecord> healthMonitorIssueRecorder,
            @Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier
    ) {
        return new ObservationBalloonDelegateWithIssueRecorder(healthMonitorIssueRecorder, specialSnapshotModifier);
    }

    static ObservationBalloonDelegate createWithIssueRecorder(
            @Nonnull KeelIssueRecorder<HealthMonitorIssueRecord> healthMonitorIssueRecorder
    ) {
        return new ObservationBalloonDelegateWithIssueRecorder(healthMonitorIssueRecorder, null);
    }

    static ObservationBalloonDelegate createWithMetricRecorder(@Nonnull KeelMetricRecorder metricRecorder, @Nullable Function<MonitorSnapshot, List<HealthMonitorMetricRecord>> specialSnapshotModifier) {
        return new ObservationBalloonDelegateWithMetricRecorder(metricRecorder, specialSnapshotModifier);
    }

    static ObservationBalloonDelegate createWithMetricRecorder(@Nonnull KeelMetricRecorder metricRecorder) {
        return new ObservationBalloonDelegateWithMetricRecorder(metricRecorder, null);
    }

    long getInterval();

    void recordMonitorSnapshot(@Nonnull MonitorSnapshot monitorSnapshot);
}
