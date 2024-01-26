package io.github.sinri.drydock.naval.base;

import io.github.sinri.drydock.common.CommonUnit;
import io.github.sinri.keel.logger.event.KeelEventLogger;

/**
 * @since 1.1.0
 */
public interface Boat extends CommonUnit {
    @Override
    default KeelEventLogger getUnitLogger() {
        return getNavalLogger();
    }

    KeelEventLogger getNavalLogger();

    void launch();

    void shipwreck(Throwable throwable);

    void sink();
}
