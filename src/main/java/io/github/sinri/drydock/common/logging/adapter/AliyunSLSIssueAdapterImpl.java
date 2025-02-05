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
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordStringRender;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.3.4
 */
public class AliyunSLSIssueAdapterImpl extends AliyunSLSIssueAdapter {
    /**
     * @since 1.4.21
     */
    private static int bufferSize = 1000;
    private final boolean disabled;
    private final String project;
    private final String logstore;
    private final String source;
    private final String endpoint;
    //private Producer producer;
    private final AtomicReference<Producer> producerRef = new AtomicReference<>();
    private volatile boolean stopped = false;
    private volatile boolean closed = true;

    public AliyunSLSIssueAdapterImpl() {
        KeelConfigElement aliyunSlsConfig = Keel.getConfiguration().extract("aliyun", "sls");
        if (aliyunSlsConfig == null) {
            disabled = true;
            this.project = null;
            this.logstore = null;
            this.endpoint = null;
            this.source = null;
        } else {
            String disabledString = aliyunSlsConfig.readString("disabled", null);
            // System.out.println("disabledString: "+disabledString);
            disabled = ("YES".equalsIgnoreCase(disabledString));

            this.project = aliyunSlsConfig.readString("project", null);
            this.logstore = aliyunSlsConfig.readString("logstore", null);
            this.endpoint = aliyunSlsConfig.readString("endpoint", null);
            this.source = buildSource(aliyunSlsConfig.readString("source", null));
        }
        buildProducer();
        start();
    }

    /**
     * @return the configured switch to decide whether the Aliyun SLS should be disabled.
     * @since 1.4.9
     */
    public static boolean isDisabled() {
        String x = Keel.config("aliyun.sls.disabled");
        return "YES".equalsIgnoreCase(x);
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
        String localHostAddress = Keel.netHelper().getLocalHostAddress();
        if (localHostAddress == null) {
            Keel.getLogger().warning("Could not get local host address for SLS source!");
            return "";
        }
        return configuredSourceExpression.replaceAll("\\[IP]", localHostAddress);
    }

    /**
     * @since 1.4.21
     */
    public static void setBufferSize(int bufferSize) {
        AliyunSLSIssueAdapterImpl.bufferSize = bufferSize;
    }

    /**
     * @since 1.4.20
     */
    private Future<Void> rebuildProducer() {
        Promise<Void> promise = Promise.promise();
        var producer = producerRef.get();
        if (producer != null) {
            Keel.getLogger().info("io.github.sinri.drydock.common.logging.adapter.AliyunSLSIssueAdapterImpl.rebuildProducer to close producer");
            this.close(promise);
            producerRef.set(null);
        }

        return promise.future()
                .compose(v -> {
                    buildProducer();
                    return Future.succeededFuture();
                });
    }

    /**
     * @since 1.4.20
     */
    private void buildProducer() {
        if (!disabled) {
            KeelConfigElement aliyunSlsConfig = Keel.getConfiguration().extract("aliyun", "sls");

            String accessKeyId = aliyunSlsConfig.readString("accessKeyId", null);
            String accessKeySecret = aliyunSlsConfig.readString("accessKeySecret", null);

            var producer = new LogProducer(new ProducerConfig());
            Objects.requireNonNull(project);
            Objects.requireNonNull(endpoint);
            Objects.requireNonNull(accessKeyId);
            Objects.requireNonNull(accessKeySecret);
            producer.putProjectConfig(new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret));

            producerRef.set(producer);

            Keel.getLogger().info("io.github.sinri.drydock.common.logging.adapter.AliyunSLSIssueAdapterImpl.buildProducer built producer.");
            //KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()).info("Aliyun SLS Producer relied aliyunSlsConfig: " + aliyunSlsConfig.toJsonObject());
        } else {
            producerRef.set(null);
            // a bug in 1.4.2, to stdout not means closed.
        }
        closed = false;
        stopped = false;
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

        List<LogItem> logItems = new ArrayList<>();

        try {
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
        } catch (Throwable throwable) {
            Keel.getLogger().exception(throwable, "Pack Logs into Aliyun SLS Log Items Failed");
            buffer.forEach(item -> {
                String s = KeelIssueRecordStringRender.getInstance().renderIssueRecord(topic, item);
                System.out.println(s);
            });
            promise.fail(throwable);
            return promise.future();
        }

        try {
            //Keel.getLogger().info("AliyunSLSIssueAdapterImpl handleIssueRecordsForTopic "+topic+" buffer to send with producer");
            producerRef.get().send(project, logstore, topic, source, logItems, result -> {
                if (!result.isSuccessful()) {
                    Keel.getLogger().error(r -> r
                            .classification(getClass().getName())
                            .message("Producer Send Error: " + result)
                    );
                    buffer.forEach(item -> {
                        String s = KeelIssueRecordStringRender.getInstance().renderIssueRecord(topic, item);
                        System.out.println(s);
                    });
                }

                //Keel.getLogger().info("AliyunSLSIssueAdapterImpl handleIssueRecordsForTopic "+topic+" promise to complete");
                promise.complete(null);
            });
        } catch (Throwable e) {
            Keel.getLogger().exception(e, r -> r
                    .classification(getClass().getName())
                    .message("Aliyun SLS Producer Exception")
            );
            rebuildProducer().andThen(ar -> {
                promise.complete(null);
            });
            buffer.forEach(item -> {
                String s = KeelIssueRecordStringRender.getInstance().renderIssueRecord(topic, item);
                System.out.println(s);
            });
        }
        return promise.future();
    }

    /**
     * @since 1.4.21
     */
    @Override
    protected int bufferSize() {
        return bufferSize;
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
        if (this.disabled || this.producerRef.get() == null || closed) {
            closed = true;
            promise.complete();
        } else {
            try {
                var producer = this.producerRef.get();
                if (producer != null) {
                    producer.close();
                }
                producerRef.set(null);
                closed = true;
                promise.complete();
            } catch (Throwable e) {
                promise.fail(e);
            }
        }
    }
}
