package io.github.sinri.drydock.common.logging.metric;

import io.github.sinri.drydock.common.logging.adapter.AliyunSLSMetricRecorder;
import io.github.sinri.keel.logger.metric.KeelMetricRecord;

public class HealthMonitorMetricRecord extends KeelMetricRecord {

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
