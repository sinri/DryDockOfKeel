package io.github.sinri.drydock.common;

import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.drydock.common.logging.issue.HealthMonitorIssueRecord;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

public interface HealthMonitorMixin
        extends CommonUnit {
    default HealthMonitor buildHealthMonitor() {
        return new HealthMonitor();
    }

    default Future<Void> loadHealthMonitor() {
        return Future.succeededFuture(buildHealthMonitor())
                .compose(healthMonitor -> {
                    if (healthMonitor == null) return Future.succeededFuture();

                    healthMonitor.setIssueRecorder(getIssueRecordCenter().generateIssueRecorder(DryDockLogTopics.TopicHealthMonitor, HealthMonitorIssueRecord::new));

                    return healthMonitor.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
                })
                .onFailure(throwable -> {
                    getUnitLogger().exception(throwable, "Failed to deploy HealthMonitor");
                })
                .compose(deploymentId -> {
                    getUnitLogger().info("Deployed HealthMonitor: " + deploymentId);
                    return Future.succeededFuture();
                });
    }
}
