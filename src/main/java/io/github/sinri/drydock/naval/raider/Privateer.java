package io.github.sinri.drydock.naval.raider;

import io.github.sinri.keel.tesuto.KeelTest;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.2.0
 */
abstract public class Privateer extends KeelTest {
    /**
     * Override it, if you need more initialization.
     */
    @Nonnull
    @Override
    protected Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return Future.succeededFuture();
    }
}
