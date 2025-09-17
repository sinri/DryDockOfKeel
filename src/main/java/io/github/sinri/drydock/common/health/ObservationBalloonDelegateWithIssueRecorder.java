package io.github.sinri.drydock.common.health;

import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.github.sinri.keel.core.helper.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;

/**
 * @since 3.0.1
 */
class ObservationBalloonDelegateWithIssueRecorder implements ObservationBalloonDelegate {
    private final long startTimestamp;
    private final @Nonnull KeelIssueRecorder<HealthMonitorIssueRecord> healthMonitorIssueRecorder;
    private final @Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier;

    public ObservationBalloonDelegateWithIssueRecorder(
            @Nonnull KeelIssueRecorder<HealthMonitorIssueRecord> healthMonitorIssueRecorder,
            @Nullable BiConsumer<MonitorSnapshot, JsonObject> specialSnapshotModifier
    ) {
        this.healthMonitorIssueRecorder = healthMonitorIssueRecorder;
        this.specialSnapshotModifier = specialSnapshotModifier;
        this.startTimestamp = System.currentTimeMillis();
    }

    @Override
    public void recordMonitorSnapshot(@Nonnull MonitorSnapshot monitorSnapshot) {
        this.healthMonitorIssueRecorder.record(t -> {
            final JsonObject snapshot = new JsonObject()
                    .put("survived", System.currentTimeMillis() - startTimestamp)
                    .put("gc", monitorSnapshot.getGCStat().toJsonObject())
                    .put("cpu_time", monitorSnapshot.getCPUTime().toJsonObject())
                    .put("jvm_memory_stat", monitorSnapshot.getJvmMemoryResult().toJsonObject());

            if (this.specialSnapshotModifier != null) {
                this.specialSnapshotModifier.accept(monitorSnapshot, snapshot);
            }

            t.snapshot(snapshot);

            double heapUsage = 1.0 * monitorSnapshot.getJvmMemoryResult().getRuntimeHeapUsedBytes()
                    / monitorSnapshot.getJvmMemoryResult().getRuntimeHeapMaxBytes();
            if (monitorSnapshot.getCPUTime().getCpuUsage() >= 0.50 || heapUsage >= 0.50) {
                t.level(KeelLogLevel.WARNING);
            }
            if (monitorSnapshot.getCPUTime().getCpuUsage() >= 0.75 || heapUsage >= 0.75
                    || monitorSnapshot.getGCStat().getMajorGCCount() > 0) {
                t.level(KeelLogLevel.ERROR);
            }
        });
    }

    @Override
    public long getInterval() {
        return 60_000L;
    }
}
