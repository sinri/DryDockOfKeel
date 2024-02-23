package io.github.sinri.drydock.naval.ranged;

import io.github.sinri.drydock.naval.base.Warship;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

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


    /**
     * @since 1.3.4
     */
    @Override
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }

    @Override
    final protected Future<Void> launchAsWarship() {
        return launchAsQuadrireme();
    }

    abstract protected Future<Void> launchAsQuadrireme();
}
