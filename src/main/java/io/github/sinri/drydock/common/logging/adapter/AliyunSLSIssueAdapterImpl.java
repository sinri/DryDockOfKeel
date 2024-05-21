package io.github.sinri.drydock.common.logging.adapter;

import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.log.common.LogItem;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.adapter.AliyunSLSIssueAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 1.3.4
 */
public class AliyunSLSIssueAdapterImpl extends AliyunSLSIssueAdapter {
    private final boolean disabled;
    private final String project;
    private final String logstore;
    private final String source;
    private final Producer producer;
    private final String endpoint;
    private volatile boolean stopped = false;
    private volatile boolean closed = true;

    /**
     * @return the configured switch to decide whether the Aliyun SLS should be disabled.
     * @since 1.4.9
     */
    public static boolean isDisabled() {
        String x = Keel.config("aliyun.sls.disabled");
        return "YES".equalsIgnoreCase(x);
    }

    public AliyunSLSIssueAdapterImpl() {
        KeelConfigElement aliyunSlsConfig = Keel.getConfiguration().extract("aliyun", "sls");
        Objects.requireNonNull(aliyunSlsConfig);

        String disabledString = aliyunSlsConfig.readString("disabled", null);
        // System.out.println("disabledString: "+disabledString);
        disabled = ("YES".equalsIgnoreCase(disabledString));

        this.project = aliyunSlsConfig.readString("project", null);
        this.logstore = aliyunSlsConfig.readString("logstore", null);
        this.endpoint = aliyunSlsConfig.readString("endpoint", null);
        this.source = buildSource(aliyunSlsConfig.readString("source", null));

        if (!disabled) {
            String accessKeyId = aliyunSlsConfig.readString("accessKeyId", null);
            String accessKeySecret = aliyunSlsConfig.readString("accessKeySecret", null);

            producer = new LogProducer(new ProducerConfig());
            Objects.requireNonNull(project);
            Objects.requireNonNull(endpoint);
            Objects.requireNonNull(accessKeyId);
            Objects.requireNonNull(accessKeySecret);
            producer.putProjectConfig(new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret));

            //KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()).info("Aliyun SLS Producer relied aliyunSlsConfig: " + aliyunSlsConfig.toJsonObject());
            closed = false;
        } else {
            producer = null;
            // a bug in 1.4.2, to stdout not means closed.
            closed = false;
        }
        start();
    }

    /**
     * Build source from configuration.
     * Source Expression should be:
     * - EMPTY/BLANK STRING or NULL: use SLS default source generation;
     * - A TEMPLATED STRING
     * --- Rule 1: Replace [IP] to local address;
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
    protected Future<Void> handleIssueRecordsForTopic(@Nonnull final String topic, @Nonnull final List<KeelIssueRecord<?>> buffer) {
        // Keel.getLogger().info("handleIssueRecordsForTopic["+topic+"] "+ buffer.size());
        if (buffer.isEmpty()) return Future.succeededFuture();

        if (disabled) {
            //Keel.getLogger().info("AliyunSLSIssueAdapterImpl handleIssueRecordsForTopic "+topic+" disabled");
            buffer.forEach(item -> {
                SyncStdoutAdapter.getInstance().record(topic, item);
            });
            return Future.succeededFuture();
        }

        Promise<Void> promise = Promise.promise();

        try {
            List<LogItem> logItems = new ArrayList<>();

            //Keel.getLogger().info("AliyunSLSIssueAdapterImpl handleIssueRecordsForTopic "+topic+" for each in buffer...");
            buffer.forEach(eventLog -> {
                LogItem logItem = new LogItem(Math.toIntExact(eventLog.timestamp() / 1000));
                logItem.PushBack(KeelIssueRecord.AttributeLevel, eventLog.level().name());
                List<String> classification = eventLog.classification();
                if (!classification.isEmpty()) {
                    logItem.PushBack(KeelIssueRecord.AttributeClassification, String.valueOf(new JsonArray(classification)));
                }
                eventLog.attributes().forEach(entry -> {
                    if (entry.getValue() == null) {
                        logItem.PushBack(entry.getKey(), null);
                    } else {
                        logItem.PushBack(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                });
                Throwable exception = eventLog.exception();
                if (exception != null) {
                    logItem.PushBack(KeelIssueRecord.AttributeException, String.valueOf(issueRecordRender().renderThrowable(exception)));
                }
                logItems.add(logItem);
            });
            //Keel.getLogger().info("AliyunSLSIssueAdapterImpl handleIssueRecordsForTopic "+topic+" buffer to send with producer");
            producer.send(project, logstore, topic, source, logItems, result -> {
                if (!result.isSuccessful()) {
                    Keel.getLogger().error(r -> r
                            .classification(getClass().getName())
                            .message("Producer Send Error: " + result)
                    );
                }

                //Keel.getLogger().info("AliyunSLSIssueAdapterImpl handleIssueRecordsForTopic "+topic+" promise to complete");
                promise.complete(null);
            });
        } catch (Throwable e) {
            Keel.getLogger().exception(e, r -> r
                    .classification(getClass().getName())
                    .message("Aliyun SLS Producer Exception")
            );
            promise.fail(e);
        }

        return promise.future();
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        stopped = true;
        if (this.disabled || this.producer == null || closed) {
            closed = true;
            promise.complete();
        } else {
            try {
                this.producer.close();
                closed = true;
                promise.complete();
            } catch (Throwable e) {
                promise.fail(e);
            }
        }
    }
}
