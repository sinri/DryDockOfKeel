package io.github.sinri.drydock.plugin.aliyun.sls.writer;

import io.github.sinri.drydock.plugin.aliyun.sls.writer.entity.LogGroup;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.entity.LogItem;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.logger.metric.KeelMetricRecord;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.0
 */
public class AliyunSLSMetricRecorder extends KeelMetricRecorder {
    private final String source;
    private final AliyunSlsConfigElement aliyunSlsConfig;
    private final AliyunSLSLogPutter logPutter;

    public AliyunSLSMetricRecorder() {
        KeelConfigElement extract = Keel.getConfiguration().extract("aliyun", "sls_metric");
        if (extract == null) {
            KeelConfigElement temp = new KeelConfigElement("sls_metric");
            temp.ensureChild("disabled").setValue("YES");
            aliyunSlsConfig = new AliyunSlsConfigElement(temp);
        } else {
            aliyunSlsConfig = new AliyunSlsConfigElement(extract);
        }

        this.source = AliyunSLSLogPutter.buildSource(aliyunSlsConfig.getSource());
        this.logPutter = this.buildProducer();
    }

    @Nullable
    private AliyunSLSLogPutter buildProducer() {
        if (!aliyunSlsConfig.isDisabled()) {
            return new AliyunSLSLogPutter(
                    aliyunSlsConfig.getAccessKeyId(),
                    aliyunSlsConfig.getAccessKeySecret(),
                    aliyunSlsConfig.getEndpoint()
            );
        } else {
            return null;
        }
    }

    @Override
    protected Future<Void> handleForTopic(String topic, List<KeelMetricRecord> buffer) {
        if (buffer.isEmpty()) return Future.succeededFuture();

        if (aliyunSlsConfig.isDisabled()) {
            buffer.forEach(item -> Keel.getLogger().debug(log -> {
                log.classification("TOPIC:" + topic);
                log.context(item.toJsonObject());
            }));
            return Future.succeededFuture();
        }

        LogGroup logGroup = new LogGroup(topic, source);
        buffer.forEach(metricRecord -> {
            var logItem = buildLogItem(metricRecord);
            logGroup.addLogItem(logItem);
        });

        return logPutter.putLogs(
                aliyunSlsConfig.getProject(),
                aliyunSlsConfig.getLogstore(),
                logGroup
        );
    }

    /**
     * metricName: the metric name, eg: http_requests_count
     * labels:     labels map, eg: {'idc': 'idc1', 'ip': '192.0.2.0', 'hostname': 'appserver1'}
     * value:      double value, eg: 1.234
     *
     * @return LogItem
     */
    private LogItem buildLogItem(@Nonnull KeelMetricRecord metricRecord) {
        String labelsKey = "__labels__";
        String timeKey = "__time_nano__";
        String valueKey = "__value__";
        String nameKey = "__name__";

        int timeInSec = (int) (metricRecord.timestamp() / 1000);
        LogItem logItem = new LogItem(timeInSec);
        logItem.addContent(timeKey, metricRecord.timestamp() + "000");
        logItem.addContent(nameKey, metricRecord.metricName());
        logItem.addContent(valueKey, String.valueOf(metricRecord.value()));

        // 按照字典序对labels排序, 如果您的labels已排序, 请忽略此步骤。
        metricRecord.label("source", this.source);
        TreeMap<String, String> sortedLabels = new TreeMap<>(metricRecord.labels());
        StringBuilder labelsBuilder = new StringBuilder();

        boolean hasPrev = false;
        for (Map.Entry<String, String> entry : sortedLabels.entrySet()) {
            if (hasPrev) {
                labelsBuilder.append("|");
            }
            hasPrev = true;
            labelsBuilder.append(entry.getKey());
            labelsBuilder.append("#$#");
            labelsBuilder.append(entry.getValue());
        }
        logItem.addContent(labelsKey, labelsBuilder.toString());
        return logItem;
    }
}
