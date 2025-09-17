package io.github.sinri.drydock.plugin.aliyun.sls.writer.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol.LogEntityDescriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.1.0
 * @deprecated 阿里云的SLS有这个定义，但API里没有用到
 */
@Deprecated(since = "3.0.0.2", forRemoval = true)
public class LogGroupList {
    private final List<LogGroup> logGroups;

    public LogGroupList() {
        this.logGroups = new ArrayList<>();
    }

    public LogGroupList addLogGroup(LogGroup logGroup) {
        this.logGroups.add(logGroup);
        return this;
    }

    public LogGroupList addLogGroups(List<LogGroup> logGroups) {
        this.logGroups.addAll(logGroups);
        return this;
    }

    public List<LogGroup> getLogGroups() {
        return logGroups;
    }

    public DynamicMessage toProtobuf() {
        var logGroupListDescriptor = LogEntityDescriptors.getInstance().getLogGroupListDescriptor();
        var builder = DynamicMessage.newBuilder(logGroupListDescriptor);
        logGroups.forEach(logGroup -> builder.addRepeatedField(logGroupListDescriptor.findFieldByName("logGroupList"), logGroup.toProtobuf()));
        return builder.build();
    }
}
