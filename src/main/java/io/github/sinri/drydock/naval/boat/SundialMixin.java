package io.github.sinri.drydock.naval.boat;

import io.github.sinri.drydock.naval.caravel.AliyunSLSAdapterImpl;
import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
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

}
