package io.github.sinri.drydock.common;

import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import java.util.Collection;

/**
 * @since 1.1.0
 */
public interface SundialMixin extends CommonUnit {
    default KeelSundial buildSundial() {
        var sundial = new KeelSundial() {
            @Override
            protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                return fetchSundialPlans();
            }
        };
        sundial.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicSundial, entries -> entries.classification("Sundial")));
        return sundial;
    }

    Future<Collection<KeelSundialPlan>> fetchSundialPlans();

    default Future<Void> loadSundial() {
        return Future.succeededFuture(this.buildSundial())
                .compose(sundial -> {
                    if (sundial == null) return Future.succeededFuture();
                    return sundial.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                            .onFailure(throwable -> {
                                getUnitLogger().exception(throwable, "Failed to load sundial");
                            })
                            .compose(deploymentId -> {
                                getUnitLogger().info("Loaded sundial: " + deploymentId);
                                return Future.succeededFuture();
                            });
                });
    }
}
