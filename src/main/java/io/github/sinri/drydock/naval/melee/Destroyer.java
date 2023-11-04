package io.github.sinri.drydock.naval.melee;

import io.github.sinri.drydock.naval.base.QueueMixin;
import io.github.sinri.drydock.naval.base.SundialMixin;
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
    final protected Future<Void> launchAsIronclad() {
        getNavalLogger().info("To deploy async services");

        return Future.all(
                        this.loadQueue(),
                        this.loadSundial()
                )
                .compose(compositeFuture -> {
                    getNavalLogger().info("Async services loaded.");

                    return this.launchAsDestroyer();
                });
    }


    abstract protected Future<Void> launchAsDestroyer();

}
