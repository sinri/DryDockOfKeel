package io.github.sinri.drydock.plugin.aliyun.sls.writer;

import io.github.sinri.drydock.plugin.aliyun.sls.writer.entity.LogGroup;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.entity.LogItem;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 2.1.0
 */
public class AliyunSLSIssueAdapterImpl extends AliyunSLSIssueAdapter {
    private final String source;
    private final AliyunSlsConfigElement aliyunSlsConfig;
    private final AtomicBoolean stopRef = new AtomicBoolean(false);
    private final AliyunSLSLogPutter logPutter;

    public AliyunSLSIssueAdapterImpl() {
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
        if (aliyunSlsConfig.isDisabled()) {
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
                logItem.addContent(KeelIssueRecord.AttributeException,
                        String.valueOf(issueRecordRender().renderThrowable(exception)));
            }
            logGroup.addLogItem(logItem);
        });

        // System.out.println("!");
        return this.logPutter.putLogs(aliyunSlsConfig.getProject(), aliyunSlsConfig.getLogstore(), logGroup);
    }

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        if (this.logPutter != null) {
            this.logPutter.close();
        }
        promise.complete();
    }

    @Override
    public boolean isStopped() {
        return stopRef.get();
    }

    @Override
    public boolean isClosed() {
        if (aliyunSlsConfig.isDisabled()) {
            return isStopped();
        }
        return isStopped() && this.logPutter == null;
    }

    @Override
    protected int bufferSize() {
        return 512;
    }
}
