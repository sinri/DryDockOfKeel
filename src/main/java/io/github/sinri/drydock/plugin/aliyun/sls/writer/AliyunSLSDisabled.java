package io.github.sinri.drydock.plugin.aliyun.sls.writer;

public final class AliyunSLSDisabled extends Exception {
    public AliyunSLSDisabled() {
        super("Aliyun SLS logging is disabled");
    }
}
