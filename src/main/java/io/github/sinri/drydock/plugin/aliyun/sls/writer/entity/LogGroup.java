package io.github.sinri.drydock.plugin.aliyun.sls.writer.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol.LogEntityDescriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a
 *         href="https://help.aliyun.com/zh/sls/developer-reference/api-sls-2020-12-30-struct-loggroup?spm=a2c4g.11186623.0.0.1dde4941qHi34T">LogGroup实体格式定义</a>
 * @since 2.1.0
 */
public class LogGroup {
    private final String topic;
    private final String source;
    private final List<LogTag> logTags;
    private final List<LogItem> logItems;

    /**
     * Create a LogGroup with topic and source
     *
     * @param topic  The log topic, user-defined field for distinguishing different types of log data
     * @param source The log source, e.g., IP address of the machine that generated the log
     */
    public LogGroup(String topic, String source) {
        this.topic = topic;
        this.source = source;
        this.logTags = new ArrayList<>();
        this.logItems = new ArrayList<>();
    }

    /**
     * Get the log topic
     *
     * @return The log topic
     */
    public String getTopic() {
        return topic;
    }


    /**
     * Get the log source
     *
     * @return The log source
     */
    public String getSource() {
        return source;
    }

    /**
     * Get the log tags
     *
     * @return The list of log tags
     */
    public List<LogTag> getLogTags() {
        return logTags;
    }

    /**
     * Set the log tags
     *
     * @param logTags The list of log tags to set
     * @return this instance for chaining
     */
    public LogGroup addLogTags(List<LogTag> logTags) {
        this.logTags.addAll(logTags);
        return this;
    }

    public LogGroup addLogTag(LogTag logTag) {
        this.logTags.add(logTag);
        return this;
    }

    /**
     * Get the log items
     *
     * @return The list of log items
     */
    public List<LogItem> getLogItems() {
        return logItems;
    }

    /**
     * Set the log items
     *
     * @param logItems The list of log items to set
     * @return this instance for chaining
     */
    public LogGroup addLogItems(List<LogItem> logItems) {
        this.logItems.addAll(logItems);
        return this;
    }

    public LogGroup addLogItem(LogItem logItem) {
        this.logItems.add(logItem);
        return this;
    }

    public DynamicMessage toProtobuf() {
        var logGroupDescriptor = LogEntityDescriptors.getInstance().getLogGroupDescriptor();
        var builder = DynamicMessage.newBuilder(logGroupDescriptor);
        if (topic != null) {
            builder.setField(logGroupDescriptor.findFieldByName("Topic"), topic);
        }
        if (source != null) {
            builder.setField(logGroupDescriptor.findFieldByName("Source"), source);
        }
        logItems.forEach(logItem -> builder.addRepeatedField(logGroupDescriptor.findFieldByName("Logs"), logItem.toProtobuf()));
        logTags.forEach(logTag -> builder.addRepeatedField(logGroupDescriptor.findFieldByName("LogTags"), logTag.toProtobuf()));
        return builder.build();
    }
}
