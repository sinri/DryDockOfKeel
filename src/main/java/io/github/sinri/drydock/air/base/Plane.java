package io.github.sinri.drydock.air.base;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Handler;

/**
 * For fat-jar's Main Entrance!
 */
@TechnicalPreview
abstract public class Plane extends KeelVerticleBase {
    private static KeelEventLogger flightLogger;
    private KeelEventLogCenter logCenter;

    public static void main(String[] args) throws ClassNotFoundException {
        String calledClass = System.getProperty("sun.java.command");
        Flight flight = new Flight(calledClass);
        flightLogger = flight.logger();
        flight.launch(args);
    }

    abstract protected KeelEventLogCenter buildLogCenter();

    public final KeelEventLogCenter getLogCenter() {
        return logCenter;
    }

    public final KeelEventLogger generateLogger(String topic, Handler<KeelEventLog> eventLogHandler) {
        return getLogCenter().createLogger(topic, eventLogHandler);
    }

    abstract public void fly();

    public void land() {
        this.undeployMe();
    }

    @Override
    public final void start() throws Exception {
        setLogger(flightLogger);

        // local config.properties had been loaded into this verticle's config.
        logCenter = buildLogCenter();

        fly();
    }
}
