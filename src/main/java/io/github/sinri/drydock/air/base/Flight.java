package io.github.sinri.drydock.air.base;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.launcher.KeelLauncherAdapter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

@TechnicalPreview
public class Flight implements KeelLauncherAdapter {
    private final String mainVerticleClass;
    private final KeelEventLogger flightLogger;

    public Flight(Class<? extends Plane> mainVerticleClass) {
        this(mainVerticleClass.getName());
    }

    public Flight(String mainVerticleClass) {
        this.mainVerticleClass = mainVerticleClass;
        this.flightLogger = KeelOutputEventLogCenter.getInstance()
                .createLogger("DryDock::Flight", x -> x
                        .put("main", this.mainVerticleClass)
                        .put("local_address", KeelHelpers.netHelper().getLocalHostAddress()));
    }

    @Override
    public KeelEventLogger logger() {
        return this.flightLogger;
    }

    @Override
    public void beforeStoppingVertx() {
        logger().info("beforeStoppingVertx");
    }

    @Override
    public void afterConfigParsed(JsonObject jsonObject) {
        Keel.getConfiguration().reloadDataFromJsonObject(jsonObject);
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        jsonObject.mergeIn(Keel.getConfiguration().toJsonObject());
    }

    @Override
    public void beforeStartingVertx(VertxOptions vertxOptions) {
        logger().info("beforeStartingVertx");
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
