package io.github.sinri.drydock.naval.raider;

import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.2.0
 */
abstract public class Privateer extends KeelInstantRunner {
    protected void loadLocalConfiguration() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
    }

    /**
     * 本地配置已加载。
     * 准备数据库连接之类的东西。
     *
     * @since 1.2.0
     * @since 2.0.0 provide a default implementation.
     */
    @Nonnull
    protected Future<Void> prepareEnvironment() {
        return Future.succeededFuture();
    }

    /**
     * Override it, if you need more initialization.
     */
    @Nonnull
    @Override
    protected final Future<Void> starting() {
        loadLocalConfiguration();
        return prepareEnvironment();
    }
}
