package io.github.sinri.drydock.common.logging.adapter;

import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.log.common.LogItem;
import io.github.sinri.keel.facade.KeelConfiguration;
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

    public AliyunSLSIssueAdapterImpl() {
        KeelConfiguration aliyunSlsConfig = Keel.getConfiguration().extract("aliyun", "sls");
        Objects.requireNonNull(aliyunSlsConfig);

        String disabledString = aliyunSlsConfig.readString("disabled");
        disabled = ("YES".equals(disabledString));

        this.project = aliyunSlsConfig.readString("project");
        this.logstore = aliyunSlsConfig.readString("logstore");
        this.endpoint = aliyunSlsConfig.readString("endpoint");
        this.source = buildSource(aliyunSlsConfig.readString("source"));

        if (!disabled) {
            String accessKeyId = aliyunSlsConfig.readString("accessKeyId");
            String accessKeySecret = aliyunSlsConfig.readString("accessKeySecret");

            producer = new LogProducer(new ProducerConfig());
            Objects.requireNonNull(project);
            Objects.requireNonNull(endpoint);
            Objects.requireNonNull(accessKeyId);
            Objects.requireNonNull(accessKeySecret);
            producer.putProjectConfig(new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret));

            //KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()).info("Aliyun SLS Producer relied aliyunSlsConfig: " + aliyunSlsConfig.toJsonObject());

            start();
            closed = false;
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
    protected Future<Void> handleIssueRecordsForTopic(@Nonnull String topic, @Nonnull List<KeelIssueRecord<?>> buffer) {
        if (buffer.isEmpty()) return Future.succeededFuture();

        if (disabled) {
            buffer.forEach(item -> {
                SyncStdoutAdapter.getInstance().record(topic, item);
            });
            return Future.succeededFuture();
        }

        Promise<Void> promise = Promise.promise();

        try {
            List<LogItem> logItems = new ArrayList<>();

            buffer.forEach(eventLog -> {
                LogItem logItem = new LogItem(Math.toIntExact(eventLog.timestamp() / 1000));
                logItem.PushBack(KeelIssueRecord.AttributeLevel, eventLog.level().name());
                List<String> classification = eventLog.classification();
                if (!classification.isEmpty()) {
                    logItem.PushBack(KeelIssueRecord.AttributeClassification, String.valueOf(new JsonArray()));
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

            producer.send(project, logstore, topic, source, logItems, result -> {
                if (!result.isSuccessful()) {
                    Keel.getLogger().getIssueRecorder().error(r -> r
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