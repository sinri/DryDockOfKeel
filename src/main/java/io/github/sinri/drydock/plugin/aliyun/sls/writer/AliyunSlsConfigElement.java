package io.github.sinri.drydock.plugin.aliyun.sls.writer;

import io.github.sinri.keel.facade.configuration.KeelConfigElement;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * @since 2.1.0
 */
public class AliyunSlsConfigElement extends KeelConfigElement {

    public AliyunSlsConfigElement(@Nonnull KeelConfigElement another) {
        super(another);
    }

    public final boolean isDisabled() {
        return Objects.equals("YES", readString(List.of("disabled")));
    }

    public final String getProject() {
        return readString(List.of("project"));
    }

    public final String getLogstore() {
        return readString(List.of("logstore"));
    }

    public final String getSource() {
        return readString(List.of("source"));
    }

    public final String getEndpoint() {
        return readString(List.of("endpoint"));
    }

    public final String getAccessKeyId() {
        return readString(List.of("accessKeyId"));
    }

    public final String getAccessKeySecret() {
        return readString(List.of("accessKeySecret"));
    }
}
