package io.github.sinri.drydock.common.health;

import io.github.sinri.drydock.common.logging.metric.HealthMonitorMetricRecord;
import io.github.sinri.keel.core.helper.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * @since 3.0.1
 */
class ObservationBalloonDelegateWithMetricRecorder implements ObservationBalloonDelegate {
    private final long startTimestamp;
    private final @Nullable Function<MonitorSnapshot, List<HealthMonitorMetricRecord>> specialSnapshotModifier;
    protected @Nonnull KeelMetricRecorder metricRecorder;

    public ObservationBalloonDelegateWithMetricRecorder(@Nonnull KeelMetricRecorder metricRecorder, @Nullable Function<MonitorSnapshot, List<HealthMonitorMetricRecord>> specialSnapshotModifier) {
        this.specialSnapshotModifier = specialSnapshotModifier;
        this.metricRecorder = metricRecorder;
        this.startTimestamp = System.currentTimeMillis();
    }

    @Override
    public void recordMonitorSnapshot(@Nonnull MonitorSnapshot monitorSnapshot) {
        long now = System.currentTimeMillis();

        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asSurvived(
                                                 System.currentTimeMillis() - startTimestamp
                                         )
                                         .timestamp(now)
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asHardwareMemoryUsage(
                                                 1.0 * monitorSnapshot.getJvmMemoryResult()
                                                                      .getPhysicalUsedBytes() / monitorSnapshot.getJvmMemoryResult()
                                                                                                               .getPhysicalMaxBytes()
                                         )
                                         .timestamp(monitorSnapshot.getJvmMemoryResult().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asJvmMemoryUsage(
                                                 1.0 * monitorSnapshot.getJvmMemoryResult()
                                                                      .getRuntimeHeapUsedBytes() / monitorSnapshot.getJvmMemoryResult()
                                                                                                                  .getRuntimeHeapMaxBytes()
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
                                                 monitorSnapshot.getGCStat().getMajorGCCount()
                                         )
                                         .timestamp(monitorSnapshot.getGCStat().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asMajorGCTime(
                                                 monitorSnapshot.getGCStat().getMajorGCTime()
                                         )
                                         .timestamp(monitorSnapshot.getGCStat().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asMinorGCCount(
                                                 monitorSnapshot.getGCStat().getMinorGCCount()
                                         )
                                         .timestamp(monitorSnapshot.getGCStat().getStatTime())
        );
        metricRecorder.recordMetric(
                HealthMonitorMetricRecord.asMinorGCTime(
                                                 monitorSnapshot.getGCStat().getMinorGCTime()
                                         )
                                         .timestamp(monitorSnapshot.getGCStat().getStatTime())
        );
        if (this.specialSnapshotModifier != null) {
            List<HealthMonitorMetricRecord> list = this.specialSnapshotModifier.apply(monitorSnapshot);
            if (list != null) {
                list.forEach(metricRecorder::recordMetric);
            }
        }
    }

    @Override
    public long getInterval() {
        return 10_000L;
    }
}
