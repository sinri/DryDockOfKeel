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
    final static long maxSizeBytes = 5L * 1024 * 1024; // 5MB limit
    private final String topic;
    private final String source;
    private final List<LogTag> logTags;
    private final List<LogItem> logItems;
    private int probableSize = 0;

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
        for (var logItem : logItems) {
            addLogItem(logItem);
        }

        return this;
    }

    public LogGroup addLogItem(LogItem logItem) {
        this.logItems.add(logItem);
        probableSize += logItem.getProbableSize();
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

    /**
     * 阿里云日志服务要求日志组中每条日志下的Value部分建议不超过1MB，而写入日志的接口每一次可以接受的原始数据大小不超过10MB。
     * 所以需要将日志组拆分成多个日志组，尽量确保每次调用不超标。
     * <p>
     * 拆分规则为仅看日志组里的Value部分字节数来计算，在日志组内Value已达到5MB时即拆分。
     *
     * @return 拆分后的日志组列表
     * @deprecated 在上层直接divide，而不是在
     */
    @Deprecated(since = "3.0.0.2", forRemoval = true)
    public List<LogGroup> divide() {
        final List<LogItem> logItems = getLogItems();

        // Early exit if no items to process
        if (logItems.isEmpty()) {
            return List.of(this);
        }

        // Use divideLogItemsParts to get the divided log item groups
        List<List<LogItem>> logItemParts = divideLogItemsParts();

        // Convert each part to a LogGroup
        final List<LogGroup> result = new ArrayList<>();
        final List<LogTag> sharedLogTags = getLogTags(); // Cache to avoid repeated calls

        for (List<LogItem> part : logItemParts) {
            LogGroup group = new LogGroup(getTopic(), getSource());
            group.addLogTags(sharedLogTags);
            group.addLogItems(part);
            result.add(group);
        }

        return result;
    }

    @Deprecated(since = "3.0.0.2", forRemoval = true)
    private List<List<LogItem>> divideLogItemsParts() {
        final List<LogItem> logItems = getLogItems();
        if (logItems.isEmpty()) {
            return List.of();
        }

        final List<List<LogItem>> parts = new ArrayList<>();
        List<LogItem> part = new ArrayList<>();
        long cache = 0;

        for (LogItem logItem : logItems) {
            long itemSize = logItem.getProbableSize();

            // Check if adding this item would exceed the limit
            if (cache > 0 && (cache + itemSize) > maxSizeBytes) {
                // Current part is full, start a new one
                parts.add(part);
                part = new ArrayList<>();
                cache = 0;
            }

            // Add item to current part
            part.add(logItem);
            cache += itemSize;
        }

        // Add the last part if it has items
        parts.add(part);

        return parts;
    }

    /**
     * @return 缓存好的大约尺寸
     * @since 3.0.0.2
     */
    public int getProbableSize() {
        return probableSize;
    }
}
