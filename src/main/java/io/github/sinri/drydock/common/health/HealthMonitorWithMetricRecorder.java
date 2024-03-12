package io.github.sinri.drydock.common.health;

import io.github.sinri.drydock.common.logging.metric.HealthMonitorMetricRecord;
import io.github.sinri.keel.helper.runtime.MonitorSnapshot;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.4.0
 */
public class HealthMonitorWithMetricRecorder extends HealthMonitor<List<HealthMonitorMetricRecord>> {
    //protected AliyunSLSMetricRecorder metricRecorder;
    protected KeelMetricRecorder metricRecorder;

    public HealthMonitorWithMetricRecorder(@Nonnull KeelMetricRecorder metricRecorder) {
        this.metricRecorder = metricRecorder;
    }

    @Override
    protected void prepare() {
        metricRecorder.start();
    }

    @Override
    protected void moreMonitorItems(@Nonnull List<HealthMonitorMetricRecord> draft) {

    }

    @Override
    protected List<HealthMonitorMetricRecord> createDraft() {
        return new ArrayList<>();
    }

    @Override
    protected void handleRecord(@Nonnull MonitorSnapshot monitorSnapshot, @Nonnull List<HealthMonitorMetricRecord> moreMetricRecords) {
        long now = System.currentTimeMillis();

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

    /**
     * @since 1.4.2
     */
    @Override
    protected long interval() {
        return 10_000L;
    }
}
