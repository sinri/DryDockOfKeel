package io.github.sinri.drydock.air.base;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.keel.verticles.KeelVerticle;

public interface Flyable extends KeelVerticle, CommonUnit {
    default void land() {
        this.undeployMe();
    }
}
