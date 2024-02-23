package io.github.sinri.drydock.common.logging.adapter;

import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.log.common.LogItem;
import io.github.sinri.keel.facade.KeelConfiguration;
import io.github.sinri.keel.logger.metric.KeelMetricRecord;
import io.github.sinri.keel.logger.metric.KeelMetricRecorder;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nullable;
import java.util.*;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public class AliyunSLSMetricRecorder extends KeelMetricRecorder {
    public static final String TopicHealthMonitorMetric = "HealthMonitorMetric";

    private static boolean disabled;
    private final String project;
    private final String logstore;
    private final String source;
    private final Producer producer;
    private final String endpoint;

    public AliyunSLSMetricRecorder() {
        super();

        KeelConfiguration aliyunSlsMetricConfig = Keel.getConfiguration().extract("aliyun", "sls_metric");
        Objects.requireNonNull(aliyunSlsMetricConfig);

        String disabledString = aliyunSlsMetricConfig.readString("disabled");
        disabled = ("YES".equals(disabledString));

        this.project = aliyunSlsMetricConfig.readString("project");
        this.logstore = aliyunSlsMetricConfig.readString("logstore");
        this.endpoint = aliyunSlsMetricConfig.readString("endpoint");
        this.source = buildSource(aliyunSlsMetricConfig.readString("source"));

        if (!disabled) {
            String accessKeyId = aliyunSlsMetricConfig.readString("accessKeyId");
            String accessKeySecret = aliyunSlsMetricConfig.readString("accessKeySecret");

            producer = new LogProducer(new ProducerConfig());
            Objects.requireNonNull(project);
            Objects.requireNonNull(endpoint);
            Objects.requireNonNull(accessKeyId);
            Objects.requireNonNull(accessKeySecret);
            producer.putProjectConfig(new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret));

            //KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()).info("Aliyun SLS Producer relied aliyunSlsConfig: " + aliyunSlsConfig.toJsonObject());
        } else {
            producer = null;
        }
    }

    /**
     * Build source from configuration.
     * Source Expression should be:
     * - EMPTY/BLANK STRING or NULL: use SLS default source generation;
     * - A TEMPLATED STRING
     * --- Rule 1: Replace [IP] to local address;
     *
     * @since 1.2.7 add source template and rule 1.
     */
    private static String buildSource(@Nullable String configuredSourceExpression) {
        if (configuredSourceExpression == null || configuredSourceExpression.isBlank()) {
            return "";
        }
        // Rule 1: Replace [IP] to local address
        String localHostAddress = KeelHelpers.netHelper().getLocalHostAddress();
        if (localHostAddress == null) {
            Keel.getLogger().warning("Could not get local host address for SLS source!");
            return "";
        }
        return configuredSourceExpression.replaceAll("\\[IP]", localHostAddress);
    }

    @Override
    protected Future<Void> handleForTopic(String topic, List<KeelMetricRecord> list) {
        if (list.isEmpty()) return Future.succeededFuture();

//        System.out.println("dealWithLogsForOneTopic<"+topic+"> handling "+list.size()+" logs");

        if (disabled) {
            list.forEach(item -> {
                Keel.getLogger().getIssueRecorder().record(log -> {
                    log.topic(item.topic());
                    log.context(item.toJsonObject());
                });
            });
            return Future.succeededFuture();
        }

        Promise<Void> promise = Promise.promise();
        try {
            List<LogItem> logItems = new ArrayList<>();
            list.forEach(metricRecord -> {
                logItems.add(buildLogItem(metricRecord));
            });
            producer.send(project, logstore, topic, source, logItems, result -> {
                if (!result.isSuccessful()) {
                    Keel.getLogger().getIssueRecorder().error(eventLog -> eventLog
                            .classification(getClass().getName())
                            .message("Producer Send Error: " + result)
                    );
                }
                promise.complete(null);
            });
        } catch (Throwable e) {
            Keel.getLogger().getIssueRecorder().exception(e, r -> r
                    .classification(getClass().getName())
                    .message("Aliyun SLS Producer Exception")
            );
            promise.fail(e);
        }

        return promise.future();
    }

    /**
     * metricName: the metric name, eg: http_requests_count
     * labels:     labels map, eg: {'idc': 'idc1', 'ip': '192.0.2.0', 'hostname': 'appserver1'}
     * value:      double value, eg: 1.234
     *
     * @return LogItem
     */
    private LogItem buildLogItem(
            KeelMetricRecord metricRecord
            //        String metricName, Map<String, String> labels, double value
    ) {
        String labelsKey = "__labels__";
        String timeKey = "__time_nano__";
        String valueKey = "__value__";
        String nameKey = "__name__";
        LogItem logItem = new LogItem();
        int timeInSec = (int) (metricRecord.timestamp() / 1000);
        logItem.SetTime(timeInSec);
        logItem.PushBack(timeKey, metricRecord.timestamp() + "000");
        logItem.PushBack(nameKey, metricRecord.metricName());
        logItem.PushBack(valueKey, String.valueOf(metricRecord.value()));

        // 按照字典序对labels排序, 如果您的labels已排序, 请忽略此步骤。
        metricRecord.label("source", this.source);
        TreeMap<String, String> sortedLabels = new TreeMap<String, String>(metricRecord.labels());
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
        logItem.PushBack(labelsKey, labelsBuilder.toString());
        return logItem;
    }

}
