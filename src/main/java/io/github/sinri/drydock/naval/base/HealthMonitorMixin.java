package io.github.sinri.drydock.naval.base;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

public interface HealthMonitorMixin extends Boat {
    default HealthMonitor buildHealthMonitor() {
        return new HealthMonitor();
    }

    default Future<Void> loadHealthMonitor() {
        return Future.succeededFuture(buildHealthMonitor())
                .compose(healthMonitor -> {
                    if (healthMonitor == null) return Future.succeededFuture();
                    healthMonitor.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicHealthMonitor, null));
                    return healthMonitor.deployMe(new DeploymentOptions().setWorker(true));
                })
                .onFailure(throwable -> {
                    getNavalLogger().exception(throwable, "Failed to deploy HealthMonitor");
                })
                .compose(deploymentId -> {
                    getNavalLogger().info("Deployed HealthMonitor: " + deploymentId);
                    return Future.succeededFuture();
                });
    }
}
