package io.github.sinri.drydock.plugin.aliyun.sls.writer;

import io.github.sinri.keel.facade.configuration.KeelConfigElement;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 2.1.0
 */
public class AliyunSlsConfigElement extends KeelConfigElement {

    // Configuration keys
    private static final String CONFIG_KEY_DISABLED = "disabled";
    private static final String CONFIG_KEY_PROJECT = "project";
    private static final String CONFIG_KEY_LOGSTORE = "logstore";
    private static final String CONFIG_KEY_SOURCE = "source";
    private static final String CONFIG_KEY_ENDPOINT = "endpoint";
    private static final String CONFIG_KEY_ACCESS_KEY_ID = "accessKeyId";
    private static final String CONFIG_KEY_ACCESS_KEY_SECRET = "accessKeySecret";

    public AliyunSlsConfigElement(@Nonnull KeelConfigElement another) {
        super(another);
    }

    public final boolean isDisabled() {
        return Boolean.TRUE.equals(readBoolean(List.of(CONFIG_KEY_DISABLED)));
    }

    public final String getProject() {
        return readString(List.of(CONFIG_KEY_PROJECT));
    }

    public final String getLogstore() {
        return readString(List.of(CONFIG_KEY_LOGSTORE));
    }

    public final String getSource() {
        return readString(List.of(CONFIG_KEY_SOURCE));
    }

    public final String getEndpoint() {
        return readString(List.of(CONFIG_KEY_ENDPOINT));
    }

    public final String getAccessKeyId() {
        return readString(List.of(CONFIG_KEY_ACCESS_KEY_ID));
    }

    public final String getAccessKeySecret() {
        return readString(List.of(CONFIG_KEY_ACCESS_KEY_SECRET));
    }
}
