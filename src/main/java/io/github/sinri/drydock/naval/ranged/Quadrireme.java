package io.github.sinri.drydock.naval.ranged;

import io.github.sinri.drydock.naval.base.Warship;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

/**
 * 四段帆船。
 * 约等于Galley。
 */
abstract public class Quadrireme extends Warship {
    @Override
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    @Override
    protected Future<Void> loadRemoteConfiguration() {
        // For Quadrireme, config file could be packaged.
        return Future.succeededFuture();
    }

    public VertxOptions buildVertxOptions() {
        return new VertxOptions()
                .setWorkerPoolSize(64);
    }

    protected KeelEventLogCenter buildLogCenter() {
        return KeelOutputEventLogCenter.getInstance();
    }

    @Override
    final protected Future<Void> launchAsWarship() {
        return launchAsQuadrireme();
    }

    abstract protected Future<Void> launchAsQuadrireme();
}
