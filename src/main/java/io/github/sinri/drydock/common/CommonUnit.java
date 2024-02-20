package io.github.sinri.drydock.common;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogToBeExtended;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Handler;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface CommonUnit {
    KeelEventLogger getUnitLogger();

    KeelEventLogCenter getLogCenter();

    /**
     * @since 1.3.4 the type in eventLogHandler KeelEventLog changed to KeelEventLogToBeExtended
     */
    KeelEventLogger generateLogger(String topic, Handler<KeelEventLogToBeExtended> eventLogHandler);

    /**
     * @since 1.3.4
     */
    <T extends KeelEventLog> KeelEventLogger generateLoggerForCertainEvent(String topic, @Nullable Supplier<T> baseLogBuilder);

}
