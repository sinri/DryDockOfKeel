package io.github.sinri.drydock.aviation;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.drydock.naval.carrier.AircraftCarrierDeck;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;


public interface Biplane extends CommonUnit {

    @Nonnull
    AircraftCarrierDeck getAircraftCarrierDeck();

    /**
     * @since 1.3.4
     */
    @Override
    default KeelIssueRecordCenter getIssueRecordCenter() {
        return getAircraftCarrierDeck().getIssueRecordCenter();
    }

    Future<String> load();
}
