package io.github.sinri.drydock.plugin.aliyun.sls.writer.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol.LogEntityDescriptors;

import java.nio.charset.StandardCharsets;

/**
 * @see <a
 *      href=
 *      "https://help.aliyun.com/zh/sls/developer-reference/api-sls-2020-12-30-struct-logcontent">LogContent</a>
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

    /**
     * Calculates and returns the probable size of the log content in bytes.
     * The size is computed by summing the UTF-8 byte lengths of the value
     * string.
     *
     * @return The total size in bytes of the UTF-8 encoded key and value combined.
     * @since 3.0.0
     */
    public Integer getProbableSize() {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
