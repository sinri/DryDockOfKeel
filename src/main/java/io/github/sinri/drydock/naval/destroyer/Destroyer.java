package io.github.sinri.drydock.destroyer;

import io.github.sinri.drydock.caravel.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.ironclad.Ironclad;
import io.github.sinri.keel.servant.funnel.KeelFunnel;
import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

abstract public class Destroyer extends Ironclad {
    private KeelSundial sundial = null;
    private KeelFunnel funnel = null;

    public Destroyer(String configPropertiesFile, boolean useSundial, boolean useFunnel) {
        super(configPropertiesFile);
        if (useFunnel) {
            funnel = new KeelFunnel();
        }
        if (useSundial) {
            sundial = new KeelSundial() {
                @Override
                protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                    return reloadSundialPlans();
                }
            };
        }
    }

    @Override
    final protected void launchAsIronclad() {
        if (sundial != null) {
            sundial = new KeelSundial() {
                @Override
                protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                    return reloadSundialPlans();
                }
            };
            sundial.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicSundial, null));
            sundial.deployMe(new DeploymentOptions().setWorker(true));
        }

        if (funnel != null) {
            funnel.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicFunnel, null));
            funnel.deployMe(new DeploymentOptions().setWorker(true));
        }

        launchAsDestroyer();
    }

    protected void launchAsDestroyer() {
        // if anymore to prepare
    }

    /**
     * If the plan collection returned in future is null, the sundial would not be deployed.
     *
     * @return future of plans or null.
     */
    protected Future<Collection<KeelSundialPlan>> reloadSundialPlans() {
        return Future.succeededFuture(null);
    }


    public void putIntoFunnel(Supplier<Future<Void>> supplier) {
        Objects.requireNonNull(funnel).add(supplier);
    }
}
