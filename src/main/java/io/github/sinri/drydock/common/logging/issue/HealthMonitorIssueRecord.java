package io.github.sinri.drydock.common.logging.issue;

import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

public class HealthMonitorIssueRecord extends BaseIssueRecord<HealthMonitorIssueRecord> {
    public static final String AttributeSnapshot = "snapshot";

    @Nonnull
    @Override
    public String topic() {
        return DryDockLogTopics.TopicHealthMonitor;
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
