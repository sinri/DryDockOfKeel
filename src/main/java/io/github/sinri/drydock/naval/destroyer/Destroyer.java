package io.github.sinri.drydock.naval.destroyer;

import io.github.sinri.drydock.naval.boat.FunnelMixin;
import io.github.sinri.drydock.naval.boat.QueueMixin;
import io.github.sinri.drydock.naval.boat.SundialMixin;
import io.github.sinri.drydock.naval.ironclad.Ironclad;
import io.github.sinri.keel.servant.funnel.KeelFunnel;
import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 驱逐舰。
 * 为在单节点服务器上运行的独立应用设计，在主炮（HTTP服务）之外，新增了副炮、鱼雷等辅助武器。
 * Based on Ironclad.
 * Support Sundial, Queue and Funnel.
 *
 * @since 1.0.0
 */
abstract public class Destroyer extends Ironclad implements SundialMixin, QueueMixin, FunnelMixin {
    private KeelFunnel funnel;

    @Override
    final protected Future<Void> launchAsIronclad() {
        getNavalLogger().info("To deploy async services");

        return Future.all(
                        this.loadFunnel(),
                        this.loadQueue(),
                        this.loadSundial()
                )
                .compose(compositeFuture -> {
                    getNavalLogger().info("Async services loaded.");

                    return this.launchAsDestroyer();
                });
    }

    private Future<Void> loadQueue() {
        return Future.succeededFuture()
                .compose(v -> {
                    KeelQueue queue = this.buildQueue();
                    if (queue != null) {
                        return queue.deployMe(new DeploymentOptions().setWorker(true))
                                .onFailure(throwable -> {
                                    getNavalLogger().exception(throwable, "load queue failed");
                                })
                                .compose(deploymentId -> {
                                    getNavalLogger().info("load queue: " + deploymentId);
                                    return Future.succeededFuture();
                                });
                    } else
                        return Future.succeededFuture();
                });
    }

    private Future<Void> loadSundial() {
        return Future.succeededFuture()
                .compose(v -> {
                    KeelSundial sundial = this.buildSundial();
                    if (sundial != null) {
                        return sundial.deployMe(new DeploymentOptions().setWorker(true))
                                .onFailure(throwable -> {
                                    getNavalLogger().exception(throwable, "load sundial failed");
                                })
                                .compose(deploymentId -> {
                                    getNavalLogger().info("load sundial: " + deploymentId);
                                    return Future.succeededFuture();
                                });
                    } else
                        return Future.succeededFuture();
                });
    }

    private Future<Void> loadFunnel() {
        return Future.succeededFuture()
                .compose(v -> {
                    funnel = this.buildFunnel();
                    if (funnel != null) {
                        return funnel.deployMe(new DeploymentOptions().setWorker(true))
                                .onFailure(throwable -> {
                                    getNavalLogger().exception(throwable, "load funnel failed");
                                })
                                .compose(deploymentId -> {
                                    getNavalLogger().info("load funnel: " + deploymentId);
                                    return Future.succeededFuture();
                                });
                    } else
                        return Future.succeededFuture();
                });
    }

    abstract protected Future<Void> launchAsDestroyer();


    public void funnel(Supplier<Future<Void>> supplier) {
        Objects.requireNonNull(funnel).add(supplier);
    }
}
