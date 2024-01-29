package io.github.sinri.drydock.air.base;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.launcher.KeelLauncherAdapter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

@TechnicalPreview
@Deprecated(forRemoval = true)
public class Flight implements KeelLauncherAdapter {
    private final String mainVerticleClass;
    private static KeelEventLogger flightLogger = KeelOutputEventLogCenter.instantLogger();
    private final @Nullable Handler<VertxOptions> vertxOptionsHandler;

    public Flight(Class<? extends Plane> mainVerticleClass, @Nullable Handler<VertxOptions> vertxOptionsHandler) {
        this(mainVerticleClass.getName(), vertxOptionsHandler);
    }

    public Flight(String mainVerticleClass, @Nullable Handler<VertxOptions> vertxOptionsHandler) {
        this.mainVerticleClass = mainVerticleClass;
        this.vertxOptionsHandler = vertxOptionsHandler;

        flightLogger = KeelOutputEventLogCenter.getInstance()
                .createLogger("DryDock::Flight", x -> x
                        .put("main", this.mainVerticleClass)
                        .put("local_address", KeelHelpers.netHelper().getLocalHostAddress()));
    }

    public static KeelEventLogger getFlightLogger() {
        return flightLogger;
    }

    @Override
    public KeelEventLogger logger() {
        return getFlightLogger();
    }

    @Override
    public void beforeStoppingVertx() {
        logger().info("beforeStoppingVertx");
    }

    @Override
    public void afterConfigParsed(JsonObject jsonObject) {
        // load the local config file
        Keel.getConfiguration().reloadDataFromJsonObject(jsonObject);
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        jsonObject.mergeIn(Keel.getConfiguration().toJsonObject());
    }

    @Override
    public void beforeStartingVertx(VertxOptions vertxOptions) {
        logger().info("beforeStartingVertx");
        // change vertx options
        if (vertxOptionsHandler != null) {
            vertxOptionsHandler.handle(vertxOptions);
        }
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        logger().info("afterStartingVertx");
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        logger().info("beforeDeployingVerticle");
    }

    @Override
    public void beforeStoppingVertx(Vertx vertx) {
        logger().info("beforeStoppingVertx");
    }

    @Override
    public void afterStoppingVertx() {
        logger().info("afterStoppingVertx");
    }

    @Override
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        logger().exception(cause, "handleDeployFailed");
        vertx.close();
    }

    @Nullable
    @Override
    public String getDefaultCommand() {
        return "run";
    }

    @Override
    public final String getMainVerticle() {
        return mainVerticleClass;
    }


}
