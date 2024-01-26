package io.github.sinri.drydock.common;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Handler;

public interface CommonUnit {
    KeelEventLogger getUnitLogger();

    KeelEventLogCenter getLogCenter();

    KeelEventLogger generateLogger(String topic, Handler<KeelEventLog> eventLogHandler);
}
