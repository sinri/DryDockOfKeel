package io.github.sinri.drydock.common.health;

import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.github.sinri.keel.helper.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

/**
 * @since 1.4.0
 */
public class HealthMonitorWithIssueRecorder extends HealthMonitor<JsonObject> {
    private final KeelIssueRecorder<HealthMonitorIssueRecord> healthMonitorIssueRecorder;

    public HealthMonitorWithIssueRecorder(@Nonnull KeelIssueRecorder<HealthMonitorIssueRecord> issueRecorder) {
        this.healthMonitorIssueRecorder = issueRecorder;
    }

    @Override
    protected void prepare() {
    }

    @Override
    protected JsonObject createDraft() {
        return new JsonObject();
    }

    @Override
    protected void moreMonitorItems(@Nonnull JsonObject draft) {

    }

    @Override
    protected void handleRecord(@Nonnull MonitorSnapshot monitorSnapshot, @Nonnull JsonObject moreEvent) {
        this.healthMonitorIssueRecorder.record(t -> {
            var snapshot = new JsonObject()
                    .put("survived", System.currentTimeMillis() - startTimestamp)
                    .put("gc", monitorSnapshot.getGCStat().toJsonObject())
                    .put("cpu_time", monitorSnapshot.getCPUTime().toJsonObject())
                    .put("jvm_memory_stat", monitorSnapshot.getJvmMemoryResult().toJsonObject());
            moreEvent.forEach(entry -> snapshot.put(entry.getKey(), entry.getValue()));
            t.snapshot(snapshot);

            double heapUsage = 1.0 * monitorSnapshot.getJvmMemoryResult().getRuntimeHeapUsedBytes() / monitorSnapshot.getJvmMemoryResult().getRuntimeHeapMaxBytes();
            if (monitorSnapshot.getCPUTime().getCpuUsage() >= 0.50 || heapUsage >= 0.50) {
                t.level(KeelLogLevel.WARNING);
            }
            if (monitorSnapshot.getCPUTime().getCpuUsage() >= 0.75 || heapUsage >= 0.75 || monitorSnapshot.getGCStat().getOldGCCount() > 0) {
                t.level(KeelLogLevel.ERROR);
            }
        });
    }
}
