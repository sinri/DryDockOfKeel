package io.github.sinri.drydock.naval.melee;

import io.github.sinri.drydock.common.QueueMixin;
import io.github.sinri.drydock.common.SundialMixin;
import io.vertx.core.Future;

/**
 * 驱逐舰。
 * 为在单节点服务器上运行的独立应用设计，在主炮（HTTP服务）之外，新增了副炮、鱼雷等辅助武器，不支持横向扩展。
 * Based on Ironclad.
 * Support Sundial, Queue and Funnel.
 *
 * @since 1.0.0
 */
abstract public class Destroyer extends Ironclad implements SundialMixin, QueueMixin {

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        // For Destroyer, config file could be packaged.
        return Future.succeededFuture();
    }

    @Override
    final protected Future<Void> launchAsIronclad() {
        getUnitLogger().info("To deploy async services");

        return Future.succeededFuture()
                .compose(v -> {
                    return this.loadSundial();
                })
                .compose(v -> {
                    return this.loadQueue();
                })
                .compose(compositeFuture -> {
                    getUnitLogger().info("Async services loaded.");

                    return this.launchAsDestroyer();
                });
    }


    abstract protected Future<Void> launchAsDestroyer();

}
