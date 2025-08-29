package io.github.sinri.drydock.naval.base;

import io.github.sinri.drydock.common.CommonUnit;

/**
 * @since 1.1.0
 *         单体船只（非航母）的基类。
 */
public interface Boat extends CommonUnit {

    void launch();

    void shipwreck(Throwable throwable);

    void sink();
}
