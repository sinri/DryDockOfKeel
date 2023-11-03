package io.github.sinri.drydock.naval.frigate;

import io.github.sinri.drydock.naval.boat.QueueMixin;
import io.github.sinri.drydock.naval.boat.SundialMixin;
import io.github.sinri.drydock.naval.caravel.Caravel;
import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.sundial.KeelSundial;
import io.vertx.core.DeploymentOptions;

/**
 * 护卫舰。
 * 专门搞队列和定时任务，不提供HTTP服务。
 *
 * @since 1.0.1
 * @since 1.1.0
 */
public abstract class Frigate extends Caravel implements QueueMixin, SundialMixin {

    public Frigate() {
        super();
    }

    @Override
    final protected void launchAsCaravel() {
        KeelQueue queue = this.buildQueue();
        if (queue != null) {
            queue.deployMe(new DeploymentOptions().setWorker(true));
        }

        KeelSundial sundial = this.buildSundial();
        if (sundial != null) {
            sundial.deployMe(new DeploymentOptions().setWorker(true));
        }

        launchAsFrigate();
    }

    abstract protected void launchAsFrigate();
}
