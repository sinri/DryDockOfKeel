package io.github.sinri.drydock.aviation.aircraft;

import io.github.sinri.drydock.aviation.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.servant.sundial.KeelSundial;
import io.github.sinri.keel.core.servant.sundial.KeelSundialPlan;
import io.github.sinri.keel.core.servant.sundial.SundialIssueRecord;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * 和AircraftCarrierDeck配合使用的舰载轰炸机类，用于按照战术设计定时发起轰炸。
 *
 * @since 1.5.0
 */
public abstract class Bomber extends Biplane {
    public Bomber(@Nonnull AircraftCarrierDeck deck) {
        super(deck);
    }

    /**
     * Build a KeelSundial instance. A default implementation is provided.
     *
     * @return The built KeelSundial instance.
     */
    protected KeelSundial buildSundial() {
        KeelIssueRecordCenter issueRecordCenter = getIssueRecordCenter();
        return new KeelSundial() {
            @Override
            protected KeelIssueRecordCenter getIssueRecordCenter() {
                return issueRecordCenter;
            }

            @Override
            protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder = getSundialIssueRecorder();
                return fetchSundialPlans(sundialIssueRecorder);
            }
        };
    }

    /**
     * @param sundialIssueRecorder as of 2.0.4 it is added.
     * @return the asynchronously fetched sundial plans to completely overwrite; return null to modify none of the
     *         existed plans.
     */
    abstract protected Future<Collection<KeelSundialPlan>> fetchSundialPlans(KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder);

    /**
     * Try to build a KeelSundial instance and start it up. Do nothing if this ability is not required.
     *
     * @return a future of the deployment of KeelSundial
     */
    public Future<String> loadSundial() {
        return Future.succeededFuture(this.buildSundial())
                     .compose(sundial -> {
                         if (sundial == null) return Future.succeededFuture();
                         return sundial.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
                     });
    }
}
