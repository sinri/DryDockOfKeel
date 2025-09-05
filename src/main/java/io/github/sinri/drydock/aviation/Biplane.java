package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.drydock.naval.carrier.AircraftCarrier;
import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.core.verticles.KeelVerticle;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * The interface to define the abilities of a biplane,
 * to be initialized and deployed in an {@link AircraftCarrierDeck} (or clearly
 * {@link AircraftCarrier}) implementation.
 * 
 * @since 3.0.0
 */
public interface Biplane extends CommonUnit, KeelVerticle {

    /**
     * @return the {@link AircraftCarrierDeck} instance that the biplane is deployed
     *         in.
     */
    @Nonnull
    AircraftCarrierDeck getAircraftCarrierDeck();

    /**
     * @return the {@link KeelIssueRecordCenter} instance that the biplane is
     *         associated with.
     */
    @Override
    default KeelIssueRecordCenter getIssueRecordCenter() {
        return getAircraftCarrierDeck().getIssueRecordCenter();
    }

    /**
     * @return a future after the biplane is deployed with the deployment id.
     */
    Future<String> deployMe();
}
