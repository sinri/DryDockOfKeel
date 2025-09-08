package io.github.sinri.drydock.plugin.aliyun.sls.writer;

import io.github.sinri.drydock.plugin.aliyun.sls.writer.entity.LogGroup;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.entity.LogItem;
import io.github.sinri.keel.core.json.JsonifiedThrowable;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.adapter.AliyunSLSIssueAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 2.1.0
 */
public class AliyunSLSIssueAdapterImpl extends AliyunSLSIssueAdapter {
    private final String source;
    private final AliyunSlsConfigElement aliyunSlsConfig;
    private final AtomicBoolean stopRef = new AtomicBoolean(false);
    private final int bufferSize;
    @Nullable
    private AliyunSLSLogPutter logPutter;

    public AliyunSLSIssueAdapterImpl() {
        this(128);
    }

    /**
     * Constructs an instance of {@code AliyunSLSIssueAdapterImpl} with the specified buffer size.
     * Initializes the configuration for Aliyun SLS logging, and sets up the necessary components
     * such as source and log producer based on the configuration.
     *
     * @param bufferSize the size of the buffer to be used for managing issue records. This determines
     *                   how many issue records can be held before processing.
     * @since 3.0.0
     */
    public AliyunSLSIssueAdapterImpl(int bufferSize) {
        this.bufferSize = bufferSize;

        KeelConfigElement extract = Keel.getConfiguration().extract("aliyun", "sls");
        if (extract == null) {
            KeelConfigElement temp = new KeelConfigElement("sls");
            temp.ensureChild("disabled").setValue("YES");
            aliyunSlsConfig = new AliyunSlsConfigElement(temp);
        } else {
            aliyunSlsConfig = new AliyunSlsConfigElement(extract);
        }

        this.source = AliyunSLSLogPutter.buildSource(aliyunSlsConfig.getSource());
        this.logPutter = this.buildProducer();

        // let us start
        this.start();
    }

    public boolean isDisabled() {
        return aliyunSlsConfig.isDisabled();
    }

    @Nullable
    private AliyunSLSLogPutter buildProducer() {
        if (aliyunSlsConfig.isDisabled()) {
            return null;
        }

        return new AliyunSLSLogPutter(
                aliyunSlsConfig.getAccessKeyId(),
                aliyunSlsConfig.getAccessKeySecret(),
                aliyunSlsConfig.getEndpoint()
        );
    }

    @Override
    protected Future<Void> handleIssueRecordsForTopic(@Nonnull String topic, @Nonnull List<KeelIssueRecord<?>> buffer) {
        if (aliyunSlsConfig.isDisabled() || logPutter == null) {
            buffer.forEach(item -> SyncStdoutAdapter.getInstance().record(topic, item));
            return Future.succeededFuture();
        }

        LogGroup logGroup = new LogGroup(topic, source);
        buffer.forEach(eventLog -> {
            LogItem logItem = new LogItem(Math.toIntExact(eventLog.timestamp() / 1000));
            logItem.addContent(KeelIssueRecord.AttributeLevel, eventLog.level().name());
            List<String> classification = eventLog.classification();
            if (!classification.isEmpty()) {
                logItem.addContent(KeelIssueRecord.AttributeClassification,
                        String.valueOf(new JsonArray(classification)));
            }
            eventLog.attributes().forEach(entry -> {
                if (entry.getValue() == null) {
                    logItem.addContent(entry.getKey(), null);
                } else {
                    logItem.addContent(entry.getKey(), String.valueOf(entry.getValue()));
                }
            });
            Throwable exception = eventLog.exception();
            if (exception != null) {
                // as of 2.1.0.1, use JsonifiedThrowable
                JsonifiedThrowable jsonifiedThrowable = JsonifiedThrowable.wrap(exception);
                logItem.addContent(KeelIssueRecord.AttributeException, jsonifiedThrowable.toJsonExpression());
            }
            logGroup.addLogItem(logItem);
        });

        // System.out.println("!");
        return this.logPutter.putLogs(aliyunSlsConfig.getProject(), aliyunSlsConfig.getLogstore(), logGroup);
    }

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        this.stopRef.set(true);
        if (this.logPutter != null) {
            Keel.getVertx().executeBlocking((Callable<Void>) () -> {
                awaitRecording();
                return null;
            });
            logPutter.close();
            logPutter = null;
        }
        promise.complete();
    }

    @Override
    public boolean isStopped() {
        return stopRef.get();
    }

    @Override
    public boolean isClosed() {
        //        if (aliyunSlsConfig.isDisabled()) {
        //            return isStopped();
        //        }
        return isStopped() && this.logPutter == null;
    }

    @Override
    protected int bufferSize() {
        return bufferSize;
    }
}
