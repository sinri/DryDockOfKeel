package io.github.sinri.drydock.common.logging.issue;

import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;

import javax.annotation.Nonnull;

/**
 * @since 1.3.4
 */
public final class DryDockRoutineIssueRecord extends BaseIssueRecord<DryDockRoutineIssueRecord> {
    @Nonnull
    @Override
    public String topic() {
        return DryDockLogTopics.TopicDryDock;
    }

    @Nonnull
    @Override
    public DryDockRoutineIssueRecord getImplementation() {
        return this;
    }
}
