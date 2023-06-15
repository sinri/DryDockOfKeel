package io.github.sinri.drydock.naval.destroyer;

import io.github.sinri.drydock.naval.caravel.AliyunSLSAdapterImpl;
import io.github.sinri.drydock.naval.ironclad.Ironclad;
import io.github.sinri.keel.servant.funnel.KeelFunnel;
import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.github.sinri.keel.servant.sundial.KeelSundialPlan;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 驱逐舰。
 * Based on Ironclad.
 * Support Sundial and Funnel.
 *
 * @since 1.0.0
 */
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
    final protected void launchAsIronclad(Promise<Void> promise) {
        getNavalLogger().info("to deploy sundial and funnel");
        CompositeFuture.all(
                        Future.succeededFuture()
                                .compose(v -> {
                                    if (sundial != null) {
                                        sundial = new KeelSundial() {
                                            @Override
                                            protected Future<Collection<KeelSundialPlan>> fetchPlans() {
                                                return reloadSundialPlans();
                                            }
                                        };
                                        sundial.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicSundial, null));
                                        return sundial.deployMe(new DeploymentOptions().setWorker(true));
                                    } else {
                                        return Future.succeededFuture("SUNDIAL DISABLED");
                                    }
                                })
                                .onSuccess(sundialDeploymentID -> {
                                    getNavalLogger().info("DEPLOY SUNDIAL: " + sundialDeploymentID);
                                })
                                .onFailure(sundialDeployFailure -> {
                                    getNavalLogger().exception(sundialDeployFailure, "DEPLOY SUNDIAL FAILED");
                                }),
                        Future.succeededFuture()
                                .compose(v -> {
                                    if (funnel != null) {
                                        funnel.setLogger(generateLogger(AliyunSLSAdapterImpl.TopicFunnel, null));
                                        return funnel.deployMe(new DeploymentOptions().setWorker(true));
                                    } else {
                                        return Future.succeededFuture("FUNNEL DISABLED");
                                    }
                                })
                                .onSuccess(sundialDeploymentID -> {
                                    getNavalLogger().info("DEPLOY FUNNEL: " + sundialDeploymentID);
                                })
                                .onFailure(sundialDeployFailure -> {
                                    getNavalLogger().exception(sundialDeployFailure, "DEPLOY FUNNEL FAILED");
                                })
                )
                .compose(compositeFuture -> {
                    Promise<Void> destroyerPromise = Promise.promise();
                    launchAsDestroyer(destroyerPromise);
                    return destroyerPromise.future();
                })
                .onComplete(promise);
        getNavalLogger().info("Make Ironclad Strong Again!");
    }

    @Deprecated(since = "1.0.1")
    protected void launchAsDestroyer() {
        // if anymore to prepare
    }

    protected void launchAsDestroyer(Promise<Void> promise) {
        launchAsDestroyer();
        promise.complete();
    }

    /**
     * If the plan collection returned in future is null, the sundial would not be deployed.
     *
     * @return future of plans or null.
     */
    protected Future<Collection<KeelSundialPlan>> reloadSundialPlans() {
        return Future.succeededFuture(null);
    }


    public void funnel(Supplier<Future<Void>> supplier) {
        Objects.requireNonNull(funnel).add(supplier);
    }
}
