package io.github.sinri.drydock.plugin.aliyun.sls.writer.entity;

import com.google.protobuf.DynamicMessage;
import io.github.sinri.drydock.plugin.aliyun.sls.writer.protocol.LogEntityDescriptors;

/**
 * @see <a
 *         href="https://help.aliyun.com/zh/sls/developer-reference/api-sls-2020-12-30-struct-logtag">LogTag</a>
 * @since 2.1.0
 */
public class LogTag {
    private String key;
    private String value;

    /**
     * Default constructor
     */
    public LogTag() {
    }

    /**
     * Create a LogTag with key and value
     *
     * @param key   The tag key
     * @param value The tag value
     */
    public LogTag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the tag key
     *
     * @return The tag key
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the tag key
     *
     * @param key The tag key to set
     * @return this instance for chaining
     */
    public LogTag setKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Get the tag value
     *
     * @return The tag value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the tag value
     *
     * @param value The tag value to set
     * @return this instance for chaining
     */
    public LogTag setValue(String value) {
        this.value = value;
        return this;
    }

    public DynamicMessage toProtobuf() {
        var logTagDescriptor = LogEntityDescriptors.getInstance().getLogTagDescriptor();
        return DynamicMessage.newBuilder(logTagDescriptor)
                             .setField(logTagDescriptor.findFieldByName("Key"), key)
                             .setField(logTagDescriptor.findFieldByName("Value"), value)
                             .build();
    }
}
