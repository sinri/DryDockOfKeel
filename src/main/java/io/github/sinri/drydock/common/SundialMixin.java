package io.github.sinri.drydock.common;

import io.github.sinri.keel.core.servant.sundial.KeelSundial;
import io.github.sinri.keel.core.servant.sundial.KeelSundialPlan;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import java.util.Collection;

/**
 * The mixin interface for a unit for Sundial.
 *
 * @since 1.1.0
 */
public interface SundialMixin extends CommonUnit {
    /**
     * Build a KeelSundial instance.
     * A default implementation is provided.
     *
     * @return The built KeelSundial instance.
     */
    default KeelSundial buildSundial() {
        KeelIssueRecordCenter issueRecordCenter = getIssueRecordCenter();
        return new KeelSundial() {
            @Override
            protected KeelIssueRecordCenter getIssueRecordCenter() {
                return issueRecordCenter;
            }

            @Override
            protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                return fetchSundialPlans();
            }
        };
    }

    /**
     * @return the asynchronously fetched sundial plans to completely overwrite; return null to modify none of the existed plans.
     */
    Future<Collection<KeelSundialPlan>> fetchSundialPlans();

    /**
     * Try to build a KeelSundial instance and start it up.
     * Do nothing if this ability is not required.
     *
     * @return a future as all work scheduled.
     */
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
