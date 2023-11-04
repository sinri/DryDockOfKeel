package io.github.sinri.drydock.naval.base;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Handler;

/**
 * @since 1.1.0
 */
public interface Boat {
    KeelEventLogger getNavalLogger();

    KeelEventLogCenter getLogCenter();

    KeelEventLogger generateLogger(String topic, Handler<KeelEventLog> eventLogHandler);

    void launch();

    void shipwreck(Throwable throwable);

    void sink();
}
