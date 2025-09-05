package io.github.sinri.drydock.plugin.aliyun.sls.writer.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol.LogEntityDescriptors;

/**
 * @see <a
 *         href="https://help.aliyun.com/zh/sls/developer-reference/api-sls-2020-12-30-struct-logcontent">LogContent</a>
 * @since 2.1.0
 */
public class LogContent {
    private final String key;
    private final String value;

    public LogContent(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public DynamicMessage toProtobuf() {
        var contentDescriptor = LogEntityDescriptors.getInstance().getContentDescriptor();
        return DynamicMessage.newBuilder(contentDescriptor)
                             .setField(contentDescriptor.findFieldByName("Key"), key)
                             .setField(contentDescriptor.findFieldByName("Value"), value)
                             .build();
    }
}
