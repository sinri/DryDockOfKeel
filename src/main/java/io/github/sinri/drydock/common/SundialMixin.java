package io.github.sinri.drydock.common;

import io.github.sinri.drydock.common.logging.DryDockLogTopics;
import io.github.sinri.keel.logger.event.KeelEventLogger;
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
        KeelEventLogger eventLogger = getIssueRecordCenter().generateEventLogger(DryDockLogTopics.TopicSundial);
        return new KeelSundial() {
            @Override
            protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                return fetchSundialPlans();
            }

            @Override
            protected KeelEventLogger buildEventLogger() {
                return eventLogger;
            }
        };
    }

    Future<Collection<KeelSundialPlan>> fetchSundialPlans();

    default Future<Void> loadSundial() {
        return Future.succeededFuture(this.buildSundial())
                .compose(sundial -> {
                    if (sundial == null) return Future.succeededFuture();
                    return sundial.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                            .onFailure(throwable -> {
                                getLogger().exception(throwable, "Failed to load sundial");
                            })
                            .compose(deploymentId -> {
                                getLogger().info("Loaded sundial: " + deploymentId);
                                return Future.succeededFuture();
                            });
                });
    }
}
