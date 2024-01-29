package io.github.sinri.drydock.air.base;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.verticles.KeelVerticle;

/**
 * @since 1.3.0 Technical Preview
 */
@TechnicalPreview(since = "1.3.0")
public interface Flyable extends KeelVerticle, CommonUnit {
    default void land() {
        this.undeployMe();
    }

    @Override
    default KeelEventLogger getUnitLogger() {
        return getFlightLogger();
    }

    KeelEventLogger getFlightLogger();
}
