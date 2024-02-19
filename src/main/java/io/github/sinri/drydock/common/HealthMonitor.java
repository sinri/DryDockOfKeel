package io.github.sinri.drydock.common;

import io.github.sinri.keel.helper.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.logger.metric.KeelMetricRecord;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.0.0
 */
public class HealthMonitor extends KeelVerticleBase {
    private final long startTimestamp;
    protected boolean recordWithMetric = false;
    protected boolean recordWithEvent = true;

    private AliyunSLSMetricRecorder metricRecorder;

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
        getLogger().notice("BOOTED");

        new KeelRuntimeMonitor().startRuntimeMonitor(
                60_000L,
                monitorSnapshot -> {
                    long now = System.currentTimeMillis();

                    JsonObject moreEvent = new JsonObject();
                    List<HealthMonitorMetricRecord> moreMetricRecords = new ArrayList<>();
                    moreMonitorItems(moreEvent, moreMetricRecords);

                    if (recordWithMetric) {
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

                    if (recordWithEvent) {
                        getLogger().info(eventLog -> {
                            var snapshot = new JsonObject()
                                    .put("survived", System.currentTimeMillis() - startTimestamp)
                                    .put("gc", monitorSnapshot.getGCStat().toJsonObject())
                                    .put("cpu_time", monitorSnapshot.getCPUTime().toJsonObject())
                                    .put("jvm_memory_stat", monitorSnapshot.getJvmMemoryResult().toJsonObject())
                                    // todo remove
                                    .put("hardware_memory", monitorSnapshot.getHardwareMemory().toJsonObject())
                                    .put("jvm_memory", monitorSnapshot.getJvmMemory().toJsonObject())
                                    .put("jvm_heap_memory", monitorSnapshot.getJvmHeapMemory().toJsonObject());
                            moreEvent.forEach(entry -> snapshot.put(entry.getKey(), entry.getValue()));
                            eventLog.put("snapshot", snapshot);
                        });
                    }
                }
        );
    }

    public static class HealthMonitorMetricRecord extends KeelMetricRecord {

        public HealthMonitorMetricRecord(String metricName, double value) {
            super(metricName, value);
            this.topic(AliyunSLSMetricRecorder.TopicHealthMonitorMetric);
        }

        public static HealthMonitorMetricRecord asSurvived(long value) {
            return new HealthMonitorMetricRecord("survived", value);
        }

        public static HealthMonitorMetricRecord asMinorGCCount(long value) {
            return new HealthMonitorMetricRecord("minor_gc_count", value);
        }

        public static HealthMonitorMetricRecord asMinorGCTime(long value) {
            return new HealthMonitorMetricRecord("minor_gc_time", value);
        }

        public static HealthMonitorMetricRecord asMajorGCCount(long value) {
            return new HealthMonitorMetricRecord("major_gc_count", value);
        }

        public static HealthMonitorMetricRecord asMajorGCTime(long value) {
            return new HealthMonitorMetricRecord("major_gc_time", value);
        }

        public static HealthMonitorMetricRecord asCpuUsage(double value) {
            return new HealthMonitorMetricRecord("cpu_usage", value);
        }

        public static HealthMonitorMetricRecord asHardwareMemoryUsage(double value) {
            return new HealthMonitorMetricRecord("hardware_memory_usage", value);
        }

        public static HealthMonitorMetricRecord asJvmMemoryUsage(double value) {
            return new HealthMonitorMetricRecord("jvm_memory_usage", value);
        }

        public static HealthMonitorMetricRecord asJvmHeapMemoryUsedBytes(long value) {
            return new HealthMonitorMetricRecord("jvm_heap_memory_used_bytes", value);
        }

        public static HealthMonitorMetricRecord asJvmNonHeapMemoryUsedBytes(long value) {
            return new HealthMonitorMetricRecord("jvm_non_heap_memory_used_bytes", value);
        }
    }
}
