package io.github.sinri.drydock.naval.base;

import io.github.sinri.drydock.common.CommonUnit;

/**
 * @since 1.1.0
 */
public interface Boat extends CommonUnit {

    void launch();

    void shipwreck(Throwable throwable);

    void sink();
}
