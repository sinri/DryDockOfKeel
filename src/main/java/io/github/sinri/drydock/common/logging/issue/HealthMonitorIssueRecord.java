package io.github.sinri.drydock.common.logging.issue;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

public class HealthMonitorIssueRecord extends KeelIssueRecord<HealthMonitorIssueRecord> {
    public static final String TopicHealthMonitor = "HealthMonitor";
    public static final String AttributeSnapshot = "snapshot";

    public HealthMonitorIssueRecord() {
        super();
    }

    @Nonnull
    @Override
    public HealthMonitorIssueRecord getImplementation() {
        return this;
    }

    public HealthMonitorIssueRecord snapshot(@Nonnull JsonObject snapshot) {
        this.attribute(AttributeSnapshot, snapshot);
        return this;
    }
}
