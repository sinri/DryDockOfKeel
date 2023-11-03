package io.github.sinri.drydock.naval.galley;

import io.github.sinri.drydock.naval.boat.Warship;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.VertxOptions;

/**
 * 桨帆船。
 * 一种最为简易的，可自由拓展性最强的Warship实现，可以用于实现各种场景。
 * Absolutely customized VertxOptions to initialize, standalone or clustered.
 * Pure STDOUT logger.
 * Fundamental Keel Initialization.
 *
 * @since 1.0.0
 * @since 1.1.0 extends Warship
 */
public abstract class Galley extends Warship {
    public VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    protected KeelEventLogCenter buildLogCenter() {
        return KeelOutputEventLogCenter.getInstance();
    }

    @Override
    protected final void launchAsWarship() {
        launchAsGalley();
    }

    /**
     * 在桨帆船的基础上安装模块。
     */
    abstract protected void launchAsGalley();
}
