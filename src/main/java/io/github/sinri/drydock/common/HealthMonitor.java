package io.github.sinri.drydock.common;

import io.github.sinri.drydock.common.logging.adapter.AliyunSLSMetricRecorder;
import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.github.sinri.drydock.common.logging.metric.HealthMonitorMetricRecord;
import io.github.sinri.keel.helper.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.helper.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.0.0
 */
public class HealthMonitor extends KeelVerticleBase<HealthMonitorIssueRecord> {
    private final long startTimestamp;
    protected boolean recordWithMetric = false;
    protected boolean recordWithIssue = false;

    protected AliyunSLSMetricRecorder metricRecorder;

    public HealthMonitor() {
        startTimestamp = System.currentTimeMillis();
    }

    /**
     * 如果需要初始化一些数据记录器等，就在这里。
     * - modify the value of recordWithMetric and recordWithEvent;
     * - initialize metricRecorder (but not start)
     */
    protected void prepare() {

    }

    protected void moreMonitorItems(@Nonnull final JsonObject event, @Nonnull final List<HealthMonitorMetricRecord> metricRecords) {
        // to be overridden
    }

    @Override
    public void start() {
        prepare();

        if (recordWithMetric) {
            if (metricRecorder == null) {
                metricRecorder = new AliyunSLSMetricRecorder();
            }
            metricRecorder.start();
        }

        // since 1.2.10 add a boot record
        getIssueRecorder().notice(r -> r.message("BOOTED"));

        new KeelRuntimeMonitor().startRuntimeMonitor(
                60_000L,
                monitorSnapshot -> {
                    long now = System.currentTimeMillis();

                    JsonObject moreEvent = new JsonObject();
                    List<HealthMonitorMetricRecord> moreMetricRecords = new ArrayList<>();
                    moreMonitorItems(moreEvent, moreMetricRecords);

                    if (recordWithMetric) {
                        handleRecordWithMetric(now, monitorSnapshot, moreMetricRecords);
                    }
                    if (recordWithIssue) {
                        handleRecordWithIssue(monitorSnapshot, moreEvent);
                    }
                }
        );
    }

    protected void handleRecordWithMetric(long now, @Nonnull MonitorSnapshot monitorSnapshot, @Nonnull List<HealthMonitorMetricRecord> moreMetricRecords) {
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asSurvived(
                                System.currentTimeMillis() - startTimestamp
                        )
                        .timestamp(now)
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asHardwareMemoryUsage(
                                1.0 * monitorSnapshot.getJvmMemoryResult().getPhysicalUsedBytes() / monitorSnapshot.getJvmMemoryResult().getPhysicalMaxBytes()
                        )
                        .timestamp(monitorSnapshot.getJvmMemoryResult().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asJvmMemoryUsage(
                                1.0 * monitorSnapshot.getJvmMemoryResult().getRuntimeHeapUsedBytes() / monitorSnapshot.getJvmMemoryResult().getRuntimeHeapMaxBytes()
                        )
                        .timestamp(monitorSnapshot.getJvmMemoryResult().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asJvmHeapMemoryUsedBytes(
                                monitorSnapshot.getJvmMemoryResult().getMxHeapUsedBytes()
                        )
                        .timestamp(monitorSnapshot.getJvmMemoryResult().getStatTime())

        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asJvmNonHeapMemoryUsedBytes(
                                monitorSnapshot.getJvmMemoryResult().getMxNonHeapUsedBytes()
                        )
                        .timestamp(monitorSnapshot.getJvmMemoryResult().getStatTime())

        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asCpuUsage(
                                monitorSnapshot.getCPUTime().getCpuUsage()
                        )
                        .timestamp(monitorSnapshot.getCPUTime().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asMajorGCCount(
                                monitorSnapshot.getGCStat().getOldGCCount()
                        )
                        .timestamp(monitorSnapshot.getGCStat().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asMajorGCTime(
                                monitorSnapshot.getGCStat().getOldGCTime()
                        )
                        .timestamp(monitorSnapshot.getGCStat().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asMinorGCCount(
                                monitorSnapshot.getGCStat().getYoungGCCount()
                        )
                        .timestamp(monitorSnapshot.getGCStat().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asMinorGCTime(
                                monitorSnapshot.getGCStat().getYoungGCTime()
                        )
                        .timestamp(monitorSnapshot.getGCStat().getStatTime())
        );
        moreMetricRecords.forEach(x -> {
            metricRecorder.recordMetric(x);
        });
    }

    protected void handleRecordWithIssue(@Nonnull MonitorSnapshot monitorSnapshot, @Nonnull JsonObject moreEvent) {
        getIssueRecorder().record(t -> {
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
