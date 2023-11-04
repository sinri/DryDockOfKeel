package io.github.sinri.drydock.naval.base;

import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.Collection;

/**
 * @since 1.1.0
 */
public interface SundialMixin extends Boat {
    default KeelSundial buildSundial() {
        var sundial = new KeelSundial() {
            @Override
            protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                return fetchSundialPlans();
            }
        };
        sundial.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicSundial, null));
        return sundial;
    }

    Future<Collection<KeelSundialPlan>> fetchSundialPlans();

    default Future<Void> loadSundial() {
        return Future.succeededFuture(this.buildSundial())
                .compose(sundial -> {
                    if (sundial == null) return Future.succeededFuture();
                    return sundial.deployMe(new DeploymentOptions().setWorker(true))
                            .onFailure(throwable -> {
                                getNavalLogger().exception(throwable, "load sundial failed");
                            })
                            .compose(deploymentId -> {
                                getNavalLogger().info("load sundial: " + deploymentId);
                                return Future.succeededFuture();
                            });
                });
    }
}
