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
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * For fat-jar's Main Entrance!
 */
@TechnicalPreview
@Deprecated(forRemoval = true)
abstract public class Plane extends KeelVerticleBase implements Flyable {
    //private static KeelEventLogger flightLogger;
    private KeelEventLogCenter logCenter;
    private static String calledClass;
    private static Class<?> planeClass;
    private static Method configVertxOptionsMethod;

    public static void main(String[] args) {

        try {
            // 首先看运行时环境中的命令行参数，主要用于开发环境和直接用class调用的情况。
            calledClass = System.getProperty("sun.java.command");
            KeelOutputEventLogCenter.instantLogger().notice("calledClass from sun.java.command: " + calledClass);

            // 正式环境中，一般使用JAR包来运行，需要从JAR包里的META-INF/MANIFEST.MF文件里取Main-Class的值。
            if (calledClass.endsWith(".jar")) {
                // Get the location of this class file
                byte[] bytes = Keel.fileHelper().readFileAsByteArray("META-INF/MANIFEST.MF", true);
                String s = new String(bytes);
                KeelOutputEventLogCenter.instantLogger().info("META-INF/MANIFEST.MF content: " + s);
                String[] lines = s.split("[\r\n]+");
                for (var line : lines) {
                    if (line.startsWith("Main-Class:")) {
                        calledClass = line.substring(11).trim();
                        break;
                    }
                }

                KeelOutputEventLogCenter.instantLogger().notice("calledClass from MainClass: " + calledClass);
                Objects.requireNonNull(calledClass);
            }
            planeClass = Class.forName(calledClass);
        } catch (Throwable throwable) {
            KeelOutputEventLogCenter.instantLogger().exception(throwable, "Cannot find Plane class");
            throw new RuntimeException();
        }

        try {
            Method method = planeClass.getDeclaredMethod("configVertxOptions", VertxOptions.class);
            if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                configVertxOptionsMethod = method;
            } else {
                throw new RuntimeException();
            }
        } catch (Throwable throwable) {
            KeelOutputEventLogCenter.instantLogger().warning(
                    "Did not found the public static method `configVertxOptions(VertxOptions)` in class " + calledClass + ", will use default VertxOptions.");
            configVertxOptionsMethod = null;
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

        setLogger(getFlightLogger());

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
