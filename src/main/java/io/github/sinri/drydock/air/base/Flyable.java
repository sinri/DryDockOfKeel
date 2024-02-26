package io.github.sinri.drydock.air.base;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.keel.facade.launcher.KeelLauncherAdapter;
import io.github.sinri.keel.verticles.KeelVerticle;

/**
 * @since 1.3.0 Technical Preview
 */
public interface Flyable extends KeelVerticle, CommonUnit, KeelLauncherAdapter {
    default void land() {
        this.undeployMe();
    }


}
