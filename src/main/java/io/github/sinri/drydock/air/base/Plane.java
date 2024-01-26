package io.github.sinri.drydock.air.base;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * For fat-jar's Main Entrance!
 */
@TechnicalPreview
abstract public class Plane extends KeelVerticleBase implements Flyable {
    //private static KeelEventLogger flightLogger;
    private KeelEventLogCenter logCenter;

    public static void main(String[] args) {
        String calledClass = System.getProperty("sun.java.command");

        Method configVertxOptionsMethod;

        try {
            Class<?> planeClass = Class.forName(calledClass);
            Method method = planeClass.getDeclaredMethod("configVertxOptions", VertxOptions.class);
            if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                configVertxOptionsMethod = method;
            } else {
                KeelOutputEventLogCenter.instantLogger().warning(
                        "Did not found the public static method `configVertxOptions(VertxOptions)` in class " + calledClass + ", will use default VertxOptions.");
                configVertxOptionsMethod = null;
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("Plane is not ready to fly", throwable);
        }

        Flight flight = new Flight(calledClass, vertxOptions -> {
            if (configVertxOptionsMethod != null) {
                try {
                    configVertxOptionsMethod.invoke(null, vertxOptions);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    KeelOutputEventLogCenter.instantLogger().exception(e, "Failed to execute configVertxOptions");
                }
            }
        });
        flight.launch(args);
    }

    protected KeelEventLogCenter buildLogCenter() {
        return KeelOutputEventLogCenter.getInstance();
    }

    @Override
    public final KeelEventLogCenter getLogCenter() {
        return logCenter;
    }

    @Override
    public KeelEventLogger getFlightLogger() {
        return Flight.getFlightLogger();
    }

    @Override
    public final KeelEventLogger generateLogger(String topic, Handler<KeelEventLog> eventLogHandler) {
        return getLogCenter().createLogger(topic, eventLogHandler);
    }

    abstract public Future<Void> flyAsPlane();


    @Override
    public final void start(Promise<Void> startPromise) throws Exception {
        start();

        // local config.properties had been loaded into this verticle's config.
        Future.succeededFuture()
                .compose(v -> {
                    return loadRemoteConfiguration();
                })
                .compose(remoteConfigurationLoaded -> {
                    logCenter = buildLogCenter();
                    return Future.succeededFuture();
                })
                .compose(logCenterBuilt -> {
                    return flyAsPlane();
                })
                .andThen(ar -> {
                    if (ar.failed()) {
                        getFlightLogger().exception(ar.cause(), "Failed to start flying");
                        startPromise.fail(ar.cause());
                    } else {
                        startPromise.complete();
                    }
                });
    }

    @Override
    public final void start() {
    }

    // public static void configVertxOptions(VertxOptions);

    /**
     * 加载远程配置。
     * 此时已加载本地配置，已初始化Keel(Vert.x)。
     * 仅可以使用 flightLogger。
     */
    abstract protected Future<Void> loadRemoteConfiguration();
}
